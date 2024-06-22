package task_person_utility.task_person_server;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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
    final AssignTasksServices assignTasksServices;
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
        assignTasksServices.assignTasks();
        return persons;
    }

    public Person getPerson(String name) {

        Bson findId = Filters.eq("name", name);
        Person findIdPerson = null;
        try {
            findIdPerson = personsDB.find(findId).first();
            if (findIdPerson == null) {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, "Unable to find any person with name: {0}", name);
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
        assignTasksServices.assignTasks();
        return findIdPerson;
    }

    public Person updatePerson(String name, Person person) {
        Person updatePerson = assignTasksServices.onlyUpdatePerson(name,person);
        assignTasksServices.assignTasks();
        return updatePerson;
    }

    public Person deletePerson(String name) {
        Person deletedPerson = getPerson(name);
        Bson filter = Filters.eq("name", name);
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
