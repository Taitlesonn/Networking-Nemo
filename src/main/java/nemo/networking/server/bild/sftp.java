package nemo.networking.server.bild;

public class sftp extends Node_s {
    public sftp(){
        super();
        this.setHandler("save", event -> {
            JsonUtils.writeJsonToFile(this, this.getPath() + "sftp.json");
        });
    }
}
