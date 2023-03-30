package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import pojo.TotemResult;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import java.net.*;
import java.util.ResourceBundle;
import javax.ws.rs.GET;
import lxo.OWLWriter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import pojo.maec.MAECDocument;

/**
 * REST Web Service
 *
 * @author Tibor Galko
 */
@Path("convert")
public class MaecToOwlResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of MaecToOwlResource
     */
    public MaecToOwlResource() {
    }

    /**
     * Retrieves representation of an instance of service.MaecToOwlResource
     *
     * @param req json request REQUIRED filename a data
     * @return an instance of java.lang.String
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadFile(TotemResult req) {
        if (req == null) {
            System.out.println("Must supply a valid request");
            return Response.status(Status.BAD_REQUEST).entity("Must supply a valid request").build();
        }
        if (req.getFilename() == null || "".equals(req.getFilename())
                || req.getData() == null || "".equals(req.getData())) {
            System.out.println("Data or filename are empty");
            return Response.status(Status.BAD_REQUEST).entity("Data or filename are empty.").build();
        }

        MAECDocument maecData;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            maecData = gson.fromJson(req.getData(), MAECDocument.class);
        } catch (JsonSyntaxException e) {
            System.out.println("Error while parsing request data: " + e.getLocalizedMessage());
            return Response.status(Status.BAD_REQUEST).entity("Error while parsing request data: " + e.getLocalizedMessage()).build();
        }
        
        if (!maecData.isValid()) {
            System.out.println("MAEC request is not valid.");
            return Response.status(Status.BAD_REQUEST).entity("Request has invalid MAEC data.").build();
        }

        ResourceBundle config = ResourceBundle.getBundle("config.config");

        System.out.println(config.getString("fusekiURL"));
        System.out.println(config.getString("datasetName"));
        System.out.println(config.getString("downloadsFolder"));

        // Create download folder if it doesnt exists
        new File(config.getString("downloadsFolder") + File.separator + "tmp").mkdirs();

        // This is default clean owl ontology file in rdf/xml format. Maec report is added to this file,
        // rdf/xml format is extracted from it.
        URL blank_dataset_url = Thread.currentThread().getContextClassLoader().getResource("blank_rdfxml.owl");

        File blankOntologyFile = new File(blank_dataset_url.getFile());
        if (!blankOntologyFile.exists()) {
            System.out.println("Blank ontology file wasn't found. " + blankOntologyFile.getAbsolutePath());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Blank ontology file wasn't found.").build();
        }

        File outFile = new File(config.getString("downloadsFolder") + File.separator + "tmp" + File.separator + "dataset_" + req.getFilename() + ".owl");
        try {
            System.out.println("Creating out dataset. " + outFile.getAbsolutePath());
            outFile.createNewFile();
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
        }
        // Copy blank ontology to outfile
        if (!copyFile(blankOntologyFile, outFile)) {
            System.out.println("Error while copying blank ontology.");
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error while copying black ontology.").build();
        }

        // load data from config
        String url = config.getString("fusekiURL"); // url to tomcat running fuseki
        String query = config.getString("datasetName"); // name of dataset

        try {
            System.out.println("Converting maec result to owl.");

            OWLWriter w = new OWLWriter(maecData);
            boolean completed = w.writeFile(outFile);
            if (completed) {
                System.out.println("DONE");
            } else {
                System.out.println("ERROR");
            }

        } catch (OWLOntologyCreationException | OWLOntologyStorageException | IllegalArgumentException e) {
            System.out.println(e.getLocalizedMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
        }

        boolean err;
        try {
            // Send file to update Fuseki dataset
            err = !postFileToFuseki(url + query, outFile);
        } catch (IOException ex) {
            System.out.println("Error " + ex.getLocalizedMessage());
            err = true;
        }

        System.out.println("Cleaning...");
        if (!outFile.delete()) {
            System.out.println("File couldnt be deleted " + outFile.getAbsolutePath());
        }

        if (err) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not upload result to fuseki server.").build();
        }
        System.out.println("Conversion finished. Ontology uploaded to fuseki at " + config.getString("fusekiURL") + config.getString("datasetName"));

        return Response.status(Status.ACCEPTED)
                .entity("Conversion finished succesfully.").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response status() {
        return Response.status(Status.ACCEPTED).entity("{\"status\": \"OK\"}").build();
    }

    private static boolean postFileToFuseki(String url, final File ontology) throws IOException {

        // Close is called automatically to dealocate resources
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            // Create POST request to update ontology dataset on fuseki server
            HttpPost httppost = new HttpPost(url + "/upload");

            FileBody bin = new FileBody(ontology);
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("bin", bin)
                    // Charset has to be set otherwise fuseki will throw nullptr exception
                    .setMimeSubtype("RDF/XML") 
                    .build();

            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost.getRequestLine());
            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                }
                // All results must be consumed to properly close connection
                EntityUtils.consume(resEntity);
            }
        }
        return true;
    }

    // credits: https://beginnersbook.com/2014/05/how-to-copy-a-file-to-another-file-in-java/
    private static boolean copyFile(File from, File to) {
        try (FileInputStream inStream = new FileInputStream(from)) {
            try (FileOutputStream outstream = new FileOutputStream(to)) {
                byte[] buffer = new byte[1024];

                int length;
                /*copying the contents from input stream to
                 * output stream using read and write methods
                 */
                while ((length = inStream.read(buffer)) > 0) {
                    outstream.write(buffer, 0, length);
                }
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
                return false;
            }
            System.out.println("Dataset copy created succesfully.");
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            return false;
        }
        return true;
    }
}
