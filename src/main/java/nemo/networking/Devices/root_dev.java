package nemo.networking.Devices;

import java.util.List;

public class root_dev {

    private String Name;
    private int type;
    private boolean runnig;
    private String notes;
    private  String[] ips = new String[4];

    private boolean valid_ip(String ip) {
        if (ip == null || ip.isEmpty() || ip.length() > 15) {
            return false;
        }

        for (char c : ip.toCharArray()) {
            if (c != '.' && (c < '0' || c > '9')) {
                return false;
            }
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            if (part.isEmpty() || (part.length() > 1 && part.startsWith("0"))) {
                return false;
            }
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


    public void setName(String name) { Name = name; }
    public void setType(int type) { this.type = type; }
    public int getType() { return  this.type; }
    public String getName() {return this.Name; }

    public boolean isRunnig() { return runnig; }
    public void setRunnig(boolean runnig) { this.runnig = runnig; }

    public void setNotes(String notes) { this.notes = notes; }


    public String getNotes() { return this.notes; }

    public boolean add_ip(int index, String ip) {
        if(index < 4 && index >= 0){
            if (this.valid_ip(ip)) {
                this.ips[index] = ip;
                return true;
        }
    }
        return false;
    }

    public String[] getIps() { return this.ips; }
}
