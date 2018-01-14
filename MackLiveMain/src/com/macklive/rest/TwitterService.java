package com.macklive.rest;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.Game;
import com.macklive.storage.DataManager;
import com.macklive.twitter.TwitterManager;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import twitter4j.*;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.crypto.Data;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Created by Nick on 10/3/16.
 */
@Path("/twitter")
public class TwitterService {

    private static final String CONSUMER_KEY = "3Aifbpp7c9Ot3BUYtCRyZQnnD";

    private static final String CONSUMER_SECRET = "QGoXhYxdq4MtyGgg0hZUnDjzzBLNdfYOudGw04oijro8h42z5l";

    private Twitter twitter;

    private Twitter getTwitter() {
        if (this.twitter == null) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey(CONSUMER_KEY);
            cb.setOAuthConsumerSecret(CONSUMER_SECRET);
            this.twitter = new TwitterFactory(cb.build()).getInstance();
        }
        return this.twitter;

    }

    @GET
    @Path("/signin")
    public Response signIn(@Context UriInfo uriInfo, @Context HttpServletRequest request) throws
            EntityNotFoundException, EntityMismatchException, TwitterException {
        TwitterManager tm = TwitterManager.getInstance();
        String callbackUrl = uriInfo.getAbsolutePath().toString().replace("signin", "callback");
        return Response.temporaryRedirect(tm.getAuthorizeUri(callbackUrl, request.getSession())).build();
    }

    @GET
    @Path("/signout")
    public Response signOut() {
        TwitterManager.getInstance().signOut();
        return Response.temporaryRedirect(URI.create("/console/console.html")).build();
    }

    @GET
    @Path("/callback")
    public Response callback(@Context HttpServletRequest request, @Context UriInfo uriInfo,
                             @QueryParam("oauth_verifier") String verifier) {
        try {
            TwitterManager tm = TwitterManager.getInstance();
            HttpSession session = request.getSession();
            tm.createAuthToken((RequestToken) session.getAttribute("twitterRequestToken"), verifier);
            session.removeAttribute("twitterRequestToken");
            return Response.temporaryRedirect(URI.create("/console/console.html")).build();
        } catch (TwitterException e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/status")
    public Response getStatus() throws EntityNotFoundException, EntityMismatchException {
        TwitterManager tm = TwitterManager.getInstance();
        return Response.status(tm.verifyCredentials() ? Response.Status.OK : Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("/limit")
    public Response getLimit() {
        JSONObject jso = new JSONObject();
        jso.put("limit", TwitterManager.getInstance().getLimit());
        return Response.ok(jso.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/stream/{gameId}")
    public Response startStream(@PathParam("gameId") long gameId, @Context UriInfo uriInfo) {
        try {
            TwitterManager.getInstance().setUpTwitterStream(gameId, uriInfo.getBaseUri().toString());
        } catch (Exception e) {
            return Response.serverError().build();
        }

        return Response.ok().build();
    }

    @GET
    @Path("/stream/cleanup")
    public Response cleanupStreams() {
        TwitterManager.getInstance().cleanUpTwitterStreams();
        return Response.ok().build();
    }
}
