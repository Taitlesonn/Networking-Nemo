package nemo.networking;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Nemo extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        int w = 800;
        int h = 1500;

        FXMLLoader fxmlLoader = new FXMLLoader(Nemo.class.getResource("nemo-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), h, w);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/style.css")).toExternalForm());
        stage.setTitle("Networkin - Nemo");
        stage.setScene(scene);
        stage.show();
    }
}
