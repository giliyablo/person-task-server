package task_person_utility.task_person_server;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AssignTasksServices {

    // private final DBUtil dbutil;
    // private final MongoDatabase personTaskDataBase;
    private final MongoCollection<Task> tasksDB;
    private final MongoCollection<Person> personsDB ;
    private final Logger logger;

    @Autowired
    public AssignTasksServices(MongoDatabase mongoDatabase){
        // this.dbutil = dbutil;
        // personTaskDataBase=mongoDatabase;// this.dbutil.getPersonTaskDataBase();
        tasksDB = mongoDatabase.getCollection("tasks", Task.class);
        personsDB = mongoDatabase.getCollection("persons", Person.class);
        logger = Logger.getLogger(AssignTasksServices.class.getName());
    }

    private Logger getLogger() {
        return logger;
    }

    public boolean assignTasks() {
        return assignTasks(false);
    }

    public boolean assignTasks(boolean forced) {
        List<Person> availablePersons = getAvailablePersons();
        List<Task> notDoneTasks = getNotDoneTasks(true);
        if (!availablePersons.isEmpty() && !notDoneTasks.isEmpty()) {
            double numberOfTasksPerAvailablePerson = Math.floor((notDoneTasks.size()+0.0) / (availablePersons.size()+0.0));
            distributeTasks(availablePersons, notDoneTasks, numberOfTasksPerAvailablePerson);
            return true;
        }
        if (!availablePersons.isEmpty()){
            Iterator<Person> availablePersonsIterator = availablePersons.iterator();
            while(availablePersonsIterator.hasNext()){
                Person person = availablePersonsIterator.next();
                fixAPersonsTasksCount(person);
            }
        }
        return false;
    }

    private List<Person> getAvailablePersons() {
        List<Person> availablePersons = new ArrayList<>();
        Bson findAvailable = Filters.eq("availability", true);
        try (MongoCursor<Person> personCursor = personsDB.find(findAvailable).iterator()) {
            while (personCursor.hasNext()) {
                Person currentPerson = personCursor.next();
                availablePersons.add(currentPerson);
                logPerson(currentPerson);
            }
        } catch (MongoException me) {
            handleMongoException(me);
        }
        return availablePersons;
    }

    private List<Task> getNotDoneTasks(boolean forced) {
        List<Task> notDoneTasks = new ArrayList<>();
        Bson findDone = Filters.eq("done", false);
        try (MongoCursor<Task> taskCursor = tasksDB.find(findDone).iterator()) {
            while (taskCursor.hasNext()) {
                Task currentTask = taskCursor.next();
                if (forced || (currentTask.getPersonAssigned() == null)) {
                    notDoneTasks.add(currentTask);
                }else{
                    Person personForCheck = currentTask.getPersonAssigned();
                    Bson personExists = Filters.eq("name",personForCheck.getName());
                    FindIterable<Person> existingPersons = personsDB.find(personExists);
                    if (!existingPersons.iterator().hasNext()){
                        notDoneTasks.add(currentTask);
                    }
                }
                logTask(currentTask);
            }
        } catch (MongoException me) {
            handleMongoException(me);
        }
        return notDoneTasks;
    }

    private void distributeTasks(List<Person> availablePersons, List<Task> notDoneTasks, double numberOfTasksPerAvailablePerson) {
        for (Task task : notDoneTasks) {
            assignTask(availablePersons, numberOfTasksPerAvailablePerson, task);
        }
    }

    private boolean assignTask(List<Person> availablePersons, double numberOfTasksPerAvailablePerson, Task task) {
        boolean foundMin = false;
        if (!availablePersons.isEmpty()) {
            Person minTasksPerson = availablePersons.get(0);
            int minTasksNumber=minTasksPerson.getTasksAssignedNumber();

            for (Person person : availablePersons) {
                int taskAssignNumber = person.getTasksAssignedNumber();
                if ((taskAssignNumber <= minTasksNumber) && (taskAssignNumber < numberOfTasksPerAvailablePerson)) {
                    foundMin = true;
                    minTasksPerson = person;
                    minTasksNumber=taskAssignNumber;
                }
            }
            if (foundMin) {
                Person oldPerson = task.getPersonAssigned();
                task.setPersonAssigned(minTasksPerson);
                onlyUpdateTask(task.getName(),task);

                if (oldPerson != null){
                    fixAPersonsTasksCount(oldPerson);
                }
                fixAPersonsTasksCount(minTasksPerson);
            }
        }
        return foundMin;
    }

    private void fixAPersonsTasksCount(Person person) {
        Bson filterPersonExists = Filters.exists("personAssigned");
        FindIterable<Task> tasksWithPersons = tasksDB.find(filterPersonExists);

        List<Task> tasks = new ArrayList<>();
        try (MongoCursor<Task> cursor = tasksWithPersons.iterator()) {
            while (cursor.hasNext()) {
                Task currentTask = cursor.next();
                if (currentTask.getPersonAssigned().getName().compareTo(person.getName()) == 0){
                    tasks.add(currentTask);
                }
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, String.format("%s is in DB%n", currentTask.getName()));
                }
            }
            person.setTasksAssignedNumber(tasks.size());
            onlyUpdatePerson(person.getName(),person);
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any tasks in MongoDB due to an error: ", me);
            }
        }
    }

    private void logPerson(Person person) {
        if (getLogger().isLoggable(Level.INFO)) {
            getLogger().log(Level.INFO, String.format("%s is in DB%n", person.getName()));
        }
    }

    private void logTask(Task task) {
        if (getLogger().isLoggable(Level.INFO)) {
            getLogger().log(Level.INFO, String.format("%s is in DB%n", task.getName()));
        }
    }

    private void handleMongoException(MongoException me) {
        if (getLogger().isLoggable(Level.SEVERE)) {
            getLogger().log(Level.SEVERE, "Unable to find any tasks in MongoDB due to an error: ", me);
        }
        System.exit(1);
    }

    public Person onlyUpdatePerson(String name, Person person) {
        Person updatedPerson = null;
        Bson filter = Filters.eq("name", name);
        Bson update = Updates.combine(
                Updates.set("name", person.getName()),
                Updates.set("availability", person.getAvailability()),
                Updates.set("tasksAssignedNumber", person.getTasksAssignedNumber())
        );
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        try {
            updatedPerson = personsDB.findOneAndUpdate(filter, update, options);
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to update person in MongoDB due to an error: ", me);
            }
        }
        return updatedPerson;
    }

    public Task onlyUpdateTask(String name, Task task) {
        Bson filter = Filters.eq("name", name);
        Bson updates = Updates.combine(
                Updates.set("name", task.getName()),
                Updates.set("description", task.getDescription()),
                Updates.set("dateOfCreation", task.getDateOfCreation()),
                Updates.set("done", task.getDone()),
                Updates.set("personAssigned", task.getPersonAssigned())
        );
        try {
            Task updatedTask = tasksDB.findOneAndUpdate(filter, updates);
            if (updatedTask != null && getLogger().isLoggable(Level.INFO)) {
                getLogger().log(Level.INFO, String.format("Updated document with name: %s", name));
            }
            return updatedTask;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to update Task in MongoDB due to an error: ", me);
            }
            return null;
        }
    }
}
