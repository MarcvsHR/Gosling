package prodo.marc.gosling.controllers;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javafx.application.Platform;
import javafx.collections.ObservableMap;
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

    @FXML
    MediaPlayer mplayer;

    @FXML
    Label mp3Label, mp3Time;

    @FXML
    TextField textArtist, textTitle;

    @FXML
    Button backSongs, pickFile, buttonPlay, buttonPause, skipBack, skipForward;

    @FXML
    Slider mp3Slider;

    private boolean updateCheck = true;
    final int skipIncrement = 10000;


    @FXML
    protected void showSongWindow() throws IOException {
        Stage stage = (Stage) backSongs.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/song-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Songs");
        stage.setScene(scene);
    }

    @FXML
    protected void openMP3(){
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("C:\\intel"));
        FileChooser.ExtensionFilter mp3Filter = new FileChooser.ExtensionFilter( "MP3","*.mp3");
        fc.getExtensionFilters().add(mp3Filter);
        File mp3File = fc.showOpenDialog(null);
        mp3Label.setText(mp3File.getName());
        String mp3Path = mp3File.toURI().toASCIIString();
        Media mp3Media = new Media(mp3Path);
        mplayer = new MediaPlayer(mp3Media);
        try {
            Mp3File song = new Mp3File(mp3File);

        ID3v2 tag= song.getId3v2Tag();

            System.out.println("tag: "+tag.getAlbumArtist());
            System.out.println("tag: "+tag.getAlbum());
            System.out.println("tag: "+tag.getComposer());




            System.out.println("tag2: "+song.getId3v1Tag()  );

        }catch (Exception ignored){
            System.out.println(ignored);
        }



        ObservableMap<String, Object> id3Data =  mp3Media.getMetadata();
        System.out.println(id3Data.size());
//        textArtist.setText(id3Data.get("artist").toString());
//        textTitle.setText(id3Data.get("title").toString());

    }

    @FXML
    protected void playMP3() {
        if (mplayer == null) {
            openMP3(); }
        mplayer.play();

        Timer sliderUpdateTimer =  new Timer();
        TimerTask sliderUpdateTask =  new TimerTask() {
            public void run() {
                double currentTime = mplayer.getCurrentTime().toSeconds();
                double percentTime = currentTime / mplayer.getStopTime().toSeconds();
                int minutes = (int) (currentTime / 60);
                double seconds = currentTime - minutes * 60;
                Platform.runLater(() -> {
                    mp3Time.setText(String.format("%02x m - %03.1f s",minutes,seconds));
                    if (updateCheck) { mp3Slider.setValue(percentTime * mp3Slider.getMax()); }
                });
            }
        };
        sliderUpdateTimer.scheduleAtFixedRate(sliderUpdateTask, 90,90);

    }
    @FXML
    protected void pauseMP3() {
        if (mplayer != null) {
            mplayer.pause(); }
    }

    @FXML
    protected void moveTime() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mp3Slider.getValue()/mp3Slider.getMax()*mplayer.getStopTime().toMillis()));
            updateCheck = true;
        }
    }

    @FXML
    protected void sliderDrag() {
        updateCheck = false;
    }

    @FXML
    protected void moveTimeForward() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() + skipIncrement)); }
    }

    @FXML
    protected void moveTimeBack() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() - skipIncrement)); }
    }
}
