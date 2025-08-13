package nemo.networking;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class new_machineController {
    private final String info_t;
    @FXML private VBox menu;
    @FXML private Label info;


    public new_machineController(int type){
        switch (type){
            case Topologia.NetworkDevice_t -> this.info_t = "Network Devices";
            case Topologia.NetworkService_t -> this.info_t = "Network Services";
            case Topologia.PC_t -> this.info_t = "Server or Work Stacion";
            case Topologia.VM_t -> this.info_t = "VM";
            default -> this.info_t = "Error type device";
        }
    }

    @FXML
    private void initialize(){
        this.info.setText(this.info_t);
    }
}
