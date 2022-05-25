package prodo.marc.gosling.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


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
    //switch to database window
    protected void showSongWindow(ActionEvent event) throws IOException  {
        SceneController.openScene(event,"Song editor", "view/songDatabase.fxml");
    }

}