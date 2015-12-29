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
import com.macklive.objects.Message;
import com.macklive.objects.Team;

public class DataManager {

    private static DataManager instance = null;
    private DatastoreService dstore = null;
    private UserService userService;

    public static DataManager getInstance(){
        if (instance == null){
            instance = new DataManager();
        }
        return instance;
    }

    private DataManager(){
        this.dstore = DatastoreServiceFactory.getDatastoreService();
        this.userService = UserServiceFactory.getUserService();
    }

    public Key storeItem(IBusinessObject obj){
        Entity toStore = obj.getEntity();
        toStore.setIndexedProperty("owner",
                userService.getCurrentUser().getUserId());
        Key k = dstore.put(toStore);
        obj.setKey(k);
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
        Query q = new Query("Message");
        Key game = KeyFactory.createKey("Game", gameId);
        q.setFilter(CompositeFilterOperator.and(
                new FilterPredicate("game", FilterOperator.EQUAL, game),
                new FilterPredicate("owner", FilterOperator.EQUAL,
                        userService.getCurrentUser().getUserId())));
        q.addSort("time", SortDirection.ASCENDING);

        return getMessageByQuery(q);

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
        Query q = new Query("Message");
        Key game = KeyFactory.createKey("Game", gameId);

        q.setFilter(CompositeFilterOperator.and(
                new FilterPredicate("owner", FilterOperator.EQUAL, userService.getCurrentUser().getUserId()),
                new FilterPredicate("game", FilterOperator.EQUAL, game),
                new FilterPredicate("time", FilterOperator.GREATER_THAN,
                        date)));
        q.addSort("time", SortDirection.ASCENDING);

        return getMessageByQuery(q);
    }

    /**
     * Gets a list of messages based on the given query
     * 
     * @param q
     *            Query to use to fetch messages
     * @return A list of messages satisfying the query
     */
    private List<Message> getMessageByQuery(Query q) {
        List<Entity> queryResults = dstore.prepare(q)
                .asList(FetchOptions.Builder.withDefaults());

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
}
