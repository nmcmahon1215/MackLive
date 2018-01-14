package com.macklive.twitter;

import com.google.appengine.api.datastore.DatastoreApiHelper;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.repackaged.org.joda.time.DateTimeUtils;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.Game;
import com.macklive.objects.TwitterAuthorization;
import com.macklive.storage.DataManager;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import javax.servlet.http.HttpSession;
import javax.xml.crypto.Data;
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

    private static long THREE_HOUR_MS = 10800000;

    private Twitter twitter;

    private Map<Long, TwitterStreamWrapper> streamMap;

    private Logger logger = Logger.getLogger(getClass().getName());

    private TwitterManager() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(CONSUMER_KEY);
        cb.setOAuthConsumerSecret(CONSUMER_SECRET);
        twitter = new TwitterFactory(cb.build()).getInstance();
        streamMap = new HashMap<>();
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
                twitter.setOAuthAccessToken(ta.getAccessToken());
                twitter.verifyCredentials();
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
            twitter.setOAuthAccessToken(ta.getAccessToken());
            twitter.updateStatus(text);
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
            twitter.setOAuthAccessToken(null);
            RequestToken rt = twitter.getOAuthRequestToken(callbackUrl);
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
        AccessToken at = twitter.getOAuthAccessToken(twitterRequestToken, verifier);
        twitter.setOAuthAccessToken(at);
        twitter.verifyCredentials();
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
            twitter.setOAuthAccessToken(DataManager.getInstance().getTwitterAuth().getAccessToken());
            limit = TWITTER_LIMIT - twitter.getAPIConfiguration().getShortURLLengthHttps() - 1;
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
            List<User> users = twitter.lookupUsers(twitterHandles.toArray(new String[]{}));
            return users.stream()
                    .filter(Objects::nonNull)
                    .mapToLong(User::getId)
                    .toArray();
        } catch (TwitterException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw e;
        }
    }

    /**
     * Sets up twitter streaming for game
     *
     * @param gameId         Game ID
     * @param twitterHandles Handles to follow
     * @throws TwitterException when twitter service or network is unavailable
     */
    public synchronized void setUpTwitterStream(long gameId, List<String> twitterHandles, String url) throws
            TwitterException,
            EntityNotFoundException, EntityMismatchException {
        if (twitterHandles.isEmpty()) {
            return;
        }

        if (streamMap.containsKey(gameId)) {
            List<String> currentHandles = streamMap.get(gameId).getHandles();
            if (currentHandles.containsAll(twitterHandles) && twitterHandles.containsAll(currentHandles)) {
                //No change, nothing to do
                return;
            }
            streamMap.get(gameId).getStream().cleanUp();
            streamMap.remove(gameId);
        }

        TwitterStream stream = new TwitterStreamFactory(twitter.getConfiguration()).getInstance();
        stream.setOAuthAccessToken(DataManager.getInstance().getTwitterAuth().getAccessToken());
        stream.addListener(new TwitterListener(gameId, url, twitterHandles));

        FilterQuery filter = new FilterQuery();
        filter.follow(getUserIds(twitterHandles));

        stream.filter(filter);
        streamMap.put(gameId, new TwitterStreamWrapper(stream, twitterHandles));
    }

    /**
     * Sets up twitter streaming based on game data
     *
     * @param gameId Game ID
     * @throws TwitterException when twitter service or network in unavailable
     */
    public synchronized void setUpTwitterStream(long gameId, String url) throws TwitterException,
            EntityMismatchException,
            EntityNotFoundException {
        Game g = DataManager.getInstance().getGame(gameId);
        setUpTwitterStream(gameId, g.getTwitterAccounts(), url);
    }

    public synchronized void cleanUpTwitterStreams() {
        for (long gameId : streamMap.keySet()) {
            Game g = DataManager.getInstance().getGame(gameId);
            long timeRunning = System.currentTimeMillis() - g.getLastUpdated().getTime();
            if (timeRunning >= THREE_HOUR_MS) {
                streamMap.get(gameId).getStream().cleanUp();
                streamMap.remove(gameId);
            }
        }
    }

}
