package nemo.networking.server.bild;

public class snmp extends Node_s {
    public snmp(){
        super();

        this.setHandler("save", event -> {
            JsonUtils.writeJsonToFile(this, this.getPath() + "snmp.json");
        });
    }
}
