module nemo.networkingnemo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires org.jetbrains.annotations;
    requires java.logging;
    requires com.google.gson;

    opens nemo.networking to javafx.fxml;
    exports nemo.networking;
    exports nemo.networking.Devices;
    exports nemo.networking.Devices.maper;
    exports nemo.networking.server;
    opens nemo.networking.server to javafx.fxml;
}