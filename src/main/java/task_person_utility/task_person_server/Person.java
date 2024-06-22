package task_person_utility.task_person_server;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "persons")
public class Person {
    @Id
    private ObjectId id;
    private String name;
    private boolean availability;
    private int tasksAssignedNumber;

    public Person(ObjectId id, String name, boolean availability, int tasksAssignedNumber) {
        this.id = id;
        this.name = name;
        this.availability = availability;
        this.tasksAssignedNumber = tasksAssignedNumber;
    }

    public Person(String name, boolean availability, int tasksAssignedNumber) {
        this.name = name;
        this.availability = availability;
        this.tasksAssignedNumber = tasksAssignedNumber;
    }

    public Person(String name) {
        this(name,true,0);
    }

    public Person() {
        this("Unknown");
    }

    // Getters and setters

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

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
                "id='" + id + '\'' +
                "name='" + name + '\'' +
                ", availability=" + availability +
                ", tasksAssignedNumber=" + tasksAssignedNumber +
                '}';
    }
}
