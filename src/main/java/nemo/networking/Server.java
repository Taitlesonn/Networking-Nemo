package nemo.networking;


public class Server {
    private static boolean running = false;

    public static void run() {
        running = true;
    }

    public static void stop() {
        running = false;
    }

    private static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isIpRunning(String ip) {
        if (!isValidIp(ip)) {
            return false;
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder builder;

            if (os.contains("win")) {
                // Windows: -n 1 = 1 pakiet, -w 2000 = timeout 2 sekundy
                builder = new ProcessBuilder("ping", "-n", "1", "-w", "2000", ip);
            } else {
                // Linux/Mac: -c 1 = 1 pakiet, -W 2 = timeout 2 sekundy
                builder = new ProcessBuilder("ping", "-c", "1", "-W", "2", ip);
            }

            Process process = builder.start();
            int exitCode = process.waitFor();

            return exitCode == 0; // 0 = host odpowiada
        } catch (Exception e) {
            System.out.println("Błąd podczas pinga: " + e.getMessage());
            return false;
        }
    }

    public static boolean isRunning() {
        return running;
    }

    public static void setRunning(boolean running) {
        Server.running = running;
    }
}
