package nemo.networking;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import nemo.networking.Devices.NetworkDevice;
import nemo.networking.Devices.NetworkService;
import nemo.networking.Devices.PC;
import nemo.networking.Devices.VM;
import nemo.networking.Devices.maper.Mapper_t;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


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

    private final String Pc_s = "pc";
    private final String Vm_s = "vm";
    private final String NetworkService_s = "ns";
    private final String NetworkDevice_s = "nd";
    private final Mapper_t center_m = new Mapper_t();



    @FXML
    private void initialize(){
        Topologia.setCenter_m(center_m);
    }

    @FXML
    private void top_new_action() {
        try {
            URL fxmlUrl = getClass().getResource("/nemo/networking/new_b.fxml");
            if (fxmlUrl == null) throw new RuntimeException("FXML not found!");

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Region root = fxmlLoader.load();

            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/nemo/networking/styles/style.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            Stage newStage = new Stage();
            newStage.setTitle("new");
            newStage.setScene(scene);
            newStage.setResizable(true); // ← pozwala na runtime resize

            if (menu != null && menu.getScene() != null && menu.getScene().getWindow() != null) {
                newStage.initOwner(menu.getScene().getWindow());
            }

            // ustaw minimalne wymiary, jeśli chcesz ograniczenia
            newStage.setMinWidth(root.getMinWidth());
            newStage.setMinHeight(root.getMinHeight());

            newStage.show();
            newStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void top_computer_action() {
            try {
                URL fxmlUrl = getClass().getResource("/nemo/networking/machine.fxml");
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

                new_machineController ctrl = fxmlLoader.getController();
                newStage.setOnCloseRequest(e-> ctrl.shutdownExecutor());

                // pokaż i wycentruj
                newStage.show();
                newStage.sizeToScene();
                newStage.centerOnScreen();

            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    @FXML
    private void top_network_device_action() {
            try {
                URL fxmlUrl = getClass().getResource("/nemo/networking/machine.fxml");
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

                new_machineController ctrl = fxmlLoader.getController();
                newStage.setOnCloseRequest(e-> ctrl.shutdownExecutor());

                // pokaż i wycentruj
                newStage.show();
                newStage.sizeToScene();
                newStage.centerOnScreen();

            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    @FXML
    private void top_network_service_action() {
        try {
            URL fxmlUrl = getClass().getResource("/nemo/networking/machine.fxml");
            if (fxmlUrl == null) throw new RuntimeException("FXML not found!");

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            fxmlLoader.setController(new new_machineController(Topologia.NetworkService_t));

            Region root = fxmlLoader.load();

            root.applyCss();
            root.layout();

            double prefW = root.prefWidth(-1);
            double prefH = root.prefHeight(-1);


            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/nemo/networking/styles/style.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            Stage newStage = new Stage();
            newStage.setTitle("Network Service");
            newStage.setScene(scene);


            if (menu != null && menu.getScene() != null && menu.getScene().getWindow() != null) {
                newStage.initOwner(menu.getScene().getWindow());
            }


            double decoW = 16;
            double decoH = 39;


            newStage.setWidth(Math.max(prefW + decoW, 300)); // minimalna szerokość 300
            newStage.setHeight(Math.max(prefH + decoH, 200)); // minimalna wysokość 200

            new_machineController ctrl = fxmlLoader.getController();
            newStage.setOnCloseRequest(e-> ctrl.shutdownExecutor());

            // pokaż i wycentruj
            newStage.show();
            newStage.sizeToScene();
            newStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void top_vm_action() {
        try {
            URL fxmlUrl = getClass().getResource("/nemo/networking/machine.fxml");
            if (fxmlUrl == null) throw new RuntimeException("FXML not found!");

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            fxmlLoader.setController(new new_machineController(Topologia.VM_t));

            Region root = fxmlLoader.load();

            root.applyCss();
            root.layout();

            double prefW = root.prefWidth(-1);
            double prefH = root.prefHeight(-1);


            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/nemo/networking/styles/style.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            Stage newStage = new Stage();
            newStage.setTitle("VMs");
            newStage.setScene(scene);


            if (menu != null && menu.getScene() != null && menu.getScene().getWindow() != null) {
                newStage.initOwner(menu.getScene().getWindow());
            }


            double decoW = 16;
            double decoH = 39;


            newStage.setWidth(Math.max(prefW + decoW, 300)); // minimalna szerokość 300
            newStage.setHeight(Math.max(prefH + decoH, 200)); // minimalna wysokość 200

            new_machineController ctrl = fxmlLoader.getController();
            newStage.setOnCloseRequest(e-> ctrl.shutdownExecutor());

            // pokaż i wycentruj
            newStage.show();
            newStage.sizeToScene();
            newStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void top_server_action(){
        try {
            URL fxmlUrl = getClass().getResource("/nemo/networking/server.fxml");
            if (fxmlUrl == null) throw new RuntimeException("FXML not found!");

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);

            Region root = fxmlLoader.load();
            root.applyCss();
            root.layout();

            double prefW = root.prefWidth(-1);
            double prefH = root.prefHeight(-1);


            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/nemo/networking/styles/style.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            Stage newStage = new Stage();
            newStage.setTitle("Server");
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
    }

}
