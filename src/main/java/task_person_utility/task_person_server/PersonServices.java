package task_person_utility.task_person_server;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PersonServices {

    private final MongoCollection<Person> personsDB;
    private final AssignTasksServices assignTasksServices;
    private final Logger logger;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    public PersonServices(MongoDatabase mongoDatabase, AssignTasksServices assignTasksServices) {
        personsDB = mongoDatabase.getCollection("persons", Person.class);
        this.assignTasksServices = assignTasksServices;
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
                    getLogger().log(Level.INFO, String.format("%s is in DB%n", currentPerson.getName()));
                }
            }
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any persons in MongoDB due to an error: ", me);
            }
            System.exit(1);
        }
        return persons;
    }

    public Person getPerson(String id) {
        Bson findId = Filters.eq("_id", id);
        Person findIdPerson = null;
        try {
            findIdPerson = personsDB.find(findId).first();
            if (findIdPerson == null) {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, "Unable to find any person with ID: {0}", id);
                }
                System.exit(1);
            }
            return findIdPerson;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any persons in MongoDB due to an error: ", me);
            }
            System.exit(1);
        }
        return findIdPerson;
    }

    public Person updatePerson(String id, Person person) {
        Person updatedPerson = null;
        Bson filter = Filters.eq("_id", id);
        Bson update = Updates.combine(
                Updates.set("name", person.getName()),
                Updates.set("availability", person.getAvailability()),
                Updates.set("tasksAssignedNumber", person.getTasksAssignedNumber())
        );
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        try {
            updatedPerson = personsDB.findOneAndUpdate(filter, update, options);
            assignTasksServices.assignTasks();
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to update person in MongoDB due to an error: ", me);
            }
        }
        return updatedPerson;
    }

    public Person deletePerson(String id) {
        Person deletedPerson = null;
        Bson filter = Filters.eq("_id", id);
        try {
            deletedPerson = personsDB.findOneAndDelete(filter);
            assignTasksServices.assignTasks();
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to delete person from MongoDB due to an error: ", me);
            }
        }
        return deletedPerson;
    }
}
