package service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import pojo.TotemResult;
import lxo.MaecToOwl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import java.net.*;
import java.util.ResourceBundle;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

/**
 * REST Web Service
 *
 * @author h
 */
@Path("generic")
public class MaecToOwlResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of MaecToOwlResource
     */
    public MaecToOwlResource() {
    }

    /**
     * Retrieves representation of an instance of sk.service.MaecToOwlResource
     * credits:
     * https://stackoverflow.com/questions/15262965/send-file-as-a-parameter-to-a-rest-service-from-a-client
     *
     * @param req json request
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

        ResourceBundle config = ResourceBundle.getBundle("config.config");

        System.out.println(config.getString("fusekiURL"));
        System.out.println(config.getString("datasetName"));
        System.out.println(config.getString("downloadsFolder"));
        
        // Create download folder if it doesnt exists
        new File(config.getString("downloadsFolder") + File.separator + "tmp").mkdirs();

        System.out.println(req.getFilename());
        String inputFilePath = config.getString("downloadsFolder") + File.separator + "tmp" + File.separator + req.getFilename() + ".json";
        File inFile = new File(inputFilePath);
        try {
            System.out.println("Creating new file for request. " + inputFilePath);
            inFile.createNewFile();
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
        }
        System.out.println("Saving request to downloads folder.");

        try (PrintWriter pw = new PrintWriter(inFile, "UTF-8")) {
            //System.out.println(req.getData());
            pw.print(req.getData());
//            Gson gson = new Gson();
//            CuckooReport report = gson.fromJson(req.getData(), CuckooReport.class);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
        }

        // This is default clean owl ontology file in rdf/xml format. Maec report is added to this file,
        // rdf/xml format is extracted from it.
        URL blank_dataset_url = Thread.currentThread().getContextClassLoader().getResource("blank_rdfxml.owl");
        System.out.println(blank_dataset_url.toString());

        File blankOntologyFile = new File(blank_dataset_url.getFile());
        if (!blankOntologyFile.exists()) {
            System.out.println("Blank ontology file wasn't found. " + blankOntologyFile.getAbsolutePath());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Blank ontology file wasn't found. " + blankOntologyFile.getAbsolutePath()).build();
        }

        File outFile = new File(config.getString("downloadsFolder") + File.separator + "tmp" + File.separator + "dataset_" + req.getFilename() + ".owl");
        try {
            System.out.println("Creating out dataset. " + outFile.getAbsolutePath());
            outFile.createNewFile();
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
        }

        if (!copyFile(blankOntologyFile, outFile)) {
            System.out.println("Error while copying black ontology.");
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error while copying black ontology.").build();
        }

        // load dataset from fuseki server
        String url = config.getString("fusekiURL"); // url to tomcat running fuseki
        String query = config.getString("datasetName"); // name of dataset

        try {
            MaecToOwl converter = new MaecToOwl(inFile, outFile);
            System.out.println("Converting maec result to owl.");
            converter.convert();
        } catch (OWLOntologyCreationException | OWLOntologyStorageException | IllegalArgumentException e) {
            System.out.println(e.getLocalizedMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
        }
        
        boolean err = false;

        //loadDatasetFromFuseki(url+query, outFile);
        if (!uploadOntologyToFuseki(url + query, outFile)) {
            err = true;
        }
        
        System.out.println("Cleaning...");
        
        if (! inFile.delete()) {
            System.out.println("File couldnt be deleted " + inFile.getAbsolutePath());
        }
        if (!outFile.delete()) {
            System.out.println("File couldnt be deleted " + outFile.getAbsolutePath());            
        }
        
        if (err) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not upload result to fuseki server.").build();
        }
        
        System.out.println("Conversion finished. Ontology uploaded to fuseki at " + config.getString("fusekiURL") + config.getString("datasetName"));

        return Response.status(Status.ACCEPTED)
                .entity("Conversion finished succesfully. Ontology uploaded to fuseki at " + config.getString("fusekiURL") + config.getString("datasetName")).build();
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

    private static boolean uploadOntologyToFuseki(String url, final File ontology) {
        System.out.println("Uploading dataset to fuseki. " + url);

        Model model = ModelFactory.createDefaultModel();
        model.read(ontology.getAbsolutePath(), "RDF/XML");

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(url);

        System.out.println("trying to upload ontology to fuseki");
        // In this variation, a connection is built each time. 
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            System.out.println("Putting ontology from " + ontology.getAbsolutePath());
            conn.put(model);

            System.out.println("Comitting");
            conn.commit();

            System.out.println("Closing RDF connection");
            //conn.abort();
            conn.end();
        }
        return true;
    }

    private static boolean loadDatasetFromFuseki(String url, File outFile) {
        System.out.println("Loading dataset from fuseki. " + url);

        // credits: https://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
        URLConnection connection;
        BufferedInputStream response = null;
        try {
            System.out.println("Trying to connect.");
            connection = new URL(url).openConnection();
            //connection.setRequestProperty("Accept-Charset", "UTF-8");
            response = new BufferedInputStream(connection.getInputStream());
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buff = new byte[1024];
                while (response.read(buff, 0, 1024) != -1) {
                    System.out.println("reading response");
                    fos.write(buff); // save loaded dataset to file
                }
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
                return false;
            }
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
            return false;
        } finally {
            if (response != null) {
                try {
                    response.close(); // close buffered input reader if not null
                } catch (IOException e) {
                    System.out.println(e.getLocalizedMessage());
                    return false;
                }
            }
        }
        return true;
    }

    private static void execSelectAndPrint(String serviceURI, String query) {
        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI,
                query);
        ResultSet results = q.execSelect();

        // write to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //convert to JSON format
        ResultSetFormatter.outputAsJSON(outputStream, results);
        //turn json to string
        String json = new String(outputStream.toByteArray());
        //print json string
        System.out.println(json);

    }

    private static void execSelectAndProcess(String serviceURI, String query) {
        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI,
                query);
        ResultSet results = q.execSelect();

        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            // assumes that you have an "?x" in your query
            RDFNode x = soln.get("x");
            System.out.println(x);
        }
    }
}
