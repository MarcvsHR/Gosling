package prodo.marc.gosling.service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.hibernate.repository.SongRepository;

import java.io.File;

public class ID3v2Utils {

    private static final Logger logger = LogManager.getLogger(ID3v2Utils.class);

    /**
     * Extract idv3 tage for given file
     */
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
            logger.error("can't fetch ID3 data from file", error);
        }

        logger.debug("----- ending getID3");

        return id3tag;
    }

    public static Song songDataFromID3(ID3v24Tag id3Data, String path, String editor) {

        logger.debug("----- Executing id3ToSong");

        Song testSong = new Song();

        testSong.setArtist(id3Data.getArtist());
        if (testSong.getArtist() == null) testSong.setArtist("");
        testSong.setTitle(id3Data.getTitle());
        if (testSong.getTitle() == null) testSong.setTitle("");
        testSong.setAlbum(id3Data.getAlbum());
        if (testSong.getAlbum() == null) testSong.setAlbum("");

        testSong.setPublisher(id3Data.getPublisher());
        testSong.setComposer(id3Data.getComposer());
        testSong.setYear(StringUtils.parseYear(id3Data.getYear()));
        testSong.setGenre(id3Data.getGenreDescription());
        //testSong.setISRC(id3Data.getISRC());
        testSong.setISRC(null);
        testSong.setFileLoc(path);
        testSong.setEditor(editor);
        String key = id3Data.getKey();
        if (key == null) {
            key = " ";
        }
        testSong.setDone(key.equals("true"));

        Integer ID = SongRepository.getIDofFile(path);
        if (ID != null) {
            testSong.setId(ID);
        }

        logger.debug("----- ending id3ToSong");

        return testSong;
    }
}
