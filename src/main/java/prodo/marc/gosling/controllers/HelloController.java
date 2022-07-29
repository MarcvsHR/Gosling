package prodo.marc.gosling.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import prodo.marc.gosling.hibernate.repository.SongRepository;


import java.io.IOException;
import java.util.List;

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
            SceneController.openScene(event, "Song Database", "view/songDatabase.fxml", 1100, 490);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}