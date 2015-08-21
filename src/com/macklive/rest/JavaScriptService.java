package com.macklive.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

@Path("/js")
public class JavaScriptService {

	/**
	 * Gets the common JavaScript files and returns them.
	 * @param context ServletContext used for finding the JS files
	 * @return The combined JavaScript
	 * @throws IOException if the files cannot be read.
	 */
	@GET
	@Produces("text/javascript")
	public String getBaseJavaScript(@Context ServletContext context) throws IOException {
		String result = "";

		//Get Common Files
		String pathName = context.getRealPath("/js/common");
		if (pathName != null){
			File commonFiles = new File(context.getRealPath("/js/common"));
			for (File f : commonFiles.listFiles()){
				result += this.getFileString(f);
			}
		}

		return result;
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
	public String getAdditionalJavaScript(@PathParam("subFolder") String subFolder, @Context ServletContext context) throws IOException{
		String result = this.getBaseJavaScript(context);

		//Get additional files, if any
		if (subFolder != null && !subFolder.isEmpty()){
			String pathName = context.getRealPath("/js/" + subFolder);
			if (pathName != null){
				File requestedFiles = new File(context.getRealPath("/js/" + subFolder));
				for (File f : requestedFiles.listFiles()){
					result += this.getFileString(f);
				}
			}
		}

		return result;
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
