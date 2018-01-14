package com.macklive.objects;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.repackaged.com.google.common.primitives.Booleans;
import com.macklive.exceptions.EntityMismatchException;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;

import java.util.Optional;

public class Tweet extends Message {

    private String profilePictureUrl;
    private long tweetId;
    private boolean hidden;

    public Tweet(long gameId, Status status, boolean hidden) {
        super(status.getUser().getScreenName(), status.getText(), gameId, false);
        this.profilePictureUrl = status.getUser().getProfileImageURL();
        this.tweetId = status.getId();
        this.hidden = hidden;
    }

    public Tweet(Entity e) throws EntityMismatchException {
        super(e);
    }

    @Override
    public Entity getEntity() {
        Entity entity = super.getEntity();
        entity.setUnindexedProperty("profilePictureUrl", profilePictureUrl);
        entity.setIndexedProperty("tweetId", tweetId);
        entity.setUnindexedProperty("hidden", hidden);
        return entity;
    }

    @Override
    public void loadEntity(Entity e) throws EntityMismatchException {
        super.loadEntity(e);
        profilePictureUrl = (String) e.getProperty("profilePictureUrl");
        tweetId = (Long) e.getProperty("tweetId");
        hidden = Optional.ofNullable((Boolean) e.getProperty("hidden")).orElse(false);
    }
}
