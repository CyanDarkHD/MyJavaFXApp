package cn.cyandark.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("hello-view.fxml"));
        stage.setTitle("Excel转Shp工具");
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        Application.setUserAgentStylesheet(Objects.requireNonNull(getClass().getResource("main.css")).toExternalForm());
        stage.getIcons().add(new Image(String.valueOf(Objects.requireNonNull(getClass().getResource("logo.ico")).toURI())));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}