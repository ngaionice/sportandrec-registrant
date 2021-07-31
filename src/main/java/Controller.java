import com.dustinredmond.fxtrayicon.FXTrayIcon;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.DataModel;
import model.Event;
import model.Serializer;
import scheduler.Scheduler;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Controller {

    DataModel m;
    Scheduler s;

    public Controller(Scheduler s, DataModel m) {
        this.m = m;
        this.s = s;
        setupEventChangeListener();
    }

    void setupTableView(TableView<Event> tv, TableColumn<Event, String> nameColumn, TableColumn<Event, String> timeColumn) {
        tv.setItems(m.getEvents());
        tv.setRowFactory(view -> {
            TableRow<Event> row = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem();

            deleteItem.setText("Delete");
            contextMenu.getStyleClass().add("context-menu");
            deleteItem.getStyleClass().add("context-item");

            deleteItem.setOnAction(a -> {
                tv.getSelectionModel().clearSelection();
                for (Event e : m.getEvents()) {
                    if (e.getId().equals(row.getItem().getId())) {
                        m.removeEvent(e);
                        break;
                    }
                }
            });

            contextMenu.getItems().addAll(deleteItem);
            row.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(row, e.getScreenX(), e.getScreenY());
                }
            });
            return row;
        });
        tv.getColumns().addAll(Arrays.asList(nameColumn, timeColumn));
        tv.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> m.setCurrentEvent(newVal));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");

        nameColumn.setCellValueFactory(cd -> cd.getValue().nameProperty());
        timeColumn.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().nextDateProperty().get().toString() + " " + cd.getValue().timeProperty().get().format(dtf), cd.getValue().nextDateProperty(), cd.getValue().timeProperty()));
    }

    void setupSidebar(TextField nameInput, CheckBox recurringBox, JFXDatePicker datePicker, JFXTimePicker timePicker, TextArea urlInput) {
        timePicker.valueProperty().addListener(((observable, oldValue, newValue) -> { // intercept changes not adhering to format
            if (newValue != null && newValue.getMinute() % 15 != 0) timePicker.setValue(roundToClosestQuarter(newValue));
        }));
        m.currentEventProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal != null) {
                nameInput.textProperty().unbindBidirectional(oldVal.nameProperty());
                recurringBox.selectedProperty().unbindBidirectional(oldVal.recurringProperty());
                datePicker.valueProperty().unbindBidirectional(oldVal.nextDateProperty());
                timePicker.valueProperty().unbindBidirectional(oldVal.timeProperty());
                urlInput.textProperty().unbindBidirectional(oldVal.urlProperty());
            }
            if (newVal == null) {
                nameInput.setText("");
                recurringBox.setSelected(false);
                datePicker.setValue(null);
                timePicker.setValue(null);
                urlInput.setText("");
            } else {
                newVal.setTime(roundToClosestQuarter(newVal.getTime()));
                nameInput.textProperty().bindBidirectional(newVal.nameProperty());
                recurringBox.selectedProperty().bindBidirectional(newVal.recurringProperty());
                datePicker.valueProperty().bindBidirectional(newVal.nextDateProperty());
                timePicker.valueProperty().bindBidirectional(newVal.timeProperty());
                urlInput.textProperty().bindBidirectional(newVal.urlProperty());
            }
        });
    }

    void setupDialog(TextField idInput, PasswordField pwdInput) {
        idInput.textProperty().bindBidirectional(m.usernameProperty());
        pwdInput.textProperty().bindBidirectional(m.passwordProperty());
    }

    void setUpTrayIcon(Stage stage, FXTrayIcon trayIcon, MenuItem close, MenuItem show) {
        close.setOnAction(e -> System.exit(0));
        show.setOnAction(e -> stage.show());
        DataModel.latestMessageProperty().addListener(((observable, oldValue, newValue) -> trayIcon.showMessage(newValue)));
        trayIcon.show();
    }

    void addEvent() {
        m.getEvents().add(new Event());
        m.setCurrentEvent(m.getEvents().get(m.getEvents().size() - 1));
    }

    private void setupEventChangeListener() {
        m.getEvents().addListener((ListChangeListener.Change<? extends Event> c) -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        rescheduleEvent(m.getEvents().get(i));
                    }
                } else if (c.wasAdded()) {
                    for (Event e : c.getAddedSubList()) {
                        if (Scheduler.getMinutesFromHappeningTime(LocalDateTime.of(e.getNextDate(), e.getTime())) > 0) {
                            s.scheduleSignupTask(e);
                        } else if (e.isRecurring() && e.isNextSignupExecuted()) {
                            s.scheduleRenewalTask(e);
                        }
                    }
                } else if (c.wasRemoved()) {
                    for (Event e : c.getRemoved()) {
                        unscheduleEvent(e);
                    }
                }
            }
        });
    }

    private void sendTestEvent() {
        m.addEvent(new Event("a", "url", LocalDate.now(), LocalTime.now(), true, false));
        m.addEvent(new Event("b", "url", LocalDate.now(), LocalTime.now(), true, false));
    }

    /**
     * Re-schedules the signup of the input event using current credentials and event configuration.
     *
     * If the event in question has not been signed up for yet (determined by the nextSignupExecuted flag),
     * then a SignupTask will be scheduled; else if the event is a recurring event and has been signed up for already,
     * a RenewalTask will be scheduled. If the event has been signed up for, and is not a recurring event, nothing happens.
     */
    private void rescheduleEvent(Event e) {
        if (e.getTime().getMinute() % 15 != 0) return; // used to 'consume' the event before the time formatter kicks in
        unscheduleEvent(e);

        if (Scheduler.getMinutesFromHappeningTime(LocalDateTime.of(e.getNextDate(), e.getTime())) > 0) {
            m.setEventTask(e.getId(), s.scheduleSignupTask(e));
        } else if (e.isRecurring() && e.isNextSignupExecuted()) {
            m.setEventTask(e.getId(), s.scheduleRenewalTask(e));
        }
    }

    private void unscheduleEvent(Event e) {
        m.cancelEventTask(e.getId());
    }

    public void serialize() {
        String path = "data.json";
        Serializer se = new Serializer();
        Gson g = se.getSerializer();
        try {
            JsonWriter writer = g.newJsonWriter(new BufferedWriter(new FileWriter(path)));
            writer.beginObject();
            se.writeCredentials(writer, m.getUsername(), m.getPassword());
            se.writeEvents(writer, g, m.getEvents());
            writer.endObject();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deserialize() {
        String path = "data.json";
        if (!new File(path).exists()) return;
        Serializer se = new Serializer();
        Gson g = se.getSerializer();
        try {
            JsonElement tree = JsonParser.parseReader(new FileReader(path));
            JsonObject allData = tree.getAsJsonObject();
            JsonObject credentials = allData.get("credentials").getAsJsonObject();
            m.setUsername(credentials.get("username").isJsonNull() ? null : credentials.get("username").getAsString());
            m.setPassword(credentials.get("password").isJsonNull() ? null : credentials.get("password").getAsString());
            JsonArray eventsArray = allData.get("events").getAsJsonArray();
            eventsArray.forEach(eo -> m.addEvent(g.fromJson(eo, Event.class)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LocalTime roundToClosestQuarter(LocalTime time) {
        int mod = time.getMinute() % 15;
        return time.plusMinutes(mod < 8 ? -mod : (15 - mod));
    }


}
