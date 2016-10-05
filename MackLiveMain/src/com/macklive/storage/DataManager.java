package com.macklive.storage;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataManager {

    private static DataManager instance = null;
    private DatastoreService dstore = null;
    private UserService userService;
    private DataCache cacheManager;

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private DataManager() {
        this.dstore = DatastoreServiceFactory.getDatastoreService();
        this.userService = UserServiceFactory.getUserService();
        this.cacheManager = DataCache.getInstance();
    }

    public Key storeItem(IBusinessObject obj) {
        Entity toStore = obj.getEntity();
        if (obj.hasOwner()) {
            toStore.setIndexedProperty("owner",
                    userService.getCurrentUser().getUserId());
        }
        Key k = dstore.put(toStore);
        obj.setKey(k);
        cacheManager.load(k, toStore);

        return k;
    }

    /**
     * Returns a list of all the teams in the data store
     *
     * @return A list of all the teams in the data store.
     */
    public List<Team> getTeams() {
        Query q = new Query("Team");
        q.setFilter(new FilterPredicate("owner", FilterOperator.EQUAL,
                userService.getCurrentUser().getUserId()));
        q.setKeysOnly();

        List<Entity> teams = dstore.prepare(q).asList(FetchOptions.Builder.withDefaults());
        List<Team> result = new ArrayList<>();
        for (Entity e : teams) {
            Key key = e.getKey();
            try {
                Entity entity = cacheManager.get(key);
                if (entity != null) {
                    result.add(new Team(entity));
                } else {
                    Team t = new Team(getEntityWithKey(key));
                    result.add(t);
                    cacheManager.load(t);
                }
            } catch (EntityMismatchException | EntityNotFoundException e1) {
                e1.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Returns the entity with the given key
     *
     * @param k The key to use as the lookup value
     * @return An entity with that unique key in the data store
     * @throws EntityNotFoundException if the key is not in the data store.
     */
    public Entity getEntityWithKey(Key k) throws EntityNotFoundException {
        Entity e = dstore.get(k);
        cacheManager.load(k, e);
        return e;
    }

    /**
     * Gets the 5 most recent games
     *
     * @param numGames The cap on games to return
     * @return The five most recent games
     */
    public List<Game> getRecentGames(int numGames) {
        Query q = new Query("Game");
        q.addSort("Date", SortDirection.DESCENDING);
        q.setFilter(new FilterPredicate("owner", FilterOperator.EQUAL,
                userService.getCurrentUser().getUserId()));
        q.setKeysOnly();

        List<Entity> queryResults = dstore.prepare(q).asList(FetchOptions.Builder.withLimit(numGames));

        List<Game> result = new ArrayList<>();
        try {

            for (Entity e : queryResults) {
                Key key = e.getKey();
                Game g = cacheManager.getGame(key);
                if (g != null) {
                    result.add(g);
                } else {
                    g = new Game(getEntityWithKey(key));
                    cacheManager.load(g);
                    result.add(g);
                }
            }

            return result;

        } catch (EntityMismatchException | EntityNotFoundException e) {
            e.printStackTrace();
            //If it fails, return an empty list
            return new ArrayList<>();
        }
    }

    /**
     * Gets all the messages belonging to a particular game.
     *
     * @param gameId The ID of the game
     * @return A list of messages for the game.
     */
    public List<Message> getMessagesForGame(long gameId) {
        return getMessagesForGame(gameId, true);
    }

    /**
     * Gets the messages belonging to a particular game
     *
     * @param gameId   Id of the game
     * @param approved True to get approved messages, false to get pending messages
     * @return A list of messages for the game
     */
    public List<Message> getMessagesForGame(long gameId, boolean approved) {
        Query q = new Query("Message");
        q.setFilter(
                CompositeFilterOperator.and(
                        new FilterPredicate("game", FilterOperator.EQUAL, gameId),
                        new FilterPredicate("approved", FilterOperator.EQUAL, approved))
        );
        q.addSort("time", SortDirection.ASCENDING);

        return getMessageByQuery(q);
    }

    /**
     * Gets all the messages belonging to a particular game, and time-stamped
     * after the given date (and time).
     *
     * @param gameId The ID of the game
     * @param date   Date starting date
     * @return A list of messages for the game posted after the given date.
     */
    public List<Message> getMessagesForGameAfterDate(long gameId, Date date) {
        return getMessagesForGameAfterDate(gameId, date, true);
    }

    /**
     * Gets all the messages belonging to a particular game, and time-stamped
     * after the given date (and time).
     *
     * @param gameId   The ID of the game
     * @param date     Date starting date
     * @param approved True to get approved messages, false for pending messages
     * @return A list of messages for the game posted after the given date.
     */
    public List<Message> getMessagesForGameAfterDate(long gameId, Date date, boolean approved) {
        List<Message> messages;
        List<Message> result = new ArrayList<>();

        Query q = new Query("Message");

        q.setFilter(
                CompositeFilterOperator.and(
                        new FilterPredicate("game", FilterOperator.EQUAL, gameId),
                        new FilterPredicate("time", FilterOperator.GREATER_THAN, date),
                        new FilterPredicate("approved", FilterOperator.EQUAL, approved)
                ));
        q.addSort("time", SortDirection.ASCENDING);

        messages = getMessageByQuery(q);

        for (int i = messages.size() - 1; i >= 0; i--) {
            Message m = messages.get(i);
            if (m.getTime().compareTo(date) > 0) {
                result.add(m);
            } else {
                break;
            }
        }

        return result;
    }

    /**
     * Gets a list of messages based on the given query
     *
     * @param q Query to use to fetch messages
     * @return A list of messages satisfying the query
     */
    private List<Message> getMessageByQuery(Query q) {
        q.setKeysOnly();
        Iterable<Entity> queryResults = dstore.prepare(q).asIterable();

        List<Message> messages = new ArrayList<>();
        try {
            for (Entity e : queryResults) {
                Key k = e.getKey();
                Entity result = cacheManager.get(k);
                if (result != null) {
                    messages.add(new Message(result));
                } else {
                    messages.add(new Message(getEntityWithKey(k)));
                }
            }
        } catch (EntityMismatchException | EntityNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return messages;
    }

    /**
     * Gets the game with the given id number
     *
     * @param idNum The id number of the game to fetch.
     * @return The game with the given id number
     */
    public Game getGame(long idNum) {
        try {
            Key k = KeyFactory.createKey("Game", idNum);
            Entity e = cacheManager.get(k);

            if (e != null) {
                return new Game(e);
            } else {
                return new Game(this.getEntityWithKey(k));
            }
        } catch (EntityMismatchException | EntityNotFoundException e) {
            return null;
        }
    }

    /**
     * Gets a message with the given ID number
     *
     * @param idNum ID of the message to retrieve
     * @return A message corresponding to the ID, or null if one does not exist.
     */
    public Message getMessage(long idNum) {
        try {
            Key k = KeyFactory.createKey("Message", idNum);
            Entity e = cacheManager.get(k);

            if (e != null) {
                return new Message(e);
            } else {
                return new Message(this.getEntityWithKey(k));
            }
        } catch (EntityMismatchException | EntityNotFoundException e) {
            return null;
        }
    }

    /**
     * Deletes entity with the given key
     *
     * @param k Key of the entity to delete
     */
    public void deleteEntity(Key k) {
        dstore.delete(k);
    }

    public TwitterAuthorization getTwitterAuth() throws EntityMismatchException, EntityNotFoundException {
        Entity e;
        Key k = KeyFactory.createKey("TwitterAuth", UserServiceFactory.getUserService().getCurrentUser().getUserId());
        if ((e = this.cacheManager.get(k)) == null) {
            e = this.dstore.get(k);
            this.cacheManager.load(k, e);
        }
        return new TwitterAuthorization(e);
    }
}
