package nemo.networking;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.net.URL;


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
    @FXML private Button top_network_device;
    @FXML private Button top_network_service;




    @FXML
    private void top_computer_action() {
        if(app_state.isApp_stet_r()){
            app_state.setApp_stet_r(false);
            try {
                URL fxmlUrl = getClass().getResource("/nemo/networking/new_machine.fxml");
                if (fxmlUrl == null) throw new RuntimeException("FXML not found!");

                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                fxmlLoader.setController(new new_machineController(Topologia.PC_t));

                Region root = (Region) fxmlLoader.load();

                root.applyCss();
                root.layout();

                double prefW = root.prefWidth(-1) ;
                double prefH = root.prefHeight(-1);


                Scene scene = new Scene(root);
                URL cssUrl = getClass().getResource("/nemo/networking/styles/style.css");
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

                Stage newStage = new Stage();
                newStage.setTitle("Computer");
                newStage.setScene(scene);


                if (menu != null && menu.getScene() != null && menu.getScene().getWindow() != null) {
                    newStage.initOwner(menu.getScene().getWindow());
                }


                double decoW = 16;
                double decoH = 39;


                newStage.setWidth(Math.max(prefW + decoW, 300)); // minimalna szerokość 300
                newStage.setHeight(Math.max(prefH + decoH, 200)); // minimalna wysokość 200

                // pokaż i wycentruj
                newStage.show();
                newStage.sizeToScene();
                newStage.centerOnScreen();

            } catch (Exception e) {
                e.printStackTrace();
            }
            app_state.setApp_stet_r(true);
        }
    }

    @FXML
    private void top_network_device_action() {
        if(app_state.isApp_stet_r()){
            app_state.setApp_stet_r(false);
            try {
                URL fxmlUrl = getClass().getResource("/nemo/networking/new_machine.fxml");
                if (fxmlUrl == null) throw new RuntimeException("FXML not found!");

                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                fxmlLoader.setController(new new_machineController(Topologia.NetworkDevice_t));

                Region root = (Region) fxmlLoader.load();

                root.applyCss();
                root.layout();

                double prefW = root.prefWidth(-1) ;
                double prefH = root.prefHeight(-1);


                Scene scene = new Scene(root);
                URL cssUrl = getClass().getResource("/nemo/networking/styles/style.css");
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

                Stage newStage = new Stage();
                newStage.setTitle("Network Device");
                newStage.setScene(scene);


                if (menu != null && menu.getScene() != null && menu.getScene().getWindow() != null) {
                    newStage.initOwner(menu.getScene().getWindow());
                }


                double decoW = 16;
                double decoH = 39;


                newStage.setWidth(Math.max(prefW + decoW, 300)); // minimalna szerokość 300
                newStage.setHeight(Math.max(prefH + decoH, 200)); // minimalna wysokość 200

                // pokaż i wycentruj
                newStage.show();
                newStage.sizeToScene();
                newStage.centerOnScreen();

            } catch (Exception e) {
                e.printStackTrace();
            }
            app_state.setApp_stet_r(true);
        }
    }




}
