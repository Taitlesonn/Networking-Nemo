package nemo.networking.server.bild;

public class WinRc extends Node_s {
    public WinRc(){
        super();
        this.setHandler("save", event -> {
            JsonUtils.writeJsonToFile(this, this.getPath() + "winrc.json");
        });
    }
}
