module nemo.networkingnemo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens nemo.networkingnemo to javafx.fxml;
    exports nemo.networkingnemo;
}