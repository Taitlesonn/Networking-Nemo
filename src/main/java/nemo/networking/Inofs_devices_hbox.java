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
import nemo.networking.Devices.*;

public class Inofs_devices_hbox {
    private final HBox main;
    private final ImageView icon = new ImageView();
    private final Label note = new Label();
    private final Label name = new Label();
    private final Label statusDotLabel = new Label();
    private final Circle dot = new Circle(6);

    private NetworkDevice nd = null;
    private NetworkService ns = null;
    private PC pc = null;
    private VM vm = null;


    private final Button ipButton = new Button("IP");

    private static class IPEntry {
        int index;
        String ip;
        IPEntry(int index, String ip) { this.index = index; this.ip = ip; }
        @Override
        public String toString() { return index + ": " + ip; }
    }


    public Inofs_devices_hbox() {
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


        dot.setStroke(Color.web("#1a3a5a"));
        dot.setStrokeWidth(1.0);


        main.setBackground(new Background(new BackgroundFill(Color.web("#0b2b4a"), new CornerRadii(8), Insets.EMPTY)));

        DropShadow shadow = new DropShadow();
        shadow.setRadius(6);
        shadow.setOffsetY(2);
        shadow.setColor(Color.color(0,0,0,0.6));
        main.setEffect(shadow);


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
        main.getChildren().clear();

        if (this.pc != null) {
            this.icon.setImage(
                    (this.pc.getType() == this.pc.windows_t) ? Images_st.getWindowsWorkStetion().getImage() :
                            (this.pc.getType() == this.pc.windows_server_t) ? Images_st.getWindowsServer().getImage() :
                                    (this.pc.getType() == this.pc.linux_t) ? Images_st.getLinuxWorkStetion().getImage() :
                                            (this.pc.getType() == this.pc.linux_server_t) ? Images_st.getLinuxServer().getImage() : null
            );
            this.note.setText(this.pc.getNotes());
            this.name.setText(this.pc.getName());
            if (Topologia.is_runnig(Topologia.PC_t, this.pc.getName())) {
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
            if (Topologia.is_runnig(Topologia.NetworkService_t, this.ns.getName())) {
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
            this.name.setText(this.nd.getName());
            if (Topologia.is_runnig(Topologia.NetworkDevice_t, this.nd.getName())) {
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
            if (Topologia.is_runnig(Topologia.VM_t, this.vm.getName())) {
                this.statusDotLabel.setText("Włączone:");
                this.dot.setFill(Color.LIMEGREEN);
            } else {
                this.statusDotLabel.setText("Wyłączone:");
                this.dot.setFill(Color.RED);
            }
        } else {
            this.icon.setImage(null);
            this.name.setText("Brak urządzenia");
            this.note.setText("");
            this.statusDotLabel.setText("");
            this.dot.setFill(Color.GRAY);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox textBox = new VBox(2, name, note);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox statusBox = new HBox(6, statusDotLabel, dot);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        main.getChildren().setAll(icon, textBox, spacer, ipButton, statusBox);
        return main;
    }

    private void showIpsWindow() {
        String[] ipsArr;
        if (pc != null) ipsArr = pc.getIps();
        else if (ns != null) ipsArr = ns.getIps();
        else if (nd != null) ipsArr = nd.getIps();
        else if (vm != null) ipsArr = vm.getIps();
        else {
            ipsArr = null;
        }

        nemo.networking.Devices.root_dev base;
        if (pc != null) base = pc;
        else if (ns != null) base = ns;
        else if (nd != null) base = nd;
        else if (vm != null) base = vm;
        else base = null;

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
        root.setMinWidth(340);
        root.setStyle("-fx-background-color: #0b2b4a; -fx-border-color: #153e62; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label title = new Label("Adresy IP:");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        root.getChildren().add(title);

        if (base == null) {
            Label none = new Label("Brak danych o adresach IP");
            none.setStyle("-fx-text-fill: #dfeeff;");
            root.getChildren().add(none);
        } else {
            // policz niepuste wpisy
            int nonEmptyCount = 0;
            if (ipsArr != null) {
                for (String s : ipsArr) {
                    if (s != null && !s.isBlank()) nonEmptyCount++;
                }
            }

            ObservableList<IPEntry> items = FXCollections.observableArrayList();
            if (ipsArr != null) {
                for (int i = 0; i < ipsArr.length; i++) {
                    String s = ipsArr[i];
                    if (s != null && !s.isBlank()) items.add(new IPEntry(i, s));
                }
            }

            if (items.isEmpty()) {
                Label none = new Label("Brak przypisanych adresów IP");
                none.setStyle("-fx-text-fill: #dfeeff;");
                root.getChildren().add(none);
            } else {
                ListView<IPEntry> lv = getIpEntryListView(items, base);
                root.getChildren().add(lv);
            }

            // --- kontrolki dodawania nowego IP (pokazywane zawsze jeśli mamy `base`)
            HBox addBox = new HBox(8);
            addBox.setAlignment(Pos.CENTER_RIGHT);

            TextField newIpTf = new TextField();
            newIpTf.setPromptText("np. 192.168.0.1");
            newIpTf.setStyle("-fx-control-inner-background: #072033; -fx-text-fill: #dfeeff;");
            newIpTf.setPrefWidth(180);

            Button addBtn = new Button("Dodaj IP");
            addBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2b6a9a; -fx-text-fill: #dfeeff;");

            // przycisk aktywny tylko gdy nie przekroczono limitu 4 (0 też jest <4 więc też pozwoli dodać)
            addBtn.setDisable(nonEmptyCount >= 4);

            addBtn.setOnAction(e -> {
                String newIp = newIpTf.getText() == null ? "" : newIpTf.getText().trim();
                if (newIp.isEmpty()) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.initOwner(dialog);
                    a.setTitle("Błąd");
                    a.setHeaderText("Brak adresu");
                    a.setContentText("Wpisz adres IP przed dodaniem.");
                    a.showAndWait();
                    return;
                }

                // znajdź pierwszy wolny indeks (jeśli brak miejsc, użyj długości tablicy)
                int idx = findFirstEmptyIndex(ipsArr);
                boolean ok = base.add_ip(idx, newIp);
                if (!ok) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.initOwner(dialog);
                    a.setTitle("Błąd walidacji");
                    a.setHeaderText("Nieprawidłowy adres IP lub indeks");
                    a.setContentText("Sprawdź format adresu IPv4 (np. 192.168.0.1).");
                    a.showAndWait();
                } else {
                    // odśwież okno: zamknij i otwórz ponownie aby pobrać aktualne dane
                    dialog.close();
                    showIpsWindow();
                }
            });

            addBox.getChildren().addAll(newIpTf, addBtn);
            root.getChildren().add(addBox);
        }

        Button close = new Button("Zamknij");
        close.setStyle("-fx-background-color: transparent; -fx-border-color: #2b6a9a; -fx-text-fill: #dfeeff;");
        close.setOnAction(e -> dialog.close());
        HBox btnBox = new HBox(close);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().add(btnBox);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Zwraca pierwszy indeks w tablicy, który jest null lub pusty;
     * jeśli nie znaleziono takiego miejsca, zwraca arr.length (czyli dopisanie na końcu).
     * Jeśli arr == null, zwraca 0.
     */
    private int findFirstEmptyIndex(String[] arr) {
        if (arr == null) return 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null || arr[i].isBlank()) return i;
        }
        return arr.length;
    }


    private ListView<IPEntry> getIpEntryListView(ObservableList<IPEntry> items, root_dev base) {
        ListView<IPEntry> lv = new ListView<>(items);
        lv.setPrefHeight(Math.min(240, items.size() * 30 + 10));
        lv.setStyle("-fx-control-inner-background: #072033; -fx-background-insets: 0; -fx-border-color: #153e62;");
        lv.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(IPEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: #dfeeff; -fx-font-size: 12px;");
                }
            }
        });

