package task_person_utility.task_person_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "task_person_utility.task_person_server")
public class TaskPersonServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskPersonServerApplication.class, args);
    }

}
