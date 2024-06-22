package task_person_utility.task_person_server;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PreDestroy;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class DBUtil {

    private MongoDatabase personTaskDataBase;
    private MongoClient mongoClient;
    private final Logger logger;

    public DBUtil(){
        initializeDB("PersonTask");
        logger = Logger.getLogger(DBUtil.class.getName());
    }

    @PreDestroy
    public void closeDBClient() {
        mongoClient.close();
    }

    @Bean
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    @Bean
    public MongoDatabase getPersonTaskDataBase() {
        return personTaskDataBase;
    }

    private Logger getLogger() {
        return logger;
    }

    public void renewDB(String dbName) {
        closeDBClient();
        initializeDB("PersonTask");
    }

    private void initializeDB(String dbName) {
        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
        ConnectionString mongoUri = new ConnectionString("mongodb://localhost:27017/");

        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(mongoUri).build();

        try {
            mongoClient = MongoClients.create(settings);
            personTaskDataBase = mongoClient.getDatabase(dbName);

        } catch (MongoException me) {
            getLogger().log(Level.SEVERE, "Unable to connect to the MongoDB instance due to an error: ", me);
            System.exit(1);
        }
    }
}
