package nemo.networking;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
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

        // Ułożenie: [ikonka] [nazwa + opis (VBox)] [spacer] [status label + dot]
        VBox textBox = new VBox(2, name, note);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox statusBox = new HBox(6, statusDotLabel, dot);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        main.getChildren().setAll(icon, textBox, spacer, statusBox);
        return main;
    }
}
