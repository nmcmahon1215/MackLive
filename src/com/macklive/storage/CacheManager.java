package com.macklive.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.macklive.objects.Game;
import com.macklive.objects.IBusinessObject;
import com.macklive.objects.Message;

/**
 * This class is used for caching results from the data store. This will greatly
 * reduce the number of calls on the data store and reduce the likelihood of
 * reaching the quota.
 * 
 */
public class CacheManager {

    private final int CACHE_SIZE = 15;

    private static CacheManager instance;

    private Map<Long, CacheObject> dataMap;

    /**
     * Singleton.
     * 
     * @return The single instance of the cache manager
     */
    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }

    /**
     * Constructor
     */
    private CacheManager() {
        dataMap = new HashMap<Long, CacheObject>();
    }

    /**
     * Returns the game in the cache manager
     * 
     * @param gameId
     *            Id of the game to return
     * @return The game with the given ID, or null if it does not exist in the
     *         cache
     */
    public Game getGame(long gameId) {
        CacheObject result = dataMap.get(gameId);
        if (result != null) {
            return result.getGame();
        } else {
            return null;
        }
    }

    /**
     * Returns the list of messages belonging to a given game
     * 
     * @param gameId
     *            The game to fetch messages for.
     * @return A list of messages for the game, or null if the game has no
     *         messages or the game does not exist.
     */
    public List<Message> getMessages(long gameId) {
        CacheObject result = dataMap.get(gameId);
        if (result != null) {
            return result.getMessages();
        } else {
            return null;
        }
    }

    /**
     * Returns whether or not the game is in the cache
     * 
     * @param gameId
     *            Id of the game being searched for
     * @return True if the game is in the cache, false otherwise
     */
    public boolean hasGame(long gameId) {
        return dataMap.containsKey(gameId) && dataMap.get(gameId).getGame() != null;
    }

    /**
     * Returns whether or not there are messages for a given game.
     * 
     * @param gameId
     *            Id of the game which the messages belong to
     * @return True if messages for the game are in the cache, false otherwise
     */
    public boolean hasMessages(long gameId) {
        return dataMap.containsKey(gameId) && !dataMap.get(gameId).getMessages().isEmpty();
    }

    /**
     * Loads a game into the cache.
     * 
     * @param g
     *            Game to load into cache
     */
    private void load(Game g) {
        long gameId = g.getKey().getId();
        CacheObject item;
        boolean exists;

        if (exists = dataMap.containsKey(gameId)) {
            item = dataMap.get(gameId);
        } else {
            item = new CacheObject();
        }

        item.setGame(g);

        if (!exists) {
            dataMap.put(gameId, item);
            trimCache();
        }
    }
    
    /**
     * Loads a single message into the cache. This message is appended to the
     * end of the list if some messages exist for the same game.
     * 
     * @param m
     *            Message to add to the list.
     */
    private void load(Message m){
        long gameId = m.getGameId();
        CacheObject item;
        boolean exists;

        if (exists = dataMap.containsKey(gameId)) {
            item = dataMap.get(gameId);
        } else {
            item = new CacheObject();
        }

        List<Message> messages = item.getMessages();
        messages.add(m);

        if (!exists) {
            dataMap.put(gameId, item);
            trimCache();
        }
    }

    /**
     * Trims the cache down to the appropriate level.
     */
    private void trimCache() {
        int currentCacheSize = dataMap.size();
        if (currentCacheSize <= CACHE_SIZE) {
            return;
        }

        List<Entry<Long, CacheObject>> currentCache = new ArrayList<Entry<Long, CacheObject>>();

        currentCache.addAll(dataMap.entrySet());

        currentCache.sort(new Comparator<Entry<Long, CacheObject>>() {

            @Override
            public int compare(Entry<Long, CacheObject> o1, Entry<Long, CacheObject> o2) {
                return o1.getValue().getLastAccessed().compareTo(o2.getValue().getLastAccessed());
            }

        });
        
        for (int i = 0; i < currentCacheSize - CACHE_SIZE; i++) {
            dataMap.remove(currentCache.get(i).getKey());
        }

    }

    /**
     * Attempts to load the business object into the cache.
     * 
     * @param obj
     *            Object to load into the cache
     * @return True if the object was loaded, false otherwise
     */
    public boolean load(IBusinessObject obj) {
        if (obj.getClass().equals(Message.class)) {
            load((Message) obj);
        } else if (obj.getClass().equals(Game.class)) {
            load((Game) obj);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Attempts to load a list of business objects into the cache
     * 
     * @param objs
     *            Objects to load into the cache.
     * @return True if the objects were loaded, false if one or more failed.
     */
    public boolean load(List<IBusinessObject> objs) {
        boolean result = true;

        for (IBusinessObject obj : objs) {
            result = load(obj) && result;
        }

        return result;
    }

}
