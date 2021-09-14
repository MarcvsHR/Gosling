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

        ID3v24Tag id3tag = new ID3v24Tag();

        try {
            Mp3File song = new Mp3File(mp3File);

            id3tag = (ID3v24Tag) song.getId3v2Tag();
        } catch (Exception error) {
            logger.error("can't fetch ID3 data from file",error);
        }

        return id3tag;
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
        testSong.setISRC(null);
        testSong.setFileLoc(file.getAbsoluteFile().toString());
        if (id3Data.getKey()==null) {id3Data.setKey(" ");}
        testSong.setDone(id3Data.getKey().equals("true"));

        logger.debug("----- ending id3ToSong");

        return testSong;
    }
}
