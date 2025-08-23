package nemo.networking.server.bild;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


@FunctionalInterface
interface EventHandler<T> {
    void handle(T event) throws IOException;
}

public abstract class Node_s {
    private int id; // Rozróżnianie procesów serwera

    private final ConcurrentHashMap<String, EventHandler<?>> handlers = new ConcurrentHashMap<>();

    public <T> void setHandler(String eventName, EventHandler<T> handler) {
        handlers.put(eventName, handler);
    }

    @SuppressWarnings("unchecked")
    public <T> void trigger(String eventName, T event) throws IOException {
        EventHandler<T> handler = (EventHandler<T>) handlers.get(eventName);
        if (handler != null) {
            handler.handle(event);
        }
    }

    private final String path = Paths.get("").toAbsolutePath().toString();

    // Thread-safe lista klientów
    private final List<Client> clients = new CopyOnWriteArrayList<>();

    // Protokoły: nazwa → ID
    private final ConcurrentHashMap<String, Integer> map_t = new ConcurrentHashMap<>();

    // Porty: port → TCP/UDP
    private final ConcurrentHashMap<Integer, String> ports = new ConcurrentHashMap<>();

    public Node_s() {
        registerDefaultProtocols();
    }

    // Rejestracja domyślnych protokołów
    private void registerDefaultProtocols() {
        map_t.put("snmp", 0);
        map_t.put("ssh", 1);
        map_t.put("WinRc", 2);
        map_t.put("ftp", 3);
        map_t.put("sftp", 4);
        map_t.put("ftps", 5);
    }

    public String getPath() {
        return path;
    }


    public record Client(String ip, int protocolType, AtomicBoolean running) {}

    public void addClient(String ip, int type, boolean running) {
        Objects.requireNonNull(ip, "Null IP");
        if (clients.stream().noneMatch(c -> Objects.equals(c.ip(), ip) && c.protocolType() == type)) {
            clients.add(new Client(ip, type, new AtomicBoolean(running)));
        }
    }


    public void removeClient(String ip, int type) {
        clients.removeIf(c -> Objects.equals(c.ip(), ip) && c.protocolType() == type);
    }


    public Client getClient(String ip, int type) {
        return clients.stream()
                .filter(c -> Objects.equals(c.ip(), ip) && c.protocolType() == type)
                .findFirst()
                .orElse(null);
    }

    // Start/Stop klienta
    public boolean startClient(String ip, int type) {
        Client client = getClient(ip, type);
        if (client != null) {
            client.running().set(true);
            return true;
        }
        return false;
    }

    public boolean stopClient(String ip, int type) {
        Client client = getClient(ip, type);
        if (client != null) {
            client.running().set(false);
            return true;
        }
        return false;
    }


    // Protokoły
    public int getType(String protocol) {
        Objects.requireNonNull(protocol, "Null protocol");
        return map_t.getOrDefault(protocol.toLowerCase(), -1);
    }

    public void registerProtocol(String name, int id) {
        Objects.requireNonNull(name, "null Protocole name");
        if(id < 0){
            System.err.println("ERROR ID TYPE");
            return;
        }
        map_t.put(name.toLowerCase(), id);
    }

    // ID
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }


}
