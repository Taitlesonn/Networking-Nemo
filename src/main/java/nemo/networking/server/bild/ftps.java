package nemo.networking.server.bild;

public class ftps extends Node_s {
    public ftps(){
        super();
        this.setHandler("save", event -> {
            JsonUtils.writeJsonToFile(this, this.getPath() + "ftps.json");
        });
    }
}
