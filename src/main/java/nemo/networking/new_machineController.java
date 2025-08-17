package nemo.networking;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nemo.networking.Devices.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class new_machineController {
    private final String info_t;
    private final int t;

    // executor na wirtualnych wątkach (Java 21)
    private final ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

    // zachowujemy listę IDH jeśli kiedyś będziesz chciał je odwołać/refreshować
    private final List<Inofs_devices_hbox> idhList = new ArrayList<>();

    @FXML private VBox menu;
    @FXML private Label info;
    @FXML private VBox infos;


    public new_machineController(int type){
        this.t = type;
        switch (type){
            case Topologia.NetworkDevice_t -> this.info_t = "Network Devices";
            case Topologia.NetworkService_t -> this.info_t = "Network Services";
            case Topologia.PC_t -> this.info_t = "Server or Work Stacion";
            case Topologia.VM_t -> this.info_t = "VM";
            default -> this.info_t = "Error type device";
        }
    }

    @FXML
    private void initialize() {
        this.info.setText(this.info_t);
        this.infos.getChildren().clear();
        this.idhList.clear();

        switch (this.t) {
            case Topologia.NetworkDevice_t -> {
                for (NetworkDevice n : Topologia.getNetworkDevices()) {
                    Inofs_devices_hbox idh = new Inofs_devices_hbox();
                    idh.setNd(n);
                    HBox box = idh.render();
                    this.infos.getChildren().add(box);
                    idhList.add(idh);

                    idh.startStatusCheck(Topologia.NetworkDevice_t, n.getName(), executor);
                }
            }
            case Topologia.NetworkService_t -> {
                for (NetworkService n : Topologia.getNetworkServices()) {
                    Inofs_devices_hbox idh = new Inofs_devices_hbox();
                    idh.setNs(n);
                    HBox box = idh.render();
                    this.infos.getChildren().add(box);
                    idhList.add(idh);

                    idh.startStatusCheck(Topologia.NetworkService_t, n.getName(), executor);
                }
            }
            case Topologia.PC_t -> {
                for (PC p : Topologia.getPcs()) {
                    Inofs_devices_hbox idh = new Inofs_devices_hbox();
                    idh.setPc(p);
                    HBox box = idh.render();
                    this.infos.getChildren().add(box);
                    idhList.add(idh);

                    idh.startStatusCheck(Topologia.PC_t, p.getName(), executor);
                }
            }
            case Topologia.VM_t -> {
                for (VM vm : Topologia.getVms()) {
                    Inofs_devices_hbox idh = new Inofs_devices_hbox();
                    idh.setVm(vm);
                    HBox box = idh.render();
                    this.infos.getChildren().add(box);
                    idhList.add(idh);

                    idh.startStatusCheck(Topologia.VM_t, vm.getName(), executor);
                }
            }
        }
    }

    /**
     * Zatrzymaj wszystkie trwające sprawdzenia (opcjonalnie) i wyłącz executor.
     * Wywołaj przy zamykaniu aplikacji/okna.
     */
    public void shutdownExecutor() {
        // anuluj sprawdzenia w idh (opcjonalne)
        for (Inofs_devices_hbox idh : idhList) {
            try { idh.cancelStatusCheck(); } catch (Throwable ignore) {}
        }
        executor.shutdownNow();
    }
}
