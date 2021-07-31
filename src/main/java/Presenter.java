import com.dustinredmond.fxtrayicon.FXTrayIcon;
import com.jfoenix.controls.*;
import com.jfoenix.effects.JFXDepthManager;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Event;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Arrays;

public class Presenter {

    Stage stage;
    Scene scene;
    AnchorPane contentRoot;
    StackPane root;
    BorderPane layoutRoot;
    JFXDialog credentialsDialog;
    FXTrayIcon trayIcon;
    Controller controller;

    public Presenter(Stage stage, Scene sc, StackPane root, Controller controller) {
        this.stage = stage;
        this.scene = sc;
        this.root = root;
        this.contentRoot = new AnchorPane();
        this.layoutRoot = new BorderPane();
        this.controller = controller;

        // dependent on controller
        this.credentialsDialog = getCredentialsDialog();
        this.trayIcon = getTrayIcon();

        setup();
    }

    private void setup() {
        HBox header = new HBox();
        Text headerText = new Text("UofT S&R Registrant");

        header.getChildren().add(headerText);
        header.getStyleClass().add("header");
        headerText.getStyleClass().add("header-text");

        root.getChildren().add(contentRoot);
        contentRoot.getChildren().add(layoutRoot);
        root.getChildren().add(credentialsDialog);
        layoutRoot.setTop(getHeader());
        getScreenContent();
    }

    private HBox getHeader() {
        HBox header = new HBox();
        Text headerText = new Text("UofT S&R Registrant");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        JFXButton credentialsBtn = new JFXButton();

        credentialsBtn.setGraphic(new FontIcon());
        credentialsBtn.setOnAction(e -> credentialsDialog.show());

        header.getChildren().addAll(Arrays.asList(headerText, spacer, credentialsBtn));
        header.getStyleClass().add("header");
        headerText.getStyleClass().add("header-text");
        credentialsBtn.getStyleClass().add("circular-button");
        credentialsBtn.setId("button-settings");
        return header;
    }

    private void getScreenContent() {
        StackPane tableViewBacking = new StackPane();
        StackPane tableViewContainer = new StackPane();

        StackPane sidebarBacking = new StackPane();
        GridPane sidebarContainer = new GridPane();

        TableView<Event> tv = new TableView<>();
        TableColumn<Event, String> nameColumn = new TableColumn<>("Event");
        TableColumn<Event, String> timeColumn = new TableColumn<>("Next signup date/time");
        Text tvPlaceholder = new Text("No signups scheduled.");

        JFXTextField nameInput = new JFXTextField();
        JFXCheckBox recurringBox = new JFXCheckBox("Recurring (weekly)");
        JFXDatePicker datePicker = new JFXDatePicker();
        JFXTimePicker timePicker = new JFXTimePicker();
        JFXTextArea urlInput = new JFXTextArea();
        JFXButton addBtn = new JFXButton();

        tv.setPlaceholder(tvPlaceholder);
        nameInput.setPromptText("Label");
        datePicker.setPromptText("(Next) date");
        timePicker.setPromptText("Time (to nearest quarter-hour)");
        urlInput.setPromptText("URL to the event page");
        addBtn.setGraphic(new FontIcon());

        controller.setupTableView(tv, nameColumn, timeColumn);
        controller.setupSidebar(nameInput, recurringBox, datePicker, timePicker, urlInput);
        addBtn.setOnAction(e -> controller.addEvent());

        sidebarBacking.getChildren().add(sidebarContainer);
        tableViewBacking.getChildren().add(tableViewContainer);
        tableViewContainer.getChildren().add(tv);
        sidebarContainer.add(nameInput, 0, 0);
        sidebarContainer.add(recurringBox, 0, 1);
        sidebarContainer.add(datePicker, 0, 2);
        sidebarContainer.add(timePicker, 0, 3);
        sidebarContainer.add(urlInput, 0, 4);

        tvPlaceholder.getStyleClass().add("text-normal");
        tableViewBacking.getStyleClass().add("pane-background");
        tableViewContainer.getStyleClass().add("card-backing");
        sidebarBacking.getStyleClass().add("pane-background-no-left");
        sidebarContainer.getStyleClass().addAll("card-backing", "grid-sidebar");
        addBtn.getStyleClass().add("floating-button");
        addBtn.setId("button-add");

        JFXDepthManager.setDepth(tableViewContainer, 1);
        JFXDepthManager.setDepth(sidebarContainer, 1);

        sidebarContainer.prefWidthProperty().bind(layoutRoot.widthProperty().multiply(0.28));
        datePicker.prefWidthProperty().bind(sidebarContainer.widthProperty());
        timePicker.prefWidthProperty().bind(sidebarContainer.widthProperty());
        nameColumn.prefWidthProperty().bind(tv.widthProperty().multiply(0.6));
        timeColumn.prefWidthProperty().bind(tv.widthProperty().multiply(0.4));

        contentRoot.getChildren().add(addBtn);
        setMaxAnchor(layoutRoot);
        setFabAnchor(addBtn);

        layoutRoot.setCenter(tableViewBacking);
        layoutRoot.setRight(sidebarBacking);
    }

    private JFXDialog getCredentialsDialog() {
        JFXDialogLayout layout = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog(root, layout, JFXDialog.DialogTransition.CENTER);

        StackPane content = new StackPane();
        JFXButton closeBtn = new JFXButton("CLOSE");
        Text header = new Text("Credentials");
        JFXTextField idInput = new JFXTextField();
        JFXPasswordField pwdInput = new JFXPasswordField();
        VBox inputContainer = new VBox();

        idInput.setPromptText("UTORid");
        pwdInput.setPromptText("Password");
        inputContainer.getChildren().addAll(Arrays.asList(idInput, pwdInput));

        closeBtn.setOnAction(e -> dialog.close());
        controller.setupDialog(idInput, pwdInput);

        content.getChildren().add(inputContainer);

        header.getStyleClass().add("dialog-header");
        closeBtn.getStyleClass().add("raised-button");
        inputContainer.getStyleClass().add("dialog-input-container");
        layout.getStyleClass().add("dialog-layout");

        layout.setHeading(header);
        layout.setBody(content);
        layout.setActions(closeBtn);

        layout.prefWidthProperty().bind(layoutRoot.widthProperty().multiply(0.6));
        layout.prefHeightProperty().bind(layoutRoot.heightProperty().multiply(0.6));
        idInput.maxWidthProperty().bind(content.widthProperty().multiply(0.6));
        pwdInput.maxWidthProperty().bind(content.widthProperty().multiply(0.6));

        return dialog;
    }

    private FXTrayIcon getTrayIcon() {
        FXTrayIcon trayIcon = new FXTrayIcon(stage, getClass().getResource("icon.png"));
        trayIcon.setTrayIconTooltip("UofT S&R Registrant");

        MenuItem close = new MenuItem("Exit");
        MenuItem show = new MenuItem("Show");

        trayIcon.addMenuItem(show);
        trayIcon.addMenuItem(close);

        controller.setUpTrayIcon(stage, trayIcon, close, show);

        return trayIcon;
    }

    private void setMaxAnchor(Node node) {
        AnchorPane.setRightAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
    }

    private void setFabAnchor(Node node) {
        AnchorPane.setRightAnchor(node, 54.0);
        AnchorPane.setBottomAnchor(node, 54.0);
    }
}
