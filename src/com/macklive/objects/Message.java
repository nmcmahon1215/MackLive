package com.macklive.objects;

import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import com.macklive.exceptions.EntityMismatchException;

/**
 * Describes a message in a game.
 */
public class Message extends AbsBusinessObject {

    private String author;
    private String text;
    private Date time;
    private long gameId;
    private boolean approved;

    public Message(String author, String text, long gameId, boolean approved) {
        this.author = author;
        this.text = text;
        this.approved = approved;
        this.time = new Date();
        this.gameId = gameId;
    }

    public Message(Entity e) throws EntityMismatchException {
        this.loadEntity(e);
    }

    @Override
    public Entity getEntity() {
        Entity e;
        if (this.key != null) {
            e = new Entity(this.key);
        } else {
            e = new Entity("Message");
        }

        e.setProperty("author", author);
        e.setUnindexedProperty("text", text);
        e.setProperty("time", time);
        e.setProperty("game", gameId);
        e.setProperty("approved", approved);
        return e;
    }

    @Override
    public void loadEntity(Entity e) throws EntityMismatchException {
        if (e.getKind().equals("Message")) {

            this.author = (String) e.getProperty("author");
            this.text = (String) e.getProperty("text");
            this.time = (Date) e.getProperty("time");
            this.gameId = (long) e.getProperty("game");
            this.approved = (boolean) e.getProperty("approved");
            this.key = e.getKey();

        } else {
            throw new EntityMismatchException(
                    "Expected entity of type \"Team\"," + "but received \""
                            + e.getKind());
        }
    }
}
