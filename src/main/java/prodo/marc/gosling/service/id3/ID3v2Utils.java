package prodo.marc.gosling.service.id3;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.dao.id3Header;
import prodo.marc.gosling.dao.MP3Frame;
import prodo.marc.gosling.hibernate.repository.SongRepository;
import prodo.marc.gosling.service.MyStringUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class ID3v2Utils {

    private static final Logger logger = LogManager.getLogger(ID3v2Utils.class);

    /**
     * Extract song object from id3 tag
     */
    public static Song songDataFromID3(MyID3 id3Data, String path, String editor) {

        //logger.debug("----- Executing id3ToSong");

        Song testSong = new Song();

        testSong.setArtist(id3Data.getData(id3Header.ARTIST));
        testSong.setTitle(id3Data.getData(id3Header.TITLE));
        testSong.setAlbum(id3Data.getData(id3Header.ALBUM));


        testSong.setPublisher(id3Data.getData(id3Header.PUBLISHER));
        testSong.setComposer(id3Data.getData(id3Header.COMPOSER));
        testSong.setYear(MyStringUtils.parseYear(id3Data.getData(id3Header.YEAR)));
        testSong.setGenre(id3Data.getData(id3Header.GENRE));
        testSong.setDuration(Integer.parseInt(id3Data.getData(id3Header.LENGTH)));
        testSong.setISRC(id3Data.getData(id3Header.ISRC));
        testSong.setFileLoc(path);
        testSong.setEditor(editor);
        String key = id3Data.getData(id3Header.KEY);
        if (key == null) {
            key = " ";
        }
//        logger.debug("key: "+id3Data.getData(id3Header.KEY));
        testSong.setDone(key.equals("true"));

        Integer ID = SongRepository.getFileID(path);
        if (ID != null) {
            testSong.setId(ID);
        }

        //logger.debug("----- ending id3ToSong");

        return testSong;
    }

    public static long getDuration(byte[] fileContent, int size, String fileName) {

        //logger.debug("----- Executing getDuration");
        if (size != 0) size += 10;

        byte[] mp3Data = Arrays.copyOfRange(fileContent, size, fileContent.length);
        double frameLen;
        int counter = 0;
        double estDur = 0;
        while (counter < mp3Data.length) {
            BigInteger bigIntHeader = BigInteger.valueOf(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data, counter, counter + 4)).getInt());
            MP3Frame frame = new MP3Frame(bigIntHeader);

            if (counter < 0) {
                logger.debug(size + counter);
                logger.debug(frame.getAllData());
            }

            if (!frame.isValid().isEmpty()) {
                int distance = mp3Data.length - counter;
                String headerString = new String(Arrays.copyOfRange(mp3Data, counter, counter + 3));
                logger.debug("");
                logger.debug("Song name: " + fileName);

                if (headerString.equals("TAG")) {
                    logger.debug("Found V1 ID3 tag at " + (counter + size) + " with " + distance + " bytes left");
                    if (distance == 128) logger.debug(new String(Arrays.copyOfRange(mp3Data,counter,counter+distance)));
                    break;
                }

                if (headerString.equals("ID3")) {
                    logger.debug("Found V2 ID3 tag at " + (counter + size) + " with " + distance + " bytes left");
                    logger.debug("If you see this message, please report it to the developers");
                    logger.debug(frame.isValid());
                    break;
                }

                if (headerString.equals("\u0000\u0000\u0000")) {
                    logger.debug("Found NULL tag at " + (counter + size) + " with " + distance + " bytes left");
                    break;
                }

                if (headerString.equals("APE")) {
                    logger.debug("Found APE tag at " + (counter + size) + " with " + distance + " bytes left");
                    logger.debug("Wny... just... why...");
                    break;
                }

                logger.debug(frame.isValid());
                logger.debug(headerString);
                StringBuilder bytes = new StringBuilder();
                for (byte b : Arrays.copyOfRange(mp3Data, counter, counter + 30)) {
                    bytes.append(Integer.toHexString(b & 0xff)).append(" ");
                }
                //convert bytes string to upper case
                logger.debug("Byte values: " + bytes.toString().toUpperCase());
                logger.debug("weird header found at " + (counter + size) + " with " + distance + " bytes left");
                logger.debug(size + counter);
                break;
            }
//            logger.debug(frame.getAllData());
            frameLen = frame.getFrameSizeInBytes();
//            logger.debug("frame length: "+frameLen);
            counter += frameLen;
            estDur += frame.getFrameTimeInMS();
//            logger.debug(estDur);
//            logger.debug(frame.getAllData());
//            logger.debug("frame number: " + counter/frameLen);
//            logger.debug("bytes left: " + (mp3Data.length - counter));
//            logger.debug("mp3 length: " + mp3Data.length);
//            logger.debug("header size: " + size);
//            logger.debug("total size: " + (mp3Data.length + size));
//            logger.debug("file size: " + fileContent.length);
//            logger.debug("estimated left predicted: " + (fileContent.length - size));

            //TODO: this needs to raise an error instead of just returning the duration
            //if this is 0, that means there's somehow a bad frame in the data
            if (frame.getFrameSizeInBytes() == 0) {
                logger.debug("error at byte: " + (counter + size));
                return (long) estDur;
            }
        }
        //logger.debug(counter+size);
        //logger.debug("Estimated file duration in s: " + estDur/1000);
        //logger.debug("----- ending getDuration");

        return estDur < 1000 ? 0 : (long) estDur;
    }

}
