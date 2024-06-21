package task_person_utility.task_person_server;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AssignTasksServices {

    private final MongoDatabase personTaskDataBase;
    private final MongoCollection<Task> tasksDB;
    private final MongoCollection<Person> personsDB ;
    private final Logger logger;

    @Autowired
    public AssignTasksServices(DBUtil dbutil){
        personTaskDataBase=dbutil.getPersonTaskDataBase();
        tasksDB = personTaskDataBase.getCollection("tasks", Task.class);
        personsDB = personTaskDataBase.getCollection("persons", Person.class);
        logger = Logger.getLogger(AssignTasksServices.class.getName());
    }

    private Logger getLogger() {
        return logger;
    }

    public void assignTasks() {
        List<Person> availablePersons = getAvailablePersons();
        List<Task> notDoneTasks = getNotDoneTasks();

        if (!availablePersons.isEmpty() && !notDoneTasks.isEmpty()) {
            distributeTasks(availablePersons, notDoneTasks);
        }
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

    private List<Task> getNotDoneTasks() {
        List<Task> notDoneTasks = new ArrayList<>();
        Bson findDone = Filters.eq("done", false);
        try (MongoCursor<Task> taskCursor = tasksDB.find(findDone).iterator()) {
            while (taskCursor.hasNext()) {
                Task currentTask = taskCursor.next();
                notDoneTasks.add(currentTask);
                logTask(currentTask);
            }
        } catch (MongoException me) {
            handleMongoException(me);
        }
        return notDoneTasks;
    }

    private void distributeTasks(List<Person> availablePersons, List<Task> notDoneTasks) {
        int numberOfTasksPerAvailablePerson = notDoneTasks.size() / availablePersons.size();
        for (Task task : notDoneTasks) {
            assignTask(availablePersons, numberOfTasksPerAvailablePerson, task);
        }
    }

    private boolean assignTask(List<Person> availablePersons, int numberOfTasksPerAvailablePerson, Task task) {
        boolean foundMin = false;
        if (!availablePersons.isEmpty()) {
            Person minTasksPerson = availablePersons.get(0);
            int minTasksNumber=minTasksPerson.getTasksAssignedNumber();

            for (Person person : availablePersons) {
                int taskAssignNumber = person.getTasksAssignedNumber();
                if ((taskAssignNumber < minTasksNumber) && (taskAssignNumber < numberOfTasksPerAvailablePerson)) {
                    foundMin = true;
                    minTasksPerson = person;
                    minTasksNumber=taskAssignNumber;
                }
            }
            if (foundMin) {
                task.setPersonAssigned(minTasksPerson);
                minTasksPerson.setTasksAssignedNumber(minTasksNumber + 1);
            }
        }
        return foundMin;
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
}
