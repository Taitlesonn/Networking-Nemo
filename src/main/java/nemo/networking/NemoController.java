package nemo.networking;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import nemo.networking.Devices.maper.Mapper_t;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class NemoController {
    // Główne elementu głównego panelu
    @FXML private BorderPane menu;
    @FXML private HBox top_menu;
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

    public final Mapper_t center_m = new Mapper_t();

    private Timeline centerTimeline;

    public final Thread center_a;
    {
        center_a = new Thread(() -> {
            try {
                Thread.sleep(Long.MAX_VALUE); //
            } catch (InterruptedException ex) {
                // gdy ktoś przerwie center_a -> zatrzymaj Timeline bezpiecznie na wątku FX
                if (centerTimeline != null) {
                    Platform.runLater(() -> {
                        try {
                            centerTimeline.stop();
                        } catch (Exception ignored) {}
                    });
                }
            }
        }, "center_a-compat");
        center_a.setDaemon(true);
        center_a.start();
    }

    private record render_center(List<Integer> point_raw,
                                 List<Integer> point_cal,
                                 List<Double> pane_stat,
                                 AtomicReference<Double> step_x,
                                 AtomicReference<Double> step_y){ }

    @FXML
    private void initialize() {
        Topologia.setCenter_m(center_m);

        centerTimeline = new Timeline(new KeyFrame(Duration.millis(35), ev -> center_topologia()));
        centerTimeline.setCycleCount(Timeline.INDEFINITE);
        centerTimeline.play();
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
            newStage.setResizable(true);

            if (menu != null && menu.getScene() != null && menu.getScene().getWindow() != null) {
                newStage.initOwner(menu.getScene().getWindow());
            }

            newStage.setMinWidth(root.getMinWidth());
            newStage.setMinHeight(root.getMinHeight());

            newStage.show();
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


    private void center_topologia() {

        render_center renderer_stat = new render_center(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new AtomicReference<>(0.0),
                new AtomicReference<>(0.0));

        renderer_stat.point_raw().addAll(Arrays.asList(0, 0, 0, 0));
        renderer_stat.point_cal().addAll(Arrays.asList(0, 0));

        if (center_menu == null) return;
        double paneW = center_menu.getWidth();
        double paneH = center_menu.getHeight();
        if (paneW <= 0 || paneH <= 0) return;

        renderer_stat.pane_stat().clear();
        renderer_stat.pane_stat().add(paneW);
        renderer_stat.pane_stat().add(paneH);

        int x0, x1, y0, y1;
        try {
            x0 = center_m.get_pane_x0();
            x1 = center_m.get_pane_x1();
            y0 = center_m.get_pane_y0();
            y1 = center_m.get_pane_y1();
        } catch (IllegalStateException ex) {
            // brak punktów w mapperze -> nic nie rysujemy teraz
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        renderer_stat.point_raw().set(0, x0);
        renderer_stat.point_raw().set(1, x1);
        renderer_stat.point_raw().set(2, y0);
        renderer_stat.point_raw().set(3, y1);


        if (renderer_stat.point_raw().get(0) > 100)
            renderer_stat.point_raw().set(0, renderer_stat.point_raw().get(0) - 50);
        if (renderer_stat.point_raw().get(2) > 100)
            renderer_stat.point_raw().set(2, renderer_stat.point_raw().get(2) - 50);

        renderer_stat.point_cal().set(0, renderer_stat.point_raw().get(1) - renderer_stat.point_raw().get(0));
        renderer_stat.point_cal().set(1, renderer_stat.point_raw().get(3) - renderer_stat.point_raw().get(2));

        if (renderer_stat.point_cal().get(0) == 0 || renderer_stat.point_cal().get(1) == 0) {
            return;
        }

        // --- Skalowanie liczby linii względem ilości maszyn ---
        int deviceCount = center_m.getPointsCount(); // nowa metoda w Mapper_t
        // proste reguły skalowania:
        //  - minimum linii: 4
        //  - maksimum linii: 100 (żeby nie renderować za dużo)
        //  - docelowo: około 2 linii na maszynę
        int targetLines = Math.max(4, Math.min(100, deviceCount * 2));

        double width = renderer_stat.pane_stat().get(0);
        double height = renderer_stat.pane_stat().get(1);

        // Obliczamy kroki jako równy podział przestrzeni na targetLines
        double stepX = width / (double) targetLines;
        double stepY = height / (double) targetLines;

        // jeśli kroki są zbyt małe (np. mniejszy niż 8 px), zmniejszamy liczbę linii jeszcze bardziej
        final double MIN_PIXEL_STEP = 8.0;
        if (stepX < MIN_PIXEL_STEP) {
            int reduced = Math.max(4, (int) (width / MIN_PIXEL_STEP));
            stepX = width / (double) reduced;
        }
        if (stepY < MIN_PIXEL_STEP) {
            int reduced = Math.max(4, (int) (height / MIN_PIXEL_STEP));
            stepY = height / (double) reduced;
        }
        center_menu.getChildren().clear();

        renderer_stat.step_x().set(stepX);
        renderer_stat.step_y().set(stepY);
        
        double sx = renderer_stat.step_x().get();
        double sy = renderer_stat.step_y().get();

        // Linie OX (pionowe)
        for (double x = 0; x <= width; x += sx) {
            Line line = new Line(x, 0, x, height);
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(2);
            center_menu.getChildren().add(line);
        }
        // Linie OY (poziome)
        for (double y = 0; y <= height; y += sy) {
            Line line = new Line(0, y, width, y);
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(2);
            center_menu.getChildren().add(line);
        }
    }

}
