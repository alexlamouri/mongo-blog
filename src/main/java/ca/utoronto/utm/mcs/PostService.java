package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
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
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

public class PostService implements HttpHandler {

	private MongoCollection<Document> collection;

	public PostService(MongoCollection<Document> collection) {
		this.collection = collection;
	}

	@Override
	public void handle(HttpExchange r) throws IOException {
		// TODO Auto-generated method stub
		
		try {
			
			// PUT /api/v1/post
			if (r.getRequestMethod().equals("PUT")) {
				
				// Convert HTTP Request Body to JSON Object
				JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
				
				// Parse title parameter from JSON Object
				String title = "";
                if (body.has("title")) title = body.getString("title");
                
                // Parse author parameter from JSON Object
                String author = "";
                if (body.has("author")) author = body.getString("author");
                
                // Parse content parameter from JSON Object
                String content = "";
                if (body.has("content")) content = body.getString("content");
                
                // Parse tags parameter from JSON Object
                JSONArray jArray = null;
                if (body.has("tags")) jArray = body.getJSONArray("tags");
                
                // Check if all required parameters provided
                if (title.isEmpty() || author.isEmpty() || content.isEmpty() || jArray == null) {
                	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST 
                	return;
                }
                
                // Create new Document
				Document document = new Document();
				
				// Add parameters to Document
				document.put("title", title);
				document.put("author", author);
				document.put("content", content);
				
				// Convert JSONArray to ArrayList
				ArrayList<String> tags = new ArrayList<String>(jArray.length());
                for (int i = 0; i < jArray.length(); i++) {
                	tags.add(jArray.getString(i));
                }
				document.put("tags", tags);
				
				// Insert Document to Collection
				collection.insertOne(document);
				
				// Get _id of inserted Document
				ObjectId id = document.getObjectId("_id");
				
				// Create JSON Response with inserted Document _id
				JSONObject jObject = new JSONObject();
				jObject.put("_id", id);
				
				// Write JSON Response with appropriate Status Code
				String response = jObject.toString();
				r.sendResponseHeaders(200, response.length()); // 200 OK
				OutputStream output = r.getResponseBody();
                output.write(response.getBytes());
                output.close();
				return;
			}
			
			// GET /api/v1/post
			else if (r.getRequestMethod().equals("GET")) {
			}
			
			// DELETE /api/v1/post
			else if (r.getRequestMethod().equals("DELETE")) {

				// Convert HTTP Request Body to JSON Object
				JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
				
				// Parse _id parameter from JSON Object
				String id = "";
                if (body.has("_id")) id = body.getString("_id");
              
                // Check if all required parameters provided
                if (id.isEmpty()) {
                	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST 
                	return;
                }
                  
                // Create new Query Document
				Document document = new Document();
				
				// Add given _id to Query Document
				document.put("_id",  new ObjectId(id));
			
				// Delete post if post exists
				if (collection.findOneAndDelete(document) != null) {
					r.sendResponseHeaders(200, -1); // 200 OK
					return;
				};
				
				// If post does not exist
				r.sendResponseHeaders(404, -1); // 404 NOT FOUND
				return;
			}
			
			else {
				r.sendResponseHeaders(405, -1); // 405 METHOD NOT ALLOWED
				return;
			}
		}
		
		catch (JSONException e) {
        	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST
            e.printStackTrace();
            return;
        }
    	
    	catch (IOException e) {
        	r.sendResponseHeaders(500, -1); // 500 INTERNAL SERVER ERROR
            e.printStackTrace();
            return;
        }	
	}
}
