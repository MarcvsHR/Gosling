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

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("view/hello-view.fxml")));
        //JMetro jMetro = new JMetro(root, Style.DARK);
        Scene scene = new Scene(root);
        stage.setTitle("main");
        stage.setScene(scene);
        stage.setX(150); stage.setY(100);
        stage.setMinWidth(1100);
        stage.setMinHeight(490);
        //stage.initStyle(StageStyle.TRANSPARENT);
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