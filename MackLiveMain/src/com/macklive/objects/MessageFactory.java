package com.macklive.objects;

import com.google.appengine.api.datastore.Entity;
import com.macklive.exceptions.EntityMismatchException;

public class MessageFactory {

    public static Message fromEntity(Entity e) throws EntityMismatchException {
        if (e.hasProperty("tweetId")) {
            return new Tweet(e);
        } else {
            return new Message(e);
        }
    }
}
