package nemo.networking.Devices;

import java.util.Objects;

public class Connection {
    private final Dev dev1;
    private final Dev dev2;

    public Connection(Dev dev1, Dev dev2){
        Objects.requireNonNull(dev1, "dev1 cannot be null");
        Objects.requireNonNull(dev2, "dev2 cannot be null");
        this.dev1 = dev1;
        this.dev2 = dev2;
    }

    public boolean is_con(Dev dev1, Dev dev2){
        Objects.requireNonNull(dev1, "dev1 cannot be null");
        Objects.requireNonNull(dev2, "dev2 cannot be null");

        return this.dev1.equals(dev1) && this.dev2.equals(dev2);
    }

    public String get_name1() { return this.dev1.name(); }
    public String get_name2() { return this.dev2.name(); }
    public int get_type1() { return  this.dev1.type_dev(); }
    public int get_type2() { return this.dev2.type_dev(); }
}
