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
    public static ID3v2 getID3(File mp3File) {

        try {
            Mp3File song = new Mp3File(mp3File);

            return song.getId3v2Tag();
        } catch (Exception errpr) {
            logger.error("can't fetch ID3 data from file",errpr);

            return new ID3v24Tag();
        }
    }

    public static Song songDataFromFile(File file) {

        logger.debug("----- Executing id3ToSong");

        Song testSong = new Song();

        ID3v2 id3Data = getID3(file);

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
        testSong.setISRC("ISRC");
        testSong.setFileLoc(file.getAbsoluteFile().toString());

        logger.debug("----- ending id3ToSong");

        return testSong;
    }
}
