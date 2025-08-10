package nemo.networking.Devices;

public class NetworkDevice {
    private final String Name;
    private final int type;


    public NetworkDevice(String name, int type){
        this.Name = name;
        this.type = type;
    }

    public int getType() { return type; }

    public String getName() { return Name; }
}
