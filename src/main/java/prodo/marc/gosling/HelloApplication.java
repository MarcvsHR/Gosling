package prodo.marc.gosling;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("view/hello-view.fxml")));
        //JMetro jMetro = new JMetro(root, Style.DARK);
        Scene scene = new Scene(root);
        Image icon = new Image(String.valueOf(getClass().getResource("images/small_icon.png")));

        stage.getIcons().add(icon);
        stage.setTitle("main");
        stage.setScene(scene);
        stage.setX(150); stage.setY(100);
        stage.setMinWidth(1100);
        stage.setMinHeight(490);
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