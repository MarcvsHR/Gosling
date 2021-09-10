package prodo.marc.gosling.controllers;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.HelloApplication;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.hibernate.repository.SongRepository;
import prodo.marc.gosling.service.FileUtils;
import prodo.marc.gosling.service.ID3v2Utils;
import prodo.marc.gosling.service.Popups;
import prodo.marc.gosling.service.StringUtils;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class SongController {

    private static final Logger logger = LogManager.getLogger(SongController.class);

    @FXML MediaPlayer mplayer;
    @FXML Button songBackButton, addSongButton, addFolderButton;
    @FXML Button backSongs, buttonPlay, buttonPause, skipBack, skipForward, skipForwardSmall, skipBackSmall, buttonRevert;
    @FXML Label mp3Label, mp3Time, labelVolume;
    @FXML TableView<Song> songDatabaseTable;
    @FXML TableColumn<Song, String> tableArtist, tableTitle, tableAlbum, tablePublisher, tableComposer, tableGenre, tableISRC, tableFileLoc;
    @FXML TableColumn<Song, Integer> tableYear, tableID;
    @FXML TextField textAlbum, textArtist, textTitle, textPublisher, textComposer, textYear, textGenre, textISRC;
    @FXML Slider mp3Slider, volumeSlider;

    ObservableList<Song> songList = FXCollections.observableArrayList();

    private boolean updateCheck = true;
    final int skipIncrement = 10000;
    private Integer currentSongID = 0;
    ID3v2 copiedID3 = new ID3v24Tag();
    /**Initial volume for mp3*/
    private static final Integer INITIAL_VOLUME_SO_MY_EARS_DONT_EXPLODE=20;
    private String currentFileLoc = "";

    public void initialize() {

        logger.debug("----- Executing initialize");

        tableYear.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getYear()));
        tableID.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        tableArtist.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getArtist()));
        tableTitle.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTitle()));
        tableAlbum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAlbum()));
        tablePublisher.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPublisher()));
        tableComposer.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getComposer()));
        tableGenre.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getGenre()));
        tableISRC.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getISRC()));
        tableFileLoc.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getFileLoc()));

        updateTable();

        volumeSlider.setValue(INITIAL_VOLUME_SO_MY_EARS_DONT_EXPLODE);  changeVolume();

        //adding songs from c:\test so that I don't have to manually load every time I test
        //String fileLoc = "C:\\Users\\glazb\\Downloads";
        String fileLoc = "c:\\test";
        //String fileLoc = "C:\\Users\\glazb\\Music\\Unknown artist";
        try { addSongsFromFolder(new File(fileLoc)); }
        catch (IOException io) { logger.error("can't open folder",io); }

        logger.debug("----- ending initialize");
    }

    @FXML
    protected void backToMain() throws IOException {

        logger.debug("----- Executing backToMain");

        Stage stage = (Stage) songBackButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Songs");
        stage.setScene(scene);

        logger.debug("----- ending backToMain");
    }

    @FXML
    protected void clickedFolderButton() {
        logger.debug("----- Executing clickedFolderButton");
        File pickedFolder = FileUtils.pickFolder();
        try {
        addSongsFromFolder(pickedFolder); }
        catch (IOException io) {
            Popups.giveInfoAlert("Error", "Could not open folder", pickedFolder.getAbsolutePath());
            logger.error("can't open folder",io);
        }
        logger.debug("----- ending clickedFolderButton");
    }

    @FXML
    protected void addSongsFromFolder(File directory) throws IOException {

        logger.debug("----- Executing addSongsFromFolder");

        List<Path> mp3List = FileUtils.getFileListFromFolder(directory,"mp3");
        logger.debug("number of files in the list: "+mp3List.size());

        putMP3ListIntoDB(mp3List);
        updateTable();

        logger.debug("----- ending addSongsFromFolder");
   }

    private void putMP3ListIntoDB(List<Path> mp3List) {
        AtomicInteger i = new AtomicInteger();
        mp3List.forEach(file -> {
            i.getAndIncrement();
            logger.debug("processing file: "+ file);
            logger.debug(i+" out of "+mp3List.size());

            addMP3(file);
        });
    }

    private void addMP3(Path path) {

        logger.debug("----- Executing addMP3");

        SongRepository songRepo = new SongRepository();
        Song song;
        song = ID3v2Utils.songDataFromFile(new File(String.valueOf(path)));
        if (songRepo.checkForDupes(song)) {
            logger.debug("---song already exists - "+song);
            Popups.giveInfoAlert("Duplicate file import",
                    "There is already a song with that name or location in the database",
                    "adding file: "+song.getFileLoc());
        } else {
            SongRepository.addSong(song); }

        logger.debug("----- ending addMP3");
    }

    private void updateTable() {

        logger.debug("----- Executing updateTable");

        SongRepository songRepo = new SongRepository();
        songList.clear();
        songList.addAll(songRepo.getSongs());
        songDatabaseTable.setItems(songList);
        songDatabaseTable.getSelectionModel().select(currentSongID);

        logger.debug("----- ending updateTable");
    }

    @FXML
    public void clickTable(MouseEvent event) {

        logger.debug("----- Executing clickTable");

        if (event.getButton() == MouseButton.SECONDARY) {
            logger.debug("right click");
        }
        else if (event.getButton() == MouseButton.PRIMARY && !songDatabaseTable.getSelectionModel().isEmpty()) {
            try {
                openMP3(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
            } catch (Exception e) {
                logger.error("no table entry clicked",e);
            }
        }

        logger.debug("----- ending clickTable");
    }

    @FXML
    protected void openMP3(String fileLoc) {

        logger.debug("----- Executing openMP3");

        int changeCounter = 0;
        if (!Objects.equals(currentFileLoc, "")) {
            try {
                File file = new File(currentFileLoc);
                ID3v2 id3Data = ID3v2Utils.getID3(file);

                if (!StringUtils.compareStrings(textArtist.getText(), id3Data.getArtist())) {
                    changeCounter++;}
                if (!StringUtils.compareStrings(textTitle.getText(), id3Data.getTitle())) {
                    changeCounter++;}
                if (!StringUtils.compareStrings(textAlbum.getText(), id3Data.getAlbum())) {
                    changeCounter++;}
                if (!StringUtils.compareStrings(textPublisher.getText(), id3Data.getPublisher())) {
                    changeCounter++;}
                if (!StringUtils.compareStrings(textComposer.getText(), id3Data.getComposer())) {
                    changeCounter++;}
                if (!StringUtils.compareStrings(textYear.getText(), id3Data.getYear())) {
                    changeCounter++;}
                if (!StringUtils.compareStrings(textGenre.getText(), id3Data.getGenreDescription())) {
                    changeCounter++;}
//                if (!StringUtils.compareStrings(textISRC.getText(), id3Data.getISRC())) {
//                    changeCounter++;}
            } catch (Exception problem) {
                logger.error("Error while opening file " + currentFileLoc, problem);
            }
        }

        if (changeCounter > 0) {
            boolean result = Popups.giveConfirmAlert("Unsaved changes",
                    "You are switching to another file with possible unsaved changes",
                    "Do you want to save the ID3 changes you have made?");

            if (result) {
                updateMP3();
            } else {
                songDatabaseTable.getSelectionModel().select(currentSongID);
                logger.debug("i should be selecting number: "+currentSongID);
            }
        } else {
            updateTextFields(fileLoc);
        }

        logger.debug("----- ending openMP3");
    }

    private void updateTextFields(String fileLoc) {

        logger.debug("----- Executing updateTextFields");

        //close old mp3
        if (mplayer != null) {
            mplayer.stop();
            mplayer.dispose();
        }

        //show file name
        mp3Label.setText(fileLoc);
        currentFileLoc = fileLoc;

        //load file into media player
        openMediaFile(fileLoc);

        //load id3 data into text fields
        try {
            ID3v2 id3Data = ID3v2Utils.getID3(new File(fileLoc));
            textArtist.setText(id3Data.getArtist());
            textArtist.setStyle("-fx-background-color: #FFFFFF");
            textTitle.setText(id3Data.getTitle());
            textAlbum.setText(id3Data.getAlbum());
            textPublisher.setText(id3Data.getPublisher());
            textComposer.setText(id3Data.getComposer());
            textYear.setText(id3Data.getYear());
            textGenre.setText(id3Data.getGenreDescription());
            //textISRC.setText(id3Data.getISRC());

            //set slider to tick = 0.1s precision
            double sliderValue = new Mp3File(new File(fileLoc)).getLengthInMilliseconds();
            mp3Slider.setMax(sliderValue / 100);

            currentSongID = songDatabaseTable.getSelectionModel().getSelectedIndex();
            logger.debug("change song to: "+currentSongID);

        } catch (Exception report) {
            logger.error("Error while opening file " + fileLoc, report);
        }

        logger.debug("----- ending updateTextFields");
    }

    @FXML
    protected void playMP3() {

        logger.debug("----- Executing playMP3");

        //none is loaded, load file first
        if (mplayer == null) {
            logger.debug("no file open to play");
        } else {

            //play file
            mplayer.setVolume(volumeSlider.getValue() / 100);
            mplayer.play();

            //timer for updating current position slider
            Timer sliderUpdateTimer = new Timer();
            TimerTask sliderUpdateTask = new TimerTask() {
                public void run() {
                    double currentTime = mplayer.getCurrentTime().toSeconds();
                    int minutes = (int) (currentTime / 60);
                    double seconds = currentTime - minutes * 60;
                    DecimalFormat df = new DecimalFormat("##.#");
                    df.setRoundingMode(RoundingMode.DOWN);
                    String secondsString = df.format(seconds);
                    if (!secondsString.contains(".")) {
                        secondsString += ".0";
                    }
                    if (seconds < 10) {
                        secondsString = "0" + secondsString;
                    }
                    String finalSecondsString = secondsString;
                    Platform.runLater(() -> {
                        //show current time text, needs improving
                        mp3Time.setText(String.format("%02xm ", minutes) + finalSecondsString + "s");
                        //update slider to current time
                        if (updateCheck) {
                            mp3Slider.setValue(mplayer.getCurrentTime().toMillis() / 100);
                        }
                    });
                }
            };
            sliderUpdateTimer.scheduleAtFixedRate(sliderUpdateTask, 100, 100);
        }

        logger.debug("----- ending playMP3");

    }

    @FXML
    protected void pauseMP3() {
        if (mplayer != null) {
            mplayer.pause(); }
    }

    @FXML
    protected void moveTime() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mp3Slider.getValue() *100));
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

    @FXML
    protected void moveTimeForwardLittle() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() + 100)); }
    }

    @FXML
    protected void moveTimeBackLittle() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() - 100)); }
    }

    public void updateMP3() {

        logger.debug("----- Executing updateMP3");

        Song song = new Song();

        song.setId(currentSongID+1);
        song.setArtist(textArtist.getText());
        song.setTitle(textTitle.getText());
        song.setAlbum(textAlbum.getText());
        song.setPublisher(textPublisher.getText());
        song.setComposer(textComposer.getText());
        if (textYear.getText() != null) {song.setYear(Integer.valueOf(textYear.getText()));}
        song.setGenre(textGenre.getText());
        song.setISRC(textISRC.getText());
        song.setFileLoc(currentFileLoc);

        //unload song if playing so the file can be saved
        if (mplayer != null) { mplayer.dispose(); }

        //save file
        writeToMP3(song, currentFileLoc);

        //update database and table
        SongRepository.addSong(song);
        updateTable();
        makeTextFieldsWhite();

        logger.debug("----- ending updateMP3");
    }

    private void makeTextFieldsWhite() {
        textArtist.setStyle("-fx-background-color: #FFFFFF");
    }

    private void writeToMP3(Song song, String fileLoc) {

        logger.debug("----- Executing writeToMP3");

        if (mplayer != null) { mplayer.dispose(); }

        String backupFileLoc = currentFileLoc + ".bak";
        try {
            Mp3File mp3 = new Mp3File(fileLoc);
            ID3v24Tag id3Data = new ID3v24Tag();
            mp3.setId3v2Tag(id3Data);

            id3Data.setArtist(song.getArtist());
            id3Data.setTitle(song.getTitle());
            id3Data.setAlbum(song.getAlbum());
            id3Data.setPublisher(song.getPublisher());
            id3Data.setComposer(song.getComposer());
            if (song.getYear() != null) { id3Data.setYear(Integer.toString(song.getYear()));
            } else { id3Data.setYear("0"); }
            if (song.getGenre() != null) { id3Data.setGenreDescription(song.getGenre());
            } else { id3Data.setGenreDescription(""); }
            //id3Data.setISRC(song.getISRC());

            mp3.save(backupFileLoc);
        } catch (Exception e) {
            logger.error("Error while opening file " + fileLoc, e);
        }

        try { Files.delete(Path.of(fileLoc));
        } catch (IOException e) { logger.debug("File cannot be deleted! " + fileLoc);
        }
        File mp3FileNew = new File(backupFileLoc);
        boolean info = mp3FileNew.renameTo(new File(fileLoc));
        logger.debug("rename results are " + info);

        logger.debug("----- ending writeToMP3");
    }

    private void openMediaFile(String fileLoc) {
        logger.debug("----- Executing openMediaFile");

        File mp3File = new File(fileLoc);
        String mp3Path = mp3File.toURI().toASCIIString();
        Media mp3Media = new Media(mp3Path);
        mplayer = new MediaPlayer(mp3Media);

        logger.debug("----- ending openMediaFile");
    }

    public void addSong2DB(ActionEvent actionEvent) {
        logger.debug("----- Executing addSong2DB");

        //select file
        File mp3 = FileUtils.openFile("MP3 files (*.mp3)","mp3");

        //add file to DB
        addMP3(mp3.toPath());

        //preview changes
        updateTable();

        logger.debug("----- ending addSong2DB");
    }

    public void checkArtistField(KeyEvent inputMethodEvent) {
        logger.debug("----- Executing checkArtistField");

        ID3v2 id3Data = ID3v2Utils.getID3(new File(currentFileLoc));
        if (!StringUtils.compareStrings(id3Data.getArtist(), textArtist.getText())) {
            textArtist.setStyle("-fx-background-color: #FFCCBB");
        } else {
            textArtist.setStyle("-fx-background-color: #FFFFFF");
        }

        logger.debug("----- ending checkArtistField");
    }

    public void revertID3(ActionEvent actionEvent) {
        logger.debug("----- Executing revertID3");
        boolean result = Popups.giveConfirmAlert("Unsaved changes",
                "You are resetting the ID3 changes you made for this MP3",
                "Do you want to load the old data without saving the changes?");

        if (result){
            updateTextFields(currentFileLoc);
        }

        logger.debug("----- ending revertID3");
    }

    public void changeVolume() {
        labelVolume.setText("Volume: "+ String.format("%.0f",volumeSlider.getValue())+"%");
        if (mplayer != null) {
            mplayer.setVolume(volumeSlider.getValue()/100);
        }
    }

    public void copyID3(ActionEvent actionEvent) {
        logger.debug("----- Executing copyID3");

        copiedID3 = ID3v2Utils.getID3(new File(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc()));
        logger.debug(copiedID3.getArtist());
        songDatabaseTable.getSelectionModel().select(currentSongID);

        logger.debug("----- ending copyID3");
    }

    public void pasteID3(ActionEvent actionEvent) {
        logger.debug("----- Executing pasteID3");

        songDatabaseTable.getSelectionModel().select(currentSongID);

        logger.debug("----- ending pasteID3");
    }

    public void deleteFile(ActionEvent actionEvent) {
        logger.debug("----- Executing deleteFile");

        String fileLoc = songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc();

        String[] dialogData = {"Database entry", "ID3 data", "File"};

        ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData[2], dialogData);
        dialog.setTitle("Delete");
        dialog.setHeaderText("Select what you want to delete");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String picked = result.get();
            logger.debug(picked);
            if (Objects.equals(picked, "File")) {
                Song song = songDatabaseTable.getSelectionModel().getSelectedItem();
                SongRepository.delete(song);
                updateTable();
                try { Files.delete(Path.of(fileLoc));
                } catch (IOException er) {
                    logger.error("can't delete file: ",er); }
            }
            else if (Objects.equals(picked, "Database entry")) {
                Song song =  songDatabaseTable.getSelectionModel().getSelectedItem();
                SongRepository.delete(song);
                updateTable();
            } else {
                logger.debug("code to delete id3 tag here");
            }
        }

        logger.debug("----- ending deleteFile");
    }


}

