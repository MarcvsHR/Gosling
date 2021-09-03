package prodo.marc.gosling.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import prodo.marc.gosling.HelloApplication;

import java.io.IOException;

public class SongController {

    @FXML
    Button songBackButton, addSongButton;


    @FXML
    //goes back to the main window
    protected void backToMain() throws IOException {
        Stage stage = (Stage) songBackButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("view/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Songs");
        stage.setScene(scene);
    }

    @FXML
    //goes to the mp3 edit window
    protected void addSong2DB() throws IOException {
        Stage stage = (Stage) addSongButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("view/mp3.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("mp3");
        stage.setScene(scene);
    }

}
