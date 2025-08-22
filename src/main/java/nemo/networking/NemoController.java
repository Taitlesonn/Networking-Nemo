package nemo.networking;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
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
    @FXML private Pane center_menu;

    private final List<Pane> worldPane = new ArrayList<>();

    private Pane overlayPane;
    private ImageView ghost = null;



    public final Mapper_t center_m = new Mapper_t();

    private Canvas gridCanvas;
    private Group contentGroup;
    private double zoom = 1.0;
    private static final double DEFAULT_IMAGE_SIZE = 64.0;

    // Łączenie i rozłączenie
    private final List<Line> connections = new ArrayList<>();
    private final Map<ImageView, List<Line>> deviceConnections = new ConcurrentHashMap<>();
    private ImageView selectedDevice = null;

    private static final Logger LOGGER = Logger.getLogger(NemoController.class.getName());

    // Pola pomocnicze do obsługi panowania/przeciągania widoku (jeśli zaimplementowane w setupPanHandlers)
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean panning = false;




    @FXML
    private void initialize() {
        Topologia.setClassys(center_m, this);

        // Canvas odpowiedzialny za rysowanie siatki (grid)
        // Nie cache'ujemy rysunku canvasa, ponieważ rysujemy dynamicznie przy zmianie rozmiaru/zoomu
        // Canvas nie przechwytuje zdarzeń myszy — zdarzenia "przechodzą" do elementów poniżej
        gridCanvas = new Canvas();
        gridCanvas.setMouseTransparent(true);
        gridCanvas.setCache(false);

        // Grupa przechowująca wszystkie elementy topologii (np. ImageView)
        // Cache'ujemy zawartość grupy, by przyspieszyć render przy skalowaniu
        contentGroup = new Group();
        contentGroup.setCache(true);
        contentGroup.setCacheHint(CacheHint.SPEED);

        // overlayPane - nie skalowana, leży nad contentGroup
        // ważne: overlay powinien przechwytywać eventy podczas draga
        overlayPane = new Pane();
        overlayPane.setPickOnBounds(true);
        overlayPane.setMouseTransparent(false);



        if (center_menu != null) {
            // Kolejność: najpierw grid (tło), potem contentGroup (elementy nad siatką), no i ovarlayPane dla przesyuwania
            center_menu.getChildren().addAll(gridCanvas, contentGroup, overlayPane);

            // Reakcja na zmianę rozmiaru kontenera — dopasuj canvas i przerysuj siatkę
            center_menu.widthProperty().addListener((obs, oldV, newV) -> resizeAndRedrawGrid());
            center_menu.heightProperty().addListener((obs, oldV, newV) -> resizeAndRedrawGrid());

            // Obsługa kółka myszy do zoomowania
            // delikatna zmiana współczynnika zoom (eksponencjalna skala)
            // Jeśli zmiana istotna, zastosuj zoom i przerysuj siatkę
            center_menu.addEventFilter(ScrollEvent.SCROLL, ev -> {
                double delta = ev.getDeltaY();
                double factor = Math.pow(1.001, delta);
                double newZoom = clamp(zoom * factor);

                if (Math.abs(newZoom - zoom) > 1e-5) {
                    zoom = newZoom;
                    applyZoom();
                    redrawGridOnZoom();
                }
                // Zatrzymaj propagację zdarzenia (nie przewijamy np. rodzica)
                ev.consume();
            });

            // Podłącz obsługę panowania (przeciągania widoku) — metoda definiuje listenery myszy
            setupPanHandlers();
        }
        // Po inicjalizacji UI -- dopasuj rozmiar i przerysuj siatkę.
        // Wywoływane na Platform.runLater, żeby upewnić się że layout jest policzony.
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



    /**
     * Dopasowuje rozmiar canvasa do rozmiaru kontenera i przerysowuje siatkę.
     */
    private void resizeAndRedrawGrid() {
        if (center_menu == null) return;

        double w = center_menu.getWidth();
        double h = center_menu.getHeight();
        // Jeśli wymiary nieustalone — nic nie rób
        if (w <= 0 || h <= 0) return;


        // Ustaw rozmiar canvasa i przerysuj
        gridCanvas.setWidth(w);
        gridCanvas.setHeight(h);
        redrawGrid();
    }

    private void redrawGridOnZoom() {
        redrawGrid();
    }

    /**
     * Rysuje siatkę na canvasie opartą o aktualny faktor zoom.
     * Rysowane są linie podstawowe oraz co 'major' linia główna (grubsza).
     */
    private void redrawGrid() {
        double w = gridCanvas.getWidth();
        double h = gridCanvas.getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        // Parametry siatki
        double BASE_GRID_STEP = 40.0;           // krok siatki przy zoom = 1.0
        double step = BASE_GRID_STEP * zoom;    // krok dostosowany do zoomu

        // Ograniczenia kroku (czytelność)
        if (step < 8) step = 8;
        if (step > 200) step = 200;

        // Rysujemy cienkie linie siatki
        gc.setLineWidth(1.0);
        gc.setStroke(Color.rgb(200, 200, 200));

        // Pionowe linie (od offsetu 0.5, żeby linie były kreskowane ostrzej)
        double x = 0.5;
        while (x <= w) {
            gc.strokeLine(x, 0, x, h);
            x += step;
        }

        // Poziome linie
        double y = 0.5;
        while (y <= h) {
            gc.strokeLine(0, y, w, y);
            y += step;
        }

        // Linia główna co `major` kroków (trochę ciemniejsza / grubsza)
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

    /**
     * Zastosuj aktualny współczynnik zoom do zawartości (contentGroup).
     * Skalowanie odbywa się przez transformację skali grupy (nie przez zmianę layoutów elementów).
     */
    private void applyZoom() {
        if (contentGroup == null) return;
        contentGroup.setScaleX(zoom);
        contentGroup.setScaleY(zoom);
    }

    private double clamp(double v) {
        return Math.max(0.25, Math.min(4.0, v));
    }






    /**
     * Dodaje ImageView do topologii w określonej pozycji (layoutX/layoutY),
     * ustawia domyślny rozmiar, dodaje obsługę drag i menu kontekstowe.
     *
     * @param iv ImageView reprezentujący węzeł
     * @param x1000 pozycja X w układzie contentGroup
     * @param y1000 pozycja Y w układzie contentGroup
     * @param name nazwa (używana w menu kontekstowym)
     * @param type typ (używany w menu kontekstowym)
     */
    public void addImageToTopology(ImageView iv, double x1000, double y1000, String name, int type) {
        if (iv == null) return;

        // Sklaowanie x i y do aktualnego layout
        double lx = center_menu.getWidth();
        double ly = center_menu.getHeight();
        if(lx < x1000) x1000 /= lx;
        if(ly < y1000) y1000 /= ly;


        // Ustawienia wyglądu obrazu — stała szerokość, zachowanie proporcji, wygładzenie
        // Pozwala klikać w transparentne obszary obrazu (łatwiej łapać zdarzenia)
        iv.setFitWidth(DEFAULT_IMAGE_SIZE);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setPickOnBounds(true);

        // Pozycjonowanie przez layoutX/layoutY — istotne, bo drag używa layoutów
        iv.setLayoutX(x1000);
        iv.setLayoutY(y1000);

        // Dodaj do grupy z elementami
        contentGroup.getChildren().add(iv);

        makeDraggable(iv);
        addContextMenu(iv, name, type);

        // Cache'owanie pojedynczego ImageView dla lepszej wydajności
        iv.setCache(true);
        iv.setCacheHint(CacheHint.SPEED);
    }


    /**
     * Umożliwia przeciąganie ImageView wewnątrz contentGroup.
     * Uwaga: obliczamy offset względem współrzędnych lokalnych contentGroup (sceneToLocal),
     * i ustawiamy layoutX/layoutY — to sprawia, że element poprawnie "podąża" za kursorem niezależnie
     * od transformacji (np. skali contentGroup).
     */
    // pole klasy (jeśli jeszcze go nie masz)
    private ImageView activeDragged = null;

    /**
     * Główna metoda drag — implementacja "ghost overlay".
     * Tworzymy snapshot-ghost w overlayPane, ustawiamy go w pozycji odpowiadającej
     * oryginałowi, odpinamy/będziemy aktualizować linie ręcznie podczas drag,
     * po zakończeniu przywracamy bindingi i przenosimy oryginał do pozycji finalnej.
     */
    private void makeDraggable(ImageView iv) {
        final double[] mouseOffsetScene = new double[2]; // offset myszki względem top-left node w scenie
        final Point2D[] lastTopLeftScene = new Point2D[1]; // przechowuje aktualny top-left (w scenie)

        iv.setOnMousePressed(e -> {
            if (!e.isPrimaryButtonDown()) return;
            if (activeDragged != null) return; // tylko 1 drag na raz
            activeDragged = iv;
            iv.toFront();

            // wyłącz cache contentGroup (opcjonalne)
            if (contentGroup.isCache()) contentGroup.setCache(false);

            // oblicz top-left oryginału w scene (dokładny punkt, nie boundsInParent)
            Point2D nodeTopLeftScene = iv.localToScene(0, 0);
            lastTopLeftScene[0] = nodeTopLeftScene;

            // offset: gdzie kliknięto względem top-left
            mouseOffsetScene[0] = e.getSceneX() - nodeTopLeftScene.getX();
            mouseOffsetScene[1] = e.getSceneY() - nodeTopLeftScene.getY();

            // stwórz ghost i ustaw jego top-left na overlay w tym samym miejscu
            ghost = new ImageView(iv.getImage());
            ghost.setPreserveRatio(iv.isPreserveRatio());
            ghost.setFitWidth(iv.getFitWidth());
            ghost.setSmooth(true);
            ghost.setOpacity(0.98);
            ghost.setMouseTransparent(false); // ghost przechwytuje eventy

            Point2D topLeftInOverlay = overlayPane.sceneToLocal(nodeTopLeftScene);
            ghost.setLayoutX(topLeftInOverlay.getX());
            ghost.setLayoutY(topLeftInOverlay.getY());

            overlayPane.getChildren().add(ghost);

            // ukryj oryginał (ale nie usuwaj go)
            iv.setVisible(false);
            iv.setMouseTransparent(true);

            // odwiąż bindingi linii (będziemy je ręcznie aktualizować podczas drag)
            List<Line> lines = deviceConnections.get(iv);
            if (lines != null) {
                for (Line ln : lines) {
                    try { ln.startXProperty().unbind(); } catch (Exception ignored) {}
                    try { ln.startYProperty().unbind(); } catch (Exception ignored) {}
                    try { ln.endXProperty().unbind(); } catch (Exception ignored) {}
                    try { ln.endYProperty().unbind(); } catch (Exception ignored) {}
                }
            }

            e.consume();
        });

        // obsługa drag na ghost (ghost przechwytuje eventy)
        // jeśli ghost nie istnieje, nic się nie dzieje
        // używamy spiralnej logiki: noweTopLeftScene = event.scene - mouseOffsetScene
        // konwertujemy go do overlay coords i ustawiamy ghost.setLayoutX/Y
        // jednocześnie aktualizujemy powiązane linie ręcznie (konwertując centra do contentGroup coords)
        iv.setOnMouseDragged(e -> {
            // noop: zostawimy handlery na ghost (więcej deterministyczne).
        });

        // ustaw handler-y bezpośrednio na ghost, ale stworzony dynamicznie:
        // Jednak jeśli ghost powstaje dopiero w press, musimy dodać listener w runtime - poniżej robimy to przez jeden wspólny listener
        // Zrobimy: dodaj periodic listener do overlayPane - ale prościej: ustaw globalny filter gdy activeDragged != null.
        // Tutaj dodamy prosty mouse listener do overlayPane jeśli jeszcze nie ma:
        overlayPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, ev -> {
            if (activeDragged == null) return;
            if (ghost == null) return;
            if (!ev.isPrimaryButtonDown()) return;

            // oblicz nowy top-left w scenie jako: scena kursora - offset
            double newTopLeftSceneX = ev.getSceneX() - mouseOffsetScene[0];
            double newTopLeftSceneY = ev.getSceneY() - mouseOffsetScene[1];
            Point2D newTopLeftScene = new Point2D(newTopLeftSceneX, newTopLeftSceneY);

            // zapisz aktualny top-left (przy release użyjemy go)
            lastTopLeftScene[0] = newTopLeftScene;

            // ustaw ghost w overlay coords
            Point2D newTopLeftOverlay = overlayPane.sceneToLocal(newTopLeftScene);
            ghost.setLayoutX(newTopLeftOverlay.getX());
            ghost.setLayoutY(newTopLeftOverlay.getY());

            // Aktualizuj linie powiązane (ręcznie)
            List<Line> lns = deviceConnections.get(activeDragged);
            if (lns != null && !lns.isEmpty()) {
                // centrum ghost w scene:
                Bounds gb = ghost.getBoundsInLocal();
                Point2D ghostCenterScene = ghost.localToScene(gb.getWidth() / 2.0, gb.getHeight() / 2.0);
                // ghost center -> contentGroup coords
                Point2D ghostCenterInContent = contentGroup.sceneToLocal(ghostCenterScene);

                for (Line ln : lns) {
                    ImageView other = findOtherDeviceForLine(ln, activeDragged);
                    if (other != null) {
                        // center other in contentGroup coords (dokładnie)
                        Bounds ob = other.getBoundsInParent();
                        double ocx = ob.getMinX() + ob.getWidth() / 2.0;
                        double ocy = ob.getMinY() + ob.getHeight() / 2.0;
                        // ustaw linie: jeden koniec - other center, drugi - ghost center (w content coords)
                        // zachowujemy orientację: jeśli wcześniej start był związany z other, nie musimy się tym przejmować,
                        // bo wizualnie linia i tak powinna łączyć środki.
                        ln.setStartX(ocx);
                        ln.setStartY(ocy);
                        ln.setEndX(ghostCenterInContent.getX());
                        ln.setEndY(ghostCenterInContent.getY());
                    } else {
                        // brak drugiego węzła -> ustaw końcówkę na ghost
                        ln.setEndX(ghostCenterInContent.getX());
                        ln.setEndY(ghostCenterInContent.getY());
                    }
                }
            }

            ev.consume();
        });

        // release na overlayPane — finalizujemy pozycję
        overlayPane.addEventFilter(MouseEvent.MOUSE_RELEASED, ev -> {
            if (activeDragged == null) return;
            // Jeśli user release'ował poza overlayPane, upewnij się że użyjesz ostatniego znanego top-left
            Point2D finalTopLeftScene = lastTopLeftScene[0];
            if (finalTopLeftScene == null) {
                // fallback: pobierz kursorscene - offset
                finalTopLeftScene = new Point2D(ev.getSceneX() - mouseOffsetScene[0], ev.getSceneY() - mouseOffsetScene[1]);
            }

            // konwertuj finalTopLeftScene do contentGroup coords
            Point2D finalTopLeftInContent = contentGroup.sceneToLocal(finalTopLeftScene);

            // ustaw layout oryginału
            ImageView ivRef = activeDragged;
            ivRef.setLayoutX(finalTopLeftInContent.getX());
            ivRef.setLayoutY(finalTopLeftInContent.getY());

            // usuń ghost
            if (ghost != null) {
                overlayPane.getChildren().remove(ghost);
                ghost = null;
            }

            // przywróć widoczność oryginału
            ivRef.setVisible(true);
            ivRef.setMouseTransparent(false);

            // Uwaga: rebind linii dopiero po kolejnej pętli layout - żeby boundsInParent były aktualne.
            Platform.runLater(() -> {
                List<Line> lns = deviceConnections.get(ivRef);
                if (lns != null) {
                    for (Line ln : lns) {
                        // odwiąż na wszelki wypadek
                        try { ln.startXProperty().unbind(); } catch (Exception ignored) {}
                        try { ln.startYProperty().unbind(); } catch (Exception ignored) {}
                        try { ln.endXProperty().unbind(); } catch (Exception ignored) {}
                        try { ln.endYProperty().unbind(); } catch (Exception ignored) {}

                        ImageView other = findOtherDeviceForLine(ln, ivRef);
                        if (other != null) {
                            bindLineToNodes(ln, ivRef, other);
                        }
                    }
                }

                // przywróć cache contentGroup (opcjonalne)
                contentGroup.setCache(true);
            });

            // reset stanu
            activeDragged = null;
            lastTopLeftScene[0] = null;
            mouseOffsetScene[0] = 0;
            mouseOffsetScene[1] = 0;

            ev.consume();
        });
    }



    /**
     * Pomocnik: znajdź drugi ImageView dla danej linii (czyli urządzenie po drugiej stronie linii)
     */
    private ImageView findOtherDeviceForLine(Line line, ImageView self) {
        for (Map.Entry<ImageView, List<Line>> e : deviceConnections.entrySet()) {
            ImageView key = e.getKey();
            List<Line> lst = e.getValue();
            if (lst != null && lst.contains(line) && key != self) {
                return key;
            }
        }
        return null;
    }

    /**
     * Pomocnik do bindowania linii (start -> a, end -> b) używając boundsInParentProperty.
     */
    private void bindLineToNodes(Line line, ImageView a, ImageView b) {
        // startX = centerX(a)
        line.startXProperty().bind(Bindings.createDoubleBinding(() -> {
            Bounds ba = a.getBoundsInParent();
            return ba.getMinX() + ba.getWidth() / 2.0;
        }, a.boundsInParentProperty()));

        line.startYProperty().bind(Bindings.createDoubleBinding(() -> {
            Bounds ba = a.getBoundsInParent();
            return ba.getMinY() + ba.getHeight() / 2.0;
        }, a.boundsInParentProperty()));

        // endX = centerX(b)
        line.endXProperty().bind(Bindings.createDoubleBinding(() -> {
            Bounds bb = b.getBoundsInParent();
            return bb.getMinX() + bb.getWidth() / 2.0;
        }, b.boundsInParentProperty()));

        line.endYProperty().bind(Bindings.createDoubleBinding(() -> {
            Bounds bb = b.getBoundsInParent();
            return bb.getMinY() + bb.getHeight() / 2.0;
        }, b.boundsInParentProperty()));
    }





    private void setupPanHandlers() {
        if (center_menu == null || contentGroup == null) return;

        center_menu.addEventFilter(MouseEvent.MOUSE_PRESSED, ev -> {
            if (activeDragged != null) return;
            if (isOnDevice(ev)) return;
            if (ev.isPrimaryButtonDown() || ev.isMiddleButtonDown()) {
                panning = true;
                lastMouseX = ev.getSceneX();
                lastMouseY = ev.getSceneY();
                ev.consume();
            }
        });

        center_menu.addEventFilter(MouseEvent.MOUSE_DRAGGED, ev -> {
            if (activeDragged != null) return;
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

                    line.startXProperty().bind(
                            selectedDevice.boundsInParentProperty().map(b -> b.getMinX() + b.getWidth() / 2)
                    );
                    line.startYProperty().bind(
                            selectedDevice.boundsInParentProperty().map(b -> b.getMinY() + b.getHeight() / 2)
                    );

                    line.endXProperty().bind(
                            iv.boundsInParentProperty().map(b -> b.getMinX() + b.getWidth() / 2)
                    );
                    line.endYProperty().bind(
                            iv.boundsInParentProperty().map(b -> b.getMinY() + b.getHeight() / 2)
                    );

                    line.setStrokeWidth(2);
                    line.setStroke(Color.GREY);
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
