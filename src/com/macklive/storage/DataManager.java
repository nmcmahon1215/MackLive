package com.macklive.storage;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
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
    
    public Team getTeamByName(String name) throws TooManyResultsException, EntityMismatchException{
        Query q = new Query("Team");
        q.setFilter(new FilterPredicate("Name", FilterOperator.EQUAL, name));
        
        Entity e = dstore.prepare(q).asSingleEntity();
        
        if (e == null){
            return null;
        }
        
        return new Team(e);
    }
}
