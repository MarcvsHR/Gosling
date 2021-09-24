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
    public Song song = SongGlobal.getCurrentSong();
    public Label titlePublisher;

    private List<String> getRegexStuff() {
        List<String> regexStuff = new ArrayList<>();
        regexStuff.add("Artist - Title");
        regexStuff.add("Title - Artist");
        regexStuff.add("Track_Title_Artist");
        regexStuff.add("Title-Artist");
        regexStuff.add("ISRC_Title_Artist_Publisher");
        return regexStuff;
    }

    public void changeSelectedRegex(MouseEvent mouseEvent) {
        String selection = regexList.getSelectionModel().getSelectedItem();
        boolean isSet = false;
        switch (selection) {
            case "Artist - Title": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[0]);
                song.setTitle(output[1]);
                isSet = true;
                break;
            }
            case "Title - Artist": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[1]);
                song.setTitle(output[0]);
                isSet = true;
                break;
            }
            case "Track_Title_Artist": {
                String[] output = mp3Filename.getText().split("_");
                song.setArtist(output[2]);
                song.setTitle(output[1]);
                isSet = true;
                break;
            }
            case "Title-Artist": {
                String[] output = mp3Filename.getText().split("-");
                song.setArtist(output[1]);
                song.setTitle(output[0]);
                isSet = true;
                break;
            }
            case "ISRC_Title_Artist_Publisher": {
                String[] output = mp3Filename.getText().split("_");
                song.setArtist(output[2]);
                song.setTitle(output[1]);
                song.setPublisher(output[3]);
                isSet = true;
                break;
            }
        }
        if (isSet) {
            artistLabel.setText("Artist: " + song.getArtist());
            titleLabel.setText("Title: " + song.getTitle());
            titlePublisher.setText("Publisher: "+song.getPublisher());
        }
    }

    public void initialize() {

        regexList.getItems().addAll(getRegexStuff());

        String fileLoc = song.getFileLoc();
        mp3Filename.setText(new File(fileLoc).getName().replaceAll("(?i).mp3", ""));
    }

    public void closeAndSave(ActionEvent event) throws IOException {

        SongGlobal.setCurrentSong(song);
        SongGlobal.setFilenameParsed(true);
        SceneController.openScene(event, "view/songDatabase.fxml");
    }


}
