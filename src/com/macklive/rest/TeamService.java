package com.macklive.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;

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

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.Team;
import com.macklive.storage.DataManager;

@Path("/teams")
public class TeamService {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createTeam(@FormDataParam("teamName") String name,
            @FormDataParam("teamAbbr") String abbr,
            @FormDataParam("teamLogo") InputStream logoStream){

        try {
            if (DataManager.getInstance().getTeamByName(name) != null){
                return Response.status(409).build();
            }
        } catch (TooManyResultsException e1) {
            e1.printStackTrace();
        }

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
        
        if (logoStream != null){
            DataManager.getInstance().storeItem(new Team(name, abbr, teamLogo));
        } else {
            DataManager.getInstance().storeItem(new Team(name, abbr));
        }

        return Response.status(204).build();
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getTeamNames(){
        List<Team> teams = DataManager.getInstance().getTeams();
        teams.sort(new Comparator<Team>(){

            @Override
            public int compare(Team t1, Team t2) {
                return t1.getName().compareTo(t2.getName());
            }
            
        });
        String result = "";
        for (Team t : teams){
            result += t.getName() + ",";
        }
        return result;
    }
    
    @GET
    @Path("/image/{name}")
    public Response getImage(@PathParam("name") String teamName){
        ResponseBuilder builder = Response.status(500);
        try {
            Team t = DataManager.getInstance().getTeamByName(teamName);
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
}
