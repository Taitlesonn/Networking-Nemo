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
        int w = 1500;
        int h = 800;

        FXMLLoader fxmlLoader = new FXMLLoader(Nemo.class.getResource("/nemo/networking/nemo-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), w, h);

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/nemo/networking/styles/style.css")).toExternalForm());
        stage.setTitle("Networkin - Nemo");
        stage.setOnCloseRequest(e ->{
            NemoController n = fxmlLoader.getController();
            // Wyłączanie wątków
            n.center_m.setRunning(false);
            n.center_a.interrupt();
        });
        stage.centerOnScreen();
        stage.setScene(scene);
        stage.show();
    }
}
