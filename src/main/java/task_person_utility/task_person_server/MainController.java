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
    private PersonRepository personRepository;
    @Autowired
    private TaskRepository taskService;

    @GetMapping("/persons")
    public ResponseEntity<List<Person>> getAllPersons() {
        return ResponseEntity.ok(personRepository.getAllPersons());
    }

    @PostMapping("/persons")
    public ResponseEntity<Person> createPerson(@RequestBody Person person) {
        return ResponseEntity.ok(personRepository.createPerson(person));
    }

    @PutMapping("/persons/{name}")
    public ResponseEntity<Person> updatePerson(@PathVariable String name, @RequestBody Person person) {
        return ResponseEntity.ok(personRepository.updatePerson(name, person));
    }

    @DeleteMapping("/persons/{name}")
    public ResponseEntity<Void> deletePerson(@PathVariable String name) {
        personRepository.deletePerson(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/persons/{name}")
    public ResponseEntity<Person> getPerson(@PathVariable String name) {
        return ResponseEntity.ok(personRepository.getPerson(name));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @PutMapping("/tasks/{name}")
    public ResponseEntity<Task> updateTask(@PathVariable String name, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(name, task));
    }

    @DeleteMapping("/tasks/{name}")
    public ResponseEntity<Void> deleteTask(@PathVariable String name) {
        taskService.deleteTask(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tasks/{name}")
    public ResponseEntity<Task> getTask(@PathVariable String name) {
        return ResponseEntity.ok(taskService.getTask(name));
    }

    @GetMapping("/tasks/assign")
    public ResponseEntity<Void> assignTasks() {
        AssignTasksUtil.assignTasks();
        return ResponseEntity.ok().build();
    }
}
