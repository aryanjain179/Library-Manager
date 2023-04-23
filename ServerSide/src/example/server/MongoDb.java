package example.server;

import com.mongodb.*;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDb {
    private MongoClient mongo;
    private MongoDatabase database;

    public static MongoCollection<Document> library;

    public static MongoCollection<Document> users;
    private final String URI =
            "mongodb+srv://aryanj179:aryanj@cluster0.hl2i0bx.mongodb.net/?retryWrites=true&w=majority";
    private final String DB = "database";
    private final String COLLECTION = "users";

    public MongoDb(){
//        mongo = MongoClients.create(URI);
//        database = mongo.getDatabase(DB);
//        collection = database.getCollection(COLLECTION);

    }

    public void connect() {
        String connectionString = "mongodb+srv://aryanj179:aryanj@cluster0.hl2i0bx.mongodb.net/?retryWrites=true&w=majority";
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
        // Create a new client and connect to the server
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("database");
        library = database.getCollection("library");
        users = database.getCollection("users");
//        database.runCommand(new Document("ping", 1));
//        System.out.println("Pinged your deployment. You successfully connected to MongoDB!");

    }


    public static void main(String[] args) {
//        mongo = MongoClients.create(URI);
//        database = mongo.getDatabase(DB);
//        collection = database.getCollection(COLLECTION);
//        mongo.close();
    }
}