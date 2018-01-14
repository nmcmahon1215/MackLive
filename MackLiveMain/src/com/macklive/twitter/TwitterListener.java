package com.macklive.twitter;

import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.cloud.sql.jdbc.internal.Url;
import com.google.gson.Gson;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.Message;
import com.macklive.objects.Tweet;
import com.macklive.serialize.GsonUtility;
import com.macklive.storage.DataManager;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

public class TwitterListener implements StatusListener {

    private long gameId;
    private Logger logger = Logger.getLogger(getClass().getName());
    private String urlString;
    private List<String> validHandles;

    public TwitterListener(long gameId, String urlString, List<String> validHandles) {
        this.gameId = gameId;
        this.urlString = urlString;
        this.validHandles = validHandles;
    }

    private void postTweetChange(String methodName, Object payload) {
        try {
            URL url = new URL(urlString + "messages/twitter/" + methodName + "/" + gameId);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(GsonUtility.getGson().toJson(payload).getBytes());
            os.flush();
            os.close();
            int resp = con.getResponseCode();
            if (resp != 200) {
                logger.severe("Error making request for: " + methodName);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    @Override
    public void onStatus(Status status) {
        if (!validHandles.contains(status.getUser().getScreenName())) {
            return;
        }
        postTweetChange("tweet", status);
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        postTweetChange("delete", statusDeletionNotice);
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        logger.info(String.format("(Game %d)On track limitation notice: %d", gameId, numberOfLimitedStatuses));
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
        //N/A as we don't store geo data
    }

    @Override
    public void onStallWarning(StallWarning warning) {
        logger.warning("Stall Warning: " + warning.getMessage());
    }

    @Override
    public void onException(Exception ex) {
        logger.severe("Listener Exception: " + ex.getMessage());
        ex.printStackTrace();
    }
}
