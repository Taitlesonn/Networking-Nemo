package nemo.networking;
import nemo.networking.Devices.*;
import nemo.networking.Devices.maper.Mapper_t;

import java.util.ArrayList;
import java.util.Collections;
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

    private static Mapper_t center_m = null;


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
        NetworkDevice n = new NetworkDevice(Name, type);
        Topologia.networkDevices.add(n);
        if(Topologia.center_m != null){
            if(!Topologia.center_m.add_device(Topologia.NetworkDevice_t, n)){
                System.err.println("Mapper center ERROR in Network Device");
            }
        }
        return true;
    }

    public static boolean add_network_service(String Name, int type) {
        Objects.requireNonNull(Name, "Netowrk service have to have name");
        for(int i = 0; i < Topologia.networkServices.size(); i++){
            if(Objects.equals(Topologia.networkServices.get(i).getName(), Name)){
                return false;
            }
        }
        NetworkService n = new NetworkService(Name, type);
        Topologia.networkServices.add(n);
        if(Topologia.center_m != null){
            if(!Topologia.center_m.add_device(Topologia.NetworkService_t, n)){
                System.err.println("Mapper center ERROR in Network Service");
            }
        }
        return true;
    }

    public static boolean add_pc(String Name, int system) {
        Objects.requireNonNull(Name, "pc have to have name");
        for(int i = 0; i < Topologia.pcs.size(); i++){
            if(Objects.equals(Topologia.pcs.get(i).getName(), Name)){
                return false;
            }
        }
        PC p = new PC(Name, system);
        Topologia.pcs.add(p);
        if(Topologia.center_m != null){
            if(!Topologia.center_m.add_device(Topologia.PC_t, p)){
                System.err.println("Mapper center ERROR in PC");
            }
        }
        return true;
    }

    public static boolean add_vm(String Name, int system) {
        Objects.requireNonNull(Name, "vm have to have name");
        for(int i = 0; i < Topologia.vms.size(); i++){
            if(Objects.equals(Topologia.vms.get(i).getName(), Name)){
                return false;
            }
        }
        VM v = new VM(Name, system);
        Topologia.vms.add(v);
        if(Topologia.center_m != null){
            if(!Topologia.center_m.add_device(Topologia.VM_t, v)){
                System.err.println("Mapper center ERROR in VM's");
            }
        }
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
                if(Topologia.center_m != null){
                    if(!Topologia.center_m.remove_device(Topologia.NetworkDevice_t, Topologia.networkDevices.get(i))){
                        System.err.println("ERROR in center Network Device");
                    }
                }
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
                if(Topologia.center_m != null){
                    if(!Topologia.center_m.remove_device(Topologia.NetworkService_t, Topologia.networkServices.get(i))){
                        System.err.println("ERROR in center Network Service delete");
                    }
                }
                Topologia.networkServices.remove(i);
                return true;
            }
        }
        return false;
    }

    public static boolean delete_pc(String Name){
        Objects.requireNonNull(Name, "Null Name");
        for(int i = 0; i < Topologia.pcs.size(); i++){
            if(Objects.equals(Topologia.pcs.get(i).getName(), Name)) {
                if(Topologia.center_m != null){
                    if(!Topologia.center_m.remove_device(Topologia.PC_t, Topologia.pcs.get(i))){
                        System.err.println("ERROR in center PC delete");
                    }
                }
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
                if(Topologia.center_m != null){
                    if(!Topologia.center_m.remove_device(Topologia.VM_t, Topologia.vms.get(i))){
                        System.err.println("ERROR in center VM delete");
                    }
                }
                Topologia.vms.remove(i);
                return true;
            }
        }
        return false;
    }

    public static boolean add_ip(String Name, int t, String ip, int index){
        switch (t){
            case Topologia.NetworkService_t -> {
                for(NetworkService ns : Topologia.networkServices){
                    if(Objects.equals(ns.getName(), Name)){
                        if(ns.add_ip(index, ip)){
                            return true;
                        }
                    }
                }
            }
            case Topologia.NetworkDevice_t-> {
                for(NetworkDevice nd : Topologia.networkDevices){
                    if(Objects.equals(nd.getName(), Name)){
                        if(nd.add_ip(index, ip)){
                            return true;
                        }
                    }
                }
            }
            case Topologia.PC_t -> {
                for(PC p : Topologia.pcs){
                    if(Objects.equals(p.getName(), Name)){
                        if(p.add_ip(index, ip)){
                            return true;
                        }
                    }
                }
            }
            case Topologia.VM_t -> {
                for(VM v : Topologia.vms){
                    if(Objects.equals(v.getName(), Name)){
                        if(v.add_ip(index, ip)){
                            return true;
                        }
                    }
                }
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
            case Topologia.NetworkDevice_t -> {
                for (NetworkDevice n : Topologia.networkDevices){
                    if(Objects.equals(n.getName(), name)) {
                        for (String ip : n.getIps()){
                            if(Server.isIpRunning(ip)){
                                return true;
                            }
                        }
                        return n.isRunnig();
                    }
                }
            }
            case Topologia.NetworkService_t -> {
                for (NetworkService n : Topologia.networkServices) {
                    if(Objects.equals(n.getName(), name)) {
                        for (String ip : n.getIps()){
                            if(Server.isIpRunning(ip)){
                                return true;
                            }
                        }
                        return n.isRunnig();
                    }
                }
            }
            case Topologia.PC_t -> {
                for (PC p : Topologia.pcs) {
                    if(Objects.equals(p.getName(), name)){
                        for(String ip : p.getIps()){
                            if(Server.isIpRunning(ip)){
                                return true;
                            }
                        }
                        return p.isRunnig();
                    }
                };
            }
            case Topologia.VM_t -> {
                for (VM v : Topologia.vms) {
                    if(Objects.equals(v.getName(), name)) {
                        for(String ip : v.getIps()){
                            if(Server.isIpRunning(ip)){
                                return true;
                            }
                        }
                        return v.isRunnig();
                    }
                }
            }
        }
        return false;
    }

    public static boolean add_note(String name, int type, String note){
        Objects.requireNonNull(name, "NUll name error");
        Objects.requireNonNull(note, "NUll note error");
        switch (type){
            case Topologia.NetworkDevice_t -> {
                for(int i = 0; i < Topologia.networkDevices.size(); i++){
                    if(Objects.equals(Topologia.networkDevices.get(i).getName(), name)){
                        Topologia.networkDevices.get(i).setNotes(note);
                        return true;
                    }
                }
            }
            case Topologia.NetworkService_t -> {
                for(int i = 0; i < Topologia.networkServices.size(); i++){
                    if(Objects.equals(Topologia.networkServices.get(i).getName(), name)){
                        Topologia.networkServices.get(i).setNotes(note);
                        return true;
                    }
                }
            }
            case Topologia.PC_t -> {
                for (int i = 0; i < Topologia.pcs.size(); i++){
                    if(Objects.equals(Topologia.pcs.get(i).getName(), name)){
                        Topologia.pcs.get(i).setNotes(note);
                        return true;
                    }
                }
            }
            case Topologia.VM_t -> {
                for (int i = 0; i < Topologia.vms.size(); i++){
                    if(Objects.equals(Topologia.vms.get(i).getName(), name)){
                        Topologia.vms.get(i).setNotes(note);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void setCenter_m(Mapper_t m){
        Topologia.center_m = m;
    }


    public static List<NetworkDevice> getNetworkDevices() { return Collections.unmodifiableList(Topologia.networkDevices); }
    public static List<NetworkService> getNetworkServices() { return Collections.unmodifiableList(Topologia.networkServices); }
    public static List<PC> getPcs() { return  Collections.unmodifiableList(Topologia.pcs); }
    public static List<VM> getVms() { return Collections.unmodifiableList(Topologia.vms); }

}
