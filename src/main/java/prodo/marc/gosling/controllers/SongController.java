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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.log4j.LogManager;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelperBuilder;
import prodo.marc.gosling.HelloApplication;
import prodo.marc.gosling.dao.Song;

import org.apache.log4j.Logger;
import prodo.marc.gosling.hibernate.repository.SongRepository;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


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

    public void initialize() {

        logger.debug("Executing initialize....");

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

        //set starting volume to 20 so I don't get my ears blown out
        volumeSlider.setValue(20);
        changeVolume();

        //adding songs from c:\test so that I don't have to manually load every time I test
        String fileLoc = "C:\\Users\\glazb\\Downloads";
        //String fileLoc = "c:\\test";
        //String fileLoc = "C:\\Users\\glazb\\Music\\Unknown artist";
        File loadFiles = new File(fileLoc);
        addSongsFromFolder(loadFiles);
    }

    @FXML
    //goes back to the main window
    protected void backToMain() throws IOException {

        logger.debug("Executing backToMain....");

        Stage stage = (Stage) songBackButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Songs");
        stage.setScene(scene);
    }

    @FXML
    protected File pickFolder() {

        logger.debug("Executing pickFolder....");

        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File("C:\\test"));
        return dc.showDialog(null);
    }

    @FXML
    protected void clickedFolderButton() {
        addSongsFromFolder(pickFolder());
    }

    @FXML
    protected void addSongsFromFolder(File directory) {

        logger.debug("Executing addSongsFromFolder....");

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
                logger.debug("processing file: "+ file);
                logger.debug(i+" out of "+mp3List.size());

                addMP3(new File(String.valueOf(file)));
            });
            //logger.debug("Songs on end -> \n: "+Arrays.toString(songRepo.getSongs().toArray()));

            updateTable();
            logger.debug(Arrays.toString(songDatabaseTable.getItems().toArray()));

        } catch (IOException e) {
            logger.error("couldn't get files from folder",e);
        }
   }

    private void addMP3(File file) {

        logger.debug("Executing addMP3....");

        SongRepository songRepo = new SongRepository();
        Song id3Tag;
        id3Tag = getID(file);
        if (songRepo.checkForDupes(id3Tag)) {
            logger.debug("---song already exists - "+id3Tag.toString());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Duplicate file import");
            alert.setHeaderText("There is already a song with that name or location in the database");
            alert.setContentText("adding file: "+id3Tag.getFileLoc());

            alert.showAndWait();
        } else {
            SongRepository.addSong(id3Tag); }
    }

    private void updateTable() {

        logger.debug("Executing updateTable....");

        SongRepository songRepo = new SongRepository();
        songList.clear();
        songList.addAll(songRepo.getSongs());
        songDatabaseTable.setItems(songList);
        songDatabaseTable.getSelectionModel().select(currentSongID);
//        if (songDatabaseTable.getItems().size()>0) {
//        openMP3(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc()); }
//        colorizeTable();
    }

    private void colorizeTable() {

        logger.debug("Executing colorizeTable....");

//        songDatabaseTable.setRowFactory(tv -> new TableRow<Song>() {
//            @Override
//            public void updateItem(Song item, boolean empty) {
//                super.updateItem(item, empty) ;
//                if (item == null) {
//                    setStyle("");
//                } else if (item.getGenre().isEmpty()) {
//                    setStyle("-fx-background-color: tomato;");
//                } else {
//                    setStyle("");
//                }
//            }
//        });

    }

    public Song getID(File file) {

        logger.debug("Executing getID....");

       Song testSong = new Song();

        try {
            ID3v2 id3Data = getID3(file.getAbsolutePath());

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
    public void clickTable(MouseEvent event) {

       logger.debug("Executing clickTable....");

        if (event.getButton() == MouseButton.SECONDARY) {
            logger.debug("right click");
        }
        else if (event.getButton() == MouseButton.PRIMARY) {
            try {
                openMP3(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
            } catch (Exception e) {
                logger.error("nothing found on double click",e);
            }
        }
    }

    @FXML
    //open mp3 file for playing and reading id3 data
    protected void openMP3(String fileLoc) {

        logger.debug("Executing openMP3....");

        boolean sameFile = true;
        if (!mp3Label.getText().isEmpty()) {
            try {
                ID3v2 id3Data = getID3(mp3Label.getText());

                if (sameFile) {sameFile = compareTag(textArtist.getText(), id3Data.getArtist());}
                if (sameFile) {sameFile = compareTag(textTitle.getText(), id3Data.getTitle());}
                if (sameFile) {sameFile = compareTag(textAlbum.getText(), id3Data.getAlbum());}
                if (sameFile) {sameFile = compareTag(textPublisher.getText(), id3Data.getPublisher());}
                if (sameFile) {sameFile = compareTag(textComposer.getText(), id3Data.getComposer());}
                if (sameFile) {sameFile = compareTag(textYear.getText(), id3Data.getYear());}
                if (sameFile) {sameFile = compareTag(textGenre.getText(), id3Data.getGenreDescription());}
                //if (sameFile) {sameFile = compareTag(textISRC.getText(), id3Data.getISRC());}

            } catch (Exception ignored) {
                logger.error("Error while opening file " + mp3Label.getText(), ignored);
            }
        }

        if (!sameFile) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved changes");
            alert.setHeaderText("You are switching to another file with possible unsaved changes");
            alert.setContentText("Do you want to save the ID3 changes you have made?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                //songDatabaseTable.getSelectionModel().select(currentSongID);
                updateMP3();
            } else {
                songDatabaseTable.getSelectionModel().select(currentSongID);
                logger.debug("i should be selecting number: "+currentSongID);
            }
        } else {
            updateTextFields(fileLoc);
        }



    }

    private void updateTextFields(String fileLoc) {

        logger.debug("Executing updateTextFields....");

        //close old mp3
        if (mplayer != null) {
            mplayer.stop();
            mplayer.dispose();
        }

        //show file name
        mp3Label.setText(fileLoc);

        //load file into media player
        openMediaFile(fileLoc);

        //load id3 data into text fields
        //should prolly throw a message if empty
        //still needs ISRC function
        try {
            ID3v2 id3Data = getID3(fileLoc);
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
            mp3Slider.setMax(new Mp3File(new File(fileLoc)).getLengthInMilliseconds() / 100);
            currentSongID = songDatabaseTable.getSelectionModel().getSelectedIndex();
            logger.debug("chance song to: "+currentSongID);

        } catch (Exception ignored) {
            logger.error("Error while opening file " + fileLoc, ignored);
        }
    }

    private ID3v2 getID3(String fileLoc) {

        logger.debug("Executing getID3....");

        File mp3File = new File(fileLoc);
        try {
            Mp3File song = new Mp3File(mp3File);
            return song.getId3v2Tag();
        } catch (Exception ignored) {
            logger.error("can't fetch ID3 data from file",ignored);
            return new ID3v24Tag();
        }
    }

    private boolean compareTag(String text1, String text2) {
        if (text1 == null || text1.isEmpty()) {
            if (text2 == null) {
                return true;
            } else {
                return false;
            }
        }
        return text1.equals(text2);
    }

    @FXML
    //play mp3 file
    protected void playMP3() {

        logger.debug("Executing playMP3....");

        //none is loaded, load file first
        if (mplayer == null) {
            logger.debug("no file open to play");
        }

        //play file
        mplayer.setVolume(volumeSlider.getValue()/100);
        mplayer.play();

        //timer for updating current position slider
        Timer sliderUpdateTimer =  new Timer();
        TimerTask sliderUpdateTask =  new TimerTask() {
            public void run() {
                double currentTime = mplayer.getCurrentTime().toSeconds();
                int minutes = (int) (currentTime / 60);
                double seconds = currentTime - minutes * 60;
                DecimalFormat df = new DecimalFormat("##.#");
                df.setRoundingMode(RoundingMode.DOWN);
                String secondsString = df.format(seconds);
                if (!secondsString.contains(".")) {secondsString += ".0";}
                if (seconds < 10) {secondsString = "0"+secondsString;}
                String finalSecondsString = secondsString;
                Platform.runLater(() -> {
                    //show current time text, needs improving
                    mp3Time.setText(String.format("%02xm ",minutes)+ finalSecondsString +"s");
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

    @FXML
    //move forward X seconds button
    protected void moveTimeForwardLittle() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() + 100)); }
    }

    @FXML
    //move back X seconds button
    protected void moveTimeBackLittle() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() - 100)); }
    }

    public void updateMP3() {

        logger.debug("Executing updateMP3....");

        Song song = new Song();
        String fileLoc = mp3Label.getText();

        song.setId(currentSongID+1);
        song.setArtist(textArtist.getText());
        song.setTitle(textTitle.getText());
        song.setAlbum(textAlbum.getText());
        song.setPublisher(textPublisher.getText());
        song.setComposer(textComposer.getText());
        if (textYear.getText() != null) {song.setYear(Integer.valueOf(textYear.getText()));}
        song.setGenre(textGenre.getText());
        song.setISRC(textISRC.getText());
        song.setFileLoc(mp3Label.getText());

        //unload song if playing so the file can be saved
        if (mplayer != null) {mplayer.dispose();}

        //save file
        writeToMP3(song, fileLoc);

        //load file into media player
        openMediaFile(fileLoc);

        //update database and table
        SongRepository.addSong(song);
        updateTable();
    }

    private void writeToMP3(Song song, String fileLoc) {

        logger.debug("Executing writeToMP3....");

        if (mplayer != null) {mplayer.dispose();}

        try {
            Mp3File mp3 = new Mp3File(new File(fileLoc));
            ID3v24Tag id3Data = new ID3v24Tag();
            mp3.setId3v2Tag(id3Data);

            id3Data.setArtist(song.getArtist());
            id3Data.setTitle(song.getTitle());
            id3Data.setAlbum(song.getAlbum());
            id3Data.setPublisher(song.getPublisher());
            id3Data.setComposer(song.getComposer());
            if (song.getYear() != null ) {id3Data.setYear(Integer.toString(song.getYear()));} else {id3Data.setYear("0");}
            if (song.getGenre() != null ) {id3Data.setGenreDescription(song.getGenre());} else {id3Data.setGenreDescription("");}
            //id3Data.setISRC(song.getISRC());

            mp3.save(mp3Label.getText() + ".mp3");
            try {
                Files.delete(Path.of(fileLoc));
            } catch (IOException e) {
                logger.debug("can't delete cause of: "+e);
            }
            File mp3FileNew = new File(mp3Label.getText() + ".mp3");
            boolean info = mp3FileNew.renameTo(new File(fileLoc));
            logger.debug("rename results are " + info);

        } catch (Exception e) {
            logger.error("Error while opening file " + fileLoc, e);
        }
    }

    private void openMediaFile(String fileLoc) {

        logger.debug("Executing openMediaFile....");

        File mp3File = new File(fileLoc);
        String mp3Path = mp3File.toURI().toASCIIString();
        Media mp3Media = new Media(mp3Path);
        mplayer = new MediaPlayer(mp3Media);
    }

    public File openFile() {

        logger.debug("Executing openMP3File....");

        FileChooser fc = new FileChooser();
        fc.setTitle("Open MP3");
        fc.setInitialDirectory(new File("C:\\test"));
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("MP3 files (*.mp3)", "*.mp3");
        fc.getExtensionFilters().add(extFilter);
        return fc.showOpenDialog(null);
    }

    public void addSong2DB(ActionEvent actionEvent) {

        logger.debug("Executing addSong2DB....");

        //select file
        File mp3 = openFile();

        //add file to DB
        addMP3(mp3);

        //preview changes
        updateTable();
    }

    public void checkArtistField(KeyEvent inputMethodEvent) {

        logger.debug("Executing checkArtistField....");

        ID3v2 id3Data = getID3(mp3Label.getText());
        if (!compareTag(id3Data.getArtist(), textArtist.getText())) {
            textArtist.setStyle("-fx-background-color: #FFCCBB");
        } else {
            textArtist.setStyle("-fx-background-color: #FFFFFF");
        }
    }

    public void revertID3(ActionEvent actionEvent) {

        logger.debug("Executing revertID3....");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved changes");
        alert.setHeaderText("You are resetting the ID3 changes you made for this MP3");
        alert.setContentText("Do you want to load the old data without saving the changes?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            updateTextFields(mp3Label.getText());
        }
    }

    public void changeVolume() {
        labelVolume.setText("Volume: "+ String.format("%.0f",volumeSlider.getValue())+"%");
        if (mplayer != null) {
            mplayer.setVolume(volumeSlider.getValue()/100);
        }
    }

    public void copyID3(ActionEvent actionEvent) {

        logger.debug("Executing copyID3....");

        copiedID3 = getID3(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
        logger.debug(copiedID3.getArtist());
        songDatabaseTable.getSelectionModel().select(currentSongID);
    }

    public void pasteID3(ActionEvent actionEvent) {

        logger.debug("Executing pasetID3....");

        songDatabaseTable.getSelectionModel().select(currentSongID);
    }

    public void deleteFile(ActionEvent actionEvent) {

        logger.debug("Executing deleteFile....");

        String fileLoc = songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc();
        File file = new File(fileLoc);

        String[] dialogData = {"Database entry", "ID3 data", "File"};

        ChoiceDialog dialog = new ChoiceDialog(dialogData[2], dialogData);
        dialog.setTitle("Delete");
        dialog.setHeaderText("Select what you want to delete");

        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            String picked = result.get().toString();
            logger.debug(picked);
            if (picked == "File") {
                Song song = songDatabaseTable.getSelectionModel().getSelectedItem();
                SongRepository.delete(song);
                updateTable();
                file.delete();
            }
            else if (picked == "Database entry") {
                Song song =  songDatabaseTable.getSelectionModel().getSelectedItem();
                SongRepository.delete(song);
                updateTable();
            }
        }

    }
}

