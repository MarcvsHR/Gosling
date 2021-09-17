package prodo.marc.gosling.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import prodo.marc.gosling.HelloApplication;

import java.io.IOException;
import java.util.Objects;

public class SceneController {

    private static Stage stage;
    private static Scene scene;
    private static Parent root;
    private static Stage stage1;


    public static void openScene(ActionEvent event, String window) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(HelloApplication.class.getResource(window)));
        stage = (Stage) ((Node) event.getTarget()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void openWindow(ActionEvent event, String window) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(window));
        root = loader.load();

        stage1 = new Stage();
        scene = new Scene(root);
        stage1.setScene(scene);
        stage1.show();
    }


}
