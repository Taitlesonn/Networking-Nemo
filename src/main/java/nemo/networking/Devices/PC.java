package nemo.networking.Devices;

public class PC {
    private final int system;
    private final String Name;


    public  PC(String name, int system){
        this.Name = name;
        this.system = system;
    }

    public int getSystem() {
        return system;
    }

    public String getName() {
        return Name;
    }
}
