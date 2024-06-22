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
public class TaskServices  {

    // private final DBUtil dbutil;
    private final MongoDatabase personTaskDataBase;
    private final MongoCollection<Task> tasksDB;
    private final AssignTasksServices assignTasksServices;
    private final Logger logger;

    @Autowired
    public TaskServices(MongoDatabase mongoDatabase, AssignTasksServices assignTasksServices){
        // this.dbutil=dbutil;
        personTaskDataBase=mongoDatabase;// this.dbutil.getPersonTaskDataBase();
        this.assignTasksServices=assignTasksServices;
        tasksDB = personTaskDataBase.getCollection("tasks", Task.class);
        logger = Logger.getLogger(TaskServices.class.getName());
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
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, String.format("%s is in DB%n",
                            currentTask.getName()));
                }
            }
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any tasks in MongoDB due to an error: ", me);
            }
            System.exit(1);
        }

        
        return tasks;
    }

    public Task getTask(String name) {

        Bson findName = Filters.eq("name", name);
        Task findNameTask = null;
        try {
            findNameTask = tasksDB.find(findName).first();
            if (findNameTask == null) {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, "Unable to find any Task named: {0}", name);
                }
                System.exit(1);
            }
            
            return findNameTask;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any tasks in MongoDB due to an error: ", me);
            }
            System.exit(1);
        }

        return findNameTask;
    }

    public Task updateTask(String name, Task task) {

        Task taskToUpdate = getTask( name);

        Bson findName = Filters.eq("name", name);

        Bson updateFilter = Updates.set("name", task.getName());

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);

        try {
            Task updatedDocument = tasksDB.findOneAndUpdate(findName,
                    updateFilter, options);
            if (updatedDocument == null) {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, "Couldn't update the task. Did someone (or something) delete it?");
                }
            }else{
                getLogger().log(Level.INFO, "\nUpdated the task to: {0}" , updatedDocument);
            }
            assignTasksServices.assignTasks();
            return updatedDocument; // Ensure return here
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to update any tasks in MongoDB due to an error: ", me);
            }
            return null; // Return null in case of exception
        }
    }


    public Task deleteTask(String name) {

        Task taskToDelete = getTask( name);

        Bson deleteFilter = Filters.eq("name", name);
        try {
            DeleteResult deleteResult = tasksDB
                    .deleteOne(deleteFilter);
            if (deleteResult.getDeletedCount() == 0) { // Check if no documents were deleted
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, "Couldn't delete the task. Did someone (or something) delete it?");
                }
                return null; // Ensure return here
            } else {
                getLogger().log(Level.INFO, "\nDeleted the task: {0}", name);
                assignTasksServices.assignTasks();
                return taskToDelete; // Ensure return here
            }
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to delete any tasks in MongoDB due to an error: ", me);
            }
            return null; // Return null in case of exception
        }
    }
}
