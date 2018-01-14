package com.macklive.twitter;

import com.google.appengine.api.datastore.DatastoreApiHelper;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.appengine.repackaged.org.joda.time.DateTimeUtils;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.Game;
import com.macklive.objects.Tweet;
import com.macklive.objects.TwitterAuthorization;
import com.macklive.storage.DataManager;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import javax.servlet.http.HttpSession;
import javax.xml.crypto.Data;
import javax.xml.ws.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Nick on 10/4/16.
 */
public class TwitterManager {

    private static TwitterManager instance;

    private static final String CONSUMER_KEY = "3Aifbpp7c9Ot3BUYtCRyZQnnD";

    private static final String CONSUMER_SECRET = "QGoXhYxdq4MtyGgg0hZUnDjzzBLNdfYOudGw04oijro8h42z5l";

    private static int TWITTER_LIMIT = 140;

    private final Twitter twitter;

    private Logger logger = Logger.getLogger(getClass().getName());

    private TwitterManager() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(CONSUMER_KEY);
        cb.setOAuthConsumerSecret(CONSUMER_SECRET);
        twitter = new TwitterFactory(cb.build()).getInstance();
    }

    /**
     * Gets the singleton
     *
     * @return The singleton
     */
    public static TwitterManager getInstance() {
        if (instance == null) {
            instance = new TwitterManager();
        }
        return instance;
    }

    /**
     * Verifies the credentials of the current user
     *
     * @return True if the twitter credentials exist and are valid, false otherwise
     */
    public boolean verifyCredentials() {
        try {
            DataManager dmanager = DataManager.getInstance();
            TwitterAuthorization ta = dmanager.getTwitterAuth();
            if (ta != null) {
                synchronized (twitter) {
                    twitter.setOAuthAccessToken(ta.getAccessToken());
                    twitter.verifyCredentials();
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Disconnects the user's twitter account
     */
    public void signOut() {
        DataManager.getInstance().deleteTwitterAuth();
    }

    /**
     * Tweets with the user in the current thread
     *
     * @param text Text of the tweet
     * @return True if the tweet was successful, false if it failed
     */
    public boolean tweet(String text) {
        try {
            DataManager dmanager = DataManager.getInstance();
            TwitterAuthorization ta = dmanager.getTwitterAuth();
            synchronized (twitter) {
                twitter.setOAuthAccessToken(ta.getAccessToken());
                twitter.updateStatus(text);
            }
            return true;
        } catch (TwitterException | EntityMismatchException | EntityNotFoundException e) {
            return false;
        }
    }

    /**
     * Gets a URI for signing into twitter
     *
     * @param callbackUrl String for the callback URL
     * @param session
     * @return URI to directo to twitter
     */
    public URI getAuthorizeUri(String callbackUrl, HttpSession session) {
        try {
            RequestToken rt;
            synchronized (twitter) {
                twitter.setOAuthAccessToken(null);
                rt = twitter.getOAuthRequestToken(callbackUrl);
            }
            session.setAttribute("twitterRequestToken", rt);
            return URI.create(rt.getAuthenticationURL());
        } catch (TwitterException e) {
            return null;
        }
    }

    /**
     * Creates the auth token and stores it for the user
     *
     * @param twitterRequestToken Request token formed during the OAuth process
     * @param verifier            Verifier string passed back from twitter
     * @throws TwitterException If an error occurs when verifying twitter credentials
     */
    public void createAuthToken(RequestToken twitterRequestToken, String verifier) throws TwitterException {
        AccessToken at;
        synchronized (twitter) {
            at = twitter.getOAuthAccessToken(twitterRequestToken, verifier);
            twitter.setOAuthAccessToken(at);
            twitter.verifyCredentials();
        }
        DataManager.getInstance().storeItem(new TwitterAuthorization(at));
    }

    /**
     * Gets the character limit for a tweet, depending on the game.
     * This can change depending on if a link is included
     *
     * @return The character limit for a tweet
     */
    public int getLimit() {
        MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
        Integer limit = (Integer) memcache.get("twitter_limit");

        if (limit != null) {
            logger.info("Retrieving twitter limit from the cache");
            return limit;
        }

        try {
            synchronized (twitter) {
                twitter.setOAuthAccessToken(DataManager.getInstance().getTwitterAuth().getAccessToken());
                limit = TWITTER_LIMIT - twitter.getAPIConfiguration().getShortURLLengthHttps() - 1;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            limit = TWITTER_LIMIT;
        }
        memcache.put("twitter_limit", limit, Expiration.byDeltaSeconds(60 * 60 * 24));
        logger.info("Retrieved twitter limit from API");

        return limit;
    }

    private long[] getUserIds(List<String> twitterHandles) throws TwitterException {
        try {
            synchronized (twitter) {
                List<User> users = twitter.lookupUsers(twitterHandles.toArray(new String[]{}));
                return users.stream()
                        .filter(Objects::nonNull)
                        .mapToLong(User::getId)
                        .toArray();
            }
        } catch (TwitterException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw e;
        }
    }

    public Map<String, String> refreshTweets(long gameId, List<String> twitterHandles,
                                             Map<String, Long> handleToLatestTweet) {
        Map<String, Long> currentStatus = new HashMap<>(handleToLatestTweet);
        synchronized (twitter) {
            try {
                twitter.setOAuthAccessToken(DataManager.getInstance().getTwitterAuth().getAccessToken());
                for (String handle : twitterHandles) {
                    Paging p = new Paging();
                    boolean hasFirstTweet = handleToLatestTweet.containsKey(handle);

                    if (hasFirstTweet) {
                        p.sinceId(handleToLatestTweet.get(handle));
                    } else {
                        p.count(1);
                    }

                    ResponseList<Status> statuses = twitter.timelines().getUserTimeline(handle, p);
                    for (Status s : statuses) {
                        DataManager.getInstance().storeItem(new Tweet(gameId, s, !hasFirstTweet || !isValid(s)));
                        currentStatus.compute(handle, (k, v) -> Math.max(v == null ? 0 : v, s.getId()));
                    }
                }
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }

            return currentStatus.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> Long.toString(entry.getValue())));
        }

    }

    private boolean isValid(Status s) {
        return !s.isRetweet() && Strings.isNullOrEmpty(s.getInReplyToScreenName());
    }
}
