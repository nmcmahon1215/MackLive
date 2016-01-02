package com.macklive.rest;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.gson.Gson;
import com.macklive.objects.GsonUtility;
import com.macklive.objects.Message;
import com.macklive.storage.DataManager;

/**
 * Rest end point for getting new game messages.
 */
@Path("/messages")
public class MessageService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String postMessage(String messageJSON,
            @Context HttpServletResponse servletResponse) {
        try {
            JSONObject jso = new JSONObject(messageJSON);
            boolean approved = true;

            Message newMessage = new Message(jso.getString("author"),
                    jso.getString("text"), jso.getLong("game"), approved);

            DataManager.getInstance().storeItem(newMessage);

            return "OK";

        } catch (JSONException e) {
            e.printStackTrace();
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            return "Bad Request";
        }
    }

    /**
     * Gets all the messages for a particular game.
     * 
     * @return A JSON representation of all game messages.
     */
    @GET
    @Path("/{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getGameMessages(@PathParam("gameId") long gameId) {
        Gson gs = GsonUtility.getGson();
        return gs.toJson(DataManager.getInstance().getMessagesForGame(gameId));
    }

    @GET
    @Path("/{gameId}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getGameMessagesAfterDate(@PathParam("gameId") long gameId,
            @PathParam("date") long millis) {
        Gson gs = GsonUtility.getGson();
        return gs.toJson(DataManager.getInstance()
                .getMessagesForGameAfterDate(gameId, new Date(millis)));
    }
}
