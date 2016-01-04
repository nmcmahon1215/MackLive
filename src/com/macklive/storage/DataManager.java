package com.macklive.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.Game;
import com.macklive.objects.IBusinessObject;
import com.macklive.objects.ICacheableObject;
import com.macklive.objects.Message;
import com.macklive.objects.Team;

public class DataManager {

    private static DataManager instance = null;
    private DatastoreService dstore = null;
    private UserService userService;
    private CacheManager cacheManager;

    public static DataManager getInstance(){
        if (instance == null){
            instance = new DataManager();
        }
        return instance;
    }

    private DataManager(){
        this.dstore = DatastoreServiceFactory.getDatastoreService();
        this.userService = UserServiceFactory.getUserService();
        this.cacheManager = CacheManager.getInstance();
    }

    public Key storeItem(IBusinessObject obj) {
        Entity toStore = obj.getEntity();
        if (obj.hasOwner()) {
            toStore.setIndexedProperty("owner",
                    userService.getCurrentUser().getUserId());
        }
        Key k = dstore.put(toStore);
        obj.setKey(k);

        if (obj.isCacheable()) {
            cacheManager.load((ICacheableObject) obj);
        }

        return k;
    }

    /**
     * Finds a team by its name in the data store
     * @param name Unique name of the team
     * @return The team with the given name.
     * @throws TooManyResultsException If more than one team with the give name exists
     */
    public Team getTeamByName(String name) throws TooManyResultsException {
        Query q = new Query("Team");
        q.setFilter(CompositeFilterOperator.and(
                new FilterPredicate("Name", FilterOperator.EQUAL, name),
                new FilterPredicate("owner", FilterOperator.EQUAL,
                        userService.getCurrentUser().getUserId())));

        Entity e = dstore.prepare(q).asSingleEntity();

        if (e == null){
            return null;
        }

        try {
            return new Team(e);
        } catch (EntityMismatchException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a list of all the teams in the data store
     * @return A list of all the teams in the data store.
     */
    public List<Team> getTeams() {
        Query q = new Query("Team");
        q.setFilter(new FilterPredicate("owner", FilterOperator.EQUAL,
                userService.getCurrentUser().getUserId()));

        List<Entity> teams = dstore.prepare(q).asList(FetchOptions.Builder.withDefaults());
        List<Team> result = new ArrayList<Team>();
        for (Entity e : teams){
            try {
                result.add(new Team(e));
            } catch (EntityMismatchException e1) {
                e1.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Returns the entity with the given key
     * @param k The key to use as the lookup value
     * @return An entity with that unique key in the data store
     * @throws EntityNotFoundException if the key is not in the data store.
     */
    public Entity getEntityWithKey(Key k) throws EntityNotFoundException{
        return dstore.get(k);
    }

    /**
     * Gets the 5 most recent games
     * @param numGames The cap on games to return
     * @return The five most recent games
     */
    public List<Game> getRecentGames(int numGames) {
        Query q = new Query("Game");
        q.addSort("Date", SortDirection.DESCENDING);
        q.setFilter(new FilterPredicate("owner", FilterOperator.EQUAL,
                userService.getCurrentUser().getUserId()));

        List<Entity> queryResults = dstore.prepare(q).asList(FetchOptions.Builder.withLimit(numGames));

        List<Game> result = new ArrayList<Game>();
        try {

            for (Entity e : queryResults){
                result.add(new Game(e));
            }

            return result;

        } catch (EntityMismatchException e) {
            e.printStackTrace();
            //If it fails, return an empty list
            return new ArrayList<Game>();
        }
    }

    /**
     * Gets all the messages belonging to a particular game.
     * 
     * @param gameId
     *            The ID of the game
     * @return A list of messages for the game.
     */
    public List<Message> getMessagesForGame(long gameId) {
        if (this.cacheManager.hasMessages(gameId)) {
            return this.cacheManager.getMessages(gameId);
        } else {
            Query q = new Query("Message");
            q.setFilter(new FilterPredicate("game", FilterOperator.EQUAL, gameId));
            q.addSort("time", SortDirection.ASCENDING);

            this.cacheManager.load(getMessageByQuery(q));

            return getMessagesForGame(gameId);
        }

    }

    /**
     * Gets all the messages belonging to a particular game, and time-stamped
     * after the given date (and time).
     * 
     * @param gameId
     *            The ID of the game
     * @param date
     *            Date starting date
     * @return A list of messages for the game posted after the given date.
     */
    public List<Message> getMessagesForGameAfterDate(long gameId, Date date) {
        if (this.cacheManager.hasMessages(gameId)) {
            List<Message> messages = this.cacheManager.getMessages(gameId);
            List<Message> result = new ArrayList<Message>();

            for (int i = messages.size() - 1; i >= 0; i--) {
                Message m = messages.get(i);
                if (m.getTime().compareTo(date) > 0) {
                    result.add(m);
                } else {
                    break;
                }
            }

            return result;

        } else {
            Query q = new Query("Message");

            q.setFilter(CompositeFilterOperator.and(new FilterPredicate("game", FilterOperator.EQUAL, gameId),
                    new FilterPredicate("time", FilterOperator.GREATER_THAN, date)));
            q.addSort("time", SortDirection.ASCENDING);

            this.cacheManager.load(getMessageByQuery(q));

            return getMessagesForGameAfterDate(gameId, date);
        }
    }

    /**
     * Gets a list of messages based on the given query
     * 
     * @param q
     *            Query to use to fetch messages
     * @return A list of messages satisfying the query
     */
    private List<Message> getMessageByQuery(Query q) {
        Iterable<Entity> queryResults = dstore.prepare(q).asIterable();

        List<Message> messages = new ArrayList<Message>();
        try {
            for (Entity e : queryResults) {
                messages.add(new Message(e));
            }
        } catch (EntityMismatchException e) {
            e.printStackTrace();
            return new ArrayList<Message>();
        }

        return messages;
    }

    /**
     * Gets the game with the given id number
     * 
     * @param idNum
     *            The id number of the game to fetch.
     * @return The game with the given id number
     */
    public Game getGame(long idNum) {
        if (this.cacheManager.hasGame(idNum)) {
            return this.cacheManager.getGame(idNum);
        } else {
            try {
                Game g = new Game(this.getEntityWithKey(KeyFactory.createKey("Game", idNum)));
                this.cacheManager.load(g);
            } catch (EntityMismatchException | EntityNotFoundException e) {
                return null;
            }

            return getGame(idNum);
        }
    }
}