        lv.setOnMouseClicked(evt -> {
            IPEntry sel = lv.getSelectionModel().getSelectedItem();
            if (sel != null) {
                openEditIpDialog(sel, lv, base);
            }
        });
        return lv;
    }

    private void openEditIpDialog(IPEntry entry, ListView<IPEntry> lv, nemo.networking.Devices.root_dev base) {
        Stage edit = new Stage();
        edit.initStyle(StageStyle.UTILITY);
        edit.initModality(Modality.APPLICATION_MODAL);
        if (main.getScene() != null && main.getScene().getWindow() instanceof Stage) {
            edit.initOwner((Stage) main.getScene().getWindow());
        }
        edit.setTitle("Edytuj IP — " + entry.index);
        edit.setWidth(300);
        edit.setHeight(500);
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #0b2b4a; -fx-border-color: #153e62; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label lbl = new Label("Edytuj adres IP:");
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        TextField tf = new TextField(entry.ip);
        tf.setPromptText("np. 192.168.0.1");
        tf.setStyle("-fx-control-inner-background: #072033; -fx-text-fill: #dfeeff;");

        Label info = new Label("Indeks: " + entry.index);
        info.setStyle("-fx-text-fill: #dfeeff; -fx-font-size: 11px;");

        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button save = new Button("Zapisz");
        Button cancel = new Button("Anuluj");

        save.setStyle("-fx-background-color: transparent; -fx-border-color: #2b6a9a; -fx-text-fill: #dfeeff;");
        cancel.setStyle("-fx-background-color: transparent; -fx-border-color: #2b6a9a; -fx-text-fill: #dfeeff;");

        buttons.getChildren().addAll(cancel, save);

        // Przycisk Dodaj IP — tylko jeśli liczba IP <= 4
        if (base.getIpCount() <= 4) { // zakładam, że masz metodę getIpCount()
            Button add = new Button("Dodaj IP");
            add.setStyle("-fx-background-color: transparent; -fx-border-color: #2b6a9a; -fx-text-fill: #dfeeff;");
            add.setOnAction(e -> {
                String newIp = tf.getText() == null ? "" : tf.getText().trim();
                boolean ok = base.add_ip(entry.index + 1, newIp); // lub inny indeks dodania
                if (!ok) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.initOwner(edit);
                    a.setTitle("Błąd walidacji");
                    a.setHeaderText("Nieprawidłowy adres IP");
                    a.setContentText("Sprawdź format adresu IPv4 (np. 192.168.0.1).");
                    a.showAndWait();
                } else {
                    lv.refresh();
                    edit.close();
                }
            });
            buttons.getChildren().add(add);
        }

        box.getChildren().addAll(lbl, tf, info, buttons);

        Scene sc = new Scene(box);
        edit.setScene(sc);

        cancel.setOnAction(e -> edit.close());

        save.setOnAction(e -> {
            String newIp = tf.getText() == null ? "" : tf.getText().trim();
            boolean ok = base.add_ip(entry.index, newIp);
            if (!ok) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.initOwner(edit);
                a.setTitle("Błąd walidacji");
                a.setHeaderText("Nieprawidłowy adres IP");
                a.setContentText("Sprawdź format adresu IPv4 (np. 192.168.0.1).");
                a.showAndWait();
            } else {
                entry.ip = newIp;
                lv.refresh();
                edit.close();
            }
        });

        edit.showAndWait();
    }



}
