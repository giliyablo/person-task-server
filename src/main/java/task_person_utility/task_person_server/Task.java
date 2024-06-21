package task_person_utility.task_person_server;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tasks")
public class Task {
    private String name;
    private String description;
    private java.time.LocalDate dateOfCreation;
    private boolean done;
    @org.springframework.data.mongodb.core.mapping.DBRef
    private Person personAssigned;

    public Task(String name, String description, java.time.LocalDate dateOfCreation, boolean done, Person personAssigned) {
        this.name = name;
        this.description = description;
        this.dateOfCreation = dateOfCreation;
        this.done = done;
        this.personAssigned = personAssigned;
    }

    public Task(String name, String description, Person personAssigned) {
        this.name = name;
        this.description = description;
        this.dateOfCreation = java.time.LocalDate.now();
        this.done = false;
        this.personAssigned = personAssigned;
    }

    public Task(String name, String description) {
        this(name, description, null);
    }

    public Task(String name) {
        this(name, null, null);
    }

    public Task() {
        this.name = "Unnamed Task";
        this.description = "No description provided.";
        this.dateOfCreation = java.time.LocalDate.now();
        this.done = false;
        this.personAssigned = null;
    }


    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public java.time.LocalDate getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(java.time.LocalDate dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Person getPersonAssigned() {
        return personAssigned;
    }

    public void setPersonAssigned(Person personAssigned) {
        this.personAssigned = personAssigned;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", dateOfCreation=" + dateOfCreation +
                ", done=" + done +
                ", personAssigned=" + (personAssigned != null ? personAssigned.getName() : "none") +
                '}';
    }
}