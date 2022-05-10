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
import java.util.Collections;
import java.util.List;

public class RegexWindowController {
    public Button addRegexMethod;
    public ListView<String> regexList;
    public Song song = SongGlobal.getCurrentSong();
    public Label mp3Filename;
    public Label labelArtist;
    public Label labelTitle;
    public Label labelISRC;
    public Label labelPublisher;
    List<String> regex = new ArrayList<>();

    private List<String> getRegexStuff() {
        regex.add("Artist - Title");
        regex.add("Title - Artist");
        regex.add("Track_Title_Artist");
        regex.add("Title-Artist");
        regex.add("ISRC_Title_Artist_Publisher");
        regex.add("Title");
        regex.add("Track - Artist - Title");
        regex.add("Artist - Track - Title");
        regex.add("Artist-Title");
        regex.add("Track. Artist - Title_ISRC");
        regex.add("Artist-Title-CROREC-ISRC");
        regex.add("Artist - Title - ISRC");
        regex.add("ISRC - Artist - Title - Publisher");
        regex.add("Artist - Title - Publisher - ISRC");
        regex.add("Artist_Title_ISRC_Publisher");
        Collections.sort(regex);
        return new ArrayList<>(regex);
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
            case "Title": {
                song.setTitle(mp3Filename.getText().trim());
                isSet = true;
                break;
            }
            case "Track - Artist - Title": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[1]);
                song.setTitle(output[2]);
                isSet = true;
                break;
            }
            case "Artist - Track - Title": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[0]);
                song.setTitle(output[2]);
                isSet = true;
                break;
            }
            case "Artist-Title": {
                String[] output = mp3Filename.getText().split("-");
                song.setArtist(output[0]);
                song.setTitle(output[1]);
                isSet = true;
                break;
            }
            case "Track. Artist - Title_ISRC": {
                String[] output = mp3Filename.getText().split("\\.| - |_");
                song.setArtist(output[1]);
                song.setTitle(output[2]);
                song.setISRC(output[3]);
                isSet = true;
                break;
            }
            case "Artist-Title-CROREC-ISRC": {
                String[] output = mp3Filename.getText().split("-CROATIA-RECORDS-");
                String[] splitWords = output[0].split("-");
                //fine the first word in splitWords that isn't all upper case
                //put all the words before that in the artist field with spaces in between
                //put everything else in the title field with spaces in between
                StringBuilder artist = new StringBuilder();
                StringBuilder title = new StringBuilder();
                boolean putInArtist = true;
                for (String word : splitWords) {
                    if (!word.equals(word.toUpperCase()))
                        putInArtist = false;

                    if (putInArtist)
                        artist.append(word).append(" ");
                    else
                        title.append(word).append(" ");
                }
                song.setArtist(artist.toString().trim());
                song.setTitle(title.toString().trim());

                //song.setArtist(output[0].replace("-"," "));
                song.setISRC(output[1]);
                song.setPublisher("Crorec");
                isSet = true;
                break;
            }
            case "Artist - Title - ISRC": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[0]);
                song.setTitle(output[1]);
                song.setISRC(output[2]);
                isSet = true;
                break;
            }
            case "ISRC - Artist - Title - Publisher": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[1]);
                song.setTitle(output[2]);
                song.setISRC(output[0]);
                song.setPublisher(output[3]);
                isSet = true;
                break;
            }
            case "Artist - Title - Publisher - ISRC": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[0]);
                song.setTitle(output[1]);
                song.setPublisher(output[2]);
                song.setISRC(output[3]);
                isSet = true;
                break;
            }
            case "Artist_Title_ISRC_Publisher": {
                String[] output = mp3Filename.getText().split("_");
                song.setArtist(output[0]);
                song.setTitle(output[1]);
                song.setISRC(output[2]);
                song.setPublisher(output[3]);
                isSet = true;
                break;
            }
        }
        if (isSet) {
            if (song.getTitle() != null)
                song.setTitle(song.getTitle().replace("[Clean]", ""));

            labelArtist.setText("Artist: " + song.getArtist());
            labelTitle.setText("Title: " + song.getTitle());
            labelPublisher.setText("Publisher: " + song.getPublisher());
            labelISRC.setText("ISRC: " + song.getISRC());
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
