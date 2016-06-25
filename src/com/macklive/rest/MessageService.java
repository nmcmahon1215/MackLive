package com.macklive.rest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.macklive.objects.Message;
import com.macklive.serialize.GsonUtility;
import com.macklive.storage.DataManager;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Rest end point for getting new game messages.
 */
@Path("/messages")
public class MessageService {

    /**
     * Posts a message to a game
     *
     * @param messageJSON     A json representation of the message
     * @param servletResponse Used for building the response
     * @return A status message describing if the message was successfully posted or errored.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String postMessage(String messageJSON, @Context HttpServletResponse servletResponse) {
        try {
            JSONObject jso = new JSONObject(messageJSON);

            long gameId = jso.getLong("game");
            User currentUser = UserServiceFactory.getUserService().getCurrentUser();
            boolean userComment = false;

            if (currentUser != null) {
                String currentUID = UserServiceFactory.getUserService().getCurrentUser().getUserId();
                String gameOwnerUID = DataManager.getInstance().getGame(gameId).getOwnerId();
                userComment = !currentUID.equals(gameOwnerUID);
            }

            Message newMessage = new Message(jso.getString("author"), jso.getString("text"), gameId,
                    userComment);

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
     * @param gameId The id of the game
     * @return A JSON representation of all game messages.
     */
    @GET
    @Path("/{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getGameMessages(@PathParam("gameId") long gameId) {
        return formResponse(DataManager.getInstance().getMessagesForGame(gameId));
    }

    /**
     * Gets all the messages for a particular game after a certain date
     *
     * @param gameId The id of the game
     * @param millis The date, represented in milliseconds from epoch
     * @return A JSON representation of the appropriate messages
     */
    @GET
    @Path("/{gameId}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getGameMessagesAfterDate(@PathParam("gameId") long gameId, @PathParam("date") long millis) {
        return formResponse(DataManager.getInstance().getMessagesForGameAfterDate(gameId, new Date(millis)));
    }

    /**
     * Gets all of the non approved messages for a game
     *
     * @param gameId The id of the game
     * @return A JSON representation of the messages
     */
    @GET
    @Path("/pending/{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPendingMessages(@PathParam("gameId") long gameId) {
        return formResponse(DataManager.getInstance().getMessagesForGame(gameId, false));
    }

    @GET
    @Path("/pending/{gameId}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPendingMessagesAfterDate(@PathParam("gameId") long gameId, @PathParam("date") long date) {
        return formResponse(DataManager.getInstance().getMessagesForGameAfterDate(gameId, new Date(date), false));
    }

    /**
     * Forms a response with a list of messages
     * 
     * @param messages
     *            The list of messages to include, sorted oldest first
     * @return A JSON representation of the messages and the timestamp in ms.
     */
    private String formResponse(List<Message> messages) {
        Gson gs = GsonUtility.getGson();

        HashMap<String, Object> hm = new HashMap<>();

        List<Message> responseMessages = new ArrayList<>();
        
        for (Message m : messages) {
            if (m.isApproved()) {
                responseMessages.add(m);
            }
        }

        hm.put("messages", responseMessages);
        if (responseMessages.size() > 0) {
            hm.put("latestTime", responseMessages.get(responseMessages.size() - 1).getTime().getTime());
        }

        return gs.toJson(hm);
    }
}
