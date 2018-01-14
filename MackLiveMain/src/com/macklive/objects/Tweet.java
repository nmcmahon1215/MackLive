package com.macklive.objects;

import com.google.appengine.api.datastore.Entity;
import com.macklive.exceptions.EntityMismatchException;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;

public class Tweet extends Message {

    private String profilePictureUrl;
    private long tweetId;
    private boolean deleted;

    public Tweet(long gameId, String handle, String message, String profilePictureUrl, long tweetId) {
        super(handle, message, gameId, false);
        this.profilePictureUrl = profilePictureUrl;
        this.tweetId = tweetId;
        this.deleted = false;
    }

    public Tweet(long gameId, long tweetId) {
        super(null, null, gameId, false);
        this.tweetId = tweetId;
        this.deleted = true;
    }

    public Tweet(Entity e) throws EntityMismatchException {
        super(e);
    }

    @Override
    public Entity getEntity() {
        Entity entity = super.getEntity();
        entity.setUnindexedProperty("profilePictureUrl", profilePictureUrl);
        entity.setIndexedProperty("tweetId", tweetId);
        entity.setUnindexedProperty("deleted", deleted);
        return entity;
    }

    @Override
    public void loadEntity(Entity e) throws EntityMismatchException {
        super.loadEntity(e);
        profilePictureUrl = (String) e.getProperty("profilePictureUrl");
        tweetId = (long) e.getProperty("tweetId");
        deleted = (boolean) e.getProperty("deleted");
    }
}
