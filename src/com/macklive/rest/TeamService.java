package com.macklive.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.GsonUtility;
import com.macklive.objects.Team;
import com.macklive.storage.DataManager;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/teams")
public class TeamService {

    Logger logger = Logger.getLogger(this.getClass().getName());

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createTeam(@FormDataParam("teamId") long teamId,
                               @FormDataParam("teamName") String name,
                               @FormDataParam("teamAbbr") String abbr,
                               @FormDataParam("teamLogo") InputStream logoStream) {

        byte[] buffer = new byte[8192];
        Blob teamLogo = null;
        if (logoStream != null){
            try {
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                while (logoStream.read(buffer) != -1){
                    bs.write(buffer);
                }
                teamLogo = new Blob (bs.toByteArray());

            } catch (IOException e) {
                e.printStackTrace();
                return Response.status(500).build();
            }
        }

        Team t = new Team(name, abbr);


        if (teamLogo != null) {
            t.setLogo(teamLogo);
        }

        //0 is the default value for a long
        //Replace item with given id
        if (teamId != 0) {
            t.setKey(KeyFactory.createKey("Team", teamId));
        }

        DataManager.getInstance().storeItem(t);

        return Response.status(204).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON
            )
    public String getTeamNames(){
        List<Team> teams = DataManager.getInstance().getTeams();
        Collections.sort(teams, new Comparator<Team>(){

            @Override
            public int compare(Team t1, Team t2) {
                return t1.getName().compareTo(t2.getName());
            }
            
        });
        Gson gs = GsonUtility.getGson();
        return gs.toJson(teams);
    }
    
    @GET
    @Path("/image/{id}")
    public Response getImage(@PathParam("id") long id) {
        ResponseBuilder builder = Response.status(500);
        try {
            Team t = new Team(DataManager.getInstance()
                    .getEntityWithKey(KeyFactory.createKey("Team", id)));
            if (t.getLogo() == null) {
                logger.warning("No logo for team " + id);
                builder.status(404);
                return builder.build();
            }
            final byte[] imageBytes = t.getLogo().getBytes();
            builder.status(200).type("image/png").entity(new StreamingOutput(){

                @Override
                public void write(OutputStream output) throws IOException,
                        WebApplicationException {
                    output.write(imageBytes);
                    output.flush();
                }
                
            });
        } catch (Exception e){
            e.printStackTrace();
        }
        
        return builder.build();
    }

    @GET
    @Path("/{id}")
    public String getTeam(@PathParam("id") long id) {
        Key k = KeyFactory.createKey("Team", id);
        try {
            Team t = new Team(DataManager.getInstance().getEntityWithKey(k));
            return t.toJSON();
        } catch (EntityMismatchException | EntityNotFoundException e) {
            Response.status(404);
            e.printStackTrace();
            return "Not Found";
        }
    }
}
