package com.macklive.storage;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.IBusinessObject;
import com.macklive.objects.Team;

public class DataManager {

    private static DataManager instance = null;
    private DatastoreService dstore = null;
    
    public static DataManager getInstance(){
        if (instance == null){
            instance = new DataManager();
        }
        return instance;
    }
    
    private DataManager(){
        this.dstore = DatastoreServiceFactory.getDatastoreService();
    }
    
    public Key storeItem(IBusinessObject obj){
        return dstore.put(obj.getEntity());
    }
    
    /**
     * Finds a team by its name in the data store
     * @param name Unique name of the team
     * @return The team with the given name.
     * @throws TooManyResultsException If more than one team with the give name exists
     */
    public Team getTeamByName(String name) throws TooManyResultsException {
        Query q = new Query("Team");
        q.setFilter(new FilterPredicate("Name", FilterOperator.EQUAL, name));
        
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
}
