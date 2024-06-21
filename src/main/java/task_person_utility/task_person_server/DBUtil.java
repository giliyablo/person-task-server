package task_person_utility.task_person_server;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Service
public class DBUtil {

    private final MongoDatabase personTaskDataBase;
    private final Logger logger;

    public DBUtil(){
        personTaskDataBase = getMongoDatabase("PersonTask");
        logger = Logger.getLogger(DBUtil.class.getName());
    }

    public MongoDatabase getPersonTaskDataBase() {
        return personTaskDataBase;
    }

    private Logger getLogger() {
        return logger;
    }

    private MongoDatabase getMongoDatabase(String dbName) {

        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
        ConnectionString mongoUri = new ConnectionString("mongodb://localhost:27017/");

        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(mongoUri).build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {

            MongoDatabase database = mongoClient.getDatabase(dbName);

            return database;
        } catch (MongoException me) {
            getLogger().log(Level.SEVERE, "Unable to connect to the MongoDB instance due to an error: ", me);
            System.exit(1);
            return null; // Ensure the method returns a value even in case of an error
        }
    }
}
