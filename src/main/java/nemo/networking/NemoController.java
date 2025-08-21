package nemo.networking;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import nemo.networking.Devices.NetworkDevice;
import nemo.networking.Devices.NetworkService;
import nemo.networking.Devices.PC;
import nemo.networking.Devices.VM;
import nemo.networking.Devices.maper.Mapper_t;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NemoController {
    @FXML private BorderPane menu;
    @FXML private HBox top_menu;
    @FXML private Pane center_menu;
    private final List<Pane> worldPane = new ArrayList<>();

    @FXML private Button top_files;
    @FXML private Button top_help;
    @FXML private Button top_computers;
    @FXML private Button top_vms;
    @FXML private Button top_server;
    @FXML private Button top_new;
    @FXML private Button top_network_device;
    @FXML private Button top_network_service;

    public record ImageRequest(ImageView iv, double x1000, double y1000) {
    }

    public final Mapper_t center_m = new Mapper_t();

    private Canvas gridCanvas;
    private Group contentGroup;
    private double zoom = 1.0;
    private final double BASE_GRID_STEP = 40.0;
    private final double MIN_ZOOM = 0.25;
    private final double MAX_ZOOM = 4.0;
    private static final double DEFAULT_IMAGE_SIZE = 64.0;

    // Łączenie i rozłączenie
    private final List<Line> connections = new ArrayList<>();
    private final Map<ImageView, List<Line>> deviceConnections = new ConcurrentHashMap<>();
    private ImageView selectedDevice = null;

    private static final Logger LOGGER = Logger.getLogger(NemoController.class.getName());




    @FXML
    private void initialize() {
        Topologia.setClassys(center_m, this);

        gridCanvas = new Canvas();
        gridCanvas.setMouseTransparent(true);
        gridCanvas.setCache(false);

        contentGroup = new Group();
        contentGroup.setCache(true);
        contentGroup.setCacheHint(CacheHint.SPEED);

        if (center_menu != null) {
            center_menu.getChildren().addAll(gridCanvas, contentGroup);
            center_menu.widthProperty().addListener((obs, oldV, newV) -> resizeAndRedrawGrid());
            center_menu.heightProperty().addListener((obs, oldV, newV) -> resizeAndRedrawGrid());
            center_menu.addEventFilter(ScrollEvent.SCROLL, ev -> {
                double delta = ev.getDeltaY();
                double factor = Math.pow(1.001, delta);
                double newZoom = clamp(zoom * factor);
                if (Math.abs(newZoom - zoom) > 1e-5) {
                    zoom = newZoom;
                    applyZoom();
                    redrawGridOnZoom();
                }
                ev.consume();
            });
            setupPanHandlers();
        }

        Platform.runLater(this::resizeAndRedrawGrid);
        Platform.runLater(this::resizeAndRedrawGrid);
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
            LOGGER.log(Level.SEVERE, "Failed to open New window", e);
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
            double prefW = root.prefWidth(-1);
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
            newStage.setWidth(Math.max(prefW + decoW, 300));
            newStage.setHeight(Math.max(prefH + decoH, 200));
            new_machineController ctrl = fxmlLoader.getController();
            newStage.setOnCloseRequest(e-> ctrl.shutdownExecutor());
            newStage.show();
            newStage.sizeToScene();
            newStage.centerOnScreen();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open network device window", e);
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
            newStage.setWidth(Math.max(prefW + decoW, 300));
            newStage.setHeight(Math.max(prefH + decoH, 200));
            new_machineController ctrl = fxmlLoader.getController();
            newStage.setOnCloseRequest(e-> ctrl.shutdownExecutor());
            newStage.show();
            newStage.sizeToScene();
            newStage.centerOnScreen();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open network service window", e);
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
            newStage.setWidth(Math.max(prefW + decoW, 300));
            newStage.setHeight(Math.max(prefH + decoH, 200));
            new_machineController ctrl = fxmlLoader.getController();
            newStage.setOnCloseRequest(e-> ctrl.shutdownExecutor());
            newStage.show();
            newStage.sizeToScene();
            newStage.centerOnScreen();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open vm window", e);
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
            newStage.setWidth(Math.max(prefW + decoW, 300));
            newStage.setHeight(Math.max(prefH + decoH, 200));
            newStage.show();
            newStage.sizeToScene();
            newStage.centerOnScreen();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open server window", e);
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
            double prefW = root.prefWidth(-1);
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
            newStage.setWidth(Math.max(prefW + decoW, 300));
            newStage.setHeight(Math.max(prefH + decoH, 200));
            new_machineController ctrl = fxmlLoader.getController();
            newStage.setOnCloseRequest(e-> ctrl.shutdownExecutor());
            newStage.show();
            newStage.sizeToScene();
            newStage.centerOnScreen();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open computer window", e);
        }
    }



    private void resizeAndRedrawGrid() {
        if (center_menu == null) return;
        double w = center_menu.getWidth();
        double h = center_menu.getHeight();
        if (w <= 0 || h <= 0) return;
        gridCanvas.setWidth(w);
        gridCanvas.setHeight(h);
        redrawGrid();
    }

    private void redrawGridOnZoom() {
        redrawGrid();
    }

    private void redrawGrid() {
        double w = gridCanvas.getWidth();
        double h = gridCanvas.getHeight();
        if (w <= 0 || h <= 0) return;
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        double step = BASE_GRID_STEP * zoom;
        if (step < 8) step = 8;
        if (step > 200) step = 200;
        gc.setLineWidth(1.0);
        gc.setStroke(Color.rgb(200, 200, 200));
        double x = 0.5;
        while (x <= w) {
            gc.strokeLine(x, 0, x, h);
            x += step;
        }
        double y = 0.5;
        while (y <= h) {
            gc.strokeLine(0, y, w, y);
            y += step;
        }
        int major = 5;
        gc.setLineWidth(1.5);
        gc.setStroke(Color.rgb(170, 170, 170));
        x = 0.5;
        while (x <= w) {
            gc.strokeLine(x, 0, x, h);
            x += step * major;
        }
        y = 0.5;
        while (y <= h) {
            gc.strokeLine(0, y, w, y);
            y += step * major;
        }
    }

    private void applyZoom() {
        if (contentGroup == null) return;
        contentGroup.setScaleX(zoom);
        contentGroup.setScaleY(zoom);
    }

    private double clamp(double v) {
        return Math.max(0.25, Math.min(4.0, v));
    }

    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean panning = false;




    public void addImageToTopology(ImageView iv, double x1000, double y1000, String name, int type) {
        if (iv == null) return;

        iv.setFitWidth(DEFAULT_IMAGE_SIZE);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setPickOnBounds(true);

        iv.setLayoutX(x1000);
        iv.setLayoutY(y1000);

        contentGroup.getChildren().add(iv);

        makeDraggable(iv, name, type);
        addContextMenu(iv, name, type);


        iv.setCache(true);
        iv.setCacheHint(CacheHint.SPEED);
    }


    private void makeDraggable(ImageView iv, String name, int type) {
        final double[] mouseOffset = new double[2];

        iv.setOnMousePressed(e -> {
            if (!e.isPrimaryButtonDown()) return;
            iv.toFront();

            // obliczamy offset względem translateX/Y, nie layoutX/Y
            mouseOffset[0] = e.getSceneX() - iv.getTranslateX();
            mouseOffset[1] = e.getSceneY() - iv.getTranslateY();

            e.consume();
        });

        iv.setOnMouseDragged(e -> {
            if (!e.isPrimaryButtonDown()) return;

            iv.setTranslateX(e.getSceneX() - mouseOffset[0]);
            iv.setTranslateY(e.getSceneY() - mouseOffset[1]);

            e.consume();
        });
    }

    private void setupPanHandlers() {
        if (center_menu == null || contentGroup == null) return;

        center_menu.addEventFilter(MouseEvent.MOUSE_PRESSED, ev -> {
            if (isOnDevice(ev)) return;
            if (ev.isPrimaryButtonDown() || ev.isMiddleButtonDown()) {
                panning = true;
                lastMouseX = ev.getSceneX();
                lastMouseY = ev.getSceneY();
                ev.consume();
            }
        });

        center_menu.addEventFilter(MouseEvent.MOUSE_DRAGGED, ev -> {
            if (isOnDevice(ev)) return;
            if (!panning) return;
            double dx = ev.getSceneX() - lastMouseX;
            double dy = ev.getSceneY() - lastMouseY;
            lastMouseX = ev.getSceneX();
            lastMouseY = ev.getSceneY();
            contentGroup.setTranslateX(contentGroup.getTranslateX() + dx);
            contentGroup.setTranslateY(contentGroup.getTranslateY() + dy);
            ev.consume();
        });

        center_menu.addEventFilter(MouseEvent.MOUSE_RELEASED, ev -> {
            if (isOnDevice(ev)) return;
            if (panning) {
                panning = false;
                ev.consume();
            }
        });

        center_menu.addEventFilter(MouseEvent.MOUSE_CLICKED, ev -> {
            if (isOnDevice(ev)) return;
            if (ev.getClickCount() == 2) {
                zoom = 1.0;
                contentGroup.setTranslateX(0);
                contentGroup.setTranslateY(0);
                applyZoom();
                redrawGrid();
                ev.consume();
            }
        });
    }

    private boolean isOnDevice(MouseEvent ev) {
        if (!(ev.getTarget() instanceof Node n)) return false;
        while (n != null) {
            if (n instanceof ImageView) return true;
            n = n.getParent();
        }
        return false;
    }

    private void addContextMenu(ImageView iv, String name, int type) {
        ContextMenu cm = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(evt -> {
            cm.hide();
            switch (type){
                case Topologia.NetworkDevice_t -> {
                    for(NetworkDevice n : Topologia.getNetworkDevices()){
                        if(Objects.equals(n.getName(), name)){
                            this.center_m.remove_device(type, n);
                        }
                    }
                    if(!Topologia.delete_network_device(name)){
                        System.err.println("Error deleting Network Device");
                    }
                }
                case Topologia.NetworkService_t -> {
                    for(NetworkService n : Topologia.getNetworkServices()){
                        if(Objects.equals(n.getName(), name)){
                            this.center_m.remove_device(type, n);
                        }
                    }
                    if(!Topologia.delete_network_service(name)){
                        System.err.println("Error deleting a Network Service");
                    }
                }
                case Topologia.PC_t -> {
                    for(PC p : Topologia.getPcs()){
                        if(Objects.equals(p.getName(), name)){
                            this.center_m.remove_device(type, p);
                        }
                    }
                    if(!Topologia.delete_pc(name)){
                        System.err.println("Error deliting a pc");
                    }
                }
                case Topologia.VM_t -> {
                    for(VM v : Topologia.getVms()){
                        if(Objects.equals(v.getName(), name)){
                            this.center_m.remove_device(type, v);
                        }
                    }
                    if(!Topologia.delete_vm(name)){
                        System.err.println("Error deleting a VM");
                    }
                }
            }

            List<Line> lines = deviceConnections.get(iv);
            if (lines != null) {
                List<Line> copy = new ArrayList<>(lines);
                for (Line line : copy) {
                    contentGroup.getChildren().remove(line);
                    connections.remove(line);
                    deviceConnections.forEach((d, l) -> l.remove(line));
                }
                lines.clear();
            }
            contentGroup.getChildren().remove(iv);
        });

        // Connect
        MenuItem connect = getConnect(iv, cm);

        // Disconnect
        MenuItem disconnect = getDisconnect(iv, cm);
        cm.getItems().addAll(deleteItem, connect, disconnect);

        iv.setOnContextMenuRequested(e -> cm.show(iv, e.getScreenX(), e.getScreenY()));
    }

    private MenuItem getDisconnect(ImageView iv, ContextMenu cm) {
        MenuItem disconnect = new MenuItem("Disconnect");
        disconnect.setOnAction(e -> {
            cm.hide();
            if(Topologia.getMachineCunt() >=2){
                List<Line> lines = deviceConnections.get(iv);
                if (lines != null) {
                    List<Line> copy = new ArrayList<>(lines);
                    for (Line line : copy) {
                        contentGroup.getChildren().remove(line);
                        connections.remove(line);
                        deviceConnections.forEach((d, l) -> l.remove(line));
                    }
                    lines.clear();
                }
            }
        });
        return disconnect;
    }

    private MenuItem getConnect(ImageView iv, ContextMenu cm) {
        MenuItem connect = new MenuItem("Connect");
        connect.setOnAction(e -> {
            cm.hide();
            if(Topologia.getMachineCunt() >= 2){
                if (selectedDevice == null) {
                    selectedDevice = iv;
                    iv.setEffect(new DropShadow(20, Color.GREEN));
                } else if (selectedDevice != iv) {
                    // Tworzymy połączenie
                    Line line = new Line();
                    line.startXProperty().bind(selectedDevice.layoutXProperty().add(selectedDevice.translateXProperty()).add(selectedDevice.getFitWidth()/2));
                    line.startYProperty().bind(selectedDevice.layoutYProperty().add(selectedDevice.translateYProperty()).add(selectedDevice.getFitHeight()/2));
                    line.endXProperty().bind(iv.layoutXProperty().add(iv.translateXProperty()).add(iv.getFitWidth()/2));
                    line.endYProperty().bind(iv.layoutYProperty().add(iv.translateYProperty()).add(iv.getFitHeight()/2));
                    line.setStrokeWidth(2);
                    line.setStroke(Color.BLUE);
                    contentGroup.getChildren().addFirst(line);
                    connections.add(line);

                    deviceConnections.computeIfAbsent(selectedDevice, k -> new ArrayList<>()).add(line);
                    deviceConnections.computeIfAbsent(iv, k -> new ArrayList<>()).add(line);

                    selectedDevice.setEffect(null);
                    selectedDevice = null;
                }
            }
        });
        return connect;
    }

}
