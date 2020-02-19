package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.Arrays;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import dagger.Module;
import dagger.Provides;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Module
public class DaggerModule {

	private static HttpServer server;
	private static MongoClient db;
	
    @Provides public MongoClient provideMongoClient() {
        /* TODO: Fill in this function */
    	return null;
    }

    @Provides public HttpServer provideHttpServer() {
        /* TODO: Fill in this function */
        return null;
    }
}
