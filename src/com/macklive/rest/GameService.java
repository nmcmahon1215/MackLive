package com.macklive.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.Game;
import com.macklive.objects.Team;
import com.macklive.storage.DataManager;

/**
 * Rest endpoint for getting Game information
 */
@Path("/game")
public class GameService {

    @GET
    @Path("/recent/{num}")
    public String getRecentGames(@PathParam("num") int number){
        List<Game> games = DataManager.getInstance().getRecentGames(number);
        Gson gs = new Gson();
        return gs.toJson(games);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{idNum}")
    public String getGameById(@PathParam("idNum") long idNum){
        Key k = KeyFactory.createKey("Game", idNum);
        try {
            Game g = new Game(DataManager.getInstance().getEntityWithKey(k));
            return g.toJSON();
        } catch (EntityMismatchException | EntityNotFoundException e) {
            return "{}";
        }
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String createNewGame(String teams) throws EntityMismatchException, EntityNotFoundException{
        String[] teamNames = teams.split(",");
        String team1Id = teamNames[0];
        String team2Id = teamNames[1];
        
        DataManager dstore = DataManager.getInstance();
        Key k1 = KeyFactory.createKey("Team", Long.parseLong(team1Id));
        Key k2 = KeyFactory.createKey("Team", Long.parseLong(team2Id));
        Team t1 = new Team(dstore.getEntityWithKey(k1));
        Team t2 = new Team(dstore.getEntityWithKey(k2));
        
        Game g = new Game(t1, t2);
        
        dstore.storeItem(g);
        
        return g.toJSON();
    }
}
