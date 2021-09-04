package prodo.marc.gosling.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.apache.log4j.LogManager;
import prodo.marc.gosling.HelloApplication;
import prodo.marc.gosling.dao.Song;



import java.io.IOException;


import org.apache.log4j.Logger;

public class SongController {

    public TableColumn tableArtist, tableTitle, tableAlbum, tablePublisher, tableComposer, tableYear, tableGenre, tableISRC;
    public Button songBackButton, addSongButton;
    public TableView<Song> songDatabaseTable;
    private static final Logger logger = LogManager.getLogger(SongController.class);

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
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/mp3.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("mp3");
        stage.setScene(scene);
    }



}
