package prodo.marc.gosling.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import org.hibernate.tool.schema.Action;
import prodo.marc.gosling.dao.Song;

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
        } else {
            //nothing selected
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
    }

    public void closeAndSave(ActionEvent event) throws IOException {
        SceneController.closeRegexParse(event);
    }
}
