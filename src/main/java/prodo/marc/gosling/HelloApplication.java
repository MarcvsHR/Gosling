package prodo.marc.gosling;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("view/songDatabase.fxml")));
        Scene scene = new Scene(root);
        stage.setTitle("main");
        stage.setScene(scene);
        stage.setX(100); stage.setY(50);
        stage.show();

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