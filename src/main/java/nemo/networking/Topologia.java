package nemo.networking;

import nemo.networking.Devices.*;
import nemo.networking.Devices.maper.Mapper_t;
import nemo.networking.server.Server;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class Topologia {
    public static final int NetworkDevice_t = 1;
    public static final int NetworkService_t = 2;
    public static final int PC_t = 3;
    public static final int VM_t = 4;

    private static final List<NetworkDevice> networkDevices = new ArrayList<>();
    private static final List<NetworkService> networkServices = new ArrayList<>();
    private static final List<PC> pcs = new ArrayList<>();
    private static final List<VM> vms = new ArrayList<>();
    private static final List<Connection> connections = new ArrayList<>();

    private static Mapper_t center_m = null;
    private static NemoController nemoController = null;

    private static boolean existsByNameAndType(String name, int type) {
        return switch (type) {
            case NetworkDevice_t -> networkDevices.stream().anyMatch(nd -> name.equals(nd.getName()));
            case NetworkService_t -> networkServices.stream().anyMatch(ns -> name.equals(ns.getName()));
            case PC_t -> pcs.stream().anyMatch(pc -> name.equals(pc.getName()));
            case VM_t -> vms.stream().anyMatch(vm -> name.equals(vm.getName()));
            default -> false;
        };
    }

    private static @NotNull Optional<NetworkDevice> findNetworkDevice(String name) {
        return networkDevices.stream().filter(nd -> name.equals(nd.getName())).findFirst();
    }

    private static @NotNull Optional<NetworkService> findNetworkService(String name) {
        return networkServices.stream().filter(ns -> name.equals(ns.getName())).findFirst();
    }

    private static @NotNull Optional<PC> findPC(String name) {
        return pcs.stream().filter(p -> name.equals(p.getName())).findFirst();
    }

    private static @NotNull Optional<VM> findVM(String name) {
        return vms.stream().filter(v -> name.equals(v.getName())).findFirst();
    }

    public static boolean add_network_device(String Name, int type) {
        Objects.requireNonNull(Name, "Network device have to have a name !");
        if (existsByNameAndType(Name, NetworkDevice_t)) return false;
        NetworkDevice n = new NetworkDevice(Name, type);
        networkDevices.add(n);
        if (center_m != null) {
            if (!center_m.add_device(NetworkDevice_t, n)) {
                System.err.println("Mapper center ERROR in Network Device");
            } else if (nemoController != null) {
                try {
                    double x = center_m.get_x(n);
                    double y = center_m.get_y(n);
                    switch (type) {
                        case 0 -> nemoController.addImageToTopology(Images_st.getRuter(), x, y, n.getName(), Topologia.NetworkDevice_t);
                        case 1 -> nemoController.addImageToTopology(Images_st.getRuterWiFiOn(), x, y, n.getName(), Topologia.NetworkDevice_t);
                        case 2 -> nemoController.addImageToTopology(Images_st.getSwitchL2(), x, y, n.getName(), Topologia.NetworkDevice_t);
                        case 3 -> nemoController.addImageToTopology(Images_st.getSwitchL3(), x, y, n.getName(), Topologia.NetworkDevice_t);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    public static boolean add_network_service(String Name, int type) {
        Objects.requireNonNull(Name, "Netowrk service have to have name");
        if (existsByNameAndType(Name, NetworkService_t)) return false;
        NetworkService n = new NetworkService(Name, type);
        networkServices.add(n);
        if (center_m != null) {
            if (!center_m.add_device(NetworkService_t, n)) {
                System.err.println("Mapper center ERROR in Network Service");
            } else if (nemoController != null) {
                try {
                    double x = center_m.get_x(n);
                    double y = center_m.get_y(n);
                    switch (type) {
                        case 0 -> nemoController.addImageToTopology(Images_st.getFirewall(), x, y, n.getName(), Topologia.NetworkService_t);
                        case 1 -> nemoController.addImageToTopology(Images_st.getDb(), x, y, n.getName(), Topologia.NetworkService_t);
                        case 2 -> nemoController.addImageToTopology(Images_st.getApi(), x, y, n.getName(), Topologia.NetworkService_t);
                        case 3 -> nemoController.addImageToTopology(Images_st.getWan(), x, y, n.getName(), Topologia.NetworkService_t);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    public static boolean add_pc(String Name, int system) {
        Objects.requireNonNull(Name, "pc have to have name");
        if (existsByNameAndType(Name, PC_t)) return false;
        PC p = new PC(Name, system);
        pcs.add(p);
        if (center_m != null) {
            if (!center_m.add_device(PC_t, p)) {
                System.err.println("Mapper center ERROR in PC");
            } else if (nemoController != null) {
                try {
                    double x = center_m.get_x(p);
                    double y = center_m.get_y(p);
                    switch (system) {
                        case 0 -> nemoController.addImageToTopology(Images_st.getWindowsWorkStetion(), x, y, p.getName(), Topologia.PC_t);
                        case 1 -> nemoController.addImageToTopology(Images_st.getLinuxWorkStetion(), x, y,  p.getName(), Topologia.PC_t);
                        case 2 -> nemoController.addImageToTopology(Images_st.getLinuxServer(), x, y,  p.getName(), Topologia.PC_t);
                        case 3 -> nemoController.addImageToTopology(Images_st.getWindowsServer(), x, y,  p.getName(), Topologia.PC_t);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    public static boolean add_vm(String Name, int system) {
        Objects.requireNonNull(Name, "vm have to have name");
        if (existsByNameAndType(Name, VM_t)) return false;
        VM v = new VM(Name, system);
        vms.add(v);
        if (center_m != null) {
            if (!center_m.add_device(VM_t, v)) {
                System.err.println("Mapper center ERROR in VM's");
            } else if (nemoController != null) {
                try {
                    double x = center_m.get_x(v);
                    double y = center_m.get_y(v);
                    switch (system) {
                        case 0 -> nemoController.addImageToTopology(Images_st.getWindowsWorkStetion(), x, y, v.getName(), Topologia.VM_t);
                        case 1 -> nemoController.addImageToTopology(Images_st.getLinuxWorkStetion(), x, y, v.getName(), Topologia.VM_t);
                        case 2 -> nemoController.addImageToTopology(Images_st.getLinuxServer(), x, y, v.getName(), Topologia.VM_t);
                        case 3 -> nemoController.addImageToTopology(Images_st.getWindowsServer(), x, y, v.getName(), Topologia.VM_t);
                        case 4 -> nemoController.addImageToTopology(Images_st.getRuter(), x, y, v.getName(), Topologia.VM_t);
                        case 5 -> nemoController.addImageToTopology(Images_st.getSwitchL2(), x, y, v.getName(), Topologia.VM_t);
                        case 6 -> nemoController.addImageToTopology(Images_st.getSwitchL3(), x, y, v.getName(), Topologia.VM_t);
                        case 7 -> nemoController.addImageToTopology(Images_st.getFirewall(), x, y, v.getName(), Topologia.VM_t);
                        case 8 -> nemoController.addImageToTopology(Images_st.getDb(), x, y, v.getName(), Topologia.VM_t);
                        case 9 -> nemoController.addImageToTopology(Images_st.getApi(), x, y, v.getName(), Topologia.VM_t);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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
            connections.add(new Connection(new Dev(name1, t1), new Dev(name2, t2)));
            return true;
        }
        return false;
    }

    public static boolean delete_connection(String name1, String name2, int t1, int t2) {
        Objects.requireNonNull(name1, "name 1 cannot be null");
        Objects.requireNonNull(name2, "name 2 cannot be null");
        if (!existsByNameAndType(name1, t1) || !existsByNameAndType(name2, t2)) return false;
        Iterator<Connection> it = connections.iterator();
        while (it.hasNext()) {
            Connection c = it.next();
            boolean direct = Objects.equals(c.get_name1(), name1) && Objects.equals(c.get_name2(), name2)
                    && c.get_type1() == t1 && c.get_type2() == t2;
            boolean reverse = Objects.equals(c.get_name1(), name2) && Objects.equals(c.get_name2(), name1)
                    && c.get_type1() == t2 && c.get_type2() == t1;
            if (direct || reverse) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public static boolean delete_network_device(String Name) {
        Objects.requireNonNull(Name, "Null Name");
        Optional<NetworkDevice> found = findNetworkDevice(Name);
        if (found.isPresent()) {
            NetworkDevice nd = found.get();
            return networkDevices.remove(nd);
        }
        return false;
    }

    public static boolean delete_network_service(String Name) {
        Objects.requireNonNull(Name, "Null Name");
        Optional<NetworkService> found = findNetworkService(Name);
        if (found.isPresent()) {
            NetworkService ns = found.get();
            return networkServices.remove(ns);
        }
        return false;
    }

    public static boolean delete_pc(String Name) {
        Objects.requireNonNull(Name, "Null Name");
        Optional<PC> found = findPC(Name);
        if (found.isPresent()) {
            PC p = found.get();
            return pcs.remove(p);
        }
        return false;
    }

    public static boolean delete_vm(String Name) {
        Objects.requireNonNull(Name, "Null Name");
        Optional<VM> found = findVM(Name);
        if (found.isPresent()) {
            VM v = found.get();
            return vms.remove(v);
        }
        return false;
    }

    public static boolean add_ip(String Name, int t, String ip, int index) {
        switch (t) {
            case NetworkService_t -> {
                Optional<NetworkService> ns = findNetworkService(Name);
                if (ns.isPresent() && ns.get().add_ip(index, ip)) return true;
            }
            case NetworkDevice_t -> {
                Optional<NetworkDevice> nd = findNetworkDevice(Name);
                if (nd.isPresent() && nd.get().add_ip(index, ip)) return true;
            }
            case PC_t -> {
                Optional<PC> p = findPC(Name);
                if (p.isPresent() && p.get().add_ip(index, ip)) return true;
            }
            case VM_t -> {
                Optional<VM> v = findVM(Name);
                if (v.isPresent() && v.get().add_ip(index, ip)) return true;
            }
        }
        return false;
    }

    public static boolean set_runing(int t, String name, boolean s) {
        switch (t) {
            case NetworkDevice_t -> {
                Optional<NetworkDevice> nd = findNetworkDevice(name);
                if (nd.isPresent()) {
                    nd.get().setRunnig(s);
                    return true;
                }
            }
            case NetworkService_t -> {
                Optional<NetworkService> ns = findNetworkService(name);
                if (ns.isPresent()) {
                    ns.get().setRunnig(s);
                    return true;
                }
            }
            case PC_t -> {
                Optional<PC> p = findPC(name);
                if (p.isPresent()) {
                    p.get().setRunnig(s);
                    return true;
                }
            }
            case VM_t -> {
                Optional<VM> v = findVM(name);
                if (v.isPresent()) {
                    v.get().setRunnig(s);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean is_runnig(int t, String name) {
        switch (t) {
            case NetworkDevice_t -> {
                Optional<NetworkDevice> nd = findNetworkDevice(name);
                if (nd.isPresent()) {
                    for (String ip : nd.get().getIps()) if (Server.isIpRunning(ip)) return true;
                    return nd.get().isRunnig();
                }
            }
            case NetworkService_t -> {
                Optional<NetworkService> ns = findNetworkService(name);
                if (ns.isPresent()) {
                    for (String ip : ns.get().getIps()) if (Server.isIpRunning(ip)) return true;
                    return ns.get().isRunnig();
                }
            }
            case PC_t -> {
                Optional<PC> p = findPC(name);
                if (p.isPresent()) {
                    for (String ip : p.get().getIps()) if (Server.isIpRunning(ip)) return true;
                    return p.get().isRunnig();
                }
            }
            case VM_t -> {
                Optional<VM> v = findVM(name);
                if (v.isPresent()) {
                    for (String ip : v.get().getIps()) if (Server.isIpRunning(ip)) return true;
                    return v.get().isRunnig();
                }
            }
        }
        return false;
    }

    public static void add_note(String name, int type, String note) {
        Objects.requireNonNull(name, "NUll name error");
        Objects.requireNonNull(note, "NUll note error");
        switch (type) {
            case NetworkDevice_t -> {
                Optional<NetworkDevice> nd = findNetworkDevice(name);
                nd.ifPresent(networkDevice -> networkDevice.setNotes(note));
            }
            case NetworkService_t -> {
                Optional<NetworkService> ns = findNetworkService(name);
                ns.ifPresent(networkService -> networkService.setNotes(note));
            }
            case PC_t -> {
                Optional<PC> p = findPC(name);
                p.ifPresent(pc -> pc.setNotes(note));
            }
            case VM_t -> {
                Optional<VM> v = findVM(name);
                v.ifPresent(vm -> vm.setNotes(note));
            }
        }
    }

    public static void setClassys(Mapper_t m, NemoController n) {
        center_m = m;
        nemoController = n;
    }

    public static Optional<double[]> getPosition(Object deviceObject) {
        if (deviceObject == null) return Optional.empty();
        if (center_m == null) return Optional.empty();
        return center_m.getPoint(deviceObject).map(p -> new double[]{(double) p.x(), (double) p.y()});
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView List<NetworkDevice> getNetworkDevices() {
        return Collections.unmodifiableList(networkDevices);
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView List<NetworkService> getNetworkServices() {
        return Collections.unmodifiableList(networkServices);
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView List<PC> getPcs() {
        return Collections.unmodifiableList(pcs);
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView List<VM> getVms() {
        return Collections.unmodifiableList(vms);
    }

    public static int getMachineCunt(){return Topologia.networkDevices.size() + Topologia.networkServices.size() + Topologia.pcs.size() + Topologia.vms.size(); }
}
