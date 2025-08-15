package nemo.networking;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import nemo.networking.Devices.NetworkDevice;
import nemo.networking.Devices.NetworkService;
import nemo.networking.Devices.PC;
import nemo.networking.Devices.VM;

public class Inofs_devices_hbox {
    private HBox main;
    private ImageView icon = new ImageView();
    private Label note = new Label();
    private Label name = new Label();
    private Label statusDotLabel = new Label();
    private Circle dot = new Circle(6);

    private NetworkDevice nd = null;
    private NetworkService ns = null;
    private PC pc = null;
    private VM vm = null;

    // nowy przycisk do pokazywania IP
    private Button ipButton = new Button("IP");

    public Inofs_devices_hbox() {
        // podstawowa konfiguracja HBox i elementów
        main = new HBox(12);
        main.setAlignment(Pos.CENTER_LEFT);
        main.setPadding(new Insets(8));
        main.setMinHeight(56);

        // wygląd ikonki
        icon.setFitHeight(32);
        icon.setFitWidth(32);
        icon.setPreserveRatio(true);

        // teksty: domyślny wygląd (kolor biały)
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        note.setStyle("-fx-text-fill: #dfeeff; -fx-font-size: 12px;");
        statusDotLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        // kropka ma biały obrys aby była czytelna na tle
        dot.setStroke(Color.web("#1a3a5a"));
        dot.setStrokeWidth(1.0);

        // domyślne tło i zaokrąglenie (jeśli nie używasz CSS)
        main.setBackground(new Background(new BackgroundFill(Color.web("#0b2b4a"), new CornerRadii(8), Insets.EMPTY)));

        // drop shadow i efekt hover (skaluje i wzmacnia cień)
        DropShadow shadow = new DropShadow();
        shadow.setRadius(6);
        shadow.setOffsetY(2);
        shadow.setColor(Color.color(0,0,0,0.6));
        main.setEffect(shadow);

        // Scale transition przy najechaniu (delikatne powiększenie)
        ScaleTransition stEnter = new ScaleTransition(Duration.millis(140), main);
        stEnter.setToX(1.02);
        stEnter.setToY(1.02);

        ScaleTransition stExit = new ScaleTransition(Duration.millis(120), main);
        stExit.setToX(1.0);
        stExit.setToY(1.0);

        main.setOnMouseEntered(e -> {
            stExit.stop();
            stEnter.playFromStart();
            DropShadow bigger = new DropShadow(10, 0, 4, Color.color(0,0,0,0.75));
            main.setEffect(bigger);
        });
        main.setOnMouseExited(e -> {
            stEnter.stop();
            stExit.playFromStart();
            main.setEffect(shadow);
        });

        // Konfiguracja przycisku IP (pasujące kolory)
        ipButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #2b6a9a;" +
                        "-fx-border-radius: 6;" +
                        "-fx-text-fill: #dfeeff;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 4 8 4 8;"
        );
        ipButton.setTooltip(new Tooltip("Pokaż adresy IP"));
        ipButton.setOnMouseEntered(e -> ipButton.setScaleX(1.05));
        ipButton.setOnMouseExited(e -> ipButton.setScaleX(1.0));
        ipButton.setOnAction(e -> showIpsWindow());
    }

    // fluent setters (zwracają this żeby łatwiej dodawać do VBox)
    public Inofs_devices_hbox setNd(NetworkDevice nd) {
        this.nd = nd;
        this.ns = null;
        this.pc = null;
        this.vm = null;
        return this;
    }

    public Inofs_devices_hbox setNs(NetworkService ns) {
        this.ns = ns;
        this.nd = null;
        this.pc = null;
        this.vm = null;
        return this;
    }

    public Inofs_devices_hbox setPc(PC pc) {
        this.pc = pc;
        this.ns = null;
        this.nd = null;
        this.vm = null;
        return this;
    }

    public Inofs_devices_hbox setVm(VM vm) {
        this.vm = vm;
        this.ns = null;
        this.nd = null;
        this.pc = null;
        return this;
    }

    public HBox render() {
        // wyczyść poprzednie elementy (żeby nie duplikować przy ponownym renderze)
        main.getChildren().clear();

        // wybór ikony i ustawienie tekstów (zachowałem Twoją logikę mapowania typów)
        if (this.pc != null) {
            this.icon.setImage(
                    (this.pc.getType() == this.pc.windows_t) ? Images_st.getWindowsWorkStetion().getImage() :
                            (this.pc.getType() == this.pc.windows_server_t) ? Images_st.getWindowsServer().getImage() :
                                    (this.pc.getType() == this.pc.linux_t) ? Images_st.getLinuxWorkStetion().getImage() :
                                            (this.pc.getType() == this.pc.linux_server_t) ? Images_st.getLinuxServer().getImage() : null
            );
            this.note.setText(this.pc.getNotes());
            this.name.setText(this.pc.getName());
            if (this.pc.isRunnig()) {
                this.statusDotLabel.setText("Włączone:");
                this.dot.setFill(Color.LIMEGREEN);
            } else {
                this.statusDotLabel.setText("Wyłączone:");
                this.dot.setFill(Color.RED);
            }
        } else if (this.ns != null) {
            this.icon.setImage(
                    (this.ns.getType() == this.ns.firewall_t) ? Images_st.getFirewall().getImage() :
                            (this.ns.getType() == this.ns.api_t) ? Images_st.getApi().getImage() :
                                    (this.ns.getType() == this.ns.db_t) ? Images_st.getDb().getImage() :
                                            (this.ns.getType() == this.ns.wan_cloud_t) ? Images_st.getWan().getImage() : null
            );
            this.note.setText(this.ns.getNotes());
            this.name.setText(this.ns.getName());
            if (this.ns.isRunnig()) {
                this.statusDotLabel.setText("Włączone:");
                this.dot.setFill(Color.LIMEGREEN);
            } else {
                this.statusDotLabel.setText("Wyłączone:");
                this.dot.setFill(Color.RED);
            }
        } else if (this.nd != null) {
            this.icon.setImage(
                    (this.nd.getType() == this.nd.ruter_t) ? Images_st.getRuter().getImage() :
                            (this.nd.getType() == this.nd.ruter_wi_fi_on_t) ? Images_st.getRuterWiFiOn().getImage() :
                                    (this.nd.getType() == this.nd.switch_l2_t) ? Images_st.getSwitchL2().getImage() :
                                            (this.nd.getType() == this.nd.switch_l3_t) ? Images_st.getSwitchL3().getImage() : null
            );
            this.note.setText(this.nd.getNotes());
            this.name.setText(this.nd.getName()); // wcześniej było note, poprawiłem na name
            if (this.nd.isRunnig()) {
                this.statusDotLabel.setText("Włączone:");
                this.dot.setFill(Color.LIMEGREEN);
            } else {
                this.statusDotLabel.setText("Wyłączone:");
                this.dot.setFill(Color.RED);
            }
        } else if (this.vm != null) {
            this.icon.setImage(
                    (this.vm.getType() == this.vm.windows_t) ? Images_st.getWindowsWorkStetion().getImage() :
                            (this.vm.getType() == this.vm.windows_server_t) ? Images_st.getWindowsServer().getImage() :
                                    (this.vm.getType() == this.vm.linux_t) ? Images_st.getLinuxWorkStetion().getImage() :
                                            (this.vm.getType() == this.vm.linux_server_t) ? Images_st.getLinuxServer().getImage() :
                                                    (this.vm.getType() == this.vm.ruter_t) ? Images_st.getRuter().getImage() :
                                                            (this.vm.getType() == this.vm.switch_l2_t) ? Images_st.getSwitchL2().getImage() :
                                                                    (this.vm.getType() == this.vm.switch_l3_t) ? Images_st.getSwitchL3().getImage() :
                                                                            (this.vm.getType() == this.vm.api_t) ? Images_st.getApi().getImage() :
                                                                                    (this.vm.getType() == this.vm.firewall_t) ? Images_st.getFirewall().getImage() :
                                                                                            (this.vm.getType() == this.vm.db_t) ? Images_st.getDb().getImage() : null
            );
            this.note.setText(this.vm.getNotes());
            this.name.setText(this.vm.getName());
            if (this.vm.isRunnig()) {
                this.statusDotLabel.setText("Włączone:");
                this.dot.setFill(Color.LIMEGREEN);
            } else {
                this.statusDotLabel.setText("Wyłączone:");
                this.dot.setFill(Color.RED);
            }
        } else {
            // brak ustawionego urządzenia -> placeholder
            this.icon.setImage(null);
            this.name.setText("Brak urządzenia");
            this.note.setText("");
            this.statusDotLabel.setText("");
            this.dot.setFill(Color.GRAY);
        }

        // ustawienie odstępów pomiędzy elementami i dodanie do HBox
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Ułożenie: [ikonka] [nazwa + opis (VBox)] [spacer] [przycisk IP] [status label + dot]
        VBox textBox = new VBox(2, name, note);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox statusBox = new HBox(6, statusDotLabel, dot);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        main.getChildren().setAll(icon, textBox, spacer, ipButton, statusBox);
        return main;
    }

    // metoda tworząca i pokazująca okienko z IP
    private void showIpsWindow() {
        String[] ipsArr = null;
        if (pc != null) ipsArr = pc.getIps();
        else if (ns != null) ipsArr = ns.getIps();
        else if (nd != null) ipsArr = nd.getIps();
        else if (vm != null) ipsArr = vm.getIps();

        Stage owner = null;
        if (main.getScene() != null && main.getScene().getWindow() instanceof Stage) {
            owner = (Stage) main.getScene().getWindow();
        }

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) dialog.initOwner(owner);

        dialog.setTitle("Adresy IP");

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setMinWidth(300);
        root.setStyle("-fx-background-color: #0b2b4a; -fx-border-color: #153e62; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label title = new Label("Adresy IP:");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        if (ipsArr == null) {
            Label none = new Label("Brak danych o adresach IP");
            none.setStyle("-fx-text-fill: #dfeeff;");
            root.getChildren().addAll(title, none);
        } else {
            // przefiltruj null/empty
            ObservableList<String> items = FXCollections.observableArrayList();
            for (String s : ipsArr) {
                if (s != null && !s.isBlank()) items.add(s);
            }

            if (items.isEmpty()) {
                Label none = new Label("Brak przypisanych adresów IP");
                none.setStyle("-fx-text-fill: #dfeeff;");
                root.getChildren().addAll(title, none);
            } else {
                ListView<String> lv = new ListView<>(items);
                lv.setPrefHeight(Math.min(200, items.size() * 28 + 10));
                lv.setStyle("-fx-control-inner-background: #072033; -fx-background-insets: 0; -fx-border-color: #153e62;");
                // styl komórek (tekst jasny)
                lv.setCellFactory(list -> {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setStyle("");
                            } else {
                                setText(item);
                                setStyle("-fx-text-fill: #dfeeff; -fx-font-size: 12px;");
                            }
                        }
                    };
                });
                root.getChildren().addAll(title, lv);
            }
        }

        Button close = new Button("Zamknij");
        close.setStyle("-fx-background-color: transparent; -fx-border-color: #2b6a9a; -fx-text-fill: #dfeeff;");
        close.setOnAction(e -> dialog.close());
        HBox btnBox = new HBox(close);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().add(btnBox);

        Scene scene = new Scene(root);
        // jeżeli używasz globalnego stylesheetu, możesz usunąć background z root i polegać na CSS
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
