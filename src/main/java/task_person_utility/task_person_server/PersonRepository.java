package task_person_utility.task_person_server; // Add a package declaration

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
interface PersonRepository extends MongoRepository<Person, String> {

    private static Logger getLogger() {
        return Logger.getLogger(PersonRepository.class.getName());
    }

    static MongoCollection<Person> personsDB = DB_Util.getPersonsDB();

    public default Person createPerson(Person person) {

        try {
            InsertOneResult result = personsDB.insertOne(person);
            if (getLogger().isLoggable(Level.INFO)) {
                getLogger().log(Level.INFO, String.format("Inserted document with ID: %s", result.getInsertedId()));
            }
            AssignTasksUtil.assignTasks();
            return person;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to insert person into MongoDB due to an error: ", me);
            }
            return null;
        }
    }

    public default List<Person> getAllPersons() {
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

        return persons;
    }

    public default Person getPerson(String name) {

        // We can also find a single document. Let's find the first document
        // that has the string "potato" in the ingredients list. We
        // use the Filters.eq() method to search for any values in any
        // ingredients list that match the string "potato":

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
            return findNamePerson;
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to find any persons in MongoDB due to an error: ", me);
            }
            System.exit(1);
        }

        return findNamePerson;
    }

    public default Person updatePerson(String name, Person person) {

        Person personToUpdate = getPerson( name);

        Bson findName = Filters.eq("name", name);
        /*      *** UPDATE A DOCUMENT ***
         *
         * You can update a single document or multiple documents in a single call.
         *
         * Here we update the PrepTimeInMinutes value on the document we
         * just found.
         */
        Bson updateFilter = Updates.set("name", person.getName());

        // The following FindOneAndUpdateOptions specify that we want it to return
        // the *updated* document to us. By default, we get the document as it was *before*
        // the update.
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);

        // The updatedDocument object is a person object that reflects the
        // changes we just made.
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
            AssignTasksUtil.assignTasks();
            return updatedDocument; // Ensure return here
        } catch (MongoException me) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Unable to update any persons in MongoDB due to an error: ", me);
            }
            return null; // Return null in case of exception
        }
    }


    public default Person deletePerson(String name) {

        Person personToDelete = getPerson( name);

        /*      *** DELETE DOCUMENTS ***
         *
         *      As with other CRUD methods, you can delete a single document
         *      or all documents that match a specified filter. To delete all
         *      of the documents in a collection, pass an empty filter to
         *      the deleteMany() method. In this example, we'll delete 2 of
         *      the persons.
         */
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
                AssignTasksUtil.assignTasks();
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
