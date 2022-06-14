package prodo.marc.gosling.controllers;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.dao.id3Header;
import prodo.marc.gosling.hibernate.repository.SongRepository;
import prodo.marc.gosling.service.FileUtils;
import prodo.marc.gosling.service.MyStringUtils;
import prodo.marc.gosling.service.Popups;
import prodo.marc.gosling.service.SongGlobal;
import prodo.marc.gosling.service.id3.ID3Reader;
import prodo.marc.gosling.service.id3.ID3v2Utils;
import prodo.marc.gosling.service.util.TruncatedUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.Year;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SongController {

    private static final Logger logger = LogManager.getLogger(SongController.class);
    /**
     * Initial volume for mp3
     */
    private static final Integer INITIAL_VOLUME_SO_MY_EARS_DONT_EXPLODE = 20;

    @FXML
    ComboBox<String> dropGenre, doneFilter, truncatedFilter, userFilter;
    @FXML
    MediaPlayer mplayer;
    @FXML
    Button songBackButton, addSongButton, addFolderButton, parseFilenameButton, googleSongButton,
            openLegacyDataButton, updateSongs, buttonPlay, buttonPause, skipBack, skipForward, skipForwardSmall,
            skipBackSmall, buttonRevert, spotSongButton, zampSongButton, refreshTableButton, tableToggleButton,
            discogsSongButton;
    @FXML
    Label mp3Time, labelVolume, labelSongNumber, mp3Label;
    @FXML
    TableView<Song> songDatabaseTable;
    @FXML
    TableColumn<Song, String> tableArtist, tableTitle, tableAlbum, tablePublisher, tableComposer,
            tableGenre, tableISRC, tableFileLoc, tableEditor, tableDuration;
    @FXML
    TableColumn<Song, Integer> tableID;
    @FXML
    TableColumn<Song, Year> tableYear;
    @FXML
    TableColumn<Song, Boolean> tableDone;
    @FXML
    Slider mp3Slider, volumeSlider;
    @FXML
    CheckBox checkDone;
    @FXML
    TextField textAlbum, textArtist, textTitle, textPublisher, textComposer, textYear, textISRC, textFilterFolder;

    //public text fiels for getting data from regex
    public static TextField publicTextArtist, publicTextTitle, publicTextISRC, publicTextPublisher;
    ObservableList<Song> songList = FXCollections.observableArrayList();
    ObservableList<String> publisherList = FXCollections.observableArrayList();
    FilteredList<Song> filteredSongs = new FilteredList<>(songList);
    SortedList<Song> sortedSongs = new SortedList<>(filteredSongs);
    MyID3 COPIED_ID3 = new MyID3();
    String CHANGED_BACKGROUND_COLOR = "bb3333";
    String DEFAULT_BACKGROUND_COLOR = "555555";
    String AVERAGE_BACKGROUND_COLOR = "dd9999";
    private boolean UPDATE_CHECK = true;
    private String CURRENT_FILE_LOC = "";
    private String EDITOR_NAME;

    /**
     * Loads the publisher list from the database
     */
    private void publisherAutocomplete() {
        //this neeeds to be a separate database eventually
        List<String> publishers = SongRepository.getPublishers();
        publishers.remove(null);
        publishers.remove("");
        publisherList.addAll(publishers);
    }

    /**
     * Loads the list of genres, hard coded for now
     * @return sorted list of genres
     */
    private String[] getGenres() {
        String[] returnArr = {"", "Cro", "Cro Zabavne", "Instrumental", "Klape", "Kuruza",
                "Pop", "xxx", "Italian", "Susjedi", "Religiozne", "Oldies", "X-Mas", "Domoljubne",
                "Country", "World Music", "Dance"};
        Arrays.sort(returnArr);
        return returnArr;
    }

    public void initialize() {

        //logger.debug("----- Executing initialize");

        //declarations so regex can send data to song controller
        publicTextArtist = textArtist;
        publicTextTitle = textTitle;
        publicTextISRC = textISRC;
        publicTextPublisher = textPublisher;

        //drag and drop
        songDatabaseTable.setOnDragOver(dragEvent -> {
            dragEvent.acceptTransferModes(TransferMode.LINK);
            dragEvent.consume();
        });
        songDatabaseTable.setOnDragDropped(dragEvent -> {
            testFiles(dragEvent);
            dragEvent.consume();
        });

        //getting editor name from system hostname
        try {
            EDITOR_NAME = InetAddress.getLocalHost().getHostName();
            logger.debug("Editor name: " + EDITOR_NAME);
        } catch (UnknownHostException ex) {
            logger.error("Unknown host:", ex);
        }

        //initializing dropdowns
        dropGenre.getItems().addAll(getGenres());
        doneFilter.getItems().addAll("Ignore done", "Done", "Not Done");
        truncatedFilter.getItems().addAll("Ignore truncated", "Truncated");
        userFilter.getItems().addAll("Any user", "Direktor", "Glazba", "ONAIR");

        //selecting items in dropdowns
        doneFilter.getSelectionModel().select(SongGlobal.getDoneFilter());
        truncatedFilter.getSelectionModel().select(SongGlobal.getTruncatedFilter());
        textFilterFolder.setText(SongGlobal.getFolderFilter());
        userFilter.getSelectionModel().select(SongGlobal.getUserFilter());

        publisherAutocomplete();

        //initializing table
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
        tableEditor.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getEditor()));
        tableDuration.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDurationString()));

        tableDone.setCellValueFactory(cellData -> new ReadOnlyBooleanWrapper(cellData.getValue().getDone()));
        tableDone.setCellFactory(cellData -> new CheckBoxTableCell<>());

        sortedSongs.comparatorProperty().bind(songDatabaseTable.comparatorProperty());

        //setting voulume slider to initial value
        volumeSlider.setValue(INITIAL_VOLUME_SO_MY_EARS_DONT_EXPLODE);
        changeVolume();

        //loading songs from database
        updateTable();

        //load short table
        switchTable();

        //install accelerators
        Platform.runLater(this::installAccelerators);

        //if something is selected, load it into the text fields
        //this is mainy for when the window is reloaded
        if (!songDatabaseTable.getSelectionModel().isEmpty()) {
            updateTextFields(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
        }

        //logger.debug("----- ending initialize");
    }

    /**
     * Installs Accelerators, so that the user can use the keyboard as conviniently as possible.
     * <p> Called on initialization.
     */
    public void installAccelerators() {
        Scene scene = buttonPlay.getScene();

        // shortcut ctrl+s to save
        KeyCombination keyCombinationSave = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
        Runnable runSave = this::updateMP3;
        scene.getAccelerators().put(keyCombinationSave, runSave);

        //shortcut ctrl+f to select the filter text field
        KeyCombination keyCombinationFind = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        Runnable runFind = () -> textFilterFolder.requestFocus();
        scene.getAccelerators().put(keyCombinationFind, runFind);

        //shortcut ctrl+d to select done
        KeyCombination keyCombinationDone = new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN);
        Runnable runDone = () -> checkDone.setSelected(!checkDone.isSelected());
        scene.getAccelerators().put(keyCombinationDone, runDone);

        //shortcut ctrl+down to select previous song in table
        KeyCombination keyCombinationPrevious = new KeyCodeCombination(KeyCode.UP, KeyCombination.SHORTCUT_DOWN);
        Runnable runPrevious = () -> {
            songDatabaseTable.getSelectionModel().selectPrevious();
            //songDatabaseTable.getSelectionModel().select(songDatabaseTable.getSelectionModel().getSelectedIndex() - 1);
            openMP3(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
        };
        scene.getAccelerators().put(keyCombinationPrevious, runPrevious);

        //shortcut ctrl+up to select next song in table
        KeyCombination keyCombinationNext = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHORTCUT_DOWN);
        Runnable runNext = () -> {
            songDatabaseTable.getSelectionModel().selectNext();
            //songDatabaseTable.getSelectionModel().select(songDatabaseTable.getSelectionModel().getSelectedIndex() + 1);
            openMP3(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
        };
        scene.getAccelerators().put(keyCombinationNext, runNext);

        //shortcut ctrl+page_down to select last song in table
        KeyCombination keyCombinationLast = new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.SHORTCUT_DOWN);
        Runnable runLast = () -> {
            //songDatabaseTable.getSelectionModel().select(songList.size() - 1);
            songDatabaseTable.getSelectionModel().selectLast();
            songDatabaseTable.scrollTo(songList.size() - 1);
            openMP3(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
        };
        scene.getAccelerators().put(keyCombinationLast, runLast);

        //shortcut ctrl+page_up to select first song in table
        KeyCombination keyCombinationFirst = new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.SHORTCUT_DOWN);
        Runnable runFirts = () -> {
            //songDatabaseTable.getSelectionModel().select(0);
            songDatabaseTable.getSelectionModel().selectFirst();
            songDatabaseTable.scrollTo(0);
            openMP3(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
        };
        scene.getAccelerators().put(keyCombinationFirst, runFirts);

        //shortcut ctrl+space to set filter text to "download" and update table
        KeyCombination keyCombinationDownload = new KeyCodeCombination(KeyCode.SPACE, KeyCombination.SHORTCUT_DOWN);
        Runnable runDownload = () -> {
            textFilterFolder.setText("download");
            updateTable();
        };
        scene.getAccelerators().put(keyCombinationDownload, runDownload);

    }


    @FXML
    protected void backToMain(ActionEvent event) throws IOException {
        //logger.debug("----- Executing backToMain");
        closeMediaStream();
        SceneController.openScene(event, "main", "view/hello-view.fxml");
        //logger.debug("----- ending backToMain");
    }

    @FXML
    protected void clickedParseButton(ActionEvent event) throws IOException {
        //logger.debug("----- Executing clickedParseButton");
        closeMediaStream();
        if (SongGlobal.getCurrentSong() == null) {
            Popups.giveInfoAlert("Open parse window error",
                    "Couldn't open the filename parse window",
                    "no file selected, file location=null");
        } else {
            SceneController.openWindow(event, "view/parseFilename.fxml", true);
        }
        //logger.debug("----- ending clickedParseButton");
    }

    @FXML
    protected void clickedFolderButton() {
        //logger.debug("----- Executing clickedFolderButton");
        File pickedFolder = FileUtils.pickFolder(SongGlobal.getCurrentFolder());
        if (pickedFolder != null) {
            SongGlobal.setCurrentFolder(pickedFolder.toString());
            try {
                addSongsFromFolder(pickedFolder);
            } catch (IOException io) {
                Popups.giveInfoAlert("Error", "Could not open folder", pickedFolder.getAbsolutePath());
                logger.error("can't open folder", io);
            }
        }
        //logger.debug("----- ending clickedFolderButton");
    }

    @FXML
    protected void addSongsFromFolder(File directory) throws IOException {

        //logger.debug("----- Executing addSongsFromFolder");

        SongGlobal.setMP3List(FileUtils.getFileListFromFolder(directory, "mp3"));
        logger.debug("number of files in the list: " + SongGlobal.getMP3List().size());
        setEditor();

        putMP3ListIntoDB();

        //logger.debug("----- ending addSongsFromFolder");
    }

    private void setEditor() {
        Song tempSong = new Song();
        tempSong.setEditor(EDITOR_NAME);
        SongGlobal.setCurrentSong(tempSong);
    }

    private void putMP3ListIntoDB() {

        String fxmlLocation = "/prodo/marc/gosling/view/progress.fxml";
        try {
            SceneController.openWindow(null, fxmlLocation, true);
            refreshTableButton.setStyle("-fx-background-color: #" + CHANGED_BACKGROUND_COLOR);
        } catch (IOException e) {
            logger.error("couldn't open import window", e);
        }

    }


    /**
     * Clear the table and get the new list of songs from the database
     */
    @FXML
    private void updateTable() {

        //logger.debug("----- Executing updateTable");

        SongRepository songRepo = new SongRepository();
        List<Song> songList1 = songRepo.getSongs();

        songList.clear();
        songList.addAll(songList1);
        filterTable();
        selectFileFromTable(CURRENT_FILE_LOC);

        refreshTableButton.setStyle("");

        //logger.debug("----- ending updateTable");
    }


    @FXML
    public void clickTable(MouseEvent event) {

        //logger.debug("----- Executing clickTable");

        if (event.getButton() == MouseButton.SECONDARY) {
            logger.debug("right click");
        } else if (event.getButton() == MouseButton.PRIMARY && !songDatabaseTable.getSelectionModel().isEmpty()) {
            try {
                openMP3(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
            } catch (Exception e) {
                logger.error("no table entry clicked" + songDatabaseTable.getSelectionModel().getSelectedItem(), e);
            }
        }
        songDatabaseTable.setMaxWidth(getTableWidth());

        //logger.debug("----- ending clickTable");
    }

    @FXML
    protected void openMP3(String fileLoc) {

        //logger.debug("----- Executing openMP3");

        Song currentSong = new Song();

        //TODO: this will need to change read mode to database... also needs to be at a different place
        boolean localFile;
        if (!EDITOR_NAME.equals(songDatabaseTable.getSelectionModel().getSelectedItem().getEditor()) &&
                !songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc().contains("Z:\\")) {
            logger.debug("error!!!!!");
            localFile = false;
        } else {
            localFile = true;
        }

        if (!CURRENT_FILE_LOC.equals("") && new File(CURRENT_FILE_LOC).exists()) {

            currentSong.setArtist(textArtist.getText());
            currentSong.setTitle(textTitle.getText());
            currentSong.setAlbum(textAlbum.getText());
            currentSong.setPublisher(textPublisher.getText());
            currentSong.setComposer(textComposer.getText());
            currentSong.setYear(MyStringUtils.parseYear(textYear.getText()));
            currentSong.setGenre(dropGenre.getSelectionModel().getSelectedItem());
            currentSong.setISRC(textISRC.getText());
            currentSong.setFileLoc(SongGlobal.getCurrentSong().getFileLoc());
            currentSong.setDone(checkDone.isSelected());
            currentSong.setEditor(EDITOR_NAME);
        }

        if (!currentSong.isTheSame(SongGlobal.getCurrentSong()) && currentSong.getFileLoc() != null) {

            boolean result = Popups.giveConfirmAlert("Unsaved changes",
                    "You are switching to another file with possible unsaved changes",
                    "Do you want to save the ID3 changes you have made?\nChanges: " + currentSong + "\nFile data: " + SongGlobal.getCurrentSong());

            if (result) {
                updateMP3();
            } else {
                boolean resultNew = Popups.giveConfirmAlert("Continue?",
                        "do you still want to switch to another file?",
                        "Continue:");
                if (!resultNew)
                    selectFileFromTable(CURRENT_FILE_LOC);
                else changeSong(fileLoc, localFile);
            }
        } else {
            changeSong(fileLoc, localFile);
        }

        //logger.debug("----- ending openMP3");
    }

    private void changeSong(String fileLoc, boolean localFile) {
        if (localFile) {
            updateTextFields(fileLoc);
        }
    }


    private void updateTextFields(String fileLoc) {

        //logger.debug("----- Executing updateTextFields");

        //show file name
        String fileLabel = new File(fileLoc).getName();
        fileLabel = SongGlobal.getFileExists().isBlank() ? fileLabel : SongGlobal.getFileExists();
        mp3Label.setText(fileLabel.replaceAll("(?i).mp3", ""));
        CURRENT_FILE_LOC = fileLoc;

        //load id3 data into text fields
        try {
            File file = new File(fileLoc);
            MyID3 id3Data = ID3Reader.getTag(file);

            textArtist.setText(id3Data.getData(id3Header.ARTIST));
            textTitle.setText(id3Data.getData(id3Header.TITLE));
            textAlbum.setText(id3Data.getData(id3Header.ALBUM));
            textPublisher.setText(id3Data.getData(id3Header.PUBLISHER));
            textComposer.setText(id3Data.getData(id3Header.COMPOSER));
            textYear.setText(id3Data.getData(id3Header.YEAR));
            if (id3Data.getData(id3Header.KEY) != null) {
                checkDone.setSelected(id3Data.getData(id3Header.KEY).equals("true"));
            } else {
                checkDone.setSelected(false);
            }
            //TODO: ovo ne bi trebalo radit vako... al genre ce ionako radit drugacije eventually...
            if (id3Data.getData(id3Header.GENRE) != null) {
                dropGenre.getSelectionModel().select(MyStringUtils.replaceCroChars(id3Data.getData(id3Header.GENRE), id3Header.GENRE));
            }
            if (dropGenre.getSelectionModel().getSelectedItem() == null || id3Data.getData(id3Header.GENRE) == null) {
                dropGenre.getSelectionModel().select(0);
            }
            if (dropGenre.getSelectionModel().getSelectedIndex() == -1) {
                logger.debug("could not find genre: " + id3Data.getData(id3Header.GENRE));
                Popups.giveInfoAlert("Unknown genre",
                        "The song has an unknown genre",
                        "Genre: " + id3Data.getData(id3Header.GENRE));
            }
//            logger.debug("***" + id3Data.getData(id3Header.GENRE) + "***");

            textISRC.setText(id3Data.getData(id3Header.ISRC));


            SongGlobal.setCurrentSong(ID3v2Utils.songDataFromID3(id3Data, file.getAbsolutePath(), EDITOR_NAME));

            String unknownFrames = id3Data.checkFrames().toString();
            if (!unknownFrames.equals("[]")) {
                Popups.giveInfoAlert("Unknown ID3 header in file: ", file.toString(), unknownFrames);
                //logger.debug(unknownFrames);
            }

            checkFields();

        } catch (Exception report) {
            logger.error("Error while opening file " + fileLoc, report);
        }

        //logger.debug("----- ending updateTextFields");
    }

    /**
     * This method selects the current song from the table
     *
     * @param currentFileLoc the file location of the song
     */
    private void selectFileFromTable(String currentFileLoc) {
        for (Song song : songDatabaseTable.getItems()) {
            if (song.getFileLoc().equals(currentFileLoc)) {
                songDatabaseTable.getSelectionModel().select(song);
                break;
            }
        }
    }

    @FXML
    protected void playMP3() {

        //logger.debug("----- Executing playMP3");
        closeMediaStream();
        openMediaFile(CURRENT_FILE_LOC);

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
                    int minutes = (int) Math.floor(currentTime / 60);
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
                        mp3Time.setText(String.format("%02dm ", minutes) + finalSecondsString + "s");
                        //update slider to current time
                        if (UPDATE_CHECK) {
                            mp3Slider.setValue(mplayer.getCurrentTime().toMillis() / 100);
                        }
                    });
                }
            };
            sliderUpdateTimer.scheduleAtFixedRate(sliderUpdateTask, 100, 100);
        }

        //logger.debug("----- ending playMP3");

    }

    @FXML
    protected void pauseMP3() {
        if (mplayer != null) {
            if (mplayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mplayer.pause();
            } else {
                mplayer.play();
            }
        }
    }

    @FXML
    protected void moveTime() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mp3Slider.getValue() * 100));
            UPDATE_CHECK = true;
        }
    }

    @FXML
    protected void sliderDrag() {
        UPDATE_CHECK = false;
    }

    @FXML
    protected void moveTimeForward() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() + 10000));
        }
    }

    @FXML
    protected void moveTimeBack() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() - 10000));
        }
    }

    @FXML
    protected void moveTimeForwardLittle() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() + 200));
        }
    }

    @FXML
    protected void moveTimeBackLittle() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() - 200));
        }
    }

    public void updateMP3() {

        //logger.debug("----- Executing updateMP3");

        changeCRO();

        //check if title field has parenthesis that start with "ft " and if so, move the string to the artist field
        if (textTitle.getText().contains("(ft ")) {
            String titleString = textTitle.getText();
            int startOfFTString = titleString.indexOf("(ft ");
            textArtist.setText(textArtist.getText() + " ft " + titleString.substring(startOfFTString + 4, titleString.indexOf(")")));
            textTitle.setText(titleString.substring(0, startOfFTString));
        }

        MyID3 id3 = ID3Reader.getTag(new File(CURRENT_FILE_LOC));
//        logger.debug("made new id3 with size: "+id3.totalFrameSize());

        if (textAlbum.getText().isEmpty() || textAlbum.getText() == null) {
            textAlbum.setText(textTitle.getText());
        }
        if ((textTitle.getText().isEmpty() || textTitle.getText() == null) && !textAlbum.getText().isEmpty()) {
            textTitle.setText(textAlbum.getText());
        }
        //if there's no year set, set it to current year
        if (textYear.getText() == null || textYear.getText().isBlank()) {
            textYear.setText(String.valueOf(Year.now().getValue()));
        }

        id3.setFrame(id3Header.ARTIST, textArtist.getText());
        id3.setFrame(id3Header.TITLE, textTitle.getText());
        id3.setFrame(id3Header.ALBUM, textAlbum.getText());
        id3.setFrame(id3Header.PUBLISHER, textPublisher.getText());
        id3.setFrame(id3Header.COMPOSER, textComposer.getText());
        id3.setFrame(id3Header.YEAR, textYear.getText());
        id3.setFrame(id3Header.LENGTH, String.valueOf(SongGlobal.getCurrentSong().getDuration()));
        if (checkDone.isSelected()) {
            id3.setFrame(id3Header.KEY, "true");
        } else {
            id3.setFrame(id3Header.KEY, " ");
        }
        id3.setFrame(id3Header.GENRE, dropGenre.getSelectionModel().getSelectedItem());
        id3.setFrame(id3Header.ISRC, textISRC.getText());

        if (!updateSongs.isDisable()) {
            if (renameFile()) {
                updateSongEntry(id3, SongGlobal.getCurrentSong().getId(), CURRENT_FILE_LOC);
                SongGlobal.setCurrentSong(ID3v2Utils.songDataFromID3(id3, CURRENT_FILE_LOC, EDITOR_NAME));
                writeToMP3(id3, CURRENT_FILE_LOC);
                selectFileFromTable(CURRENT_FILE_LOC);
            }
        }

        checkFields();
        //logger.debug("----- ending updateMP3");
    }

    private void writeToMP3(MyID3 song, String fileLoc) {

        //logger.debug("----- Executing writeToMP3");

        try {
            MyID3 id3Data = ID3Reader.getTag(new File(fileLoc));
//            logger.debug("made new id3 with size: "+id3Data.totalFrameSize());

            id3Data.setFrame(id3Header.ARTIST, song.getData(id3Header.ARTIST));
            id3Data.setFrame(id3Header.TITLE, song.getData(id3Header.TITLE));
            id3Data.setFrame(id3Header.ALBUM, song.getData(id3Header.ALBUM));
            id3Data.setFrame(id3Header.PUBLISHER, song.getData(id3Header.PUBLISHER));
            id3Data.setFrame(id3Header.COMPOSER, song.getData(id3Header.COMPOSER));
            id3Data.setFrame(id3Header.YEAR, song.getData(id3Header.YEAR));
            if (checkDone.isSelected()) {
                id3Data.setFrame(id3Header.KEY, "true");
            } else {
                id3Data.setFrame(id3Header.KEY, " ");
            }
            if (song.getData(id3Header.GENRE) != null) {
                id3Data.setFrame(id3Header.GENRE, song.getData(id3Header.GENRE));
            }
            id3Data.setFrame(id3Header.ISRC, song.getData(id3Header.ISRC));

//            logger.debug("updated id3 to size: "+id3Data.totalFrameSize());

            ID3Reader.writeFile(fileLoc, id3Data);

        } catch (Exception e) {
            logger.error("Error while opening file " + fileLoc, e);
        }


        //logger.debug("----- ending writeToMP3");
    }

    private void openMediaFile(String fileLoc) {
        //logger.debug("----- Executing openMediaFile");
        try {
            //open a temporaty folder to store the mp3 file
            Path TEMP_DIR = Files.createTempDirectory("tmp");
            String tempMp3 = TEMP_DIR + "\\temp";
            Path filePath = Path.of(fileLoc);
            //copy the mp3 file to the temp folder
            try {
                Files.copy(filePath, Paths.get(tempMp3 + ".mp3"), StandardCopyOption.REPLACE_EXISTING);
                tempMp3 = tempMp3 + ".mp3";
            } catch (Exception ex) {
                logger.error("could not create temp mp3 file: ", ex);
                try {
                    Files.copy(filePath, Paths.get(tempMp3 + "1.mp3"), StandardCopyOption.REPLACE_EXISTING);
                    tempMp3 = tempMp3 + "1.mp3";
                } catch (Exception ex1) {
                    logger.error("could not create secondary temp mp3 file either: ", ex1);
                }
            }

            File mp3File = new File(tempMp3);
            mp3File.deleteOnExit();
            String mp3Path = mp3File.toURI().toASCIIString();
            closeMediaStream();

            //TODO: check why javafx.scene.media.Media is not releasing files, that is what is causing the save bug
            Media mp3Media = new Media(mp3Path);
            mplayer = new MediaPlayer(mp3Media);
        } catch (IOException ex) {
            logger.error("Couldn't create temp dir", ex);
        }

        //set slider
        try {
            double sliderValue = SongGlobal.getCurrentSong().getDuration();
            mp3Slider.setMax(sliderValue / 100);
        } catch (Exception ex) {
            logger.error("can't open file to set slider: ", ex);
        }

        //logger.debug("----- ending openMediaFile");
    }

    private void closeMediaStream() {
        if (mplayer != null) {
            mplayer.stop();
            mplayer.dispose();
        }
    }

    public void addSong2DB() {
        //logger.debug("----- Executing addSong2DB");
        File mp3 = FileUtils.openFile("MP3 files (*.mp3)", "mp3", SongGlobal.getCurrentFolder());
        if (mp3 != null) {
            SongGlobal.setCurrentFolder(mp3.getParent());
            FileUtils.addMP3(mp3.toPath(), EDITOR_NAME);
            updateTable();
        }
        //logger.debug("----- ending addSong2DB");
    }

    public void revertID3() {
        //logger.debug("----- Executing revertID3");
        boolean result = Popups.giveConfirmAlert("Unsaved changes",
                "You are resetting the ID3 changes you made for this MP3",
                "Do you want to load the old data without saving the changes?");

        if (result) {
            updateTextFields(CURRENT_FILE_LOC);
            checkFields();
        }

        //logger.debug("----- ending revertID3");
    }

    public void changeVolume() {
        labelVolume.setText("Volume: " + String.format("%.0f", volumeSlider.getValue()) + "%");
        if (mplayer != null) {
            mplayer.setVolume(volumeSlider.getValue() / 100);
        }
    }

    public void copyID3() {
        //logger.debug("----- Executing copyID3");

        COPIED_ID3 = ID3Reader.getTag(new File(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc()));
        selectFileFromTable(CURRENT_FILE_LOC);

        //logger.debug("----- ending copyID3");
    }

    public void pasteID3() {
        //logger.debug("----- Executing pasteID3");

        boolean confirm = Popups.giveConfirmAlert("Warning",
                "You're about to overwrite ID3 data",
                "Please comfirm your action");

        if (confirm) {
            String fileLoc = songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc();
            writeToMP3(COPIED_ID3, fileLoc);
            updateSongEntry(COPIED_ID3, SongRepository.getIDofFile(fileLoc), fileLoc);
            selectFileFromTable(CURRENT_FILE_LOC);
            updateTextFields(fileLoc);
        }

        //logger.debug("----- ending pasteID3");
    }

    public void deleteFile() {
        //logger.debug("----- Executing deleteFile");

        String fileLoc = songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc();

        String[] dialogData = {"Database entry", "ID3 data", "File"};

        ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData[0], dialogData);
        dialog.setTitle("Delete");
        dialog.setHeaderText("Select what you want to delete");

        String result = dialog.showAndWait().orElse(null);
        if (result != null) {
            if (result.equals("File")) {
                Song song = songDatabaseTable.getSelectionModel().getSelectedItem();
                SongRepository.delete(song);
                updateTable();
                if (CURRENT_FILE_LOC.equals(song.getFileLoc())) {
                    CURRENT_FILE_LOC = "";
                }
                try {
                    Files.delete(Path.of(fileLoc));
                } catch (IOException er) {
                    logger.error("can't delete file: ", er);
                }
            } else if (result.equals("Database entry")) {
                Song song = songDatabaseTable.getSelectionModel().getSelectedItem();
                SongRepository.delete(song);
                updateTable();
                if (CURRENT_FILE_LOC.equals(song.getFileLoc())) {
                    CURRENT_FILE_LOC = "";
                }
            } else {
                logger.debug("code to delete id3 tag here");
            }
        }

        //logger.debug("----- ending deleteFile");
    }

    public void changeCRO() {
        textArtist.setText(MyStringUtils.replaceCroChars(textArtist.getText(), id3Header.ARTIST));
        textTitle.setText(MyStringUtils.replaceCroChars(textTitle.getText(), id3Header.TITLE));
        textAlbum.setText(MyStringUtils.replaceCroChars(textAlbum.getText(), id3Header.ALBUM));
        textPublisher.setText(MyStringUtils.replaceCroChars(textPublisher.getText(), id3Header.PUBLISHER));
        textComposer.setText(MyStringUtils.replaceCroChars(textComposer.getText(), id3Header.COMPOSER));
    }

    private String generateNewFilename(String oldFile, boolean checkNew, String year, String genre) {
        String newFileLoc = textArtist.getText() + " - " + textTitle.getText() + ".mp3";

        if (!checkDone.isSelected() && !checkNew) {
            newFileLoc = Paths.get(oldFile).getParent() + "\\" + newFileLoc;
        } else {
            if (genre.equals("pop") || genre.equals("dance")) {
                genre = "";
            } else if (genre.equals("domoljubne")) {
                genre = "cro\\" + genre;
            }
            year = year + "\\";

            List<String> foldersWithNoYear = Arrays.asList(
                    "religiozne", "oldies", "x-mas", "cro\\domoljubne", "country"
            );
            if (foldersWithNoYear.contains(genre)) {
                year = "";
            }
            if (genre.length() > 0) genre += "\\";
            newFileLoc = "Z:\\Songs\\" + genre + year + newFileLoc;

        }
        return newFileLoc;
    }

    //TODO: this part needs to check if all the fields are there so it needs to be handled earlier, probably in updateMP3()
    public boolean renameFile() {
        //logger.debug("----- Executing renameFile");
        String newFileLoc = generateNewFilename(CURRENT_FILE_LOC, false, textYear.getText(), dropGenre.getSelectionModel().getSelectedItem().toLowerCase());

        if (dropGenre.getSelectionModel().getSelectedItem().isEmpty() && checkDone.isSelected()) {
            Popups.giveInfoAlert("file rename error",
                    "no genre selected",
                    "please select genre and try again");
            return false;
        }

        Path filePath = Path.of(newFileLoc);
        boolean mkdResult = new File(filePath.getParent().toString()).mkdirs();
        if (!mkdResult) {
            logger.debug("creating folder failed:" + newFileLoc);
        }

        File oldFile = new File(CURRENT_FILE_LOC);
        File newFile = new File(newFileLoc);
        if (!oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            if (!oldFile.renameTo(newFile)) {
                Popups.giveInfoAlert("Error",
                        "Your file can not be renamed",
                        newFile.getAbsolutePath() + " already exists or is in use.");

                if (SongRepository.getIDofFile(newFileLoc) == null) {
                    FileUtils.addMP3(filePath, EDITOR_NAME);
                }
                updateTable();

                return false;
            }
        }
        CURRENT_FILE_LOC = newFile.getAbsolutePath();
        String fileLabel = newFile.getName();
        fileLabel = SongGlobal.getFileExists().isBlank() ? fileLabel : SongGlobal.getFileExists();
        mp3Label.setText(fileLabel.replaceAll("(?i).mp3", ""));
        //logger.debug("----- ending renameFile");

        return true;
    }

    /**
     * Updates the song entry in the database with the new data
     *
     * @param id3        the new id3 data
     * @param databaseID the id of the song in the database
     * @param fileLoc    the file location of the song
     */
    public void updateSongEntry(MyID3 id3, Integer databaseID, String fileLoc) {
        //logger.debug("----- Executing updateSongEntry");
        Song song = ID3v2Utils.songDataFromID3(id3, fileLoc, EDITOR_NAME);
        song.setId(databaseID);
        song.setEditor(EDITOR_NAME);
        SongRepository.addSong(song);
        updateTable();
        //logger.debug("----- ending updateSongEntry");
    }

    public boolean checkArtistField(String artist) {
        if (artist.isEmpty()) {
            textArtist.setStyle("-fx-background-color: #" + AVERAGE_BACKGROUND_COLOR);
            return false;
        } else if (checkInvalidChars(artist, "artist")) {
            textArtist.setStyle("-fx-background-color: #" + CHANGED_BACKGROUND_COLOR);
            return true;
        } else {
            textArtist.setStyle("-fx-background-color: #" + DEFAULT_BACKGROUND_COLOR);
            return false;
        }
    }

    public boolean checkComposerField(String composer) {
        if (composer.isEmpty()) {
            textComposer.setStyle("-fx-background-color: #" + AVERAGE_BACKGROUND_COLOR);
            return false;
        } else if (checkInvalidChars(composer, "composer")) {
            textComposer.setStyle("-fx-background-color: #" + CHANGED_BACKGROUND_COLOR);
            return true;
        } else {
            textComposer.setStyle("-fx-background-color: #" + DEFAULT_BACKGROUND_COLOR);
            return false;
        }
    }

    public boolean checkTitleField(String title) {
        if (title.isEmpty()) {
            textTitle.setStyle("-fx-background-color: #" + AVERAGE_BACKGROUND_COLOR);
            return false;
        } else if (checkInvalidChars(title, "title")) {
            textTitle.setStyle("-fx-background-color: #" + CHANGED_BACKGROUND_COLOR);
            return true;
        } else {
            textTitle.setStyle("-fx-background-color: #" + DEFAULT_BACKGROUND_COLOR);
            return false;
        }
    }

    public boolean checkInvalidChars(String text, String fieldName) {
        if (text == null)
            return false;
        if (text.contains("%") || text.contains("?"))
            return true;

        return !fieldName.equals("composer") && text.contains("/");
    }

    public void checkFields() {

        if (songDatabaseTable.getSelectionModel().getSelectedItem() != null) {
            String artist = textArtist.getText() == null ? "" : textArtist.getText();
            boolean artistCheck = checkArtistField(artist);

            String composer = textComposer.getText() == null ? "" : textComposer.getText();
            boolean composerCheck = checkComposerField(composer);

            String title = textTitle.getText() == null ? "" : textTitle.getText();
            boolean titleCheck = checkTitleField(title);

            if (textYear.getText() != null && !textYear.getText().isEmpty()) {
                textYear.setStyle("-fx-background-color: #" + DEFAULT_BACKGROUND_COLOR);
            } else {
                textYear.setStyle("-fx-background-color: #" + AVERAGE_BACKGROUND_COLOR);
            }

            if (textAlbum.getText() != null && !textAlbum.getText().isEmpty()) {
                textAlbum.setStyle("-fx-background-color: #" + DEFAULT_BACKGROUND_COLOR);
            } else {
                textAlbum.setStyle("-fx-background-color: #" + AVERAGE_BACKGROUND_COLOR);
            }

            if (textPublisher.getText() != null && !textPublisher.getText().isEmpty()) {
                textPublisher.setStyle("-fx-background-color: #" + DEFAULT_BACKGROUND_COLOR);
            } else {
                textPublisher.setStyle("-fx-background-color: #" + AVERAGE_BACKGROUND_COLOR);
            }

            if (dropGenre.getValue() != null && !dropGenre.getValue().isEmpty()) {
                dropGenre.setStyle("-fx-background-color: #" + DEFAULT_BACKGROUND_COLOR);
            } else {
                dropGenre.setStyle("-fx-background-color: #" + AVERAGE_BACKGROUND_COLOR);
            }

            boolean disableUpdateButton = artistCheck || composerCheck || titleCheck;
            checkFilename(disableUpdateButton);
        }

    }

    private void checkFilename(boolean disableUpdateButton) {
        //logger.debug("checking name goes here");
        String newFileLoc = generateNewFilename((CURRENT_FILE_LOC), true, textYear.getText(), dropGenre.getSelectionModel().getSelectedItem().toLowerCase());
        boolean found = new File(newFileLoc).exists();
        updateSongs.setDisable(disableUpdateButton);
        if (!CURRENT_FILE_LOC.equalsIgnoreCase(newFileLoc)) {
            //logger.debug("names do not match!, checking file: " + newFileLoc);

            //if the song exists, update is disabled... that should work for now but maybe requires rethink...
            updateSongs.setDisable((found && checkDone.isSelected()) || disableUpdateButton);

            if (found) {
                updateSongs.setStyle("-fx-background-color: #" + CHANGED_BACKGROUND_COLOR);
            } else {
                AtomicReference<String> foundAlt = new AtomicReference<>("");
                dropGenre.getItems().forEach(genre -> {
                            String getYear = textYear.getText();
                            if (getYear != null) {
                                getYear = getYear.replaceAll("\\D", "");
                            }
                            if (getYear == null || getYear.isEmpty()) {
                                getYear = Year.now().toString();
                            }
                            //logger.debug("getYear:-" + getYear + "-");
                            int year = Integer.parseInt(getYear);
                            for (int testingYear = year - 2; testingYear <= year + 1; testingYear++) {
                                //logger.debug("testing year: " + testingYear);
                                String newFileLocAlt = generateNewFilename((CURRENT_FILE_LOC), true, testingYear + "", genre);
                                if (new File(newFileLocAlt).exists()) {
                                    foundAlt.set(newFileLocAlt.replaceAll("Z:\\\\Songs\\\\", "").replaceAll("\\\\", " - "));
                                }
                            }
                        }
                );
                if (!foundAlt.get().isEmpty()) {
                    updateSongs.setStyle("-fx-background-color: #" + AVERAGE_BACKGROUND_COLOR);
                    SongGlobal.setFileExists(foundAlt.get());
                    mp3Label.setText(foundAlt.get().replaceAll("(?i)\\.mp3", ""));
                } else {
                    updateSongs.setStyle("-fx-background-color: #" + DEFAULT_BACKGROUND_COLOR);
                    SongGlobal.setFileExists("");
                    mp3Label.setText(new File(CURRENT_FILE_LOC).getName().replaceAll("(?i).mp3", ""));
                }
                //logger.debug("set colour back");
            }
        } else {
            updateSongs.setStyle("-fx-background-color: #" + DEFAULT_BACKGROUND_COLOR);
            SongGlobal.setFileExists("");
            mp3Label.setText(new File(CURRENT_FILE_LOC).getName().replaceAll("(?i).mp3", ""));
            //logger.debug("set colour back");
        }

    }

    @FXML
    public void filterTable() {
        String[] filter = textFilterFolder.getText().toLowerCase().split("[|]");

        filteredSongs.setPredicate(currentSearchSong -> {
            if (doneFilter.getSelectionModel().getSelectedIndex() == 1 && !currentSearchSong.getDone())
                return false;
            if (doneFilter.getSelectionModel().getSelectedIndex() == 2 && currentSearchSong.getDone())
                return false;
            String title = currentSearchSong.getTitle().toLowerCase();
            String artist = currentSearchSong.getArtist().toLowerCase();
            String album = currentSearchSong.getAlbum().toLowerCase();
            String genre = currentSearchSong.getGenre().toLowerCase();
            String publisher = currentSearchSong.getPublisher().toLowerCase();
            for (String filterString : filter) {
                //System.out.println(filterString);
                if (!title.contains(filterString) &&
                        !artist.contains(filterString) &&
                        !album.contains(filterString) &&
                        !genre.contains(filterString) &&
                        !publisher.contains(filterString) &&
                        !currentSearchSong.getFileLoc().toLowerCase().contains(filterString))
                    return false;
            }
            if (userFilter.getSelectionModel().getSelectedIndex() != 0 &&
                    !userFilter.getSelectionModel().getSelectedItem().equalsIgnoreCase(currentSearchSong.getEditor()))
                return false;

            try {
                if (truncatedFilter.getSelectionModel().getSelectedIndex() == 1 &&
                        !TruncatedUtil.isTruncated(currentSearchSong))
                    return false;
            } catch (IllegalAccessException e) {
                logger.error("error while truncate checking: ", e);
            }
            return true;
        });

        songDatabaseTable.setItems(sortedSongs);

        labelSongNumber.setText("showing " + filteredSongs.size() + " out of " + songList.size() + " songs with criteria: ");

        SongGlobal.setDoneFilter(doneFilter.getSelectionModel().getSelectedIndex());
        SongGlobal.setTruncatedFilter(truncatedFilter.getSelectionModel().getSelectedIndex());
        SongGlobal.setFolderFilter(textFilterFolder.getText());
        SongGlobal.setUserFilter(userFilter.getSelectionModel().getSelectedItem());

    }

    public void openLegacyData(ActionEvent event) throws IOException {
        logger.debug("Here we open new window");

        String fxmlLocation = "/prodo/marc/gosling/view/legacyAccessDatabaseViewer.fxml";
        SceneController.openWindow(event, fxmlLocation, false);

    }


    public void googleSong() {
        String uri = textArtist.getText() + " " + textTitle.getText();
        uri = "https://www.google.com/search?q=" + uri;
        openURL(uri, "+");
    }

    public void spotSong() {
        String uri = textArtist.getText() + " " + textTitle.getText();
        uri = uri.replace(" ft ", " ");
        uri = uri.replace(" & ", " ");
        uri = "https://open.spotify.com/search/" + uri;
        openURL(uri, "%20");
    }

    public void zampSong() {
        String uri = textTitle.getText();
        uri = "https://www.zamp.hr/baza-autora/rezultati-djela/pregled/" + uri;
        openURL(uri, "+");
    }

    public void discogSong() {
        String uri = textArtist.getText() + " " + textTitle.getText();
        uri = "https://www.discogs.com/search/?type=all&q=" + uri;
        openURL(uri, "+");
    }

    private void openURL(String uri, String space) {
        uri = uri.replace(" ", space);
        uri = uri.replaceAll("[\\[\\]]", "");
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create(uri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void testFiles(DragEvent dragEvent) {
        if (dragEvent.getGestureSource() != songDatabaseTable
                && dragEvent.getDragboard().hasFiles()) {
            List<File> list = dragEvent.getDragboard().getFiles();
            List<Path> mp3List = new ArrayList<>();
            int zipCounter = 0;

            for (File file : list) {
                if (file.isDirectory()) {
                    try {
                        mp3List.addAll(FileUtils.getFileListFromFolder(file, "mp3"));
                    } catch (IOException e) {
                        logger.error("could not read files in folder: ", e);
                    }
                } else if (file.toString().substring(file.toString().length() - 4).equalsIgnoreCase(".mp3")) {
                    mp3List.add(Path.of(file.getAbsolutePath()));
                }

                //TODO: this checks for number of MP3s in a zip file, eventually maybe add a choice what to extract from a zip?
                if (file.toString().endsWith(".zip")) {
                    Enumeration<? extends ZipEntry> files;
                    try (ZipFile zip = new ZipFile(String.valueOf(file))) {
                        files = zip.entries();
                        while (files.hasMoreElements()) {
                            String filename = files.nextElement().getName();
                            if (filename.toLowerCase().endsWith(".mp3")) {
                                //if (filename.contains("stone")) logger.debug(filename);
                                zipCounter++;
                            }
                        }
                    } catch (IOException catchError) {
                        logger.error("error handling zip file!", catchError);
                    }
                }

            }

            SongGlobal.setMP3List(mp3List);
            if (!SongGlobal.getMP3List().isEmpty()) {
                if (SongGlobal.getCurrentSong() == null) {
                    setEditor();
                }
                putMP3ListIntoDB();
            } else {
                Popups.giveInfoAlert("Error importing",
                        "There were no mp3 files to import",
                        list.toString());
            }

            if (zipCounter > 0) Popups.giveInfoAlert("Zip file(s) detected",
                    "MP3 files in zip file(s) detected: ",
                    zipCounter + "");
        }
    }

    /**
     * Switches the table from short to long and vice versa
     */
    public void switchTable() {
        if (!tableEditor.isVisible()) {
            tableID.setVisible(true);
            tableComposer.setVisible(true);
            tableEditor.setVisible(true);
            //tableFileLoc.setVisible(true);
            tablePublisher.setVisible(true);
            tableDone.setVisible(true);
            tableToggleButton.setText("-");
            songDatabaseTable.setMaxWidth(getTableWidth());
        } else {
            tableID.setVisible(false);
            tableComposer.setVisible(false);
            tableEditor.setVisible(false);
            //tableFileLoc.setVisible(false);
            tablePublisher.setVisible(false);
            tableDone.setVisible(false);
            tableToggleButton.setText("+");
            songDatabaseTable.setMaxWidth(getTableWidth());
        }
    }

    private double getTableWidth() {
        double width = 20;
        if (tableArtist.isVisible()) width += tableArtist.getWidth();
        if (tableTitle.isVisible()) width += tableTitle.getWidth();
        if (tableAlbum.isVisible()) width += tableAlbum.getWidth();
        if (tablePublisher.isVisible()) width += tablePublisher.getWidth();
        if (tableComposer.isVisible()) width += tableComposer.getWidth();
        if (tableDuration.isVisible()) width += tableDuration.getWidth();
        if (tableGenre.isVisible()) width += tableGenre.getWidth();
        if (tableYear.isVisible()) width += tableYear.getWidth();
        if (tableDone.isVisible()) width += tableDone.getWidth();
        if (tableFileLoc.isVisible()) width += tableFileLoc.getWidth();
        if (tableEditor.isVisible()) width += tableEditor.getWidth();
        //logger.debug(width);
        return width;
    }

    public void listTag() {
        String id3File = songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc();
        MyID3 tempID3 = ID3Reader.getTag(new File(id3File));
        logger.debug(tempID3.listFrames());
        Popups.giveInfoAlert("ID3 tag content for: ", id3File, tempID3.listFrames() + "   ---" + tempID3.getSize() + "bytes");
    }


    public void filterChange() {
        textFilterFolder.setText(songDatabaseTable.getSelectionModel().getSelectedItem().getArtist() + "|" +
                songDatabaseTable.getSelectionModel().getSelectedItem().getTitle());

        selectFileFromTable(CURRENT_FILE_LOC);

        filterTable();
    }

    public void autofillPublisher(KeyEvent keyEvent) {
        byte[] chars = keyEvent.getCharacter().getBytes();
        boolean special = chars[0] < 31 || keyEvent.isShortcutDown() || chars[0] > 126;
        String searchTerm = textPublisher.getText().toUpperCase();

        if (!special && textPublisher.getCaretPosition() == textPublisher.getLength()) {
            for (String publisher : publisherList) {
                if (publisher.toUpperCase().startsWith(searchTerm)) {
                    textPublisher.deselect();
                    textPublisher.setText(publisher);
                    textPublisher.positionCaret(searchTerm.length());
                    textPublisher.selectEnd();
                    break;
                }
            }
        }
        checkFields();
    }

    public void openFolder(ActionEvent actionEvent) {
        String fileLoc = new File(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc()).getParent();
        String file = new File(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc()).getName();
        try {
            Runtime.getRuntime().exec("explorer.exe /select," + fileLoc + "\\" + file);
        } catch (IOException e) {
            logger.error("Error opening folder", e);
        }

        selectFileFromTable(CURRENT_FILE_LOC);
    }
}