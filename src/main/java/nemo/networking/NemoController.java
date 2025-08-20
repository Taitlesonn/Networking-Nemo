package nemo.networking;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import nemo.networking.Devices.maper.Mapper_t;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NemoController {
    @FXML private BorderPane menu;
    @FXML private HBox top_menu;
    @FXML private Pane center_menu;
    private Pane worldPane;

    @FXML private Button top_files;
    @FXML private Button top_help;
    @FXML private Button top_computers;
    @FXML private Button top_vms;
    @FXML private Button top_server;
    @FXML private Button top_new;
    @FXML private Button top_network_device;
    @FXML private Button top_network_service;

    public static final class ImageRequest {
        public final ImageView iv;
        public final double x1000;
        public final double y1000;

        public ImageRequest(ImageView iv, double x1000, double y1000) {
            this.iv = iv;
            this.x1000 = x1000;
            this.y1000 = y1000;
        }
    }

    public final Mapper_t center_m = new Mapper_t();

    private Canvas gridCanvas;
    private Group contentGroup;
    private double zoom = 1.0;
    private final double BASE_GRID_STEP = 40.0;
    private final double MIN_ZOOM = 0.25;
    private final double MAX_ZOOM = 4.0;
    private static final double WORLD_COORD_SIZE = 32.0;
    private static final double DEFAULT_IMAGE_SIZE = 32.0;

    public static final ConcurrentLinkedQueue<ImageRequest> queuedImages = new ConcurrentLinkedQueue<>();

    private ScheduledExecutorService imagePollerExecutor;
    private final Object imagePollerLock = new Object();

    public final Thread center_a;
    {
        center_a = new Thread(() -> {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ignored) {
            }
        }, "center_a-compat");
        center_a.setDaemon(true);
        center_a.start();
    }

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
                double newZoom = clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM);
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
        startImagePoller();
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
            newStage.setWidth(Math.max(prefW + decoW, 300));
            newStage.setHeight(Math.max(prefH + decoH, 200));
            new_machineController ctrl = fxmlLoader.getController();
            newStage.setOnCloseRequest(e-> ctrl.shutdownExecutor());
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
            newStage.setWidth(Math.max(prefW + decoW, 300));
            newStage.setHeight(Math.max(prefH + decoH, 200));
            new_machineController ctrl = fxmlLoader.getController();
            newStage.setOnCloseRequest(e-> ctrl.shutdownExecutor());
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
            newStage.setWidth(Math.max(prefW + decoW, 300));
            newStage.setHeight(Math.max(prefH + decoH, 200));
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
            e.printStackTrace();
        }
    }

    private void center_topologia() {
        applyZoom();
    }

    private void initWorldPane() {
        if (worldPane != null) return;
        worldPane = new Pane();
        worldPane.setPrefWidth(WORLD_COORD_SIZE);
        worldPane.setPrefHeight(WORLD_COORD_SIZE);
        worldPane.setStyle("-fx-background-color: rgba(0,0,0,0.02);");
        contentGroup.getChildren().add(worldPane);
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

    private double clamp(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }

    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean panning = false;

    private void setupPanHandlers() {
        if (center_menu == null || contentGroup == null) return;
        center_menu.addEventFilter(MouseEvent.MOUSE_PRESSED, ev -> {
            if (ev.isPrimaryButtonDown() || ev.isMiddleButtonDown()) {
                panning = true;
                lastMouseX = ev.getSceneX();
                lastMouseY = ev.getSceneY();
                ev.consume();
            }
        });
        center_menu.addEventFilter(MouseEvent.MOUSE_DRAGGED, ev -> {
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
            if (panning) {
                panning = false;
                ev.consume();
            }
        });
        center_menu.addEventFilter(MouseEvent.MOUSE_CLICKED, ev -> {
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

    private ImageView copyImageView(ImageView src) {
        ImageView copy = new ImageView();
        Image img = src == null ? null : src.getImage();
        if (img != null) copy.setImage(img);
        if (src != null) {
            copy.setPreserveRatio(src.isPreserveRatio());
            copy.setFitWidth(src.getFitWidth());
            copy.setFitHeight(src.getFitHeight());
            copy.setRotate(src.getRotate());
            copy.setOpacity(src.getOpacity());
            copy.setScaleX(src.getScaleX());
            copy.setScaleY(src.getScaleY());
            copy.setSmooth(src.isSmooth());
        }
        return copy;
    }

    public void addImageToTopology(ImageView iv, double x1000, double y1000) {
        if (worldPane == null) initWorldPane();
        if (iv == null) return;
        ImageView node = copyImageView(iv);
        double lx = Math.max(0.0, Math.min(WORLD_COORD_SIZE, x1000));
        double ly = Math.max(0.0, Math.min(WORLD_COORD_SIZE, y1000));
        node.setPreserveRatio(true);
        if (node.getFitWidth() <= 0 && node.getFitHeight() <= 0) node.setFitWidth(DEFAULT_IMAGE_SIZE);
        double w = (node.getFitWidth() > 0) ? node.getFitWidth() : node.getBoundsInLocal().getWidth();
        double h;
        if (node.getFitHeight() > 0) h = node.getFitHeight();
        else {
            Image image = node.getImage();
            if (image != null && image.getWidth() > 0) {
                double ratio = image.getHeight() / image.getWidth();
                h = w * ratio;
            } else {
                h = w;
            }
        }
        double layoutX = lx - (w / 2.0);
        double layoutY = ly - (h / 2.0);
        layoutX = Math.max(0.0, Math.min(WORLD_COORD_SIZE - w, layoutX));
        layoutY = Math.max(0.0, Math.min(WORLD_COORD_SIZE - h, layoutY));
        node.setLayoutX(layoutX);
        node.setLayoutY(layoutY);
        worldPane.getChildren().add(node);
        node.setCache(true);
        node.setCacheHint(CacheHint.SPEED);
    }

    public void addImagesToTopology(ImageRequest... requests) {
        if (requests == null || requests.length == 0) return;
        for (ImageRequest r : requests) {
            if (r == null) continue;
            addImageToTopology(r.iv, r.x1000, r.y1000);
        }
    }

    public static void enqueueImageForAddition(ImageView iv, double x1000, double y1000) {
        if (iv == null) return;
        queuedImages.add(new ImageRequest(iv, x1000, y1000));
    }

    public static void enqueueImagesForAddition(ImageRequest... requests) {
        if (requests == null || requests.length == 0) return;
        for (ImageRequest r : requests) {
            if (r != null) queuedImages.add(r);
        }
    }

    private void startImagePoller() {
        if (imagePollerExecutor != null && !imagePollerExecutor.isShutdown()) return;
        imagePollerExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "image-poller");
            t.setDaemon(true);
            return t;
        });
        imagePollerExecutor.scheduleWithFixedDelay(() -> {
            synchronized (imagePollerLock) {
                try {
                    if (queuedImages.isEmpty()) return;
                    List<ImageRequest> batch = new ArrayList<>();
                    ImageRequest req;
                    while ((req = queuedImages.poll()) != null) batch.add(req);
                    if (batch.isEmpty()) return;
                    Platform.runLater(() -> {
                        try {
                            for (ImageRequest r : batch) addImageToTopology(r.iv, r.x1000, r.y1000);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }, 0L, 500L, TimeUnit.MILLISECONDS);
    }

    public void stopImagePoller() {
        if (imagePollerExecutor != null) {
            try {
                imagePollerExecutor.shutdownNow();
                imagePollerExecutor.awaitTermination(200, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            } finally {
                imagePollerExecutor = null;
            }
        }
    }

    public void shutdownController() {
        stopImagePoller();
    }
}
