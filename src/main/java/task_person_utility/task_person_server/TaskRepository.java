package task_person_utility.task_person_server; // Add a package declaration

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
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

@Repository
interface TaskRepository extends MongoRepository<Task, String> {

    private static Logger getLogger() {
        return Logger.getLogger(TaskRepository.class.getName());
    }

    static MongoCollection<Task> tasksDB = DB_Util.getTasksDB();

    public default Task createTask(Task task) {

        try {
            InsertOneResult result = tasksDB.insertOne(task);
            if (getLogger().isLoggable(Level.INFO)) {
                getLogger().log(Level.INFO, String.format("Inserted document with ID: %s", result.getInsertedId()));
            }
            AssignTasksUtil.assignTasks();
            return task;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to insert Task into MongoDB due to an error: ", me);
            }
            return null;
        }
    }

    public default List<Task> getAllTasks() {
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

    public default Task getTask(String name) {

        // We can also find a single document. Let's find the first document
        // that has the string "potato" in the ingredients list. We
        // use the Filters.eq() method to search for any values in any
        // ingredients list that match the string "potato":

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

    public default Task updateTask(String name, Task task) {

        Task taskToUpdate = getTask( name);

        Bson findName = Filters.eq("name", name);
        /*      *** UPDATE A DOCUMENT ***
         *
         * You can update a single document or multiple documents in a single call.
         *
         * Here we update the PrepTimeInMinutes value on the document we
         * just found.
         */
        Bson updateFilter = Updates.set("name", task.getName());

        // The following FindOneAndUpdateOptions specify that we want it to return
        // the *updated* document to us. By default, we get the document as it was *before*
        // the update.
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);

        // The updatedDocument object is a task object that reflects the
        // changes we just made.
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
            AssignTasksUtil.assignTasks();
            return updatedDocument; // Ensure return here
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to update any tasks in MongoDB due to an error: ", me);
            }
            return null; // Return null in case of exception
        }
    }


    public default Task deleteTask(String name) {

        Task taskToDelete = getTask( name);

        /*      *** DELETE DOCUMENTS ***
         *
         *      As with other CRUD methods, you can delete a single document
         *      or all documents that match a specified filter. To delete all
         *      of the documents in a collection, pass an empty filter to
         *      the deleteMany() method. In this example, we'll delete 2 of
         *      the tasks.
         */
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
                AssignTasksUtil.assignTasks();
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
