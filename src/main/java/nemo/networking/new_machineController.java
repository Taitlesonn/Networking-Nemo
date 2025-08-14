package nemo.networking;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nemo.networking.Devices.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class new_machineController {
    private final String info_t;
    private final int t;


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

        switch (this.t) {
            case Topologia.NetworkDevice_t -> {
                for (NetworkDevice n : Topologia.getNetworkDevices()) {
                    Inofs_devices_hbox idh = new Inofs_devices_hbox();
                    idh.setNd(n);
                    this.infos.getChildren().add(idh.render());
                }
            }
            case Topologia.NetworkService_t -> {
                for (NetworkService n : Topologia.getNetworkServices()) {
                    Inofs_devices_hbox idh = new Inofs_devices_hbox();
                    idh.setNs(n);
                    this.infos.getChildren().add(idh.render());
                }
            }
            case Topologia.PC_t -> {
                for (PC p : Topologia.getPcs()) {
                    Inofs_devices_hbox idh = new Inofs_devices_hbox();
                    idh.setPc(p);
                    this.infos.getChildren().add(idh.render());
                }
            }
            case Topologia.VM_t -> {
                for (VM v : Topologia.getVms()) {
                    Inofs_devices_hbox idh = new Inofs_devices_hbox();
                    idh.setVm(v);
                    this.infos.getChildren().add(idh.render());
                }
            }
        }
    }

}
