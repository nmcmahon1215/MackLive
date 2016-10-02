/**
 * This class returns the applicable javascript when loading pages.
 */
package com.macklive.rest;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

@Path("/js")
public class JavaScriptService {

    private Logger log = Logger.getLogger(this.getClass().getName());
    private MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();

    /**
     * Gets the common JavaScript files and returns them.
     * @param context ServletContext used for finding the JS files
     * @return The combined JavaScript
     * @throws IOException if the files cannot be read.
     */
    @GET
    @Produces("text/javascript")
    public Response getBaseJavaScript(@Context ServletContext context) throws Exception {
        String result = getConcatJavascript("common", context);
        return Response.ok(result).build();
    }

    /**
     * Concatenates javascript in the corresponsing subfolder
     *
     * @param subFolder Subfolder to obtain javascript from
     * @param context   ServletContext for determining the root path
     * @return A string of the concatenated javascript.
     * @throws IOException
     */
    private String getConcatJavascript(String subFolder, ServletContext context) throws IOException {
        String result = "";

        String cachedFile = (String) this.memcache.get(subFolder);
        if (cachedFile != null) {
            this.log.info("Retrieving JS bundle '" + subFolder + "' from memcache");
            return cachedFile;
        }

        this.log.info("Cache miss for JS bundle: " + subFolder);
        if (subFolder != null && !subFolder.isEmpty()) {
            String pathName = context.getRealPath("/js/" + subFolder);
            if (pathName != null) {
                File requestedFiles = new File(pathName);
                for (File f : sortFiles(requestedFiles.listFiles())) {
                    if (f.getName().matches(".*\\.min\\.js$") && isProduction()) {
                        result += this.getFileString(f);
                    } else if (!isProduction()) {
                        result += this.getFileString(f);
                    }
                }
            }
        }

        this.memcache.put(subFolder, result);

        return result;
    }

    /**
     * Determines if we are running in development mode or in production
     *
     * @return True if running in production, false otherwise
     */
    private boolean isProduction() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
    }

    /**
     * Sorts the array of files by name
     *
     * @param array Array of files
     * @return Sorted list of files
     */
    private List<File> sortFiles(File[] array) {
        List<File> fileList = Arrays.asList(array);
        Collections.sort(fileList, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return fileList;
    }

    /**
     * Gets common JavaScript files and those in the given subfolder.
     * @param subFolder The subfolder to search for.
     * @param context Servlet context for finding the JS files.
     * @return The combined JavaScript.
     * @throws IOException
     */
    @GET
    @Path("/{subFolder}")
    @Produces("text/javascript")
    public Response getAdditionalJavaScript(@PathParam("subFolder") String subFolder, @Context ServletContext context) throws Exception {
        String result = this.getConcatJavascript("common", context);
        result += this.getConcatJavascript(subFolder, context);

        return Response.ok(result).build();
    }

    /**
     * Returns a string version of the contents of a file
     * @param f File to stringify
     * @return A string containing the file's contents
     * @throws IOException if I/O error occurs
     */
    private String getFileString(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = br.readLine();
        String contents = "";
        while(line != null){
            contents += line;
            contents += "\n";
            line = br.readLine();
        }
        br.close();
        contents += "\n";
        return contents;
    }
}
