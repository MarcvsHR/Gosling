package prodo.marc.gosling.service;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.Song;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SongGlobal {

    private static final Logger logger = LogManager.getLogger(SongGlobal.class);

    private static Song currentSong;
    private static String fileFolder;
    private static String propertiesFile = System.getProperty("user.home")+"\\properties.txt";

    public static Song getCurrentSong() {
        return currentSong;
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
                logger.error("there was a problem loading",error);
            }
            if (Files.notExists(Path.of(fileFolder))) {
                fileFolder = "C:";
            }
        }
        return fileFolder;
    }


    public static void setCurrentSong(Song song) {
        currentSong = song;
    }

    public static void setCurrentFolder(String string){
        fileFolder = string;
        Properties prop = new Properties();
        prop.setProperty("fileFolder",fileFolder);
        try {
            //TODO: this needs to be handled differently
            boolean fileMade =  new File(propertiesFile).createNewFile();
            logger.debug("file create result: " + fileMade);
            FileWriter out = new FileWriter(propertiesFile);
            prop.store(out, null);
            out.close();
        } catch (Exception error) {
            logger.error("there was a problem saving",error);
        }

    }

}
