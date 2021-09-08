package prodo.marc.gosling.controllers;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.log4j.LogManager;
import prodo.marc.gosling.HelloApplication;
import prodo.marc.gosling.dao.Song;

import org.apache.log4j.Logger;
import prodo.marc.gosling.hibernate.repository.SongRepository;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


public class SongController {

    private static final Logger logger = LogManager.getLogger(SongController.class);

    @FXML MediaPlayer mplayer;
    @FXML Button songBackButton, addSongButton, addFolderButton;
    @FXML Button backSongs, buttonPlay, buttonPause, skipBack, skipForward;
    @FXML Label mp3Label, mp3Time;
    @FXML TableView<Song> songDatabaseTable;
    @FXML TableColumn<Song, String> tableArtist, tableTitle, tableAlbum, tablePublisher, tableComposer, tableGenre, tableISRC, tableFileLoc;
    @FXML TableColumn<Song, Integer> tableYear, tableID;
    @FXML TextField textAlbum, textArtist, textTitle, textPublisher, textComposer, textYear, textGenre, textISRC;
    @FXML Slider mp3Slider;

    ObservableList<Song> songList = FXCollections.observableArrayList();

    private boolean updateCheck = true;
    final int skipIncrement = 10000;
    private Integer currentSongID = 0;

    public void initialize() {
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

    }

    @FXML
    //goes back to the main window
    protected void backToMain() throws IOException {
        Stage stage = (Stage) songBackButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Songs");
        stage.setScene(scene);
    }

    @FXML
    protected void addSongsFromFolder() {
        SongRepository songRepo = new SongRepository();

        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File("C:\\test"));
        File directory = dc.showDialog(null);

        List<Path> mp3List = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(Paths.get(directory.getAbsolutePath()))) {
            walk.filter(Files::isRegularFile).forEach(file -> {
                if (file.toString().endsWith(".mp3")) {
                    mp3List.add(file);
                }
            });
            logger.debug("number of files in the list: "+mp3List.size());

            AtomicInteger i = new AtomicInteger();
            mp3List.forEach(file -> {
                i.getAndIncrement();
                Song id3Tag;
                logger.debug("processing file: "+ file);
                logger.debug(i+" out of "+mp3List.size());
                id3Tag = getID(file.toFile());
                songRepo.addSong(id3Tag);
            });
            logger.debug("Songs on end -> \n: "+Arrays.toString(songRepo.getSongs().toArray()));

            updateTable();
            logger.debug(Arrays.toString(songDatabaseTable.getItems().toArray()));

        } catch (IOException e) {
            logger.error("couldn't get files from folder",e);
        }
   }

    private void updateTable() {
        SongRepository songRepo = new SongRepository();
        songList.clear();
        songList.addAll(songRepo.getSongs());
        songDatabaseTable.setItems(songList);
    }

    public Song getID(File file) {

       Song testSong = new Song();

        try {
            Mp3File mp3 = new Mp3File(file);
            ID3v2 id3Data = mp3.getId3v2Tag();

            testSong.setArtist(id3Data.getArtist());
            testSong.setTitle(id3Data.getTitle());
            testSong.setAlbum(id3Data.getAlbum());
            testSong.setPublisher(id3Data.getPublisher());
            testSong.setComposer(id3Data.getComposer());
            int year = 0;
            try {
                year = Integer.parseInt(id3Data.getYear());
            } catch (NumberFormatException e) {
                //logger.error(id3Data.getYear() +" in file "+file.getAbsoluteFile()+"is not a number", e);
                logger.debug("file "+file.getAbsolutePath()+" does not have a year");
            }
            testSong.setYear(year);
            testSong.setGenre(id3Data.getGenreDescription());
            //testSong.setISRC(id3Data.getISRC());
            testSong.setISRC("ISRC");
            //logger.debug(file.getAbsoluteFile());
            //logger.debug(testSong.toString());
            testSong.setFileLoc(file.getAbsoluteFile().toString());
        }catch (Exception ignored){
            //logger.error("Error while opening file "+file.getAbsolutePath(),ignored);
            logger.debug("Can't get id3 from file: "+file.getAbsolutePath());
        }

       return testSong;
   }

   @FXML
    public void doubleClickTable(MouseEvent event) {
        if (event.getClickCount() == 2) {
            try {
                TableView.TableViewSelectionModel<Song> p = songDatabaseTable.getSelectionModel();
                openMP3(p.getSelectedItem().getFileLoc());
                currentSongID = p.getSelectedItem().getId();
            } catch (Exception e) {
                logger.error("nothing found on double click",e);
            }
        }
    }

    @FXML
    //open mp3 file for playing and reading id3 data
    protected void openMP3(String fileLoc){

        logger.debug("Executing openMP3....");

        //pick file and close old mp3
        File mp3File = new File(fileLoc);
        if (mplayer != null) {
            mplayer.stop();
        }

        //show file name
        mp3Label.setText(mp3File.getAbsolutePath());

        //load file into media player
        openMediaFile(fileLoc);

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
            logger.error("Error while opening file "+mp3File.getAbsolutePath(),ignored);
        }
    }

    @FXML
    //play mp3 file
    protected void playMP3() {
        //none is loaded, load file first
        if (mplayer == null) {
            logger.debug("no file open to play");
        }

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

    public void updateMP3(ActionEvent actionEvent) {
        Song song = new Song();
        File mp3File = new File(mp3Label.getText());

        song.setId(currentSongID);
        song.setArtist(textArtist.getText());
        song.setTitle(textTitle.getText());
        song.setAlbum(textAlbum.getText());
        song.setPublisher(textPublisher.getText());
        song.setComposer(textComposer.getText());
        song.setYear(Integer.valueOf(textYear.getText()));
        song.setGenre(textGenre.getText());
        song.setISRC(textISRC.getText());
        song.setFileLoc(mp3Label.getText());

        try {
            Mp3File mp3 = new Mp3File(mp3File);
            ID3v24Tag id3Data = new ID3v24Tag();
            mp3.setId3v2Tag(id3Data);

            id3Data.setArtist(song.getArtist());
            id3Data.setTitle(song.getTitle());
            id3Data.setAlbum(song.getAlbum());
            id3Data.setPublisher(song.getPublisher());
            id3Data.setComposer(song.getComposer());
            id3Data.setYear(Integer.toString(song.getYear()));
            id3Data.setGenreDescription(song.getGenre());
            //id3Data.setISRC(song.getISRC());

            mp3.save(mp3Label.getText()+".mp3");

            mplayer.dispose();

            boolean info = mp3File.delete();
            logger.debug("delete results are "+ info);
            File mp3FileNew = new File(mp3Label.getText()+".mp3");
            info = mp3FileNew.renameTo(mp3File);
            logger.debug("rename results are "+ info);

            logger.debug("file data updated");

            //load file into media player
            openMediaFile(mp3Label.getText());


        }catch (Exception e){
            logger.error("Error while opening file "+mp3File.getAbsolutePath(),e);
        }

        SongRepository songRepo = new SongRepository();
        songRepo.addSong(song);

        updateTable();

    }

    private void openMediaFile(String fileLoc) {
        File mp3File = new File(fileLoc);
        String mp3Path = mp3File.toURI().toASCIIString();
        Media mp3Media = new Media(mp3Path);
        mplayer = new MediaPlayer(mp3Media);
    }

    public void addSong2DB(ActionEvent actionEvent) {
        //select file

        //add file to DB
    }
}

