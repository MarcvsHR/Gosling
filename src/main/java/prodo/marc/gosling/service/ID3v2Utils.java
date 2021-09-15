package prodo.marc.gosling.service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import lombok.NonNull;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.Song;

import java.io.File;

public class ID3v2Utils {

    private static final Logger logger = LogManager.getLogger(ID3v2Utils.class);

    public static boolean compareID3v2(@NonNull ID3v2 source,@NonNull ID3v2 target){
        //TODO ovdje neka pametnija implementacija
        return  source.equals(target);
    }
    /**
     * Extract idv3 tage for given file
     * */
    public static ID3v24Tag getID3(File mp3File) {

        logger.debug("----- Executing getID3");

        ID3v24Tag id3tag = new ID3v24Tag();
        ID3v2 tempID3;

        try {
            Mp3File song = new Mp3File(mp3File);
            tempID3 = song.getId3v2Tag();

            if (tempID3 != null) {
                id3tag.setArtist(tempID3.getArtist());
                id3tag.setTitle(tempID3.getTitle());
                id3tag.setAlbum(tempID3.getAlbum());
                id3tag.setPublisher(tempID3.getPublisher());
                id3tag.setComposer(tempID3.getComposer());
                String genre = tempID3.getGenreDescription();
                if (genre == null) {
                    genre = "";
                }
                id3tag.setGenreDescription(genre);
                id3tag.setYear(tempID3.getYear());
                if (id3tag.getYear() == null) {
                    id3tag.setYear("");
                }
                id3tag.setKey(tempID3.getKey());
                if (id3tag.getKey() == null) {
                    id3tag.setKey(" ");
                }
                //id3tag.setISRC(tempID3.getISRC());
            }

        } catch (Exception error) {
            logger.error("can't fetch ID3 data from file",error);
        }

        logger.debug("----- ending getID3");

        return id3tag;
    }

    public static Song songDataFromID3(ID3v24Tag id3Data, String path) {

        logger.debug("----- Executing id3ToSong");

        Song testSong = new Song();

        testSong.setArtist(id3Data.getArtist());
        testSong.setTitle(id3Data.getTitle());
        testSong.setAlbum(id3Data.getAlbum());
        testSong.setPublisher(id3Data.getPublisher());
        testSong.setComposer(id3Data.getComposer());
        int year = 0;
        try {
            year = Integer.parseInt(id3Data.getYear());
        } catch (NumberFormatException e) {
            logger.debug("id3 data does not have year information");
        }
        testSong.setYear(year);
        testSong.setGenre(id3Data.getGenreDescription());
        //testSong.setISRC(id3Data.getISRC());
        testSong.setISRC(null);
        testSong.setFileLoc(path);
        String key = id3Data.getKey();
        if (key == null) {key = " ";}
        testSong.setDone(key.equals("true"));

        logger.debug("----- ending id3ToSong");

        return testSong;
    }
}
