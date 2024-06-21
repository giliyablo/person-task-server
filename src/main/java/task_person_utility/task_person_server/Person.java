package task_person_utility.task_person_server;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "persons")
public class Person {
    private String name;
    private boolean availability;
    private int tasksAssignedNumber;

    public Person() {
        this.name = "Unknown";
        this.availability = true;
        this.tasksAssignedNumber = 0;
    }

    public Person(String name, boolean availability, int tasksAssignedNumber) {
        this.name = name;
        this.availability = availability;
        this.tasksAssignedNumber = tasksAssignedNumber;
    }

    public Person(String name) {
        this.name = name;
        this.availability = true;
        this.tasksAssignedNumber = 0;
    }


    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public int getTasksAssignedNumber() {
        return tasksAssignedNumber;
    }

    public void setTasksAssignedNumber(int tasksAssignedNumber) {
        this.tasksAssignedNumber = tasksAssignedNumber;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", availability=" + availability +
                ", tasksAssignedNumber=" + tasksAssignedNumber +
                '}';
    }
}
