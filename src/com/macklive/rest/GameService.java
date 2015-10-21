package com.macklive.rest;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.datastore.Key;
import com.macklive.objects.Game;
import com.macklive.objects.Team;
import com.macklive.storage.DataManager;
import com.macklive.utility.JSONUtility;

/**
 * Rest endpoint for getting Game information
 */
@Path("/game")
public class GameService {

    @GET
    @Path("/recent/{num}")
    public String getRecentGames(@PathParam("num") int number){
        List<Game> games = DataManager.getInstance().getRecentGames(number);
        String result = "";
        
        for (Game g : games){
            result += g.toString();
            result += ",";
        }
        
        return result;
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String createNewGame(String teams){
        String[] teamNames = teams.split(",");
        String team1Name = teamNames[0];
        String team2Name = teamNames[1];
        
        DataManager dstore = DataManager.getInstance();
        Team t1 = dstore.getTeamByName(team1Name);
        Team t2 = dstore.getTeamByName(team2Name);
        
        Game g = new Game(t1, t2);
        
        Key k = dstore.storeItem(g);
        
        JSONUtility jsu= new JSONUtility();
        jsu.addProperty("id", k.getId());
        jsu.addProperty("name", g);
        
        return jsu.getJSON();
    }
}
