package prodo.marc.gosling;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import prodo.marc.gosling.controllers.SongController;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/song-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("mp3");
        stage.setScene(scene);
        stage.setX(100); stage.setY(50);
        stage.show();

        new SongController().getConnection();
    }

    public static void main(String[] args) {
        launch();
    }


    @Override
    public void stop(){
        System.exit(0);
        // Save file
    }

}