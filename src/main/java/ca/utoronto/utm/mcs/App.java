package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpServer;

import javax.inject.Inject;

import org.bson.Document;


public class App
{
    static int port = 8080;

    public static void main(String[] args) throws IOException
    {
    	Dagger service = DaggerDaggerComponent.create().buildMongoHttp();
    	
    	//Create DB here
    	MongoDatabase database = service.getDb().getDatabase("csc301a2");
    	MongoCollection<Document> collection = database.getCollection("posts");
    	
    	//Create your server context here
    	service.getServer().createContext("/api/v1/post", new PostService(collection));
    	service.getServer().start();
    	
    	System.out.printf("Server started on port %d", port);
    }
}
