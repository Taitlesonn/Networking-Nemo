package nemo.networking;
import nemo.networking.Devices.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Topologia {
    //Type
    public static final int NetworkDevice_t = 1;
    public static final int NetworkService_t = 2;
    public static final int PC_t = 3;
    public static final int VM_t = 4;

    private static final List<NetworkDevice> networkDevices = new ArrayList<>();
    private static final List<NetworkService> networkServices = new ArrayList<>();
    private static final List<PC> pcs = new ArrayList<PC>();
    private static final List<VM> vms = new ArrayList<VM>();
    private static final List<Connection> connections = new ArrayList<>();


    private static boolean existsByNameAndType(String name, int type) {
        return switch (type) {
            case Topologia.NetworkDevice_t ->
                    Topologia.networkDevices.stream().anyMatch(nd -> name.equals(nd.getName()));
            case Topologia.NetworkService_t ->
                    Topologia.networkServices.stream().anyMatch(ns -> name.equals(ns.getName()));
            case Topologia.PC_t -> Topologia.pcs.stream().anyMatch(pc -> name.equals(pc.getName()));
            case Topologia.VM_t -> Topologia.vms.stream().anyMatch(vm -> name.equals(vm.getName()));
            default -> false; // nieznany typ
        };
    }

    public static boolean add_network_device(String Name, int type) {
        Objects.requireNonNull(Name, "Network device have to have a name !");
        for(int i = 0; i < Topologia.networkDevices.size(); i++){
            if(Objects.equals(Topologia.networkDevices.get(i).getName(), Name)){
                return false;
            }
        }
        Topologia.networkDevices.add(new NetworkDevice(Name, type));
        return true;
    }

    public static boolean add_network_service(String Name, int type) {
        Objects.requireNonNull(Name, "Netowrk service have to have name");
        for(int i = 0; i < Topologia.networkServices.size(); i++){
            if(Objects.equals(Topologia.networkServices.get(i).getName(), Name)){
                return false;
            }
        }
        Topologia.networkServices.add(new NetworkService(Name, type));
        return true;
    }

    public static boolean add_pc(String Name, int system) {
        Objects.requireNonNull(Name, "pc have to have name");
        for(int i = 0; i < Topologia.pcs.size(); i++){
            if(Objects.equals(Topologia.pcs.get(i).getName(), Name)){
                return false;
            }
        }
        Topologia.pcs.add(new PC(Name, system));
        return true;

    }

    public static boolean add_vm(String Name, int system) {
        Objects.requireNonNull(Name, "vm have to have name");
        for(int i = 0; i < Topologia.vms.size(); i++){
            if(Objects.equals(Topologia.vms.get(i).getName(), Name)){
                return false;
            }
        }
        Topologia.vms.add(new VM(Name, system));
        return true;
    }

    public static boolean add_connection(String name1, String name2, int t1, int t2) {
        Objects.requireNonNull(name1, "name 1 cannot be null");
        Objects.requireNonNull(name2, "name 2 cannot be null");

        boolean exists1 = existsByNameAndType(name1, t1);
        boolean exists2 = existsByNameAndType(name2, t2);

        if (exists1 && exists2) {
            Topologia.connections.add(new Connection(new Dev(name1, t1), new Dev(name2, t2)));
            return true;
        }
        return false;
    }

    public static boolean delete_connection(String name1, String name2, int t1, int t2){
        Objects.requireNonNull(name1, "name 1 cannot be null");
        Objects.requireNonNull(name2, "name 2 cannot be null");

        boolean existes1 = Topologia.existsByNameAndType(name1, t1);
        boolean existes2 = Topologia.existsByNameAndType(name2, t2);
        if(existes1 && existes2){
            for (int i = 0; i < Topologia.connections.size(); i++){
                if ((Objects.equals(Topologia.connections.get(i).get_name1(), name1) &&
                     Objects.equals(Topologia.connections.get(i).get_name2(), name2) &&
                     Objects.equals(Topologia.connections.get(i).get_type1(), t1) &&
                     Objects.equals(Topologia.connections.get(i).get_type2(), t2))
                        ||
                     (Objects.equals(Topologia.connections.get(i).get_name1(), name2) &&
                      Objects.equals(Topologia.connections.get(i).get_name2(), name1) &&
                      Objects.equals(Topologia.connections.get(i).get_type1(), t2) &&
                      Objects.equals(Topologia.connections.get(i).get_type2(), t1))
                ){
                    Topologia.connections.remove(i);
                    return  true;
                }
            }
        }
        return false;
    }

    public static boolean delete_network_device(String Name){
        Objects.requireNonNull(Name, "Null Name");
        for(int i = 0; i < Topologia.networkDevices.size(); i++){
            if(Objects.equals(Topologia.networkDevices.get(i).getName(), Name)){
                Topologia.networkDevices.remove(i);
                return true;
            }
        }
        return false;
    }

    public static boolean delete_network_service(String Name){
        Objects.requireNonNull(Name, "Null Name");
        for(int i = 0; i < Topologia.networkServices.size(); i++){
            if(Objects.equals(Topologia.networkServices.get(i).getName(), Name)){
                Topologia.networkServices.remove(i);
                return true;
            }
        }
        return false;
    }

    public static boolean delete_pc(String Name){
        Objects.requireNonNull(Name, "Null Name");
        for(int i = 0; i < Topologia.pcs.size(); i++){
            if(Objects.equals(Topologia.pcs.get(i).getName(), Name)){
                Topologia.pcs.remove(i);
                return true;
            }
        }
        return false;
    }

    public static boolean delete_vm(String Name){
        Objects.requireNonNull(Name, "Null Name");
        for(int i = 0; i < Topologia.vms.size(); i++){
            if(Objects.equals(Topologia.vms.get(i).getName(), Name)){
                Topologia.vms.remove(i);
                return true;
            }
        }
        return false;
    }

    public static boolean set_runing(int t, String name, boolean s){
        switch (t){
            case Topologia.NetworkDevice_t -> { for (NetworkDevice n : Topologia.networkDevices) if(Objects.equals(n.getName(), name)) {n.setRunnig(s); return true;} }
            case Topologia.NetworkService_t -> { for (NetworkService n : Topologia.networkServices) if(Objects.equals(n.getName(), name)) {n.setRunnig(s); return true; } }
            case Topologia.PC_t -> { for (PC p : Topologia.pcs) if(Objects.equals(p.getName(), name)) {p.setRunnig(s); return true; } }
            case Topologia.VM_t -> { for (VM v : Topologia.vms) if(Objects.equals(v.getName(), name)) {v.setRunnig(s); return true; } }
        }
        return false;
    }
    public static boolean is_runnig(int t, String name){
        switch (t){
            case Topologia.NetworkDevice_t -> { for (NetworkDevice n : Topologia.networkDevices) if(Objects.equals(n.getName(), name)) return n.isRunnig();}
            case Topologia.NetworkService_t -> { for (NetworkService n : Topologia.networkServices) if(Objects.equals(n.getName(), name)) return n.isRunnig(); }
            case Topologia.PC_t -> { for (PC p : Topologia.pcs) if(Objects.equals(p.getName(), name)) return p.isRunnig(); }
            case Topologia.VM_t -> { for (VM v : Topologia.vms) if(Objects.equals(v.getName(), name)) return v.isRunnig(); }
        }
        return false;
    }

    public static int how_much_runnig_all(){
        int cunt = 0;
        for (NetworkService n : Topologia.networkServices) if(n.isRunnig()) cunt++;
        for (NetworkDevice n : Topologia.networkDevices) if(n.isRunnig()) cunt++;
        for (PC p : Topologia.pcs) if(p.isRunnig()) cunt++;
        for (VM v : Topologia.vms) if (v.isRunnig()) cunt++;
        return  cunt;
    }

}
