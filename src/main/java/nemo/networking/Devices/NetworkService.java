package nemo.networking.Devices;

public class NetworkService extends root_dev {
    public final int firewall_t = 0;
    public final int db_t = 1;
    public final int api_t = 2;
    public final int wan_cloud_t = 3;

    public NetworkService(String name, int type){
        setName(name);
        setType(type);
    }
}
