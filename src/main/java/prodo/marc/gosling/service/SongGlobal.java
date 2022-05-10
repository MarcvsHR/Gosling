package prodo.marc.gosling.service;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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

    private static Song currentSong;
    private static String fileFolder;
    private static final String propertiesFile = System.getProperty("user.home") + "\\properties.txt";
    private static List<Path> mp3List;
    private static boolean filenameParsed = false;
    private static int doneFilter = 0;
    private static int truncatedFilter = 0;
    private static String folderFilter = "";
    private static String userFilter = "Any user";
    private static String fileExists = "";


    public static String getFileExists() {
        return fileExists;
    }

    public static void setFileExists(String fileExists) {
        SongGlobal.fileExists = fileExists;
    }

    public static int getDoneFilter() {
        return doneFilter;
    }

    public static void setDoneFilter(int doneFilter) {
        SongGlobal.doneFilter = doneFilter;
    }

    public static int getTruncatedFilter() {
        return truncatedFilter;
    }

    public static void setTruncatedFilter(int truncatedFilter) {
        SongGlobal.truncatedFilter = truncatedFilter;
    }

    public static String getFolderFilter() {
        return folderFilter;
    }

    public static void setFolderFilter(String folderFilter) {
        SongGlobal.folderFilter = folderFilter;
    }

    public static boolean isFilenameParsed() {
        return filenameParsed;
    }

    public static void setFilenameParsed(boolean filenameParsed) {
        SongGlobal.filenameParsed = filenameParsed;
    }


    public static Song getCurrentSong() {
        return currentSong;
    }

    public static void setCurrentSong(Song song) {
        currentSong = song;
        //logger.debug(currentSong);
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

    public static void setUserFilter(String selectedIndex) {
        SongGlobal.userFilter = selectedIndex;
    }

    public static String getUserFilter() {
        return SongGlobal.userFilter;
    }

}
