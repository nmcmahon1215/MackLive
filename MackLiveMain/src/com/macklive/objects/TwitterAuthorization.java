package com.macklive.objects;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserServiceFactory;
import com.macklive.exceptions.EntityMismatchException;
import twitter4j.auth.AccessToken;

/**
 * Created by Nick on 10/3/16.
 */
public class TwitterAuthorization extends AbsBusinessObject {

    public AccessToken accessToken;

    public TwitterAuthorization(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public TwitterAuthorization(Entity e) throws EntityMismatchException {
        this.loadEntity(e);
    }

    public AccessToken getAccessToken() {
        return this.accessToken;
    }

    @Override
    public Entity getEntity() {
        Entity e;
        this.key = KeyFactory.createKey("TwitterAuth",
                                        UserServiceFactory.getUserService().getCurrentUser().getUserId());
        e = new Entity(this.key);
        e.setProperty("token", this.accessToken.getToken());
        e.setProperty("token_secret", this.accessToken.getTokenSecret());
        return e;
    }

    @Override
    public void loadEntity(Entity e) throws EntityMismatchException {
        if (e.getKind().equals("TwitterAuth")) {
            this.accessToken = new AccessToken((String) e.getProperty("token"), (String) e.getProperty("token_secret"));
        } else {
            throw new EntityMismatchException("Expected entity of type \"Team\"," + "but received \"" + e.getKind());
        }
    }
}
