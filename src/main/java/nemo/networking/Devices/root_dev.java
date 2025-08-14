package nemo.networking.Devices;

public class root_dev {

    private String Name;
    private int type;
    private boolean runnig;
    private String notes;

    public void setName(String name) { Name = name; }
    public void setType(int type) { this.type = type; }
    public int getType() { return  this.type; }
    public String getName() {return this.Name; }

    public boolean isRunnig() { return runnig; }
    public void setRunnig(boolean runnig) { this.runnig = runnig; }

    public void setNotes(String notes) { this.notes = notes; }

    public String getNotes() { return this.notes; }
}
