package task_person_utility.task_person_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Service
public class PersonServices  {

    private final DBUtil dbutil;
    private final MongoDatabase personTaskDataBase;
    private final MongoCollection<Person> personsDB;
    private final AssignTasksServices assignTasksServices;
    private final Logger logger;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    public PersonServices(DBUtil dbutil, AssignTasksServices assignTasksServices){
        this.dbutil=dbutil;
        personTaskDataBase=this.dbutil.getPersonTaskDataBase();
        this.assignTasksServices=assignTasksServices;
        personsDB = personTaskDataBase.getCollection("persons", Person.class);
        logger = Logger.getLogger(PersonServices.class.getName());
    }

    private Logger getLogger() {
        return logger;
    }

    public Person createPerson(Person person) {
        try {
            InsertOneResult result = personsDB.insertOne(person);
            if (getLogger().isLoggable(Level.INFO)) {
                getLogger().log(Level.INFO, String.format("Inserted document with ID: %s", result.getInsertedId()));
            }
            assignTasksServices.assignTasks();
            return person;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to insert person into MongoDB due to an error: ", me);
            }
            return null;
        }
    }

    public List<Person> getAllPersons() {
        List<Person> persons = new ArrayList<>();

        try (MongoCursor<Person> cursor = personsDB.find().iterator()) {
            while (cursor.hasNext()) {
                Person currentPerson = cursor.next();
                persons.add(currentPerson);
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, String.format("%s is in DB%n",
                            currentPerson.getName()));
                }
            }
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any persons in MongoDB due to an error: ", me);
            }
            System.exit(1);
        }

        dbutil.closeAndOpenDB();
        return persons;
    }

    public Person getPerson(String name) {

        Bson findName = Filters.eq("name", name);
        Person findNamePerson = null;
        try {
            findNamePerson = personsDB.find(findName).first();
            if (findNamePerson == null) {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, "Unable to find any person named: {0}", name);
                }
                System.exit(1);
            }
            dbutil.closeAndOpenDB();
            return findNamePerson;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any persons in MongoDB due to an error: ", me);
            }
            System.exit(1);
        }

        return findNamePerson;
    }

    public Person updatePerson(String name, Person person) {

        Person personToUpdate = getPerson( name);

        Bson findName = Filters.eq("name", name);

        Bson updateFilter = Updates.set("name", person.getName());

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);

        try {
            Person updatedDocument = personsDB.findOneAndUpdate(findName,
                    updateFilter, options);
            if (updatedDocument == null) {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, "Couldn't update the person. Did someone (or something) delete it?");
                }
            }else{
                getLogger().log(Level.INFO, "\nUpdated the person to: {0}" , updatedDocument);
            }
            assignTasksServices.assignTasks();
            return updatedDocument; // Ensure return here
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to update any persons in MongoDB due to an error: ", me);
            }
            return null; // Return null in case of exception
        }
    }


    public Person deletePerson(String name) {

        Person personToDelete = getPerson( name);

        Bson deleteFilter = Filters.eq("name", name);
        try {
            DeleteResult deleteResult = personsDB
                    .deleteOne(deleteFilter);
            if (deleteResult.getDeletedCount() == 0) { // Check if no documents were deleted
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, "Couldn't delete the person. Did someone (or something) delete it?");
                }
                return null; // Ensure return here
            } else {
                getLogger().log(Level.INFO, "\nDeleted the person: {0}", name);
                assignTasksServices.assignTasks();
                return personToDelete; // Ensure return here
            }
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to delete any persons in MongoDB due to an error: ", me);
            }
            return null; // Return null in case of exception
        }
    }
}
