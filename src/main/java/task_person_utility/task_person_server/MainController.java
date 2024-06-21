package task_person_utility.task_person_server;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@RestController
@RequestMapping("/api")
public class MainController {


    @Autowired
    private PersonServices personServices;
    @Autowired
    private TaskServices taskServices;
    @Autowired
    private AssignTasksServices assignTasksServices;


    @GetMapping("/persons")
    public List<task_person_utility.task_person_server.Person> getAllPersons() {
        return personServices.getAllPersons();
    }

    @PostMapping("/persons")
    public Person createPerson(@RequestBody Person person) {
        return personServices.createPerson(person);
    }

    @PutMapping("/persons/{name}")
    public Person updatePerson(@PathVariable String name, @RequestBody Person person) {
        return personServices.updatePerson(name, person);
    }

    @DeleteMapping("/persons/{name}")
    public Person deletePerson(@PathVariable String name) {
        return personServices.deletePerson(name);
    }

    @GetMapping("/persons/{name}")
    public Person getPerson(@PathVariable String name) {
        return personServices.getPerson(name);
    }

    @GetMapping("/tasks")
    public List<Task> getAllTasks() {
        return taskServices.getAllTasks();
    }

    @PostMapping("/tasks")
    public Task createTask(@RequestBody Task task) {
        return taskServices.createTask(task);
    }

    @PutMapping("/tasks/{name}")
    public Task updateTask(@PathVariable String name, @RequestBody Task task) {
        return taskServices.updateTask(name, task);
    }

    @DeleteMapping("/tasks/{name}")
    public Task deleteTask(@PathVariable String name) {
        return taskServices.deleteTask(name);
    }

    @GetMapping("/tasks/{name}")
    public Task getTask(@PathVariable String name) {
        return taskServices.getTask(name);
    }

    @GetMapping("/tasks/assign")
    public boolean assignTasks() {
        return assignTasksServices.assignTasks();
    }
}
