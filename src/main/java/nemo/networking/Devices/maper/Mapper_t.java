package nemo.networking.Devices.maper;

import nemo.networking.Topologia;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Mapper_t {
    private record Point(int x, int y){ }

    private final Random rand = new Random();
    private final Map<String, List<? super Object>> devices = new ConcurrentHashMap<>();
    private final Map<Object, Point> pointMap = new ConcurrentHashMap<>();

    private final String NetworkDevice_s = "nd";
    private final String NetworkService_s = "ns";
    private final String PC_s = "pc";
    private final String VM_s = "vm";

    public Mapper_t() {
        devices.put(NetworkDevice_s, new ArrayList<>());
        devices.put(NetworkService_s, new ArrayList<>());
        devices.put(PC_s, new ArrayList<>());
        devices.put(VM_s, new ArrayList<>());
    }

    public Boolean move_o(int new_x, int new_y, Object o){
        Objects.requireNonNull(o, "NULL Object in Mapper");
        if(!pointMap.containsKey(o)){
            return false;
        }
        Point p = new Point(new_x, new_y);
        if(pointMap.containsValue(p)){
            return false;
        }
        pointMap.replace(o, p);
        return true;
    }


    public boolean remove_device(int type, Object o){
        switch (type){
            case Topologia.NetworkDevice_t -> {
                if(devices.get(NetworkDevice_s).contains(o) && pointMap.containsKey(o)){
                    devices.get(NetworkDevice_s).remove(o);
                    pointMap.remove(o);
                    return true;
                }
            }
            case Topologia.NetworkService_t -> {
                if(devices.get(NetworkService_s).contains(o) && pointMap.containsKey(o)){
                    devices.get(NetworkService_s).remove(o);
                    pointMap.remove(o);
                    return true;
                }
            }
            case Topologia.PC_t -> {
                if(devices.get(PC_s).contains(o) && pointMap.containsKey(o)){
                    devices.get(PC_s).remove(o);
                    pointMap.remove(o);
                    return true;
                }
            }
            case Topologia.VM_t -> {
                if(devices.get(VM_s).contains(o) && pointMap.containsKey(o)){
                    devices.get(VM_s).remove(o);
                    pointMap.remove(o);
                    return true;
                }
            }
        }
        return false;
    }


    public boolean add_device(int type, Object o){
        Objects.requireNonNull(o, "NULL objet in Mapper_t");
        int c = 0;
        int los_x;
        int los_y;

        while (true){
            if(c >= 1000){
                return false;
            }
            los_x = this.rand.nextInt(1000);
            los_y = this.rand.nextInt(1000);

            if(!pointMap.containsValue(new Point(los_x, los_y))){ break; }
            c++;
        }

        switch (type){
            case Topologia.NetworkDevice_t -> {
                    devices.get(NetworkDevice_s).add(o);
                    pointMap.put(o, new Point(los_x, los_y));
                    return true;
            }
            case Topologia.NetworkService_t -> {
                devices.get(NetworkService_s).add(o);
                pointMap.put(o, new Point(los_x, los_y));
                return true;
            }
            case Topologia.PC_t -> {
                devices.get(PC_s).add(o);
                pointMap.put(o, new Point(los_x, los_y));
                return true;
            }
            case Topologia.VM_t -> {
                devices.get(VM_s).add(o);
                pointMap.put(o, new Point(los_x, los_y));
                return true;
            }
        }

        return false;
    }
}
