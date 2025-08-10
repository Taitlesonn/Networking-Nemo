module nemo.networkingnemo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens nemo.networking to javafx.fxml;
    exports nemo.networking;
}