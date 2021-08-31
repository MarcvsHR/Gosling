package prodo.marc.gosling.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import prodo.marc.gosling.HelloApplication;

import java.io.File;
import java.io.IOException;

public class SongController {

    @FXML
    Button songBackButton, addSongButton;


    @FXML
    protected void backToMain() throws IOException {
        Stage stage = (Stage) songBackButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Songs");
        stage.setScene(scene);
    }

    @FXML
    protected void addSong2DB() {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("C:\\"));
        FileChooser.ExtensionFilter mp3Filter = new FileChooser.ExtensionFilter( "MP3","*.mp3");
        fc.getExtensionFilters().add(mp3Filter);
        File file = fc.showOpenDialog(null);
        System.out.println(file.getAbsoluteFile());
    }

}
