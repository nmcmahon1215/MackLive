package com.macklive.rest;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.gson.Gson;
import com.macklive.storage.DataManager;

/**
 * Rest end point for getting new game messages.
 */
@Path("/messages")
public class MessageService {

    /**
     * Gets all the messages for a particular game.
     * 
     * @return A JSON representation of all game messages.
     */
    @GET
    @Path("/{gameId}")
    @Produces("application/json")
    public String getGameMessages(@PathParam("gameId") long gameId) {
        Gson gs = new Gson();
        return gs.toJson(DataManager.getInstance().getMessagesForGame(gameId));
    }

    @GET
    @Path("/{gameId}/{date}")
    @Produces("application/json")
    public String getGameMessagesAfterDate(@PathParam("gameId") long gameId,
            @PathParam("date") long millis) {
        Gson gs = new Gson();
        return gs.toJson(DataManager.getInstance()
                .getMessagesForGameAfterDate(gameId, new Date(millis)));
    }
}
