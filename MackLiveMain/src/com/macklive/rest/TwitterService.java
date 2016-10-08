package com.macklive.rest;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.storage.TwitterManager;
import org.json.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;


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
}
