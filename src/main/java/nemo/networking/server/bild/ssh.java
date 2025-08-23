package nemo.networking.server.bild;

public class ssh extends Node_s {
    public ssh(){
        super();
        this.setHandler("save", event -> {
            JsonUtils.writeJsonToFile(this, this.getPath() + "ssh.json");
        });
    }
}
