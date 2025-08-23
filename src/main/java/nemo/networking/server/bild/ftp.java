package nemo.networking.server.bild;

public class ftp extends Node_s {
    public ftp(){
        super();
        this.setHandler("save", event -> {
            JsonUtils.writeJsonToFile(this, this.getPath() + "ftp.json");
        });
    }
}
