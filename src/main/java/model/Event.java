package model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

public class Event {

    private final StringProperty name;
    private final StringProperty url;
    private final ObjectProperty<LocalDate> nextDate;
    private final ObjectProperty<LocalTime> time;
    private final BooleanProperty recurring;
    private final BooleanProperty nextSignupExecuted; // TODO: convert this into a LocalDateTime prop. + add last signed up URL prop. so we can determine whether a re-signup is necessary
    private final String id;

    public Event() {
        this.name = new SimpleStringProperty(null);
        this.url = new SimpleStringProperty(null);
        this.nextDate = new SimpleObjectProperty<>(LocalDate.now().plusDays(3));
        this.time = new SimpleObjectProperty<>(LocalTime.now());
        this.recurring = new SimpleBooleanProperty(false);
        this.nextSignupExecuted = new SimpleBooleanProperty(false);
        this.id = UUID.randomUUID().toString().replace("-", "");
    }

    public Event(String name, String url, LocalDate nextDate, LocalTime time, boolean recurring, boolean nextSignupExecuted) {
        this.name = new SimpleStringProperty(name);
        this.url = new SimpleStringProperty(url);
        this.nextDate = new SimpleObjectProperty<>(nextDate);
        this.time = new SimpleObjectProperty<>(time);
        this.recurring = new SimpleBooleanProperty(recurring);
        this.nextSignupExecuted = new SimpleBooleanProperty(nextSignupExecuted);
        this.id = UUID.randomUUID().toString().replace("-", "");
    }

    public Event(String name, String url, LocalDate nextDate, LocalTime time, boolean recurring, boolean nextSignupExecuted, String id) {
        this.name = new SimpleStringProperty(name);
        this.url = new SimpleStringProperty(url);
        this.nextDate = new SimpleObjectProperty<>(nextDate);
        this.time = new SimpleObjectProperty<>(time);
        this.recurring = new SimpleBooleanProperty(recurring);
        this.nextSignupExecuted = new SimpleBooleanProperty(nextSignupExecuted);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public LocalDate getNextDate() {
        return nextDate.get();
    }

    public void setNextDate(LocalDate nextDate) {
        this.nextDate.set(nextDate);
    }

    public ObjectProperty<LocalDate> nextDateProperty() {
        return nextDate;
    }

    public LocalTime getTime() {
        return time.get();
    }

    public void setTime(LocalTime time) {
        this.time.set(time);
    }

    public ObjectProperty<LocalTime> timeProperty() {
        return time;
    }

    public boolean isRecurring() {
        return recurring.get();
    }

    public BooleanProperty recurringProperty() {
        return recurring;
    }

    public boolean isNextSignupExecuted() {
        return nextSignupExecuted.get();
    }

    public void setNextSignupExecuted(boolean nextSignupExecuted) {
        this.nextSignupExecuted.set(nextSignupExecuted);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Event)) return false;
        return Objects.equals(((Event) o).getId(), id);
    }
}
