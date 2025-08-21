module nemo.networkingnemo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires org.jetbrains.annotations;
    requires java.logging;

    opens nemo.networking to javafx.fxml;
    exports nemo.networking;
    exports nemo.networking.Devices;
    exports nemo.networking.Devices.maper;
}