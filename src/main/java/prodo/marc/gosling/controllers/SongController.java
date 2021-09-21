package prodo.marc.gosling.controllers;

import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.hibernate.repository.SongRepository;
import prodo.marc.gosling.service.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class SongController {

    private static final Logger logger = LogManager.getLogger(SongController.class);

    public ProgressBar progressBar;
    @FXML ComboBox dropGenre, doneFilter;
    @FXML MediaPlayer mplayer;
    @FXML Button songBackButton, addSongButton, addFolderButton, parseFilenameButton, googleSongButton,openLegacyDataButton;
    @FXML Button backSongs, buttonPlay, buttonPause, skipBack, skipForward, skipForwardSmall, skipBackSmall, buttonRevert;
    @FXML Label mp3Time, labelVolume, progressLabel;
    @FXML TableView<Song> songDatabaseTable;
    @FXML TableColumn<Song, String> tableArtist, tableTitle, tableAlbum, tablePublisher, tableComposer, tableGenre, tableISRC, tableFileLoc;
    @FXML TableColumn<Song, Integer> tableID;
    @FXML TableColumn<Song, Year> tableYear;
    @FXML TableColumn<Song, Boolean> tableDone;
    @FXML TextField textAlbum, textArtist, textTitle, textPublisher, textComposer, textYear, textISRC, textFilterFolder, mp3Label;
    @FXML Slider mp3Slider, volumeSlider;
    @FXML CheckBox checkDone;

    ObservableList<Song> songList = FXCollections.observableArrayList();
    FilteredList<Song> filteredSongs = new FilteredList<>(songList);
    SortedList<Song> sortedSongs = new SortedList<>(filteredSongs);



    private boolean updateCheck = true;
    final int skipIncrement = 10000;
    private Integer currentSongID = 0;
    private String currentFileLoc = "";
    ID3v24Tag copiedID3 = new ID3v24Tag();
    private Path tempDir;
    /**Initial volume for mp3*/
    private static final Integer INITIAL_VOLUME_SO_MY_EARS_DONT_EXPLODE=20;

    public void initialize() {

        logger.debug("----- Executing initialize");

        try {
            tempDir = Files.createTempDirectory("tmp");
        } catch (IOException ex) {
            logger.error("Couldn't create temp dir",ex);
        }

        dropGenre.getItems().addAll(getGenres());
        doneFilter.getItems().addAll("Either", "Done", "Not Done");
        doneFilter.getSelectionModel().select(2);


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
        tableDone.setCellValueFactory(cellData -> new ReadOnlyBooleanWrapper(cellData.getValue().getDone()));
        tableDone.setCellFactory(cellData -> new CheckBoxTableCell<>());
        sortedSongs.comparatorProperty().bind(songDatabaseTable.comparatorProperty());

        updateTable();

        volumeSlider.setValue(INITIAL_VOLUME_SO_MY_EARS_DONT_EXPLODE);  changeVolume();

        //TODO: if global song not null, load into txt fields and select proper song
        if (!songDatabaseTable.getSelectionModel().isEmpty()) {
//            if (SongGlobal.getCurrentSong().getFileLoc() != null) {
//                songDatabaseTable.getSelectionModel().
//            }
            updateTextFields(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
        }

        logger.debug("----- ending initialize");
    }

    private String[] getGenres() {
        return new String[]{"", "cro", "cro zabavne", "instrumental", "klape", "kuruza", "pop", "xxx"};
    }


    @FXML
    protected void backToMain(ActionEvent event) throws IOException {
        logger.debug("----- Executing backToMain");
        SceneController.openScene(event, "view/hello-view.fxml");
        logger.debug("----- ending backToMain");
    }

    @FXML
    protected void clickedParseButton(ActionEvent event) throws IOException {
        logger.debug("----- Executing clickedParseButton");
        SceneController.openScene(event, "view/parseFilename.fxml");
        logger.debug("----- ending clickedParseButton");
    }

    @FXML
    protected void clickedFolderButton() {
        logger.debug("----- Executing clickedFolderButton");
        File pickedFolder = FileUtils.pickFolder(SongGlobal.getCurrentFolder());
        if (pickedFolder != null) {
            SongGlobal.setCurrentFolder(pickedFolder.toString());
            try {
                addSongsFromFolder(pickedFolder); }
            catch (IOException io) {
                Popups.giveInfoAlert("Error", "Could not open folder", pickedFolder.getAbsolutePath());
                logger.error("can't open folder",io);
            }
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
        int max = mp3List.size();
        List<String> dupeFiles = new ArrayList<>();

        progressLabel.setText(i+"/"+max);

        Thread folderImportTask = new Thread(() -> {
            Instant start = Instant.now();
            mp3List.forEach(file -> {
                i.getAndIncrement();
                logger.debug("processing file: "+ file);
                logger.debug(i+" out of "+mp3List.size());

                String getFile = addMP3(file);
                if (getFile != null) {dupeFiles.add(getFile);}
                Platform.runLater(() -> updateProgressBar(i+"/"+max,i.doubleValue()/max));
            });
            updateTable();
            Instant stop = Instant.now();
            logger.debug("Thread finished in: "+ java.time.Duration.between(start, stop).toSeconds());
            logger.debug("duplicate files found: "+dupeFiles);
        });
        folderImportTask.start();

    }

    @FXML
    private void updateProgressBar(String string, Double value) {
        progressLabel.setText(string);
        progressBar.setProgress(value);
    }

    @FXML
    private String addMP3(Path path) {

        logger.debug("----- Executing addMP3");

        Song song;
        ID3v24Tag id3tag = ID3v2Utils.getID3(new File(String.valueOf(path)));
        song = ID3v2Utils.songDataFromID3(id3tag, String.valueOf(path));
        SongRepository songRepo = new SongRepository();
        if (songRepo.checkForDupes(song)) {
            logger.debug("---song already exists - "+song);
            return song.getFileLoc();
        } else {
            SongRepository.addSong(song);
        }

        logger.debug("----- ending addMP3");

        return null;
    }

    private void updateTable() {

        logger.debug("----- Executing updateTable");

        SongRepository songRepo = new SongRepository();
        songList.clear();
        songList.addAll(songRepo.getSongs());
        filterTable();
        //songDatabaseTable.getSelectionModel().select(currentSongID);

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

        Song currentSong = new Song();
        Song globalSong = SongGlobal.getCurrentSong();
        if (!Objects.equals(currentFileLoc, "") && new File(currentFileLoc).exists()) {

            //TODO: finally reworked but needs improvement maybe
            currentSong.setArtist(textArtist.getText());
            currentSong.setTitle(textTitle.getText());
            currentSong.setAlbum(textAlbum.getText());
            currentSong.setPublisher(textPublisher.getText());
            currentSong.setComposer(textComposer.getText());
            currentSong.setYear(StringUtils.parseYear(textYear.getText()));
            currentSong.setGenre(dropGenre.getSelectionModel().getSelectedItem().toString());
            //currentSong.setISRC(textISRC.getText());
            currentSong.setFileLoc(SongGlobal.getCurrentSong().getFileLoc());
            currentSong.setDone(checkDone.isSelected());
        }

        if (!currentSong.equals(globalSong) && currentSong.getFileLoc() != null) {

            boolean result = Popups.giveConfirmAlert("Unsaved changes",
                    "You are switching to another file with possible unsaved changes",
                    "Do you want to save the ID3 changes you have made?\n"+currentSong+"\n"+SongGlobal.getCurrentSong());

            if (result) {
                updateMP3();
            } else {
                songDatabaseTable.getSelectionModel().select(currentSongID);
            }
        } else {
            updateTextFields(fileLoc);
        }

        logger.debug("----- ending openMP3");
    }


    private void updateTextFields(String fileLoc) {

        logger.debug("----- Executing updateTextFields");


        //show file name
        mp3Label.setText(new File(fileLoc).getName().replaceAll("(?i).mp3",""));
        currentFileLoc = fileLoc;

        //load id3 data into text fields
        try {
            File file = new File(fileLoc);
            ID3v24Tag id3Data = ID3v2Utils.getID3(file);
            SongGlobal.setCurrentSong(ID3v2Utils.songDataFromID3(id3Data, fileLoc));
            textArtist.setText(id3Data.getArtist());
            textTitle.setText(id3Data.getTitle());
            textAlbum.setText(id3Data.getAlbum());
            textPublisher.setText(id3Data.getPublisher());
            textComposer.setText(id3Data.getComposer());
            textYear.setText(id3Data.getYear());
            if (id3Data.getKey() != null) {checkDone.setSelected(id3Data.getKey().equals("true"));} else {checkDone.setSelected(false);}
            if (id3Data.getGenreDescription() != null) {dropGenre.getSelectionModel().select(id3Data.getGenreDescription().toLowerCase());}
            if (dropGenre.getSelectionModel().getSelectedItem() == null) {
                dropGenre.getSelectionModel().select(0);
            }
            if (dropGenre.getSelectionModel().getSelectedIndex() == -1) {
                logger.debug("could not find genre: " + id3Data.getGenreDescription());
                Popups.giveInfoAlert("Unknown genre",
                        "The song has an unknown genre",
                        "Genre: "+id3Data.getGenreDescription());
            }

            //textISRC.setText(id3Data.getISRC());

            currentSongID = songDatabaseTable.getSelectionModel().getSelectedIndex();

            SongGlobal.setCurrentSong(ID3v2Utils.songDataFromID3(id3Data, file.getAbsolutePath()));

        } catch (Exception report) {
            logger.error("Error while opening file " + fileLoc, report);
        }

        logger.debug("----- ending updateTextFields");
    }

    @FXML
    protected void playMP3() {

        logger.debug("----- Executing playMP3");
        closeMediaStream();
        openMediaFile(currentFileLoc);

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

        changeCRO();

        ID3v24Tag id3 = ID3v2Utils.getID3(new File(currentFileLoc));

        if (textAlbum.getText().isEmpty() || textAlbum.getText() == null) {
            textAlbum.setText(textTitle.getText());
        }

        id3.setArtist(textArtist.getText());
        id3.setTitle(textTitle.getText());
        id3.setAlbum(textAlbum.getText());
        id3.setPublisher(textPublisher.getText());
        id3.setComposer(textComposer.getText());
        id3.setYear(textYear.getText());
        if (checkDone.isSelected()) {id3.setKey("true");} else {id3.setKey(" ");}
        id3.setGenreDescription(dropGenre.getSelectionModel().getSelectedItem().toString());
        //song.setISRC(textISRC.getText());

        if (renameFile()) {
        updateSongEntry(id3, songDatabaseTable.getItems().get(currentSongID).getId(), currentFileLoc);
        makeTextFieldsWhite();
        SongGlobal.setCurrentSong(ID3v2Utils.songDataFromID3(id3, currentFileLoc));
        writeToMP3(id3, currentFileLoc);
        currentSongID = -1;
        }
        logger.debug("----- ending updateMP3");
    }

    private void makeTextFieldsWhite() {
        textArtist.setStyle("-fx-background-color: #FFFFFF");
          textYear.setStyle("-fx-background-color: #FFFFFF");
    }

    private void writeToMP3(ID3v24Tag song, String fileLoc) {

        logger.debug("----- Executing writeToMP3");

        String backupFileLoc = fileLoc + ".bak";
        try {
            Mp3File mp3file = new Mp3File(fileLoc);
            ID3v24Tag id3Data = ID3v2Utils.getID3(new File(fileLoc));
            mp3file.setId3v2Tag(id3Data);

            id3Data.setArtist(song.getArtist());
            id3Data.setTitle(song.getTitle());
            id3Data.setAlbum(song.getAlbum());
            id3Data.setPublisher(song.getPublisher());
            id3Data.setComposer(song.getComposer());
            id3Data.setYear(song.getYear());
            if (checkDone.isSelected()) {id3Data.setKey("true");} else {id3Data.setKey(" ");}
            if (song.getGenreDescription()!=null) {id3Data.setGenreDescription(song.getGenreDescription());}
            //id3Data.setISRC(song.getISRC());

            mp3file.save(backupFileLoc);
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
        String tempMp3 = tempDir + "\\temp.mp3";

        try {
            Files.copy(Paths.get(fileLoc), Paths.get(tempMp3), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            logger.error("could not create temp mp3 file: ", ex);
        }

        File mp3File = new File(tempMp3);
        mp3File.deleteOnExit();
        String mp3Path = mp3File.toURI().toASCIIString();
        closeMediaStream();

        //TODO: check why javafx.scene.media.Media is not releasing files, that is what is causing the save bug
        Media mp3Media = new Media(mp3Path);
        mplayer = new MediaPlayer(mp3Media);

        //set slider
        try {
            double sliderValue = new Mp3File(mp3File).getLengthInMilliseconds();
            mp3Slider.setMax(sliderValue / 100);
        } catch (Exception ex) {
            logger.error("can't open file to set slider: ",ex);
        }

        logger.debug("----- ending openMediaFile");
    }

    private void closeMediaStream() {
        if (mplayer != null) {
            mplayer.stop();
            mplayer.dispose();
        }
    }

    public void addSong2DB() {
        logger.debug("----- Executing addSong2DB");
        File mp3 = FileUtils.openFile("MP3 files (*.mp3)","mp3", SongGlobal.getCurrentFolder());
        if (mp3 != null) {
            SongGlobal.setCurrentFolder(mp3.getParent());
            addMP3(mp3.toPath());
            updateTable();
        }
        logger.debug("----- ending addSong2DB");
    }

    public void revertID3() {
        logger.debug("----- Executing revertID3");
        boolean result = Popups.giveConfirmAlert("Unsaved changes",
                "You are resetting the ID3 changes you made for this MP3",
                "Do you want to load the old data without saving the changes?");

        if (result){
            updateTextFields(currentFileLoc);
            makeTextFieldsWhite();
        }

        logger.debug("----- ending revertID3");
    }

    public void changeVolume() {
        labelVolume.setText("Volume: "+ String.format("%.0f",volumeSlider.getValue())+"%");
        if (mplayer != null) {
            mplayer.setVolume(volumeSlider.getValue()/100);
        }
    }

    public void copyID3() {
        logger.debug("----- Executing copyID3");

        copiedID3 = ID3v2Utils.getID3(new File(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc()));
        songDatabaseTable.getSelectionModel().select(currentSongID);

        logger.debug("----- ending copyID3");
    }

    public void pasteID3() {
        logger.debug("----- Executing pasteID3");

        boolean confirm = Popups.giveConfirmAlert("Warning",
                "You're about to overwrite ID3 data",
                "Please comfirm your action");

        if(confirm) {
        String fileLoc = songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc();
        writeToMP3(copiedID3, fileLoc);
        updateSongEntry(copiedID3, songDatabaseTable.getItems().get(currentSongID).getId(),fileLoc);
        songDatabaseTable.getSelectionModel().select(currentSongID);
        updateTextFields(fileLoc); }

        logger.debug("----- ending pasteID3");
    }

    public void deleteFile() {
        logger.debug("----- Executing deleteFile");

        String fileLoc = songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc();

        String[] dialogData = {"Database entry", "ID3 data", "File"};

        ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData[0], dialogData);
        dialog.setTitle("Delete");
        dialog.setHeaderText("Select what you want to delete");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String picked = result.get();
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

    public void changeCRO() {
       textArtist.setText(StringUtils.replaceCroChars(textArtist.getText()));
       textTitle.setText(StringUtils.replaceCroChars(textTitle.getText()));
       textAlbum.setText(StringUtils.replaceCroChars(textAlbum.getText()));
       textPublisher.setText(StringUtils.replaceCroChars(textPublisher.getText()));
       textComposer.setText(StringUtils.replaceCroChars(textComposer.getText()));
    }

    //TODO: this part needs to check if all the fields are there so it needs to be handled earlier, prolly in updateMP3()
    public boolean renameFile() {
        logger.debug("----- Executing renameFile");
        if (currentSongID == -1) {
            Popups.giveInfoAlert("file renamed before",
                    "Selection is all messed up now so you can't save again...",
                    "till i figure something out");
            return false;
        }
        File oldFile = new File(currentFileLoc);
        String newFileLoc = "\\" + textArtist.getText() + " - " + textTitle.getText() + ".mp3";
        if (!checkDone.isSelected()) {
            newFileLoc = oldFile.getParent() + newFileLoc;
        } else {
            String genre = dropGenre.getSelectionModel().getSelectedItem().toString()+"\\";
            if (genre.equalsIgnoreCase("pop\\")) {genre="";}
            String year = textYear.getText()+"\\";
            newFileLoc = "Z:\\Songs\\"+genre + year + newFileLoc;
        }
        File newFile = new File(newFileLoc);
        if (!oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            if (!oldFile.renameTo(newFile)) {
                Popups.giveInfoAlert("Error",
                        "Your file can not be renamed",
                        newFile.getAbsolutePath() + " already exists or is in use.\nAdding it to database");

                addMP3(Path.of(newFileLoc));
                updateTable();

                return false;
            }
        }
        //updateSongEntry(id3, songDatabaseTable.getItems().get(currentSongID).getId(), newFile.getAbsolutePath());
        currentFileLoc = newFile.getAbsolutePath();
        mp3Label.setText(newFile.getName().replaceAll("(?i).mp3",""));
        logger.debug("----- ending renameFile");

        return true;
    }

    public void updateSongEntry(ID3v24Tag id3, Integer databaseID, String fileLoc) {
        logger.debug("----- Executing updateSongEntry");
        Song song = ID3v2Utils.songDataFromID3(id3,fileLoc);
        song.setId(databaseID);
        SongRepository.addSong(song);
        updateTable();
        logger.debug("----- ending updateSongEntry");
    }

    public void checkArtistField() {
        if (StringUtils.compareStrings(SongGlobal.getCurrentSong().getArtist(), textArtist.getText())) {
            textArtist.setStyle("-fx-background-color: #FFFFFF");
        } else {
            textArtist.setStyle("-fx-background-color: #FFCCBB");
        }
    }

    public void checkYearField() {
        int pos = textYear.getCaretPosition();
        int len = textYear.getLength();
        if (textYear.getText() != null) {textYear.setText(textYear.getText().replaceAll("[^\\d]", ""));}
        pos = textYear.getLength()-len+pos;
        textYear.positionCaret(pos);
        if (StringUtils.compareStrings(String.valueOf(SongGlobal.getCurrentSong().getYear()), textYear.getText())) {
            textYear.setStyle("-fx-background-color: #FFFFFF");
        } else {
            textYear.setStyle("-fx-background-color: #FFCCBB");
        }
    }

    private boolean getFieldChanged(TextField field) {
        return field.getStyle().contains("FFCCBB");
    }

    public void toggleDone() {
        songDatabaseTable.getSelectionModel().getSelectedItem().setDone(!songDatabaseTable.getSelectionModel().getSelectedItem().getDone());
        //updateTable();
        songDatabaseTable.getSelectionModel().select(currentSongID);
    }

    public void filterTable() {
        String filter = textFilterFolder.getText().toLowerCase();
        Boolean doneFilterCheck = null;
        if (doneFilter.getSelectionModel().getSelectedIndex() == 1) {
            doneFilterCheck = true;
        } else if (doneFilter.getSelectionModel().getSelectedIndex() == 2) {
            doneFilterCheck = false;
        }
        if (doneFilterCheck == null) {
            filteredSongs.setPredicate(x -> x.getFileLoc().toLowerCase().contains(filter));
        } else {
            Boolean finalDoneFilterCheck = doneFilterCheck;
            filteredSongs.setPredicate(x -> x.getFileLoc().toLowerCase().contains(filter) && x.getDone().equals(finalDoneFilterCheck));
        }
        songDatabaseTable.setItems(sortedSongs);
        logger.debug("set filter to: "+filter);
        //TODO: if the current song gets unselected while filtering, everything breaks
        //currentSongID = songDatabaseTable.getSelectionModel().getSelectedIndex();

    }

    public void googleSong(ActionEvent event) {
        String uri = textArtist.getText()+" "+textTitle.getText();
        uri = uri.replace(" ","+");
        uri = "https://www.google.com/search?q="+uri;
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create(uri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void openLegacyData(ActionEvent event) throws IOException {
        logger.debug("Here we open new window");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/prodo/marc/gosling/view/legacyAccessDatabaseViewer.fxml"));

            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            Stage stage = new Stage();
            stage.setTitle("Legacy Access Database Viever");

            stage.setScene(scene);
            stage.show();
        } catch (IOException  e) {

            logger.error("Error while loading screen! ",e);
            throw e;
        }


    }

}