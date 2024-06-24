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

    private final MongoCollection<Task> tasksDB;
    private final MongoCollection<Person> personsDB ;
    private final Logger logger;

    @Autowired
    public AssignTasksServices(MongoDatabase mongoDatabase){
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
        boolean changed = false;

        List<Person> availablePersons = getAvailablePersons();
        List<Task> notDoneTasks = getNotDoneTasks(true);
        if (!availablePersons.isEmpty() && !notDoneTasks.isEmpty()) {
            double numberOfTasksPerAvailablePerson = Math.floor((notDoneTasks.size()+0.0) / (availablePersons.size()+0.0));

            notDoneTasks = getNotDoneTasks(forced);
            distributeTasks(availablePersons, notDoneTasks, numberOfTasksPerAvailablePerson);

            Iterator<Person> notAvailablePersons = getAvailablePersons(false).iterator();
            while (notAvailablePersons.hasNext()){
                Person notAvailablePerson = notAvailablePersons.next();
                List<Task> tasksForDist = findTasksWithThisPerson(notAvailablePerson);
                distributeTasks(availablePersons, tasksForDist, numberOfTasksPerAvailablePerson, true);
            }

            changed = true;
        }

        fixAllPersonsTasksCount();

        return changed;
    }

    private void fixAllPersonsTasksCount() {
        Iterator<Person> persons = personsDB.find().iterator();
        while(persons.hasNext()){
            Person person = persons.next();
            fixAPersonsTasksCount(person);
        }
    }

    private List<Person> getAvailablePersons() {
        return getAvailablePersons(true);
    }

    private List<Person> getAvailablePersons(boolean available) {
        List<Person> availablePersons = new ArrayList<>();
        Bson findAvailable = Filters.eq("availability", available);
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
        Task maxPersonTask = null;
        int maxTasksCount = 0;

        Bson findDone = Filters.eq("done", false);
        try (MongoCursor<Task> taskCursor = tasksDB.find(findDone).iterator()) {
            while (taskCursor.hasNext()) {
                Task currentTask = taskCursor.next();
                if (forced || (currentTask.getPersonAssigned() == null)) {
                    notDoneTasks.add(currentTask);
                }else{
                    Iterator<Person> iterator = getRealPersonFromTask(currentTask);
                    if (!iterator.hasNext()){
                        notDoneTasks.add(currentTask);
                    }else{
                        Person person = iterator.next();
                        if (maxTasksCount < person.getTasksAssignedNumber()) {
                            maxTasksCount = person.getTasksAssignedNumber();
                            maxPersonTask = currentTask;
                        }
                    }
                }
                logTask(currentTask);
            }
            if (0 < maxTasksCount) {
                notDoneTasks.add(maxPersonTask);
            }
        } catch (MongoException me) {
            handleMongoException(me);
        }
        return notDoneTasks;
    }

    private Iterator<Person> getRealPersonFromTask(Task currentTask) {
        Person personForCheck = currentTask.getPersonAssigned();
        Bson personExists = Filters.eq("name",personForCheck.getName());
        FindIterable<Person> existingPersons = personsDB.find(personExists);
        return existingPersons.iterator();
    }

    private void distributeTasks(List<Person> availablePersons, List<Task> tasks, double numberOfTasksPerAvailablePerson) {
        distributeTasks( availablePersons, tasks,
                numberOfTasksPerAvailablePerson, false);
    }

    private void distributeTasks(List<Person> availablePersons, List<Task> tasks,
                                 double numberOfTasksPerAvailablePerson, boolean forced) {
        for (Task task : tasks) {
            findAPersonToAssignATaskTo(availablePersons, numberOfTasksPerAvailablePerson, task, forced);
        }
    }

    private boolean findAPersonToAssignATaskTo(List<Person> availablePersons, double numberOfTasksPerAvailablePerson, Task task, boolean forced) {
        boolean foundMin = false;
        if (!availablePersons.isEmpty()) {
            Person minTasksPerson = availablePersons.get(0);
            int minTasksNumber=minTasksPerson.getTasksAssignedNumber();

            for (Person person : availablePersons) {
                int taskAssignNumber = person.getTasksAssignedNumber();
                if ((taskAssignNumber <= minTasksNumber) && (forced || (taskAssignNumber < numberOfTasksPerAvailablePerson))) {
                    foundMin = true;
                    minTasksPerson = person;
                    minTasksNumber=taskAssignNumber;
                }
            }
            if (foundMin) {
                assignTaskToPerson(task, minTasksPerson);
            }
        }
        return foundMin;
    }

    private void assignTaskToPerson(Task task, Person newPerson) {
        Iterator<Person> oldPersonIterator = getRealPersonFromTask(task);
        task.setPersonAssigned(newPerson);
        onlyUpdateTask(task.getName(),task);

        if (oldPersonIterator.hasNext()){
            fixAPersonsTasksCount(oldPersonIterator.next());
        }
        fixAPersonsTasksCount(newPerson);
    }

    private void fixAPersonsTasksCount(Person person) {
        List<Task> tasks = findTasksWithThisPerson(person);
        person.setTasksAssignedNumber(tasks.size());
        onlyUpdatePerson(person.getName(),person);
    }

    private List<Task> findTasksWithThisPerson(Person person) {
        List<Task> tasks = new ArrayList<>();

        Bson filterPersonExists = Filters.exists("personAssigned");
        FindIterable<Task> tasksWithPersons = tasksDB.find(filterPersonExists);

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
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any tasks in MongoDB due to an error: ", me);
            }
        }

        return tasks;
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
            getLogger().log(Level.SEVERE, "MongoDB operation failed: ", me);
        }
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
