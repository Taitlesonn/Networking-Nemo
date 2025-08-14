package nemo.networking.Devices;

public class PC extends root_dev{
    public final int windows_t = 0;
    public final int linux_t = 1;
    public final int linux_server_t = 2;
    public final int windows_server_t = 3;

    public  PC(String name, int type){
        setName(name);
        setType(type);
    }

}
