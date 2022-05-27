package prodo.marc.gosling.controllers;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.service.SongGlobal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegexWindowController extends SongController {
    public Button addRegexMethod;
    public ListView<String> regexList;
    Song song = new Song(SongGlobal.getCurrentSong());
    public Label mp3Filename;
    public Label labelArtist;
    public Label labelTitle;
    public Label labelISRC;
    public Label labelPublisher;


    private List<String> getRegexStuff() {
        System.out.println("hello!");
        System.out.println(mp3Filename.getText());

        List<String> regex = new ArrayList<>();
        if (StringUtils.countMatches(mp3Filename.getText(), "_") == 2) {
            regex.add("[Track_Title_Artist]"); }
        if (StringUtils.countMatches(mp3Filename.getText(), "_") == 3) {
            regex.add("[ISRC_Title_Artist_Publisher]");
            regex.add("[Artist_Title_ISRC_Publisher]"); }
        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 1) {
            regex.add("[Artist - Title]");
            regex.add("[Title - Artist]");
            regex.add("[Track Artist - Title]");
            regex.add("[Track Title - Artist]");
            regex.add("[Artist - Title ISRC]"); }
        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 2) {
            regex.add("[Track - Artist - Title]");
            regex.add("[Artist - Track - Title]");
            regex.add("[Artist - Title - ISRC]"); }
        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 3) {
            regex.add("[ISRC - Artist - Title - Publisher]");
            regex.add("[Artist - Title - Publisher - ISRC]"); }
        if (StringUtils.countMatches(mp3Filename.getText(), "-") == 1 &&
                StringUtils.countMatches(mp3Filename.getText(), " - ") == 0) {
            regex.add("[Title-Artist]");
            regex.add("[Artist-Title]"); }
        if (StringUtils.countMatches(mp3Filename.getText(), "-") > 2 &&
                StringUtils.countMatches(mp3Filename.getText(), " - ") == 0) {
            regex.add("[Artist-Title-CROREC-ISRC]"); }

        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 1 &&
                StringUtils.countMatches(mp3Filename.getText(),"_") == 1 &&
                StringUtils.countMatches(mp3Filename.getText(),".") == 1) { regex.add("[Track. Artist - Title_ISRC]"); }

        regex.add("[Title]]");

        Collections.sort(regex);
        return new ArrayList<>(regex);
    }

    public void changeSelectedRegex() {
        String selection = regexList.getSelectionModel().getSelectedItem();
        if (selection == null) selection = "";
        if (!selection.isEmpty()) selection = selection.substring(selection.indexOf("[")+1, selection.indexOf("]"));
        boolean isSet = false;
        switch (selection) {
            case "Artist - Title": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[0]);
                song.setTitle(output[1]);
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());
                song.setISRC(SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Title - Artist": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[1]);
                song.setTitle(output[0]);
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());
                song.setISRC(SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Track_Title_Artist": {
                String[] output = mp3Filename.getText().split("_");
                song.setArtist(output[2]);
                song.setTitle(output[1]);
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());
                song.setISRC(SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Title-Artist": {
                String[] output = mp3Filename.getText().split("-");
                song.setArtist(output[1]);
                song.setTitle(output[0]);
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());
                song.setISRC(SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "ISRC_Title_Artist_Publisher": {
                String[] output = mp3Filename.getText().split("_");
                song.setArtist(output[2]);
                song.setTitle(output[1]);
                song.setPublisher(output[3]);
                song.setISRC(SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Title": {
                song.setTitle(mp3Filename.getText().trim());
                song.setArtist(SongGlobal.getCurrentSong().getArtist());
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());
                song.setISRC(SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Track - Artist - Title": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[1]);
                song.setTitle(output[2]);
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());
                song.setISRC(SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Artist - Track - Title": {
                String[] output = mp3Filename.getText().split(" - ");
                song.setArtist(output[0]);
                song.setTitle(output[2]);
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());
                song.setISRC(SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Artist-Title": {
                String[] output = mp3Filename.getText().split("-");
                song.setArtist(output[0]);
                song.setTitle(output[1]);
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());
                song.setISRC(SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Track. Artist - Title_ISRC": {
                String[] output = mp3Filename.getText().split("\\.| - |_");
                song.setArtist(output[1]);
                song.setTitle(output[2]);
                song.setISRC(output[3]);
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());

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
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());

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
            case "Track Artist - Title": {
                String[] output = mp3Filename.getText().split(" - ");
                String[] splitWords = output[0].split(" ",2);
                song.setArtist(splitWords[1]);
                song.setTitle(output[1]);
                song.setISRC(SongGlobal.getCurrentSong().getISRC());
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());

                isSet = true;
                break;
            }
            case "Track Title - Artist": {
                String[] output = mp3Filename.getText().split(" - ");
                String[] splitWords = output[0].split(" ",2);
                song.setTitle(splitWords[1]);
                song.setArtist(output[1]);
                song.setISRC(SongGlobal.getCurrentSong().getISRC());
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());

                isSet = true;
                break;
            }
            case "Artist - Title ISRC": {
                String[] output = mp3Filename.getText().split(" - ");
                String[] splitWords = output[1].split(" ");
                String isrc = splitWords[splitWords.length-1];
                song.setArtist(output[0]);
                song.setISRC(isrc);
                song.setTitle(output[1].replace(" "+isrc,""));
                song.setPublisher(SongGlobal.getCurrentSong().getPublisher());

                isSet = true;
                break;
            }
        }
        if (isSet) {
            song.setTitle(song.getTitle().replace("[Clean]", ""));

            labelArtist.setText("Artist: " + song.getArtist());
            labelTitle.setText("Title: " + song.getTitle());
            labelPublisher.setText("Publisher: " + song.getPublisher());
            labelISRC.setText("ISRC: " + song.getISRC());
        }
    }

    public void initialize() {

        mp3Filename.setText(new File(song.getFileLoc()).getName().replaceAll("(?i).mp3", ""));
        regexList.getItems().addAll(getRegexStuff());

    }

    public void closeAndSave(ActionEvent event) {

        SongGlobal.setCurrentSong(song);

        //send data to another stage
        publicTextArtist.setText(song.getArtist());
        publicTextTitle.setText(song.getTitle());
        publicTextISRC.setText(song.getISRC());
        publicTextPublisher.setText(song.getPublisher());

        //close window
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }


}
