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
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.gson.Gson;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.objects.Team;
import com.macklive.serialize.GsonUtility;
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
        byte[] teamLogo = null;
        if (logoStream != null) {
            try {
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                while (logoStream.read(buffer) != -1) {
                    bs.write(buffer);
                }
                teamLogo = bs.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
                return Response.status(500).build();
            }
        }

        teamLogo = resizeImage(teamLogo, 150, 150);

        Team t = new Team(name, abbr);

        if (teamLogo != null) {
            Blob logoBlob = new Blob(teamLogo);
            t.setLogo(logoBlob);
        }

        //0 is the default value for a long
        //Replace item with given id
        if (teamId != 0) {
            t.setKey(KeyFactory.createKey("Team", teamId));
        }

        DataManager.getInstance().storeItem(t);

        return Response.status(204).build();
    }

    /**
     * Resizes the image to the given height and width. Only downscales the image.
     *
     * @param originalImage The image to resize
     * @param height        Height of the resulting image
     * @param width         Width of the resulting image
     * @return A byte array representing the transformed image
     */
    private byte[] resizeImage(byte[] originalImage, int height, int width) {

        if (originalImage == null) {
            return null;
        }

        ImagesService imageService = ImagesServiceFactory.getImagesService();
        Image original = ImagesServiceFactory.makeImage(originalImage);

        if (original.getHeight() < height || original.getWidth() < width) {
            return originalImage;
        }

        Transform t = ImagesServiceFactory.makeResize(height, width);

        return imageService.applyTransform(t, original).getImageData();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON
    )
    public String getTeamNames() {
        List<Team> teams = DataManager.getInstance().getTeams();
        teams.sort(Comparator.comparing(Team::getName));
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
            builder.status(200).type("image/png").entity((StreamingOutput) output -> {
                output.write(imageBytes);
                output.flush();
            });
        } catch (Exception e) {
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
