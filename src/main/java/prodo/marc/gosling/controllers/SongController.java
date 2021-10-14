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
import org.controlsfx.control.textfield.TextFields;
import prodo.marc.gosling.dao.ID3Header;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.hibernate.repository.SongRepository;
import prodo.marc.gosling.service.*;
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
import java.util.concurrent.atomic.AtomicInteger;


public class SongController {

    private static final Logger logger = LogManager.getLogger(SongController.class);
    /**
     * Initial volume for mp3
     */
    private static final Integer INITIAL_VOLUME_SO_MY_EARS_DONT_EXPLODE = 20;
    final int skipIncrement = 10000;

    @FXML
    ComboBox<String> dropGenre, doneFilter, truncatedFilter, userFilter;
    @FXML
    MediaPlayer mplayer;
    @FXML
    Button songBackButton, addSongButton, addFolderButton, parseFilenameButton, googleSongButton,
            openLegacyDataButton, updateSongs, buttonPlay, buttonPause, skipBack, skipForward, skipForwardSmall,
            skipBackSmall, buttonRevert, spotSongButton, zampSongButton, refreshTableButton, tableToggleButton;
    @FXML
    Label mp3Time, labelVolume, labelSongNumber, mp3Label;
    @FXML
    TableView<Song> songDatabaseTable;
    @FXML
    TableColumn<Song, String> tableArtist, tableTitle, tableAlbum, tablePublisher, tableComposer,
            tableGenre, tableISRC, tableFileLoc, tableEditor;
    @FXML
    TableColumn<Song, Integer> tableID;
    @FXML
    TableColumn<Song, Year> tableYear;
    @FXML
    TableColumn<Song, Boolean> tableDone;
    @FXML
    TableColumn<Song, String> tableDuration;
    @FXML
    TextField textAlbum, textArtist, textTitle, textPublisher, textComposer, textYear, textISRC,
            textFilterFolder;
    @FXML
    Slider mp3Slider, volumeSlider;
    @FXML
    CheckBox checkDone;

    ObservableList<Song> songList = FXCollections.observableArrayList();
    FilteredList<Song> filteredSongs = new FilteredList<>(songList);
    SortedList<Song> sortedSongs = new SortedList<>(filteredSongs);
    ID3v24Tag copiedID3 = new ID3v24Tag();
    String changedBackgroundColor = "bb3333";
    String defaultTextColor = "FFFFFF";
    ObservableList<String> publisherList = FXCollections.observableArrayList();
    private boolean updateCheck = true;
    private String currentFileLoc = "";
    private Path tempDir;
    private String editorName;
    private boolean tableMin = false;


    private void publisherAutocomplete() {
        String[] array = {"Aquarius", "Black Butter", "Capitol", "Columbia", "Crorec", "Dallas", "Emi",
                "Epic", "Hit Records", "Insanity Records", "Menart", "Mikrofon Records", "Masterworks",
                "Ministry of Sound Recordings", "Polydor", "Promo", "Rca", "Scardona", "Sony", "Spona",
                "Melody", "Dancing Bear", "Heksagon", "Arista", "Geffen", "Intek", "Sedma Sekunda",
                "Bonton", "Hamar", "Rubikon"};
        Arrays.sort(array);
        publisherList.addAll(Arrays.asList(array));
    }

    private String[] getGenres() {
        String[] returnArr = {"", "Cro", "Cro Zabavne", "Instrumental", "Klape", "Kuruza",
                "Pop", "xxx", "Italian", "Susjedi"};
        Arrays.sort(returnArr);
        return returnArr;
    }

