package task_person_utility.task_person_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class MainController {
    @Autowired
    private PersonServices personServices;
    @Autowired
    private TaskServices taskServices;
    @Autowired
    private AssignTasksServices assignTasksServices;

    // Person API endpoints
    @GetMapping("/persons")
    public List<Person> getAllPersons() {
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

    // Task API endpoints
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

    // Additional endpoints
    @GetMapping("/tasks/assign")
    public boolean assignTasks() {
        return assignTasksServices.assignTasks();
    }
}
