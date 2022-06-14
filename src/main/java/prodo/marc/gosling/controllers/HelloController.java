package prodo.marc.gosling.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;


import java.io.IOException;

public class HelloController {
    @FXML
    private ImageView gooseImage;
    @FXML
    private Label welcomeText, imageLabel;
    @FXML
    private Button songViewButton;

    @FXML
    //switch to database window
    protected void showSongWindow(ActionEvent event) {
        welcomeText.setText("Please wait, loading...");
        imageLabel.setText("");
        gooseImage.setImage(null);
        songViewButton.setDisable(true);

        //Popups.giveInfoAlertAndWait("Loading", "Loading songs, please confirm", "");

        try {
            SceneController.openScene(event, "Song Database", "view/songDatabase.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}