    public void initialize() {

        logger.debug("----- Executing initialize");

        songDatabaseTable.setOnDragOver(dragEvent -> {
            dragEvent.acceptTransferModes(TransferMode.LINK);
            dragEvent.consume();
        });
        songDatabaseTable.setOnDragDropped(dragEvent -> {
            testFiles(dragEvent);
            dragEvent.consume();
        });

        try {
            tempDir = Files.createTempDirectory("tmp");
        } catch (IOException ex) {
            logger.error("Couldn't create temp dir", ex);
        }

        try {
            editorName = InetAddress.getLocalHost().getHostName();
            logger.debug("Editor name: " + editorName);
        } catch (UnknownHostException ex) {
            logger.error("Unknown host:", ex);
        }

        dropGenre.getItems().addAll(getGenres());
        doneFilter.getItems().addAll("Ignore done", "Done", "Not Done");
        truncatedFilter.getItems().addAll("Ignore truncated", "Truncated");
        userFilter.getItems().addAll("Any user","Direktor","Glazba","ONAIR");
        doneFilter.getSelectionModel().select(SongGlobal.getDoneFilter());
        truncatedFilter.getSelectionModel().select(SongGlobal.getTruncFilter());
        textFilterFolder.setText(SongGlobal.getFolderFilter());
        userFilter.getSelectionModel().select(SongGlobal.getUserFilter());


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
        tableEditor.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getEditor()));
        tableDuration.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDurationString()));
        tableDone.setCellFactory(cellData -> new CheckBoxTableCell<>());

        sortedSongs.comparatorProperty().bind(songDatabaseTable.comparatorProperty());

        updateTable();

        volumeSlider.setValue(INITIAL_VOLUME_SO_MY_EARS_DONT_EXPLODE);
        changeVolume();

        if (!songDatabaseTable.getSelectionModel().isEmpty()) {
            updateTextFields(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
        }

        if (SongGlobal.isFilenameParsed()) {
            updateTextFields(SongGlobal.getCurrentSong().getFileLoc());
        }

        publisherAutocomplete();

        switchTable();

        logger.debug("----- ending initialize");
    }

    public void installAccelerators() {
        KeyCombination kc = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        Runnable rn = this::updateMP3;
        updateSongs.getScene().getAccelerators().put(kc, rn);
        KeyCombination kc1 = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        Runnable rn1 = () -> textFilterFolder.requestFocus();
        updateSongs.getScene().getAccelerators().put(kc1, rn1);
    }


    @FXML
    protected void backToMain(ActionEvent event) throws IOException {
        logger.debug("----- Executing backToMain");
        closeMediaStream();
        SceneController.openScene(event, "view/hello-view.fxml");
        logger.debug("----- ending backToMain");
    }

    @FXML
    protected void clickedParseButton(ActionEvent event) throws IOException {
        logger.debug("----- Executing clickedParseButton");
        Song song = SongGlobal.getCurrentSong();
        closeMediaStream();
        if (song == null) {
            Popups.giveInfoAlert("Open parse window error",
                    "Couldn't open the filename parse window",
                    "no file selected, file location=null");
        } else {
            SceneController.openScene(event, "view/parseFilename.fxml");
        }
        logger.debug("----- ending clickedParseButton");
    }

    @FXML
    protected void clickedFolderButton() {
        logger.debug("----- Executing clickedFolderButton");
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
        logger.debug("----- ending clickedFolderButton");
    }

    @FXML
    protected void addSongsFromFolder(File directory) throws IOException {

        logger.debug("----- Executing addSongsFromFolder");

        SongGlobal.setMP3List(FileUtils.getFileListFromFolder(directory, "mp3"));
        logger.debug("number of files in the list: " + SongGlobal.getMP3List().size());
        setEditor();

        putMP3ListIntoDB();

        logger.debug("----- ending addSongsFromFolder");
    }

    private void setEditor() {
        Song tempSong = new Song();
        tempSong.setEditor(editorName);
        SongGlobal.setCurrentSong(tempSong);
    }

    private void putMP3ListIntoDB() {

        String fxmlLocation = "/prodo/marc/gosling/view/progress.fxml";
        try {
            SceneController.openWindow(null, fxmlLocation);
        } catch (IOException e) {
            logger.error("couldn't open import window", e);
        }

    }


    @FXML
    private void updateTable() {

        logger.debug("----- Executing updateTable");

        SongRepository songRepo = new SongRepository();
        List<Song> songList1 = songRepo.getSongs();

        songList.clear();
        songList.addAll(songList1);
        filterTable();
        selectFileFromTable(currentFileLoc);

        logger.debug("----- ending updateTable");
    }


    @FXML
    public void clickTable(MouseEvent event) {

        logger.debug("----- Executing clickTable");

        if (event.getButton() == MouseButton.SECONDARY) {
            logger.debug("right click");
        } else if (event.getButton() == MouseButton.PRIMARY && !songDatabaseTable.getSelectionModel().isEmpty()) {
            try {
                openMP3(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc());
            } catch (Exception e) {
                logger.error("no table entry clicked"+songDatabaseTable.getSelectionModel().getSelectedItem(), e);
            }
        }
        songDatabaseTable.setMaxWidth(getTableWidth());

        logger.debug("----- ending clickTable");
    }

    @FXML
    protected void openMP3(String fileLoc) {

        logger.debug("----- Executing openMP3");

        installAccelerators();

        Song currentSong = new Song();
        Song globalSong = SongGlobal.getCurrentSong();

        //TODO: this will need to change readmode to database... also needs to be at a different place
        boolean localFile;
        if (!editorName.equals(songDatabaseTable.getSelectionModel().getSelectedItem().getEditor()) &&
                !songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc().contains("Z:\\")) {
            logger.debug("error!!!!!");
            localFile = false;
            updateSongs.setDisable(true);
        } else {
            localFile = true;
            updateSongs.setDisable(false);
        }

        if (!Objects.equals(currentFileLoc, "") && new File(currentFileLoc).exists()) {

            //TODO: finally reworked but needs improvement maybe
            currentSong.setArtist(textArtist.getText());
            currentSong.setTitle(textTitle.getText());
            currentSong.setAlbum(textAlbum.getText());
            currentSong.setPublisher(textPublisher.getText());
            currentSong.setComposer(textComposer.getText());
            currentSong.setYear(MyStringUtils.parseYear(textYear.getText()));
            currentSong.setGenre(dropGenre.getSelectionModel().getSelectedItem());
            //currentSong.setISRC(textISRC.getText());
            currentSong.setFileLoc(SongGlobal.getCurrentSong().getFileLoc());
            currentSong.setDone(checkDone.isSelected());
            currentSong.setEditor(editorName);
        }

        if (!currentSong.equals(globalSong) && currentSong.getFileLoc() != null) {

            boolean result = Popups.giveConfirmAlert("Unsaved changes",
                    "You are switching to another file with possible unsaved changes",
                    "Do you want to save the ID3 changes you have made?\n" + currentSong + "\n" + SongGlobal.getCurrentSong());

            if (result) {
                updateMP3();
            } else {
                selectFileFromTable(currentFileLoc);
            }
        } else {
            if (localFile) {
                updateTextFields(fileLoc);
                TextFields.bindAutoCompletion(textPublisher, publisherList).setMaxWidth(170);
                MyID3 testing = ID3Reader.getTag(new File(fileLoc));
                logger.debug(testing.getData(ID3Header.ARTIST));
//                if (!testing.getVersionString().equals("2.4.0")) {
//                    logger.debug("no id3 found");
//                } else {
//                    ID3Reader.writeFile(fileLoc, testing);
//                }
            }
        }

        logger.debug("----- ending openMP3");
    }


    private void updateTextFields(String fileLoc) {

        logger.debug("----- Executing updateTextFields");

        //show file name
        mp3Label.setText(new File(fileLoc).getName().replaceAll("(?i).mp3", ""));
        currentFileLoc = fileLoc;

        //load id3 data into text fields
        try {
            File file = new File(fileLoc);
            ID3v24Tag id3Data = ID3v2Utils.getID3(file);
            textArtist.setText(id3Data.getArtist());
            textTitle.setText(id3Data.getTitle());
            textAlbum.setText(id3Data.getAlbum());
            textPublisher.setText(id3Data.getPublisher());
            textComposer.setText(id3Data.getComposer());
            textYear.setText(id3Data.getYear());
            if (id3Data.getKey() != null) {
                checkDone.setSelected(id3Data.getKey().equals("true"));
            } else {
                checkDone.setSelected(false);
            }
            //TODO: ovo ne bi trebalo radit vako...
            if (id3Data.getGenreDescription() != null) {
                dropGenre.getSelectionModel().select(MyStringUtils.replaceCroChars(id3Data.getGenreDescription()));
            }
            if (dropGenre.getSelectionModel().getSelectedItem() == null || id3Data.getGenreDescription() == null) {
                dropGenre.getSelectionModel().select(0);
            }
            if (dropGenre.getSelectionModel().getSelectedIndex() == -1) {
                logger.debug("could not find genre: " + id3Data.getGenreDescription());
                Popups.giveInfoAlert("Unknown genre",
                        "The song has an unknown genre",
                        "Genre: " + id3Data.getGenreDescription());
            }
            logger.debug("***" + id3Data.getGenreDescription() + "***");

            //textISRC.setText(id3Data.getISRC());

            if (SongGlobal.isFilenameParsed()) {
                textArtist.setText(SongGlobal.getCurrentSong().getArtist());
                textTitle.setText(SongGlobal.getCurrentSong().getTitle());
                textPublisher.setText(SongGlobal.getCurrentSong().getPublisher());
                selectFileFromTable(currentFileLoc);
                SongGlobal.setFilenameParsed(false);
            } else {
                SongGlobal.setCurrentSong(ID3v2Utils.songDataFromID3(id3Data, file.getAbsolutePath(), editorName));
            }

        } catch (Exception report) {
            logger.error("Error while opening file " + fileLoc, report);
        }

        logger.debug("----- ending updateTextFields");
    }

    private void selectFileFromTable(String currentFileLoc) {
        AtomicInteger i = new AtomicInteger();
        songDatabaseTable.getItems().forEach(song -> {
            if (song.getFileLoc().equals(currentFileLoc)) {
                songDatabaseTable.getSelectionModel().select(i.get());
            }
            i.getAndIncrement();
        });
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
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() + skipIncrement));
        }
    }

    @FXML
    protected void moveTimeBack() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() - skipIncrement));
        }
    }

    @FXML
    protected void moveTimeForwardLittle() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() + 100));
        }
    }

    @FXML
    protected void moveTimeBackLittle() {
        if (mplayer != null) {
            mplayer.seek(Duration.millis(mplayer.getCurrentTime().toMillis() - 100));
        }
    }

    public void updateMP3() {

        logger.debug("----- Executing updateMP3");

        changeCRO();

        ID3v24Tag id3 = new ID3v24Tag();

        if (textAlbum.getText().isEmpty() || textAlbum.getText() == null) {
            textAlbum.setText(textTitle.getText());
        }
        if ((textTitle.getText().isEmpty() || textTitle.getText() == null) && !textAlbum.getText().isEmpty()) {
            textTitle.setText(textAlbum.getText());
        }
        if (textYear.getText() == null) {
            textYear.setText(String.valueOf(2021));
        }

        id3.setArtist(textArtist.getText());
        id3.setTitle(textTitle.getText());
        id3.setAlbum(textAlbum.getText());
        id3.setPublisher(textPublisher.getText());
        id3.setComposer(textComposer.getText());
        id3.setYear(textYear.getText());
        id3.setRecordingTime(String.valueOf(SongGlobal.getCurrentSong().getDuration()));
        if (checkDone.isSelected()) {
            id3.setKey("true");
        } else {
            id3.setKey(" ");
        }
        id3.setGenreDescription(dropGenre.getSelectionModel().getSelectedItem());
        //song.setISRC(textISRC.getText());

        if (renameFile()) {
            updateSongEntry(id3, SongGlobal.getCurrentSong().getId(), currentFileLoc);
            makeTextFieldsWhite();
            SongGlobal.setCurrentSong(ID3v2Utils.songDataFromID3(id3, currentFileLoc, editorName));
            writeToMP3(id3, currentFileLoc);
            selectFileFromTable(currentFileLoc);
        }
        logger.debug("----- ending updateMP3");
    }

    private void makeTextFieldsWhite() {
        textArtist.setStyle("-fx-background-color: ");
        textYear.setStyle("-fx-background-color: ");
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
            if (checkDone.isSelected()) {
                id3Data.setKey("true");
            } else {
                id3Data.setKey(" ");
            }
            if (song.getGenreDescription() != null) {
                id3Data.setGenreDescription(song.getGenreDescription());
            }
            //id3Data.setISRC(song.getISRC());

            mp3file.save(backupFileLoc);
        } catch (Exception e) {
            logger.error("Error while opening file " + fileLoc, e);
        }

        try {
            Files.delete(Path.of(fileLoc));
        } catch (IOException e) {
            logger.debug("File cannot be deleted! " + fileLoc);
        }
        File mp3FileNew = new File(backupFileLoc);
        boolean info = mp3FileNew.renameTo(new File(fileLoc));
        logger.debug("rename results are " + info);

        logger.debug("----- ending writeToMP3");
    }

    private void openMediaFile(String fileLoc) {
        logger.debug("----- Executing openMediaFile");
        String tempMp3 = tempDir + "\\temp";

        try {
            Files.copy(Paths.get(fileLoc), Paths.get(tempMp3 + ".mp3"), StandardCopyOption.REPLACE_EXISTING);
            tempMp3 = tempMp3 + ".mp3";
        } catch (Exception ex) {
            logger.error("could not create temp mp3 file: ", ex);
            try {
                Files.copy(Paths.get(fileLoc), Paths.get(tempMp3 + "1.mp3"), StandardCopyOption.REPLACE_EXISTING);
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

        //set slider
        try {
            double sliderValue = new Mp3File(mp3File).getLengthInMilliseconds();
            mp3Slider.setMax(sliderValue / 100);
        } catch (Exception ex) {
            logger.error("can't open file to set slider: ", ex);
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
        File mp3 = FileUtils.openFile("MP3 files (*.mp3)", "mp3", SongGlobal.getCurrentFolder());
        if (mp3 != null) {
            SongGlobal.setCurrentFolder(mp3.getParent());
            FileUtils.addMP3(mp3.toPath(), editorName);
            updateTable();
        }
        logger.debug("----- ending addSong2DB");
    }

    public void revertID3() {
        logger.debug("----- Executing revertID3");
        boolean result = Popups.giveConfirmAlert("Unsaved changes",
                "You are resetting the ID3 changes you made for this MP3",
                "Do you want to load the old data without saving the changes?");

        if (result) {
            updateTextFields(currentFileLoc);
            makeTextFieldsWhite();
        }

        logger.debug("----- ending revertID3");
    }

    public void changeVolume() {
        labelVolume.setText("Volume: " + String.format("%.0f", volumeSlider.getValue()) + "%");
        if (mplayer != null) {
            mplayer.setVolume(volumeSlider.getValue() / 100);
        }
    }

    public void copyID3() {
        logger.debug("----- Executing copyID3");

        copiedID3 = ID3v2Utils.getID3(new File(songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc()));
        selectFileFromTable(currentFileLoc);

        logger.debug("----- ending copyID3");
    }

    public void pasteID3() {
        logger.debug("----- Executing pasteID3");

        boolean confirm = Popups.giveConfirmAlert("Warning",
                "You're about to overwrite ID3 data",
                "Please comfirm your action");

        if (confirm) {
            String fileLoc = songDatabaseTable.getSelectionModel().getSelectedItem().getFileLoc();
            writeToMP3(copiedID3, fileLoc);
            updateSongEntry(copiedID3, SongRepository.getIDofFile(fileLoc), fileLoc);
            selectFileFromTable(currentFileLoc);
            updateTextFields(fileLoc);
        }

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
                if (Objects.equals(song.getFileLoc(), currentFileLoc)) {
                    currentFileLoc = "";
                }
                try {
                    Files.delete(Path.of(fileLoc));
                } catch (IOException er) {
                    logger.error("can't delete file: ", er);
                }
            } else if (Objects.equals(picked, "Database entry")) {
                Song song = songDatabaseTable.getSelectionModel().getSelectedItem();
                SongRepository.delete(song);
                updateTable();
                if (Objects.equals(song.getFileLoc(), currentFileLoc)) {
                    currentFileLoc = "";
                }
            } else {
                logger.debug("code to delete id3 tag here");
            }
        }

        logger.debug("----- ending deleteFile");
    }

    public void changeCRO() {
        textArtist.setText(MyStringUtils.replaceCroChars(textArtist.getText()));
        textTitle.setText(MyStringUtils.replaceCroChars(textTitle.getText()));
        textAlbum.setText(MyStringUtils.replaceCroChars(textAlbum.getText()));
        textPublisher.setText(MyStringUtils.replaceCroChars(textPublisher.getText()));
        textComposer.setText(MyStringUtils.replaceCroChars(textComposer.getText()));
    }

    //TODO: this part needs to check if all the fields are there so it needs to be handled earlier, prolly in updateMP3()
    public boolean renameFile() {
        logger.debug("----- Executing renameFile");
        File oldFile = new File(currentFileLoc);
        String newFileLoc = "\\" + textArtist.getText() + " - " + textTitle.getText() + ".mp3";
        if (!checkDone.isSelected()) {
            newFileLoc = oldFile.getParent() + newFileLoc;
        } else {
            if (dropGenre.getSelectionModel().getSelectedItem().equals("")) {
                Popups.giveInfoAlert("file rename error",
                        "no genre selected",
                        "please select genre and try again");
                return false;
            }
            String genre = dropGenre.getSelectionModel().getSelectedItem() + "\\";
            if (genre.equalsIgnoreCase("pop\\")) {
                genre = "";
            }
            String year = textYear.getText() + "\\";
            newFileLoc = "Z:\\Songs\\" + genre + year + newFileLoc;
            new File(Paths.get(newFileLoc).getParent().toString()).mkdirs();
        }
        File newFile = new File(newFileLoc);
        if (!oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            if (!oldFile.renameTo(newFile)) {
                Popups.giveInfoAlert("Error",
                        "Your file can not be renamed",
                        newFile.getAbsolutePath() + " already exists or is in use.");

                if (SongRepository.getIDofFile(newFileLoc) == null) {
                    FileUtils.addMP3(Path.of(newFileLoc), editorName);
                }
                updateTable();

                return false;
            }
        }
        currentFileLoc = newFile.getAbsolutePath();
        mp3Label.setText(newFile.getName().replaceAll("(?i).mp3", ""));
        logger.debug("----- ending renameFile");

        return true;
    }

    public void updateSongEntry(ID3v24Tag id3, Integer databaseID, String fileLoc) {
        logger.debug("----- Executing updateSongEntry");
        Song song = ID3v2Utils.songDataFromID3(id3, fileLoc, editorName);
        song.setId(databaseID);
        song.setEditor(editorName);
        SongRepository.addSong(song);
        updateTable();
        logger.debug("----- ending updateSongEntry");
    }

    public void checkArtistField() {
        if (MyStringUtils.compareStrings(SongGlobal.getCurrentSong().getArtist(), textArtist.getText())) {
            textArtist.setStyle("-fx-background-color: ");
        } else {
            textArtist.setStyle("-fx-background-color: #" + changedBackgroundColor);
        }
    }

    public void checkYearField() {
        int pos = textYear.getCaretPosition();
        int len = textYear.getLength();
        if (textYear.getText() != null) {
            textYear.setText(textYear.getText().replaceAll("[^\\d]", ""));
        }
        pos = textYear.getLength() - len + pos;
        textYear.positionCaret(pos);
        if (MyStringUtils.compareStrings(String.valueOf(SongGlobal.getCurrentSong().getYear()), textYear.getText())) {
            textYear.setStyle("-fx-background-color: ");
            textYear.setStyle("-fx-text-color: #" + defaultTextColor);
        } else {
            textYear.setStyle("-fx-background-color: #" + changedBackgroundColor);
        }
    }

    @FXML
    public void filterTable() {
        String filter = textFilterFolder.getText().toLowerCase();

        filteredSongs.setPredicate(x -> {
            if (doneFilter.getSelectionModel().getSelectedIndex() == 1 && !x.getDone())
                return false;
            if (doneFilter.getSelectionModel().getSelectedIndex() == 2 && x.getDone())
                return false;
            String title = x.getTitle();
            if (title == null) title = "";
            title = title.toLowerCase();
            String artist = x.getArtist();
            if (artist == null) artist = "";
            artist = artist.toLowerCase();
            String album = x.getAlbum();
            if (album == null) album = "";
            album = album.toLowerCase();
            if (!title.contains(filter) &&
                    !artist.contains(filter) &&
                    !album.contains(filter))
                return false;
            if (userFilter.getSelectionModel().getSelectedIndex() != 0 &&
                    !userFilter.getSelectionModel().getSelectedItem().equals(x.getEditor()))
                return false;

            try {
                if (truncatedFilter.getSelectionModel().getSelectedIndex() == 1 &&
                        !TruncatedUtil.isTruncated(x))
                    return false;
            } catch (IllegalAccessException e) {
                logger.error("error while truncate checking: ",e);
            }
            return true;
        });

        songDatabaseTable.setItems(sortedSongs);

        labelSongNumber.setText("showing " + filteredSongs.size() + " out of " + songList.size() + " songs with criteria: ");

        SongGlobal.setDoneFilter(doneFilter.getSelectionModel().getSelectedIndex());
        SongGlobal.setTruncFilter(truncatedFilter.getSelectionModel().getSelectedIndex());
        SongGlobal.setFolderFilter(textFilterFolder.getText());
        SongGlobal.setUserFilter(userFilter.getSelectionModel().getSelectedItem());

    }

    public void googleSong() {
        String uri = textArtist.getText() + " " + textTitle.getText();
        uri = uri.replace(" ", "+");
        uri = "https://www.google.com/search?q=" + uri;
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

        String fxmlLocation = "/prodo/marc/gosling/view/legacyAccessDatabaseViewer.fxml";
        SceneController.openWindow(event, fxmlLocation);

    }


    public void dupeCheck() {
        textFilterFolder.setText(songDatabaseTable.getSelectionModel().getSelectedItem().getTitle());
        truncatedFilter.getSelectionModel().select(0);
        doneFilter.getSelectionModel().select(0);
        updateTable();
        var ref = new Object() {
            String title = "";
        };
        songDatabaseTable.getItems().forEach(song -> {
            if (song.getTitle().equals(ref.title)) {
                logger.debug(ref.title);
            }
            ref.title = song.getTitle();
        });
    }

    public void spotSong() {
        String uri = textArtist.getText() + " " + textTitle.getText();
        uri = uri.replace(" ", "%20");
        uri = "https://open.spotify.com/search/" + uri;
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create(uri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void zampSong() {
        String uri = textTitle.getText();
        uri = uri.replace(" ", "+");
        uri = "https://www.zamp.hr/baza-autora/rezultati-djela/pregled/" + uri;
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

            for (File file : list) {
                if (file.toString().substring(file.toString().length() - 4).equalsIgnoreCase(".mp3")) {
                    mp3List.add(Path.of(file.getAbsolutePath()));
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
        }
    }

    public void switchTable() {
        if (tableMin) {
            tableComposer.setVisible(true);
            tableEditor.setVisible(true);
            tableFileLoc.setVisible(true);
            tablePublisher.setVisible(true);
            tableDone.setVisible(true);
            tableToggleButton.setText("-");
            songDatabaseTable.setMaxWidth(getTableWidth());
            tableMin = false;
        } else {
            tableComposer.setVisible(false);
            tableEditor.setVisible(false);
            tableFileLoc.setVisible(false);
            tablePublisher.setVisible(false);
            tableDone.setVisible(false);
            tableToggleButton.setText("+");
            songDatabaseTable.setMaxWidth(getTableWidth());
            tableMin = true;
        }
    }

    private double getTableWidth() {
        double width = 2;
        if (tableArtist.isVisible()) width+=tableArtist.getWidth();
        if (tableTitle.isVisible()) width+=tableTitle.getWidth();
        if (tableAlbum.isVisible()) width+=tableAlbum.getWidth();
        if (tablePublisher.isVisible()) width+=tablePublisher.getWidth();
        if (tableComposer.isVisible()) width+=tableComposer.getWidth();
        if (tableDuration.isVisible()) width+=tableDuration.getWidth();
        if (tableGenre.isVisible()) width+=tableGenre.getWidth();
        if (tableYear.isVisible()) width+=tableYear.getWidth();
        if (tableDone.isVisible()) width+=tableDone.getWidth();
        if (tableFileLoc.isVisible()) width+=tableFileLoc.getWidth();
        if (tableEditor.isVisible()) width+=tableEditor.getWidth();
        //logger.debug(width);
        return width;
    }

}