package com.macklive.objects;

/**
 * Describes an object that can be stored in the cache.
 */
public interface ICacheableObject {

    /**
     * Returns if this is a game.
     * 
     * @return True if this is a game, false otherwise
     */
    public boolean isGame();

    /**
     * Returns if this is a message.
     * 
     * @return True if this is a message, false otherwise
     */
    public boolean isMessage();
}
