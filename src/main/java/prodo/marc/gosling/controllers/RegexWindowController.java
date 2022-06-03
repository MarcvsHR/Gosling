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
    public Label mp3Filename;
    public Label labelArtist;
    public Label labelTitle;
    public Label labelISRC;
    public Label labelPublisher;

    Song song = new Song(SongGlobal.getCurrentSong());


    private List<String> getRegexStuff() {
        List<String> regex = new ArrayList<>();
        if (StringUtils.countMatches(mp3Filename.getText(), "_") == 2) {
            regex.add("[Track_Title_Artist]");
        }
        if (StringUtils.countMatches(mp3Filename.getText(), "_") == 3) {
            regex.add("[ISRC_Title_Artist_Publisher]");
            regex.add("[Artist_Title_ISRC_Publisher]");
        }
        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 1) {
            regex.add("[Artist - Title]");
            regex.add("[Title - Artist]");
            regex.add("[Track Artist - Title]");
            regex.add("[Track Title - Artist]");
            regex.add("[Artist - Title ISRC]");
        }
        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 2) {
            regex.add("[Track - Artist - Title]");
            regex.add("[Artist - Track - Title]");
            regex.add("[Artist - Title - ISRC]");
        }
        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 3) {
            regex.add("[ISRC - Artist - Title - Publisher]");
            regex.add("[Artist - Title - Publisher - ISRC]");
        }
        if (StringUtils.countMatches(mp3Filename.getText(), "-") == 1 &&
                StringUtils.countMatches(mp3Filename.getText(), " - ") == 0) {
            regex.add("[Title-Artist]");
            regex.add("[Artist-Title]");
        }
        if (StringUtils.countMatches(mp3Filename.getText(), "-") > 2 &&
                StringUtils.countMatches(mp3Filename.getText(), " - ") == 0) {
            regex.add("[Artist-Title-CROREC-ISRC]");
        }

        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 1 &&
                StringUtils.countMatches(mp3Filename.getText(), "_") == 1 &&
                StringUtils.countMatches(mp3Filename.getText(), ".") == 1) {
            regex.add("[Track. Artist - Title_ISRC]");
        }

        regex.add("[Title]]");

        Collections.sort(regex);
        return new ArrayList<>(regex);
    }

    public void setSongData(String artist, String title, String publisher, String isrc) {
        song.setArtist(artist);
        song.setTitle(title);
        song.setPublisher(publisher);
        song.setISRC(isrc);
    }

    public void changeSelectedRegex() {
        String selection = regexList.getSelectionModel().getSelectedItem();
        if (selection == null) selection = "";
        if (!selection.isEmpty()) selection = selection.substring(selection.indexOf("[") + 1, selection.indexOf("]"));
        boolean isSet = false;

        switch (selection) {
            case "Artist - Title": {
                String[] output = mp3Filename.getText().split(" - ");

                setSongData(output[0], output[1], SongGlobal.getCurrentSong().getPublisher(), SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Title - Artist": {
                String[] output = mp3Filename.getText().split(" - ");

                setSongData(output[1], output[0], SongGlobal.getCurrentSong().getPublisher(), SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Track_Title_Artist": {
                String[] output = mp3Filename.getText().split("_");

                setSongData(output[2], output[1], SongGlobal.getCurrentSong().getPublisher(), SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Title-Artist": {
                String[] output = mp3Filename.getText().split("-");

                setSongData(output[1], output[0], SongGlobal.getCurrentSong().getPublisher(), SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "ISRC_Title_Artist_Publisher": {
                String[] output = mp3Filename.getText().split("_");

                setSongData(output[2], output[1], output[3], SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Title": {
                setSongData(SongGlobal.getCurrentSong().getArtist(), mp3Filename.getText().trim(), SongGlobal.getCurrentSong().getPublisher(), SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Track - Artist - Title": {
                String[] output = mp3Filename.getText().split(" - ");

                setSongData(output[1], output[2], SongGlobal.getCurrentSong().getPublisher(), SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Artist - Track - Title": {
                String[] output = mp3Filename.getText().split(" - ");

                setSongData(output[0], output[2], SongGlobal.getCurrentSong().getPublisher(), SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Artist-Title": {
                String[] output = mp3Filename.getText().split("-");

                setSongData(output[0], output[1], SongGlobal.getCurrentSong().getPublisher(), SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Track. Artist - Title_ISRC": {
                String[] output = mp3Filename.getText().split("\\.| - |_");

                setSongData(output[1], output[2], SongGlobal.getCurrentSong().getPublisher(), output[3]);

                isSet = true;
                break;
            }
            case "Artist-Title-CROREC-ISRC": {
                String[] output = mp3Filename.getText().split("-CROATIA-RECORDS-");
                String[] splitWords = output[0].split("-");

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

                setSongData(artist.toString().trim(), title.toString().trim(), "Crorec", output[1]);

                isSet = true;
                break;
            }
            case "Artist - Title - ISRC": {
                String[] output = mp3Filename.getText().split(" - ");

                setSongData(output[0], output[1], SongGlobal.getCurrentSong().getPublisher(), output[2]);

                isSet = true;
                break;
            }
            case "ISRC - Artist - Title - Publisher": {
                String[] output = mp3Filename.getText().split(" - ");

                setSongData(output[1], output[2], output[3], output[0]);

                isSet = true;
                break;
            }
            case "Artist - Title - Publisher - ISRC": {
                String[] output = mp3Filename.getText().split(" - ");

                setSongData(output[0], output[1], output[2], output[3]);

                isSet = true;
                break;
            }
            case "Artist_Title_ISRC_Publisher": {
                String[] output = mp3Filename.getText().split("_");

                setSongData(output[0], output[1], output[3], output[2]);

                isSet = true;
                break;
            }
            case "Track Artist - Title": {
                String[] output = mp3Filename.getText().split(" - ");
                String[] splitWords = output[0].split(" ", 2);

                setSongData(splitWords[1], output[1], SongGlobal.getCurrentSong().getPublisher(), SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Track Title - Artist": {
                String[] output = mp3Filename.getText().split(" - ");
                String[] splitWords = output[0].split(" ", 2);

                setSongData(output[1], splitWords[1], SongGlobal.getCurrentSong().getPublisher(), SongGlobal.getCurrentSong().getISRC());

                isSet = true;
                break;
            }
            case "Artist - Title ISRC": {
                String[] output = mp3Filename.getText().split(" - ");
                String[] splitWords = output[1].split(" ");
                String isrc = splitWords[splitWords.length - 1];

                setSongData(output[0], output[1].replace(" " + isrc, ""), SongGlobal.getCurrentSong().getPublisher(), isrc);

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
