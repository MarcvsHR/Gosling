package prodo.marc.gosling.service;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.dao.id3Header;
import prodo.marc.gosling.hibernate.repository.SongRepository;
import prodo.marc.gosling.service.id3.ID3Reader;
import prodo.marc.gosling.service.id3.ID3v2Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileUtils {

    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    //@FXML
    public static File pickFolder(String initialDir) {

        logger.debug("----- Executing pickFolder");

        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(initialDir));

        logger.debug("----- ending pickFolder");

        return dc.showDialog(null);
    }

    public static List<Path> getFileListFromFolder(File directory, String ext) throws IOException {
        logger.debug("----- Executing addSongsFromFolder");

        List<Path> mp3List = new ArrayList<>();

        Stream<Path> walk = Files.walk(Paths.get(directory.getAbsolutePath()));
        {
            walk.filter(Files::isRegularFile).forEach(file -> {
                if (file.toString().endsWith("." + ext)) {
                    mp3List.add(file);
                }
            });
        }

        logger.debug("----- Ending addSongsFromFolder");
        return mp3List;
    }

    public static File openFile(String desc, String ext, String initialDir) {
        logger.debug("----- Executing openFile");

        FileChooser fc = new FileChooser();
        fc.setTitle("Open " + desc);
        fc.setInitialDirectory(new File(initialDir));
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter(desc, "*." + ext);
        fc.getExtensionFilters().add(extFilter);

        logger.debug("----- ending openFile");

        return fc.showOpenDialog(null);
    }

    public static String addMP3(Path path, String editor) {

        logger.debug("----- Executing addMP3");

        Song song = new Song();
        song.setFileLoc(String.valueOf(path));
        song.setEditor(editor);
        if (SongRepository.getIDofFile(song.getFileLoc()) != null) {
            logger.debug("---song already exists - " + song);
            return song.getFileLoc();
        } else {
            File mp3 = new File(String.valueOf(path));
            MyID3 id3tag = ID3Reader.getTag(mp3);
            logger.debug("current time: " + id3tag.getData(id3Header.LENGTH));
            song = ID3v2Utils.songDataFromID3(id3tag, String.valueOf(path), editor);
            SongRepository.addSong(song);
        }

        logger.debug("----- ending addMP3");

        return null;
    }

}
