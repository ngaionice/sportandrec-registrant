import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.DataModel;
import scheduler.Scheduler;

import java.util.Objects;

public class Main extends Application {

    Stage stage;
    Presenter presenter;
    Controller controller;
    Scheduler scheduler;
    DataModel model;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 1280, 720);

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/data-views.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/jfx-styles.css")).toExternalForm());

        model = new DataModel();
        scheduler = new Scheduler(model);
        controller = new Controller(scheduler, model);
        presenter = new Presenter(stage, scene, root, controller);

        controller.deserialize();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> controller.serialize()));
        this.stage.setOnCloseRequest(t -> stage.hide());

        Platform.setImplicitExit(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icon.png"))));
        stage.setTitle("Registrant");
        stage.setScene(scene);
        stage.show();
    }
}
