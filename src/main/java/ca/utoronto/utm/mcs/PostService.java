package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.bson.BSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import dagger.Module;
import dagger.Provides;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

public class PostService implements HttpHandler {

	private MongoClient db;

	@Inject
	public PostService(MongoClient db) {
		this.db = db;
	}

	@Override
	public void handle(HttpExchange r) throws IOException {
		
		
		try {
			
			
			// PUT /api/v1/post
			if (r.getRequestMethod().equals("PUT")) {
				
				// Convert HTTP Request Body to JSON Object
				JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
				
				// Check if all required attributes provided
				if (!body.has("title") || !body.has("author") || !body.has("content") || !body.has("tags")) {
					r.sendResponseHeaders(400, -1); // 400 BAD REQUEST 
                	return;
				}
				
				// Parse attributes from JSON Object
				String title = body.getString("title");
                String author = body.getString("author");
                String content = body.getString("content");
                
                JSONArray temp = body.getJSONArray("tags");
				ArrayList<String> tags = new ArrayList<String>();
                for (int i = 0; i < temp.length(); i++) {
                	tags.add(temp.getString(i));
                }
                
                // Create new post with given attributes
				Document post = new Document();
				post.put("title", title);
				post.put("author", author);
				post.put("content", content);
                post.put("tags", tags);
				
				// Insert Document to Collection
				db.getDatabase("csc301a2").getCollection("posts").insertOne(post);
				
				// Write JSON Response with appropriate Status Code
				String response = new JSONObject().put("_id", post.getObjectId("_id")).toString();
				r.sendResponseHeaders(200, response.length()); // 200 OK
				OutputStream output = r.getResponseBody();
                output.write(response.getBytes());
                output.close();
				return;
			}
			
			
			// GET /api/v1/post
			else if (r.getRequestMethod().equals("GET")) {
				
				// Convert HTTP Request Body to JSON Object
				JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
				
                // Search by _id
                if (body.has("_id")) {
                	
                	String id = body.getString("_id");
    				Document query = new Document("_id",  new ObjectId(id));
    				Document post = db.getDatabase("csc301a2").getCollection("posts").find(query).first();

    				if (post != null) {
    					
        				String response = post.toJson().toString();
        				r.sendResponseHeaders(200, response.length()); // 200 OK
        				OutputStream output = r.getResponseBody();
                        output.write(response.getBytes());
                        output.close();
        				return;
    				}
    				
    				r.sendResponseHeaders(404, -1); // 404 NOT FOUND
					return;
                }
                
                
                // Search by title
                if (body.has("title")) {
                	
                	String title = body.getString("title");
                	Bson query = Filters.regex("title", title);
    				MongoCursor<Document> posts = db.getDatabase("csc301a2").getCollection("posts").find(query).cursor();
		
    				
    				if (!posts.hasNext()) {
    					r.sendResponseHeaders(404, -1); // 404 NOT FOUND 
                    	return;
    				}
    				
    				ArrayList<String> temp = new ArrayList<String>();
    				while (posts.hasNext()) {
    					temp.add(posts.next().toJson());
    				}
    				
    				String response = temp.toString();
    				r.sendResponseHeaders(200, response.length()); // 200 OK
    				OutputStream output = r.getResponseBody();
                    output.write(response.getBytes());
                    output.close();
    				return;
                }
                
                
                // If no _id and no title
                else {
                	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST 
                	return;
                }
			}
			
			
			// DELETE /api/v1/post
			else if (r.getRequestMethod().equals("DELETE")) {

				// Convert HTTP Request Body to JSON Object
				JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
				              
                if (!body.has("_id")) {
                	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST 
                	return;
                }
                
                String id = body.getString("_id");
				Document query = new Document("_id",  new ObjectId(id));
			
				if (db.getDatabase("csc301a2").getCollection("posts").find(query).first() != null) {
					db.getDatabase("csc301a2").getCollection("posts").findOneAndDelete(query);
					r.sendResponseHeaders(200, -1); // 200 OK
					return;
				}

				r.sendResponseHeaders(404, -1); // 404 NOT FOUND
				return;
			}
			
			
			// If method other than PUT, GET or DELETE
			else {
				r.sendResponseHeaders(405, -1); // 405 METHOD NOT ALLOWED
				return;
			}
		}
		
		
		// JSON Error (improper/missing format)
		catch (JSONException e) {
        	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST
            e.printStackTrace();
            return;
        }
    	
		
		// Server Error (unsuccessful add/delete)
    	catch (IOException e) {
        	r.sendResponseHeaders(500, -1); // 500 INTERNAL SERVER ERROR
            e.printStackTrace();
            return;
        }	
	}
}