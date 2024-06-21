package task_person_utility.task_person_server;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class DB_Util {

    static private MongoDatabase personTaskDataBase;
    static private MongoCollection<Task> tasksDB;
    static private MongoCollection<Person> personsDB;

    public DB_Util(){
        personTaskDataBase = getMongoDatabase("PersonTask");

        tasksDB = personTaskDataBase.getCollection("tasks", Task.class);
        personsDB = personTaskDataBase.getCollection("persons", Person.class);
    }

    private  Logger getLogger() {
        return Logger.getLogger(DB_Util.class.getName());
    }

    private  MongoDatabase getMongoDatabase(String dbName) {

        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
        // Replace the placeholder connection string below with your
        // Altas cluster specifics. Be sure it includes
        // a valid username and password! Note that in a production environment,
        // you do not want to store your password in plain-text here.
        ConnectionString mongoUri = new ConnectionString("mongodb://localhost:27017/");

        // Provide the name of the database and collection you want to use.
        // If they don't already exist, the driver and Atlas will create them
        // automatically when you first write data.
        // String dbName = "PersonTask";
        // String collectionName = "tasks";

        // a CodecRegistry tells the Driver how to move data between Java POJOs (Plain Old Java Objects) and MongoDB documents
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        // The MongoClient defines the connection to our MongoDB datastore instance (Atlas) using MongoClientSettings
        // You can create a MongoClientSettings with a Builder to configure codecRegistries, connection strings, and more
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(mongoUri).build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            // MongoDatabase defines a connection to a specific MongoDB database
            MongoDatabase database = mongoClient.getDatabase(dbName);
            // MongoCollection defines a connection to a specific collection of documents in a specific database
            return database;
        } catch (MongoException me) {
            getLogger().log(Level.SEVERE, "Unable to connect to the MongoDB instance due to an error: ", me);
            System.exit(1);
            return null; // Ensure the method returns a value even in case of an error
        }
    }

    public static MongoCollection<Task> getTasksDB() {
        return tasksDB;
    }

    public static MongoCollection<Person> getPersonsDB() {
        return personsDB;
    }

}
