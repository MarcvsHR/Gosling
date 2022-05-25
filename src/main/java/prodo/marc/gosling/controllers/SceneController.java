package prodo.marc.gosling.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import prodo.marc.gosling.HelloApplication;

import java.io.IOException;
import java.util.Objects;

public class SceneController {

    private static Scene scene;
    private static Parent root;


    public static void openScene(ActionEvent event,String windowName, String window) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(HelloApplication.class.getResource(window)));
        root = loader.load();

        Stage stage = (Stage) ((Node) event.getTarget()).getScene().getWindow();
        stage.setTitle(windowName);
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void openWindow(ActionEvent event, String window, boolean block) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(window));
        root = loader.load();

        Stage stage1 = new Stage();
        scene = new Scene(root);
        stage1.setScene(scene);
        if (block) {
            stage1.initModality(Modality.APPLICATION_MODAL);
            stage1.initStyle(StageStyle.UTILITY);
        }
        stage1.show();
    }


}
