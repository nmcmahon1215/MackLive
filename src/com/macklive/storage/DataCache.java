package com.macklive.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.Game;
import com.macklive.objects.IBusinessObject;
import com.macklive.objects.Message;

/**
 * This class is used for caching results from the data store. This will greatly
 * reduce the number of calls on the data store and reduce the likelihood of
 * reaching the quota.
 */
public class DataCache {

    private Logger log = Logger.getLogger(this.getClass().getName());
    private static DataCache instance;


    private MemcacheService cache;

    /**
     * Singleton.
     *
     * @return The single instance of the cache manager
     */
    static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }

    /**
     * Constructor
     */
    private DataCache() {
        cache = MemcacheServiceFactory.getMemcacheService();
        log.log(Level.INFO, "Cache initialized");
    }

    /**
     * Returns the game in the cache manager
     *
     * @param gameKey Key of the game to return
     * @return The game with the given ID, or null if it does not exist in the
     * cache
     */
    Game getGame(Key gameKey) {
        Entity gameEntity = (Entity) cache.get(gameKey);

        if (gameEntity != null) {
            try {
                return new Game(gameEntity);
            } catch (EntityMismatchException e) {
                e.printStackTrace();
                log.severe("Entity mismatch when retrieving game from cache");
            }
        }

        return null;
    }

    /**
     * Returns the list of messages belonging to a given game
     *
     * @param messageKey The key of the mesage to fetch
     * @return A list of messages for the game, or null if the game has no
     * messages or the game does not exist.
     */
    private Message getMessage(Key messageKey) {
        Entity messageEntity = (Entity) cache.get(messageKey);

        if (messageEntity != null) {
            try {
                return new Message(messageEntity);
            } catch (EntityMismatchException e) {
                e.printStackTrace();
                log.severe("Entity mismatch when retrieving message from cache");
            }
        }
        return null;
    }

    /**
     * Convenience method for returning multiple messages. If any message is not in the cache, it is loaded from the
     * datastore.
     *
     * @param messageKeys A list of message keys to retrieve from the cache
     * @return A list of message corresponding the with the list of keys
     */
    List<Message> getMessages(List<Key> messageKeys) {
        List<Message> result = new ArrayList<>();
        DataManager dstore = DataManager.getInstance();

        for (Key k : messageKeys) {
            Message m = getMessage(k);
            if (m != null) {
                result.add(m);
            } else {
                try {
                    Entity missingMessage = dstore.getEntityWithKey(k);
                    cache.put(k, missingMessage);
                    result.add(new Message(missingMessage));
                } catch (EntityNotFoundException e) {
                    e.printStackTrace();
                    log.severe("Message not found with key: " + k);
                } catch (EntityMismatchException e) {
                    e.printStackTrace();
                    log.severe("Entity mismatch when retrieving message from datastore");
                }
            }
        }

        return result;
    }

    /**
     * Loads the cacheblae object into the cache.
     *
     * @param obj Object to load into the cache
     */
    void load(IBusinessObject obj) {
        cache.put(obj.getKey(), obj);
    }

    void load(Key k, Entity e) {
        cache.put(k, e);
    }

    /**
     * Loads the list of cacheable objects into the cache.
     *
     * @param objs Objects to load into the cache.
     */
    void load(List<? extends IBusinessObject> objs) {
        for (IBusinessObject obj : objs) {
            load(obj);
        }
    }

    Entity get(Key id) {
        Object obj = cache.get(id);
        log.info("Retrieving object from cache: " + obj);
        return (Entity) obj;
    }
}
