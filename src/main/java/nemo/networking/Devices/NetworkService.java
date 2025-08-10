package nemo.networking.Devices;

public class NetworkService {
    private final String Name;
    private final int type;


    public NetworkService(String name, int type){
        this.type = type;
        this.Name = name;
    }

    public String getName(){ return this.Name; }
    public int getType() {return  this.type; }
}
