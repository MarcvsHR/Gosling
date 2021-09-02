package prodo.marc.gosling.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;


import java.io.IOException;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    Button songViewButton;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to Gosling");
    }

    @FXML
    protected void showSongWindow() throws IOException  {
        Stage stage = (Stage) songViewButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("view/song-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Songs");
        stage.setScene(scene);
    }

}