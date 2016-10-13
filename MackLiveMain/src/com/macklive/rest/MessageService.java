package com.macklive.rest;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.macklive.objects.Game;
import com.macklive.objects.Message;
import com.macklive.serialize.GsonUtility;
import com.macklive.storage.DataManager;
import com.macklive.storage.TwitterManager;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Rest end point for getting new game messages.
 */
@Path("/messages")
public class MessageService {

    /**
     * Posts a message to a game
     *
     * @param messageJSON     A json representation of the message
     * @return A status message describing if the message was successfully posted or errored.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postMessage(String messageJSON) {
        try {
            JSONObject jso = new JSONObject(messageJSON);

            long gameId = jso.getLong("game");
            boolean userComment = !AuthenticationUtility.authenticate(DataManager.getInstance().getGame(gameId).getOwnerId());

            Message newMessage = new Message(jso.getString("author"), jso.getString("text"), gameId,
                    userComment);

            boolean shouldTweet = !jso.isNull("twitter") && jso.getBoolean("twitter");
            boolean result = true;
            if (shouldTweet) {
                String text = newMessage.getText();
                String link = DataManager.getInstance().getGame(gameId).getLink();
                if (link != null) {
                    text += " " + link;
                }
                result = TwitterManager.getInstance().tweet(text);
            }

            DataManager.getInstance().storeItem(newMessage);

            return result ? Response.status(Response.Status.OK).build() : Response.status(Response.Status.UNAUTHORIZED).build();

        } catch (JSONException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
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

    @GET
    @Path("/delete/{messageId}")
    public Response deleteMessage(@PathParam("messageId") long messageId) {

        DataManager dm = DataManager.getInstance();
        Message m = dm.getMessage(messageId);

        if (m == null) {
            return Response.status(500).build();
        }

        Game g = dm.getGame(m.getGameId());

        if (AuthenticationUtility.authenticate(g.getOwnerId())) {
            Key k = KeyFactory.createKey("Message", messageId);
            dm.deleteEntity(k);
            return Response.status(200).build();
        } else {
            return Response.status(403).build();
        }
    }

    @GET
    @Path("/approve/{messageId}")
    public Response approveMessage(@PathParam("messageId") long messageId) {
        DataManager dm = DataManager.getInstance();

        Message m = dm.getMessage(messageId);

        if (m == null) {
            return Response.status(500).build();
        }

        Game g = dm.getGame(m.getGameId());

        if (AuthenticationUtility.authenticate(g.getOwnerId())) {
            m.approve();
            m.setTime(new Date());
            dm.storeItem(m);
            return Response.status(200).build();
        }

        return Response.status(403).build();
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

        hm.put("messages", messages);
        if (messages.size() > 0) {
            hm.put("latestTime", messages.get(messages.size() - 1).getTime().getTime());
        }

        return gs.toJson(hm);
    }
}
