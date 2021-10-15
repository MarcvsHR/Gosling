package prodo.marc.gosling.service.id3;

import javafx.util.Duration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.dao.id3Header;
import prodo.marc.gosling.hibernate.repository.SongRepository;
import prodo.marc.gosling.service.MyStringUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class ID3v2Utils {

    private static final Logger logger = LogManager.getLogger(ID3v2Utils.class);

    /**
     * Extract song object for id3 tag
     */
    public static Song songDataFromID3(MyID3 id3Data, String path, String editor) {

        logger.debug("----- Executing id3ToSong");

        Song testSong = new Song();

        testSong.setArtist(id3Data.getData(id3Header.ARTIST));
        //if (testSong.getArtist() == null) testSong.setArtist("");
        testSong.setTitle(id3Data.getData(id3Header.TITLE));
        //if (testSong.getTitle() == null) testSong.setTitle("");
        testSong.setAlbum(id3Data.getData(id3Header.ALBUM));
        //if (testSong.getAlbum() == null) testSong.setAlbum("");


        testSong.setPublisher(id3Data.getData(id3Header.PUBLISHER));
        testSong.setComposer(id3Data.getData(id3Header.COMPOSER));
        testSong.setYear(MyStringUtils.parseYear(id3Data.getData(id3Header.YEAR)));
        testSong.setGenre(id3Data.getData(id3Header.GENRE));
        if (id3Data.getData(id3Header.LENGTH) == null) id3Data.addFrame(id3Header.LENGTH, "0");
        testSong.setDuration(Duration.millis(Double.parseDouble((id3Data.getData(id3Header.LENGTH).replaceAll(" ms", "")))));
        testSong.setISRC(id3Data.getData(id3Header.ISRC));
        testSong.setFileLoc(path);
        testSong.setEditor(editor);
        String key = id3Data.getData(id3Header.KEY);
        if (key == null) {
            key = " ";
        }
//        logger.debug("key: "+id3Data.getData(id3Header.KEY));
        testSong.setDone(key.equals("true"));

        Integer ID = SongRepository.getIDofFile(path);
        if (ID != null) {
            testSong.setId(ID);
        }

        logger.debug("----- ending id3ToSong");

        return testSong;
    }

    public static long getDuration(byte[] fileContent, int size) {
        logger.debug("----- Executing getDuration");
        long duration;
        size += 10;

        byte[] mp3Data = Arrays.copyOfRange(fileContent, size, fileContent.length - size);
        String header = Integer.toBinaryString(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data, 0, 4)).getInt());
        header = header.substring(16);
        int bitrate = Integer.parseInt(header.substring(0, 4), 2);
        if (bitrate > 5) bitrate += bitrate - 5;
        if (bitrate > 14) bitrate += bitrate - 13;
        if (bitrate == 33) bitrate = 37;
        bitrate = bitrate * 8 + 24;

        int sampling = Integer.parseInt(header.substring(4, 6), 2);
        switch (sampling) {
            case 0:
                sampling = 44100;
                break;
            case 1:
                sampling = 48000;
                break;
            case 2:
                sampling = 32000;
                break;
        }

        int padding = Integer.parseInt(header.substring(6, 7));

        int frameLen = (144000 * bitrate / sampling) + padding;

        double estDur = (mp3Data.length - 4) / (double)frameLen;
        estDur = estDur * 26.1;

        duration = (long) estDur;

//        logger.debug("bitrate: " + bitrate);
//        logger.debug("Sampling: " + sampling);
//        logger.debug("Padding: " + padding);
//        logger.debug("Frame length: " + frameLen);
//        logger.debug("Estimated file duration in ms: " + estDur);

        logger.debug("----- ending getDuration");

        return duration;
    }


}
