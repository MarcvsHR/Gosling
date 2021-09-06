package prodo.marc.gosling.controllers;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.log4j.LogManager;
import org.hibernate.mapping.List;
import prodo.marc.gosling.HelloApplication;
import prodo.marc.gosling.dao.Song;

import org.apache.log4j.Logger;
import prodo.marc.gosling.hibernate.repository.SongRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SongController {

    public TableColumn tableArtist, tableTitle, tableAlbum, tablePublisher, tableComposer, tableYear, tableGenre, tableISRC;
    public Button songBackButton, addSongButton, addFolderButton;
    public TableView<Song> songDatabaseTable;
    private static final Logger logger = LogManager.getLogger(SongController.class);

    @FXML
    //goes back to the main window
    protected void backToMain() throws IOException {
        Stage stage = (Stage) songBackButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("view/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Songs");
        stage.setScene(scene);
    }

    @FXML
    //goes to the mp3 edit window
    protected void addSong2DB() throws IOException {
        Stage stage = (Stage) addSongButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/mp3.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("mp3");
        stage.setScene(scene);
    }

    @FXML
    protected void addSongsFromFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File("C:\\"));
        File directory = dc.showDialog(null);
        SongRepository songRepo = new SongRepository();
        try (Stream<Path> walk = Files.walk(Paths.get(directory.getAbsolutePath()))) {
            walk.filter(Files::isRegularFile).forEach(file -> {
                if (file.toString().endsWith(".mp3")) {
                    Song id3Tag = null;
                    logger.debug("processing file: "+ file);
                    try {
                        id3Tag = getID(file.toFile());
                    } catch (InvalidDataException e) {
                        logger.error("couldn't read ID3", e);
                    } catch (UnsupportedTagException e) {
                        logger.error("couldn't read ID3", e);
                    } catch (IOException e) {
                        logger.error("couldn't read ID3", e);
                    }
                    songRepo.addSong(id3Tag);
                }
            });

            logger.debug("Songs on end -> \n: "+Arrays.toString(songRepo.getSongs().toArray()));
            ObservableList<Song> tableList = FXCollections.observableArrayList();
            songRepo.getSongs().forEach(newEntry -> {
                songDatabaseTable.getItems().add(newEntry);
            });


        } catch (IOException e) {
            logger.error("couldn't get files from folder",e);
        }
   }

   public Song getID(File file) throws InvalidDataException, UnsupportedTagException, IOException {

       Song testSong = new Song();
       Mp3File mp3 = new Mp3File(file);
       ID3v2 id3Data= mp3.getId3v2Tag();

       testSong.setArtist(id3Data.getArtist());
       testSong.setTitle(id3Data.getTitle());
       testSong.setAlbum(id3Data.getAlbum());
       testSong.setPublisher(id3Data.getPublisher());
       testSong.setComposer(id3Data.getComposer());
       int year = 0;
       try {
           year = Integer.parseInt(id3Data.getYear());
       } catch (NumberFormatException e) {
           logger.error(id3Data.getYear() +" in file "+file.getAbsoluteFile()+"is not a number", e);
       }
       testSong.setYear(year);
       testSong.setGenre(id3Data.getGenreDescription());
       //testSong.setISRC(id3Data.getISRC());
       testSong.setISRC("ISRC");
       //logger.debug(file.getAbsoluteFile());
       //logger.debug(testSong.toString());

       return testSong;
   }

}
