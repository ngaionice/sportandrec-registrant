import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.DataModel;
import scheduler.Scheduler;

import java.util.Objects;

public class Main extends Application {

    Stage primaryStage;
    Presenter presenter;
    Controller controller;
    Scheduler scheduler;
    DataModel model;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 1280, 720);
        model = new DataModel();
        scheduler = new Scheduler(model);
        controller = new Controller(scheduler, model);

        controller.deserialize();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> controller.serialize()));

        presenter = new Presenter(primaryStage, scene, root, controller);

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/data-views.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/jfx-styles.css")).toExternalForm());

        FXTrayIcon trayIcon = new FXTrayIcon(primaryStage, getClass().getResource("icon.png"));
        trayIcon.show();

        trayIcon.setTrayIconTooltip("UofT S&R Registrant");
        MenuItem close = new MenuItem("Exit");
        MenuItem show = new MenuItem("Show");

        close.setOnAction(e -> System.exit(0));
        show.setOnAction(e -> primaryStage.show());
        trayIcon.addMenuItem(show);
        trayIcon.addMenuItem(close);

        Platform.setImplicitExit(false);
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
