package model;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class DataModel {

    private final ObservableList<Event> events = FXCollections.observableArrayList(e -> new Observable[]{e.urlProperty(), e.nextDateProperty(), e.timeProperty(), e.recurringProperty()});
    private final Map<UUID, ScheduledFuture<?>> eventTasks = new HashMap<>();
    private final ObjectProperty<Event> currentEvent;
    private final StringProperty username;
    private final StringProperty password;

    public DataModel() {
        username = new SimpleStringProperty(null);
        password = new SimpleStringProperty(null);
        currentEvent = new SimpleObjectProperty<>(null);
    }

    public void addEvent(Event e) {
        events.add(e);
    }

    public void removeEvent(Event e) {
        events.remove(e);
    }

    public ObservableList<Event> getEvents() {
        return events;
    }

    /**
     * Associates a task with an Event, so that it can be cancelled if necessary.
     */
    public void setEventTask(UUID eventId, ScheduledFuture<?> task) {
        eventTasks.put(eventId, task);
        System.out.println("added task");
    }

    /**
     * Cancels the task associated with the specified event. Cannot cancel if the task is running.
     */
    public void cancelEventTask(UUID eventId) {
        if (eventTasks.containsKey(eventId)) {
            eventTasks.get(eventId).cancel(false);
            eventTasks.remove(eventId);
            System.out.println("cancelled task");
        }
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public ObjectProperty<Event> currentEventProperty() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent.set(currentEvent);
    }
}