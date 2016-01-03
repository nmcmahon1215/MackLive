package com.macklive.objects;

import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import com.macklive.exceptions.EntityMismatchException;

/**
 * Describes a message in a game.
 */
public class Message extends AbsCacheableObject {

    private String author;
    private String text;
    private Date time;
    private long gameId;
    private boolean approved;

    /**
     * Constructor.
     * 
     * @param author
     *            Author of the message
     * @param text
     *            Text of the message
     * @param gameId
     *            ID of the game the message belongs to.
     * @param approved
     *            Whether or not the message is approved for public use
     */
    public Message(String author, String text, long gameId, boolean approved) {
        this.author = author;
        this.text = text;
        this.approved = approved;
        this.time = new Date();
        this.gameId = gameId;
    }

    /**
     * Alternate constructor
     * 
     * @param e
     *            Entity from the data store to load the object from.
     * @throws EntityMismatchException
     *             if the Entity is of the wrong type.
     */
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

    @Override
    public boolean hasOwner() {
        return false;
    }

    @Override
    public boolean isGame() {
        return false;
    }

    @Override
    public boolean isMessage() {
        return true;
    }

    /**
     * Gets the time of the message
     * 
     * @return Time of the message
     */
    public Date getTime() {
        return time;
    }

    /**
     * Getter for game id.
     * 
     * @return GameID which this message belongs to.
     */
    public long getGameId() {
        return gameId;
    }
}
