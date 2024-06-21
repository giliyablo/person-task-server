package task_person_utility.task_person_server;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

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
    public ResponseEntity<List<Person>> getAllPersons() {
        return ResponseEntity.ok(personServices.getAllPersons());
    }

    @PostMapping("/persons")
    public ResponseEntity<Person> createPerson(@RequestBody Person person) {
        return ResponseEntity.ok(personServices.createPerson(person));
    }

    @PutMapping("/persons/{name}")
    public ResponseEntity<Person> updatePerson(@PathVariable String name, @RequestBody Person person) {
        return ResponseEntity.ok(personServices.updatePerson(name, person));
    }

    @DeleteMapping("/persons/{name}")
    public ResponseEntity<Void> deletePerson(@PathVariable String name) {
        personServices.deletePerson(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/persons/{name}")
    public ResponseEntity<Person> getPerson(@PathVariable String name) {
        return ResponseEntity.ok(personServices.getPerson(name));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskServices.getAllTasks());
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskServices.createTask(task));
    }

    @PutMapping("/tasks/{name}")
    public ResponseEntity<Task> updateTask(@PathVariable String name, @RequestBody Task task) {
        return ResponseEntity.ok(taskServices.updateTask(name, task));
    }

    @DeleteMapping("/tasks/{name}")
    public ResponseEntity<Void> deleteTask(@PathVariable String name) {
        taskServices.deleteTask(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tasks/{name}")
    public ResponseEntity<Task> getTask(@PathVariable String name) {
        return ResponseEntity.ok(taskServices.getTask(name));
    }

    @GetMapping("/tasks/assign")
    public ResponseEntity<Void> assignTasks() {
        assignTasksServices.assignTasks();
        return ResponseEntity.ok().build();
    }
}
