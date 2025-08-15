package nemo.networking;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;

public class New_b_Conntroler {

    @FXML private Button vm;
    @FXML private Button ns;
    @FXML private Button nd;
    @FXML private Button pc;
    @FXML private Button submit;
    @FXML private VBox center;
    private int type_r = 0;

    // przechowujemy do 4 adresów IP (zainicjalizowane w initialize)
    private final java.util.List<String> ips2 = new java.util.ArrayList<>();

    private final TextField name = new TextField();
    private final TextField note = new TextField();
    private final ComboBox<String> how_many_ips = new ComboBox<>();
    private final ComboBox<String> device_type  = new ComboBox<>();


    private static final Map<String, Integer> VM_MAP = Map.ofEntries(
            Map.entry("Windows", 0),
            Map.entry("Linux", 1),
            Map.entry("Windows Server", 3),
            Map.entry("Linux Server", 2),
            Map.entry("Router", 4),
            Map.entry("Switch l2", 5),
            Map.entry("Switch l3", 6),
            Map.entry("Firewall", 7),
            Map.entry("Database", 8),
            Map.entry("API", 9)
    );

    private static final Map<String, Integer> PC_MAP = Map.ofEntries(
            Map.entry("Windows", 0),
            Map.entry("Linux", 1),
            Map.entry("Windows Server", 3),
            Map.entry("Linux Server", 2)
    );

    private static final Map<String, Integer> NS_MAP = Map.ofEntries(
            Map.entry("Firewall", 0),
            Map.entry("Database", 1),
            Map.entry("API", 2),
            Map.entry("WAN", 3),
            Map.entry("Windows", 4)
    );

    private static final Map<String, Integer> ND_MAP = Map.ofEntries(
            Map.entry("Router", 0),
            Map.entry("Router (wifi)", 1),
            Map.entry("Switch l2", 2),
            Map.entry("Switch l3", 3)
    );


    private VBox render_ip(int n) {
        VBox ret = new VBox(6);
        if (n < 1) return ret;
        if (n > 4) n = 4;

        for (int i = 0; i < n; i++) {
            if (i >= ips2.size()) ips2.add("-");
            else ips2.set(i, "-");

            TextField ip = new TextField();
            ip.setPromptText("Enter IP:");
            final int finalI = i;

            ip.textProperty().addListener((obs, oldVal, newVal) -> ips2.set(finalI, newVal));
            ret.getChildren().add(ip);
        }
        return ret;
    }


    private void render() {
        this.center.getChildren().clear();
        this.how_many_ips.getItems().clear();
        this.device_type.getItems().clear();

        this.name.setPromptText("Enter a name:");
        this.note.setPromptText("Note:");


        this.how_many_ips.getItems().addAll("One IP", "Two IPs", "Three IPs", "Four IPs");


        final VBox ipContainer = new VBox(6);

        Map<String, Integer> currentMap;
        switch (this.type_r) {
            case 1 -> {
                this.device_type.getItems().addAll(PC_MAP.keySet());
                currentMap = PC_MAP;
            }
            case 2 -> {
                this.device_type.getItems().addAll(NS_MAP.keySet());
                currentMap = NS_MAP;
            }
            case 3 -> {
                this.device_type.getItems().addAll(ND_MAP.keySet());
                currentMap = ND_MAP;
            }
            default -> {
                this.device_type.getItems().addAll(VM_MAP.keySet());
                currentMap = VM_MAP;
            }
        }

        // Listener dla wyboru ile IP — aktualizuje ipContainer
        this.how_many_ips.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            ipContainer.getChildren().clear();
            if (newVal == null) return;
            int n = switch (newVal) {
                case "One IP" -> 1;
                case "Two IPs" -> 2;
                case "Three IPs" -> 3;
                case "Four IPs" -> 4;
                default -> 0;
            };
            if (n > 0) ipContainer.getChildren().add(render_ip(n));
        });


        this.center.getChildren().addAll(this.name, this.note, this.device_type, this.how_many_ips, ipContainer);
    }

    @FXML
    private void submit() {
        String name = this.name.getText();
        if (name == null || name.isBlank()) {
            showError("INVALID INPUT", "Enter a name");
            return;
        }

        String typ = this.device_type.getValue();
        if (typ == null || typ.isBlank()) {
            showError("INVALID INPUT", "Choose a device type");
            return;
        }

        String notes = this.note.getText();


        Map<String, Integer> currentMap = switch (this.type_r) {
            case 1 -> PC_MAP;
            case 2 -> NS_MAP;
            case 3 -> ND_MAP;
            default -> VM_MAP;
        };

        int tt = getTypeCode(typ, currentMap);
        if (tt == -1) return;


        boolean created = false;
        switch (this.type_r) {
            case 0 -> created = Topologia.add_vm(name, tt);
            case 1 -> created = Topologia.add_pc(name, tt);
            case 2 -> created = Topologia.add_network_service(name, tt);
            case 3 -> created = Topologia.add_network_device(name, tt);
        }

        for (int i = 0; i < ips2.size(); i++){
            if(!ips2.get(i).isEmpty()){
                int topologyType = switch (this.type_r) {
                    case 1 -> Topologia.PC_t;
                    case 2 -> Topologia.NetworkService_t;
                    case 3 -> Topologia.NetworkDevice_t;
                    default -> Topologia.VM_t;
                };
                boolean success = Topologia.add_ip(name, topologyType, ips2.get(i), i);
                if (!success) {
                    showError("Invalid IP", "Invalid IP");
                }

            }
        }

        if (created) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("SUBMIT");
            alert.setHeaderText("Success");
            alert.setContentText("You successfully created a new item");
            alert.showAndWait();

            if (notes != null && !notes.isBlank()) {
                int topologyType = switch (this.type_r) {
                    case 1 -> Topologia.PC_t;
                    case 2 -> Topologia.NetworkService_t;
                    case 3 -> Topologia.NetworkDevice_t;
                    default -> Topologia.VM_t;
                };
                Topologia.add_note(name, topologyType, notes);
            }

        } else {
            showError("SUBMIT FAILED", "Could not create the item (already exist)");
        }
    }

    private int getTypeCode(String typ, Map<String, Integer> map) {
        Integer code = map.get(typ);
        if (code == null) {
            showError("INVALID INPUT", "Unknown device type: " + typ);
            return -1;
        }
        return code;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("ERROR");
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        this.vm.setMaxWidth(Double.MAX_VALUE);
        this.ns.setMaxWidth(Double.MAX_VALUE);
        this.nd.setMaxWidth(Double.MAX_VALUE);
        this.pc.setMaxWidth(Double.MAX_VALUE);

        // zainicjalizuj listę ips2 na 4 elementy
        for (int i = 0; i < 4; i++) this.ips2.add("-");

        // domyślny render
        this.render();

        // przyciski zmieniające tryb i render
        this.vm.setOnAction(e -> { this.type_r = 0; this.render(); });
        this.pc.setOnAction(e -> { this.type_r = 1; this.render(); });
        this.ns.setOnAction(e -> { this.type_r = 2; this.render(); });
        this.nd.setOnAction(e -> { this.type_r = 3; this.render(); });
    }
}
