package nemo.networking.Devices;

public class VM {
    private final int system;
    private final String Name;


    public VM(String name, int system){
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
