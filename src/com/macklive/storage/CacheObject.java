package com.macklive.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.macklive.objects.Game;
import com.macklive.objects.Message;

/**
 * Describes an object in the Cache Manager
 */
public class CacheObject {

    private Game game;
    private List<Message> messages;
    private Date lastAccessed;

    /**
     * Constructor
     */
    public CacheObject() {
        this.messages = new ArrayList<Message>();
        this.lastAccessed = new Date();
    }

    public Game getGame() {
        this.lastAccessed.setTime(System.currentTimeMillis());
        return game;
    }

    public void setGame(Game game) {
        this.lastAccessed.setTime(System.currentTimeMillis());
        this.game = game;
    }

    public List<Message> getMessages() {
        this.lastAccessed.setTime(System.currentTimeMillis());
        return messages;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

}
