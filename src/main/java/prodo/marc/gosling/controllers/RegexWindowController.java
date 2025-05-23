package prodo.marc.gosling.controllers;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.dao.id3Header;
import prodo.marc.gosling.hibernate.repository.SongRepository;
import prodo.marc.gosling.service.FileUtils;
import prodo.marc.gosling.service.SongGlobal;
import prodo.marc.gosling.service.id3.ID3Reader;

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
    public Label labelComposer;

    List<Song> songList = SongGlobal.getSongList();
    Song song = new Song(songList.get(0));
    Song originalSong = new Song(song);


    private List<String> getRegexStuff() {
        List<String> regex = new ArrayList<>();
        if (StringUtils.countMatches(mp3Filename.getText(), "_") == 2) {
            regex.add("[Track_Title_Artist]");
            regex.add("[Track_Artist_Title]");
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
            regex.add("[Track Artist - Title ISRC]");
        }
        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 2) {
            regex.add("[Track - Artist - Title]");
            regex.add("[Artist - Track - Title]");
            regex.add("[Artist - Title - ISRC]");
            regex.add("[ISRC - Artist - Title]");
            regex.add("[Artist - Title - Artist]");
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
            regex.add("[Artist-Title-ISRC]");
        }

        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 1 &&
                StringUtils.countMatches(mp3Filename.getText(), "_") == 1 &&
                StringUtils.countMatches(mp3Filename.getText(), ".") == 1) {
            regex.add("[Track. Artist - Title_ISRC]");
        }
        if (StringUtils.countMatches(mp3Filename.getText(), " - ") == 1 &&
            StringUtils.countMatches(mp3Filename.getText(), "(") == 1 &&
            StringUtils.countMatches(mp3Filename.getText(), ")") == 1) {
            regex.add("[Track. Artist - Title (Composer)]");
        }

        regex.add("[Title]]");

        Collections.sort(regex);
        return new ArrayList<>(regex);
    }

    public void setSongData(String artist, String title, String publisher, String isrc, Song song) {
        song.setArtist(artist);
        song.setTitle(title);
        song.setPublisher(publisher);
        song.setISRC(isrc);
    }

    public void setSongData(String artist, String title, String publisher, String isrc, String composer, Song song) {
        song.setComposer(composer);
        setSongData(artist,title,publisher,isrc,song);
    }

    public void changeSelectedRegex() {
        String selection = getSelection();
        updateSong(selection, song);

        song.setTitle(song.getTitle().replace("[Clean]", ""));

        labelArtist.setText("Artist: " + song.getArtist());
        labelTitle.setText("Title: " + song.getTitle());
        labelPublisher.setText("Publisher: " + song.getPublisher());
        labelISRC.setText("ISRC: " + song.getISRC());
        labelComposer.setText("Composer: " + song.getComposer());
    }

    private String getSelection() {
        String selection = regexList.getSelectionModel().getSelectedItem();
        if (selection == null) selection = "";
        if (!selection.isEmpty()) selection = selection.substring(selection.indexOf("[") + 1, selection.indexOf("]"));
        return selection;
    }

    private void updateSong(String selection, Song song) {
        String mp3Location = getFileName(song.getFileLoc());
        mp3Location = mp3Location.replace("–", "-");
        String[] output;
        switch (selection) {
            case "Track_Artist_Title": {
                output = mp3Location.split("_");

                setSongData(output[1], output[2], originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "Artist - Title": {
                output = mp3Location.split(" - ");

                setSongData(output[0], output[1], originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "Title - Artist": {
                output = mp3Location.split(" - ");

                setSongData(output[1], output[0], originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "Track_Title_Artist": {
                output = mp3Location.split("_");

                setSongData(output[2], output[1], originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "Title-Artist": {
                output = mp3Location.split("-");

                setSongData(output[1], output[0], originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "ISRC_Title_Artist_Publisher": {
                output = mp3Location.split("_");

                setSongData(output[2], output[1], output[3], originalSong.getISRC(), song);

                break;
            }
            case "Title": {
                setSongData(originalSong.getArtist(), mp3Location.trim(), originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "Track - Artist - Title": {
                output = mp3Location.split(" - ");

                setSongData(output[1], output[2], originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "Artist - Track - Title": {
                output = mp3Location.split(" - ");

                setSongData(output[0], output[2], originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "Artist-Title": {
                output = mp3Location.split("-");

                setSongData(output[0], output[1], originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "Track. Artist - Title_ISRC": {
                output = mp3Location.split("\\.| - |_");

                setSongData(output[1], output[2], originalSong.getPublisher(), output[3], song);

                break;
            }
            case "Artist-Title-CROREC-ISRC": {
                output = mp3Location.split("-CROATIA-RECORDS-");
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

                setSongData(artist.toString().trim(), title.toString().trim(), "Crorec", output[1], song);

                break;
            }
            case "Artist-Title-ISRC": {
                String[] splitWords = mp3Location.split("-");

                String artist = splitWords[0];
                String isrc = splitWords[splitWords.length-1];
                //remove first and last element from the splitWords array
                StringBuilder title = new StringBuilder();
                for (int i = 1; i < splitWords.length-1; i++) {
                    title.append(splitWords[i]).append(" ");
                }
                setSongData(artist.trim(), title.toString().trim(), originalSong.getPublisher(), isrc, song);

                break;
            }
            case "Artist - Title - ISRC": {
                output = mp3Location.split(" - ");

                setSongData(output[0], output[1], originalSong.getPublisher(), output[2], song);

                break;
            }
            case "ISRC - Artist - Title": {
                output = mp3Location.split(" - ");

                setSongData(output[1], output[2], originalSong.getPublisher(), output[0], song);

                break;
            }
            case "ISRC - Artist - Title - Publisher": {
                output = mp3Location.split(" - ");

                setSongData(output[1], output[2], output[3], output[0], song);

                break;
            }
            case "Artist - Title - Publisher - ISRC": {
                output = mp3Location.split(" - ");

                setSongData(output[0], output[1], output[2], output[3], song);

                break;
            }
            case "Artist_Title_ISRC_Publisher": {
                output = mp3Location.split("_");

                setSongData(output[0], output[1], output[3], output[2], song);

                break;
            }
            case "Track Artist - Title": {
                output = mp3Location.split(" - ");
                String[] splitWords = output[0].split(" ", 2);

                setSongData(splitWords[1], output[1], originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "Track Title - Artist": {
                output = mp3Location.split(" - ");
                String[] splitWords = output[0].split(" ", 2);

                setSongData(output[1], splitWords[1], originalSong.getPublisher(), originalSong.getISRC(), song);

                break;
            }
            case "Artist - Title ISRC": {
                output = mp3Location.split(" - ");
                String[] splitWords = output[1].split(" ");
                String isrc = splitWords[splitWords.length - 1];

                setSongData(output[0], output[1].replace(" " + isrc, ""), originalSong.getPublisher(), isrc, song);

                break;
            }
            case "Track Artist - Title ISRC": {
                output = mp3Location.split(" - ");
                String[] splitWords1 = output[0].split(" ", 2);
                String[] splitWords2 = output[1].split(" ");
                String isrc = splitWords2[splitWords2.length - 1];

                setSongData(splitWords1[1], output[1].replace(" " + isrc, ""), originalSong.getPublisher(), isrc, song);

                break;
            }
            case "Track. Artist - Title (Composer)": {
                output = mp3Location.split(" - ");
                String[] splitWords1 = output[0].split(". ", 2);
                String[] splitWords2 = output[1].split( " [(]");
                splitWords2[1] = splitWords2[1].replace(")","");

                setSongData(splitWords1[1], splitWords2[0], originalSong.getPublisher(), originalSong.getISRC(), splitWords2[1], song);
                break;
            }
            case "Artist - Title - Artist": {
                output = mp3Location.split(" - ");

                setSongData(output[0], output[1], originalSong.getPublisher(), originalSong.getISRC(), originalSong.getComposer(), song);
                break;
            }
        }
    }

    public void initialize() {

        mp3Filename.setText(getFileName(song.getFileLoc()));
        regexList.getItems().addAll(getRegexStuff());

    }

    private String getFileName(String fileLoc) {
        return new File(fileLoc).getName().replaceAll("(?i).mp3", "");
    }

    public void closeAndSave(ActionEvent event) {

        for (Song songInList : songList) {
            String seletion = getSelection();
            updateSong(seletion, songInList);
            System.out.println(songInList.getId() + " - " +songInList.getArtist() + " - " + songInList.getTitle() + " - "
                    + songInList.getPublisher() + " - " + songInList.getISRC() + " - " + songInList.getComposer() + " - " + songInList.getFileLoc());
            SongRepository.addSong(songInList);
            MyID3 id3 =  ID3Reader.getTag(new File(songInList.getFileLoc()));
            id3.setFrame(id3Header.ARTIST, songInList.getArtist());
            id3.setFrame(id3Header.TITLE, songInList.getTitle());
            id3.setFrame(id3Header.PUBLISHER, songInList.getPublisher());
            id3.setFrame(id3Header.ISRC, songInList.getISRC());
            id3.setFrame(id3Header.COMPOSER, songInList.getComposer());
            FileUtils.writeToMP3(id3, songInList.getFileLoc(),false);
            if (songList.size() == 1) {
                SongController.publicTextArtist.setText(songInList.getArtist());
                SongController.publicTextTitle.setText(songInList.getTitle());
                SongController.publicTextPublisher.setText(songInList.getPublisher());
                SongController.publicTextISRC.setText(songInList.getISRC());
                SongController.publicTextComposer.setText(songInList.getComposer());
            }
        }
        publicButtonRefresh.setStyle("-fx-background-color: #bb3333");

        //close window
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }


}
