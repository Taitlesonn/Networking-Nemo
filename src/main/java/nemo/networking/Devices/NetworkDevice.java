package nemo.networking.Devices;

public class NetworkDevice extends root_dev{
    public final int ruter_t = 0;
    public final int ruter_wi_fi_on_t = 1;
    public final  int switch_l2_t = 2;
    public final int switch_l3_t = 3;


    public NetworkDevice(String name, int type){
        setName(name);
        setType(type);
    }

}
