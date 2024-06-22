package task_person_utility.task_person_server;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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
public class TaskServices {

    private final MongoCollection<Task> tasksDB;
    private final AssignTasksServices assignTasksServices;
    private final Logger logger;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    public TaskServices(MongoDatabase mongoDatabase, AssignTasksServices assignTasksServices) {
        this.tasksDB = mongoDatabase.getCollection("tasks", Task.class);
        this.assignTasksServices = assignTasksServices;
        this.logger = Logger.getLogger(TaskServices.class.getName());
    }

    private Logger getLogger() {
        return logger;
    }

    public Task createTask(Task task) {
        try {
            InsertOneResult result = tasksDB.insertOne(task);
            if (getLogger().isLoggable(Level.INFO)) {
                getLogger().log(Level.INFO, String.format("Inserted document with ID: %s", result.getInsertedId()));
            }
            assignTasksServices.assignTasks();
            return task;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to insert Task into MongoDB due to an error: ", me);
            }
            return null;
        }
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        try (MongoCursor<Task> cursor = tasksDB.find().iterator()) {
            while (cursor.hasNext()) {
                Task currentTask = cursor.next();
                tasks.add(currentTask);
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

    public Task getTask(String id) {
        Bson filter = Filters.eq("_id", id);
        try {
            Task task = tasksDB.find(filter).first();
            if (task == null && getLogger().isLoggable(Level.INFO)) {
                getLogger().log(Level.INFO, "Unable to find any Task with ID: {0}", id);
            }
            return task;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any tasks in MongoDB due to an error: ", me);
            }
            return null;
        }
    }

    public Task updateTask(String id, Task task) {
        Bson filter = Filters.eq("_id", id);
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
                getLogger().log(Level.INFO, String.format("Updated document with ID: %s", id));
            }
            assignTasksServices.assignTasks();
            return updatedTask;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to update Task in MongoDB due to an error: ", me);
            }
            return null;
        }
    }

    public Task deleteTask(String id) {
        Task deletedTask = getTask(id);
        Bson filter = Filters.eq("_id", id);
        try {
            DeleteResult result = tasksDB.deleteOne(filter);
            if (result.getDeletedCount() > 0 && getLogger().isLoggable(Level.INFO)) {
                getLogger().log(Level.INFO, String.format("Deleted document with ID: %s", id));
                assignTasksServices.assignTasks();
                return deletedTask;
            } else {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, String.format("No task found with ID: %s", id));
                }
                return null;
            }
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to delete Task in MongoDB due to an error: ", me);
            }
            return null;
        }
    }
}
