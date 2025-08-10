package nemo.networking;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class NemoController {
    // Główne elementu głównego panelu
    @FXML private BorderPane menu;
    @FXML private HBox top_menu;
    @FXML private ScrollPane left_menu;
    @FXML private VBox lef_menu_buttons;
    @FXML private Pane center_menu;

    // Przyciski górne
    @FXML private Button top_files;
    @FXML private Button top_help;
    @FXML private Button top_computers;
    @FXML private Button top_vms;
    @FXML private Button top_server;
    @FXML private Button top_new;


}
