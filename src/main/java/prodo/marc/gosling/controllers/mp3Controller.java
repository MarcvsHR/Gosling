package prodo.marc.gosling.controllers;

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
    int skipIncrement = 10000;


    @FXML
    protected void showSongWindow() throws IOException {
        Stage stage = (Stage) backSongs.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/song-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Songs");
        stage.setScene(scene);
    }

    @FXML
    protected void openMP3() {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("C:\\intel"));
        FileChooser.ExtensionFilter mp3Filter = new FileChooser.ExtensionFilter( "MP3","*.mp3");
        fc.getExtensionFilters().add(mp3Filter);
        File mp3File = fc.showOpenDialog(null);
        //System.out.println(file.getAbsoluteFile());
        mp3Label.setText(mp3File.getName());
        Media mp3Media = new Media(mp3File.toURI().toString());
        ObservableMap id3Data = mp3Media.getMetadata();
        mplayer = new MediaPlayer(mp3Media);
        mp3Slider.setMin(0);
        mp3Slider.setValue(0);
        System.out.println(id3Data.get("artist"));
//        textArtist.setText(id3Data.get("artist").toString());
//        textTitle.setText(id3Data.get("title").toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            double currentTime = mplayer.getCurrentTime().toSeconds();
                            double percentTime = currentTime / mplayer.getStopTime().toSeconds();
                            if (updateCheck) { mp3Slider.setValue(percentTime * mp3Slider.getMax()); }
                            int minutes = (int) (currentTime / 60);
                            double seconds = currentTime - minutes * 60;
                            mp3Time.setText(String.format("%x m - %.1f s",minutes,seconds));
                        }
                    });
                    //System.out.println(currentTime);
                    try {
                        Thread.sleep(90);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @FXML
    protected void playMP3() {
        if (mplayer == null) {
            openMP3(); }
        mplayer.play();
    }
    @FXML
    protected void pauseMP3() {
        if (mplayer != null) {
            mplayer.pause(); }
    }

    @FXML
    protected void moveTime() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mp3Slider.getValue()/mp3Slider.getMax()*mplayer.getStopTime().toMillis()));  }
        updateCheck = true;
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