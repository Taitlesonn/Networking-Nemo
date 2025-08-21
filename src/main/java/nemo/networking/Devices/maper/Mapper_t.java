package nemo.networking.Devices.maper;

import nemo.networking.Topologia;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mapper_t {
    public record Point(int x, int y) { }

    public final AtomicBoolean running = new AtomicBoolean(false);

    private final Random rand = new Random();
    private final Map<String, List<Object>> devices = new ConcurrentHashMap<>();
    private final Map<Object, Point> pointMap = new ConcurrentHashMap<>();

    private static final String NETWORK_DEVICE = "nd";
    private static final String NETWORK_SERVICE = "ns";
    private static final String PC = "pc";
    private static final String VM = "vm";

    public Mapper_t() {
        devices.put(NETWORK_DEVICE, new ArrayList<>());
        devices.put(NETWORK_SERVICE, new ArrayList<>());
        devices.put(PC, new ArrayList<>());
        devices.put(VM, new ArrayList<>());
        running.set(true);
    }

    public void setRunning(boolean b) {
        running.set(b);
    }

    public boolean getRunning() {
        return running.get();
    }

    public boolean move_o(int new_x, int new_y, Object o) {
        Objects.requireNonNull(o, "NULL Object in Mapper");
        if (!pointMap.containsKey(o)) {
            return false;
        }
        Point newPoint = new Point(new_x, new_y);
        if (pointMap.containsValue(newPoint)) {
            return false;
        }
        pointMap.put(o, newPoint);
        return true;
    }

    public void remove_device(int type, Object o) {
        Objects.requireNonNull(o, "NULL Object in Mapper");
        String key = deviceKey(type);
        if (key == null) {
            return;
        }
        if (devices.get(key).remove(o)) {
            pointMap.remove(o);
        }
    }

    public boolean add_device(int type, Object o) {
        Objects.requireNonNull(o, "NULL Object in Mapper_t");
        String key = deviceKey(type);
        if (key == null) {
            return false;
        }

        Point newPoint = generateUniquePoint();
        if (newPoint == null) {
            return false;
        }

        devices.get(key).add(o);
        pointMap.put(o, newPoint);
        return true;
    }

    public double get_x(Object o) {
        Point p = pointMap.get(o);
        return (p == null) ? Double.NaN : p.x;
    }

    public double get_y(Object o) {
        Point p = pointMap.get(o);
        return (p == null) ? Double.NaN : p.y;
    }

    public Optional<Point> getPoint(Object o) {
        return Optional.ofNullable(pointMap.get(o));
    }


    @Contract(pure = true)
    private @Nullable String deviceKey(int type) {
        return switch (type) {
            case Topologia.NetworkDevice_t -> NETWORK_DEVICE;
            case Topologia.NetworkService_t -> NETWORK_SERVICE;
            case Topologia.PC_t -> PC;
            case Topologia.VM_t -> VM;
            default -> null;
        };
    }

    @org.jetbrains.annotations.Nullable
    private Point generateUniquePoint() {
        for (int attempts = 0; attempts < 1000; attempts++) {
            Point candidate = new Point(rand.nextInt(1000), rand.nextInt(1000));
            if (!pointMap.containsValue(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
