package prodo.marc.gosling;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import prodo.marc.gosling.hibernate.repository.SongRepository;


import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("view/hello-view.fxml")));
        //JMetro jMetro = new JMetro(root, Style.DARK);
        Scene scene = new Scene(root);
        Image icon = new Image(String.valueOf(getClass().getResource("images/small_icon.png")));

        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.setX(250); stage.setY(150);
        stage.setWidth(300); stage.setHeight(400);
        stage.setTitle("loading...");
        stage.setOnShown(event -> {
            Runnable preload = () -> {
                try {
                    List<String> songs = SongRepository.getPublishers();
                    Platform.runLater(() -> {
                        stage.setTitle("MP3 Editor");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            new Thread(preload).start();
        });
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }


    @Override
    public void stop(){
        // Save file

        System.exit(0);
    }

}