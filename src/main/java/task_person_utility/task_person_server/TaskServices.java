package task_person_utility.task_person_server;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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
    final AssignTasksServices assignTasksServices;
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
                getLogger().log(Level.INFO, String.format("Inserted document with id: %s", result.getInsertedId()));
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
        assignTasksServices.assignTasks();
        return tasks;
    }

    public Task getTask(String name) {
        Bson filter = Filters.eq("name", name);
        try {
            Task task = tasksDB.find(filter).first();
            if (task == null && getLogger().isLoggable(Level.INFO)) {
                getLogger().log(Level.INFO, "Unable to find any Task with name: {0}", name);
            }
            assignTasksServices.assignTasks();
            return task;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any tasks in MongoDB due to an error: ", me);
            }
            return null;
        }
    }

    public Task updateTask(String name, Task task) {
        Task updateTask = assignTasksServices.onlyUpdateTask(name,task);
        assignTasksServices.assignTasks();
        return updateTask;
    }

    public Task deleteTask(String name) {
        Task deletedTask = getTask(name);
        Bson filter = Filters.eq("name", name);
        try {
            DeleteResult result = tasksDB.deleteOne(filter);
            if (result.getDeletedCount() > 0 && getLogger().isLoggable(Level.INFO)) {
                getLogger().log(Level.INFO, String.format("Deleted document with name: %s", name));
                assignTasksServices.assignTasks();
                return deletedTask;
            } else {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, String.format("No task found with name: %s", name));
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
