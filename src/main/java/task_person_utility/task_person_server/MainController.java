package task_person_utility.task_person_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // Person API endpoints
    @GetMapping("/persons")
    public List<Person> getAllPersons() {
        return personServices.getAllPersons();
    }

    @PostMapping("/persons")
    public Person createPerson(@RequestBody Person person) {
        return personServices.createPerson(person);
    }

    @PutMapping("/persons/{id}")
    public Person updatePerson(@PathVariable String id, @RequestBody Person person) {
        return personServices.updatePerson(id, person);
    }

    @DeleteMapping("/persons/{id}")
    public Person deletePerson(@PathVariable String id) {
        return personServices.deletePerson(id);
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

    @PutMapping("/tasks/{id}")
    public Task updateTask(@PathVariable String id, @RequestBody Task task) {
        return taskServices.updateTask(id, task);
    }

    @DeleteMapping("/tasks/{id}")
    public Task deleteTask(@PathVariable String id) {
        return taskServices.deleteTask(id);
    }

    // Additional endpoints
    @GetMapping("/tasks/assign")
    public boolean assignTasks() {
        return assignTasksServices.assignTasks();
    }
}
