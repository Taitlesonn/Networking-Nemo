package nemo.networking;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Images_st {

    private static final String img_p = "images/";

    private static final String ruter = "ruter_ap.png";
    private static final String ruter_wi_fi_on = "ruter_ap_wi_fi_on.png";
    private static final String switch_l2 = "switch_l2.png";
    private static final String switch_l3 = "switch_l3.png";

    private static final String windows_server = "windows_server.png";
    private static final String linux_server = "linux_server.png";
    private static final String windows_work_stecion = "windows_work_stecion.png";
    private static final String linux_work_stecion = "linux_work_stecion.png";

    private static final String wan = "wan_cloud.png";
    private static final String api = "api.png";
    private static final String firewall = "firewall.png";
    private static final String db = "db.png";

    @Contract("_ -> new")
    private static @NotNull ImageView load(String name) {
        return new ImageView(new Image(Objects.requireNonNull(
                Images_st.class.getResourceAsStream(img_p + name),
                "Brak pliku: " + name
        )));
    }

    @Contract(" -> new")
    public static @NotNull ImageView getRuter() { return load(ruter); }
    @Contract(" -> new")
    public static @NotNull ImageView getRuterWiFiOn() { return load(ruter_wi_fi_on); }
    @Contract(" -> new")
    public static @NotNull ImageView getSwitchL2() { return load(switch_l2); }
    @Contract(" -> new")
    public static @NotNull ImageView getSwitchL3() { return load(switch_l3); }

    @Contract(" -> new")
    public static @NotNull ImageView getWindowsServer() { return load(windows_server); }
    @Contract(" -> new")
    public static @NotNull ImageView getLinuxServer() { return load(linux_server); }
    @Contract(" -> new")
    public static @NotNull ImageView getWindowsWorkStetion() { return load(windows_work_stecion); }
    @Contract(" -> new")
    public static @NotNull ImageView getLinuxWorkStetion() { return load(linux_work_stecion); }

    @Contract(" -> new")
    public static @NotNull ImageView getWan() { return load(wan); }
    @Contract(" -> new")
    public static @NotNull ImageView getApi() { return load(api); }
    @Contract(" -> new")
    public static @NotNull ImageView getFirewall() { return load(firewall); }
    @Contract(" -> new")
    public static @NotNull ImageView getDb() { return load(db); }
}
