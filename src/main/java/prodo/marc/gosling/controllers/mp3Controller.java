package prodo.marc.gosling.controllers;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import prodo.marc.gosling.HelloApplication;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class mp3Controller {

    public TextField textAlbum, textArtist, textTitle, textPublisher, textComposer, textYear, textGenre, textISRC;

    @FXML
    MediaPlayer mplayer;

    @FXML
    Label mp3Label, mp3Time;

    @FXML
    Button backSongs, pickFile, buttonPlay, buttonPause, skipBack, skipForward;

    @FXML
    Slider mp3Slider;

    private boolean updateCheck = true;
    final int skipIncrement = 10000;


    @FXML
    //switch back to song database window
    protected void showSongWindow() throws IOException {
        Stage stage = (Stage) backSongs.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("view/song-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Songs");
        stage.setScene(scene);
    }

    @FXML
    //open mp3 file for playing and reading id3 data
    protected void openMP3(){
        //pick file
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("C:\\intel"));
        FileChooser.ExtensionFilter mp3Filter = new FileChooser.ExtensionFilter( "MP3","*.mp3");
        fc.getExtensionFilters().add(mp3Filter);
        File mp3File = fc.showOpenDialog(null);

        //show file name
        mp3Label.setText(mp3File.getName());

        //load file into media player
        String mp3Path = mp3File.toURI().toASCIIString();
        Media mp3Media = new Media(mp3Path);
        mplayer = new MediaPlayer(mp3Media);

        //load id3 data into text fields
        //should prolly throw a message if empty
        //still needs ISRC function
        try {
            Mp3File song = new Mp3File(mp3File);
            ID3v2 id3Data= song.getId3v2Tag();
            textArtist.setText(id3Data.getArtist());
            textTitle.setText(id3Data.getTitle());
            textAlbum.setText(id3Data.getAlbum());
            textPublisher.setText(id3Data.getPublisher());
            textComposer.setText(id3Data.getComposer());
            textYear.setText(id3Data.getYear());
            textGenre.setText(id3Data.getGenreDescription());
            //textISRC.setText(id3Data.getISRC());

            //set slider to tick = 0.1s precision
            mp3Slider.setMax(song.getLengthInMilliseconds()/100);

        }catch (Exception ignored){
            System.out.println(ignored);
        }

    }

    @FXML
    //play mp3 file
    protected void playMP3() {
        //none is loaded, load file first
        if (mplayer == null) {
            openMP3(); }

        //play file
        mplayer.play();

        //timer for updating current position slider
        Timer sliderUpdateTimer =  new Timer();
        TimerTask sliderUpdateTask =  new TimerTask() {
            public void run() {
                double currentTime = mplayer.getCurrentTime().toSeconds();
                int minutes = (int) (currentTime / 60);
                double seconds = currentTime - minutes * 60;
                Platform.runLater(() -> {
                    //show current time text, needs improving
                    mp3Time.setText(String.format("%02x m - %03.1f s",minutes,seconds));
                    //update slider to current time
                    if (updateCheck) { mp3Slider.setValue(mplayer.getCurrentTime().toMillis()/100); }
                });
            }
        };
        sliderUpdateTimer.scheduleAtFixedRate(sliderUpdateTask, 100,100);

    }
    @FXML
    //pause mp3 if loaded
    protected void pauseMP3() {
        if (mplayer != null) {
            mplayer.pause(); }
    }

    @FXML
    //enables the moving of the slider to the time you want to listen at
    protected void moveTime() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mp3Slider.getValue() *100));
            updateCheck = true;
        }
    }

    @FXML
    //disable slider updates while dragging
    protected void sliderDrag() {
        updateCheck = false;
    }

    @FXML
    //move forward X seconds button
    protected void moveTimeForward() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() + skipIncrement)); }
    }

    @FXML
    //move back X seconds button
    protected void moveTimeBack() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() - skipIncrement)); }
    }
}
