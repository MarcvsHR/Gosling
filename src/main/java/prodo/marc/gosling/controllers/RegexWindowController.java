package prodo.marc.gosling.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.service.SongGlobal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegexWindowController {
    public Button addRegexMethod;
    public Label mp3Filename;
    public ListView<String> regexList;
    public Label artistLabel;
    public Label titleLabel;
    public Song song = new Song();

    public void changeSelectedRegex(MouseEvent mouseEvent) {
        String selection = regexList.getSelectionModel().getSelectedItem();
        boolean isSet = false;
        if (selection.equals("Artist - Title")) {
            String[] output = mp3Filename.getText().split(" - ");
            song.setArtist(output[0]);
            song.setTitle(output[1]);
            isSet = true;
        } else if  (selection.equals("Title - Artist")) {
            String[] output = mp3Filename.getText().split(" - ");
            song.setArtist(output[1]);
            song.setTitle(output[0]);
            isSet = true;
        }
        if (isSet) {
            artistLabel.setText("Artist: "+song.getArtist());
            titleLabel.setText("Title: "+song.getTitle());
        }
    }

    public void initialize() {
        List<String> regexStuff = new ArrayList<>();
        regexStuff.add("Artist - Title");
        regexStuff.add("Title - Artist");

        regexList.getItems().addAll(regexStuff);

        String fileLoc = SongGlobal.getCurrentSong().getFileLoc();
        mp3Filename.setText(new File(fileLoc).getName().replaceAll("(?i).mp3",""));
    }

    public void closeAndSave(ActionEvent event) throws IOException {

        //TODO: put data into global song

        SceneController.openScene(event, "view/songDatabase.fxml");
    }
}
