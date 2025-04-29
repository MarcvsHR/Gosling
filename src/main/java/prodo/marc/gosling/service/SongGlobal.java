package prodo.marc.gosling.service;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class SongGlobal {

    private static final Logger logger = LogManager.getLogger(SongGlobal.class);
    @Getter
    private static List<Song> songList;
    private static String fileFolder;
    private static final String propertiesFile = System.getProperty("user.home") + "\\properties.txt";
    private static List<Path> mp3List;
    @Setter
    @Getter
    private static String editor;
    private static MyID3 COPIED_ID3 = new MyID3();
    @Getter
    private static String lastSongID;


    public static void setLastSongID(String lastSongID) {
        SongGlobal.lastSongID = lastSongID;
    }


    public static String getCurrentFolder() {
        if (fileFolder == null) {
            Properties prop = new Properties();

            try {
                FileInputStream in = new FileInputStream(propertiesFile);
                prop.load(in);
                in.close();
                fileFolder = prop.getProperty("fileFolder");
            } catch (Exception error) {
                fileFolder = "c:";
                setCurrentFolder(fileFolder);
                logger.error("there was a problem loading", error);
            }
            if (Files.notExists(Path.of(fileFolder))) {
                fileFolder = "C:";
            }
        }
        return fileFolder;
    }

    public static void setCurrentFolder(String string) {
        fileFolder = string;
        Properties prop = new Properties();
        prop.setProperty("fileFolder", fileFolder);
        try {
            //TODO: this needs to be handled differently
            boolean fileMade = new File(propertiesFile).createNewFile();
            logger.debug("file create result: " + fileMade);
            FileWriter out = new FileWriter(propertiesFile);
            prop.store(out, null);
            out.close();
        } catch (Exception error) {
            logger.error("there was a problem saving", error);
        }

    }

    public static List<Path> getMP3List() {
        return mp3List;
    }

    public static void setMP3List(List<Path> inputList) {
        mp3List = inputList;
    }

    public static void setSongList(List<Song> songList) {
        SongGlobal.songList = songList;
    }

    public static MyID3 getCopiedID3() {
        return COPIED_ID3;
    }
    public static void setCopiedID3(MyID3 copiedID3) {
        COPIED_ID3 = copiedID3;
    }

}
