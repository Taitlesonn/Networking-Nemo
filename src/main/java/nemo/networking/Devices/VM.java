package nemo.networking.Devices;

public class VM extends root_dev{
    public final int windows_t = 0;
    public final int linux_t = 1;
    public final int linux_server_t = 2;
    public final int windows_server_t = 3;
    public final int ruter_t = 4;
    public final  int switch_l2_t = 5;
    public final int switch_l3_t = 6;
    public final int firewall_t = 7;
    public final int db_t = 8;
    public final int api_t = 9;

    public VM(String name, int type){
        setName(name);
        setType(type);
    }

}
