package prodo.marc.gosling.service.id3;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.dao.id3Header;
import prodo.marc.gosling.dao.mp3Frame;
import prodo.marc.gosling.hibernate.repository.SongRepository;
import prodo.marc.gosling.service.MyStringUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class ID3v2Utils {

    private static final Logger logger = LogManager.getLogger(ID3v2Utils.class);

    /**
     * Extract song object for id3 tag
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
        //if (id3Data.getData(id3Header.LENGTH) == null) id3Data.addFrame(id3Header.LENGTH, "0");
        //testSong.setDuration(Integer.parseInt(id3Data.getData(id3Header.LENGTH).replaceAll(" ms", "")));
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

        Integer ID = SongRepository.getIDofFile(path);
        if (ID != null) {
            testSong.setId(ID);
        }

        //logger.debug("----- ending id3ToSong");

        return testSong;
    }

    public static long getDuration(byte[] fileContent, int size) {

        //logger.debug("----- Executing getDuration");
        if (size != 0) size += 10;

        byte[] mp3Data = Arrays.copyOfRange(fileContent, size, fileContent.length - size);
        double frameLen;

        int counter = 0;

//        logger.debug("first header: "+header);
        //String headerString = new String(Arrays.copyOfRange(mp3Data,counter,counter+3));
        //this part was handling multiple id3s, not needed for now
//        if (headerString.equals("ID3")) {
//            logger.debug("-----------------------2 id3s found! panic!-----------------------");
//            logger.debug("file: "+fileLoc);
//            MyID3 id3Data = new MyID3();
//            logger.debug(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data,6,10)).getInt());
//            id3Data.setSize(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data, 6, 10)).getInt(), true);
//            logger.debug("delete size should be: "+id3Data.getSize());
//            counter += id3Data.getSize()+10;
//            header = Integer.toBinaryString(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data, counter, counter+4)).getInt());
//            logger.debug("retrying second header at "+(counter+size)+": "+header);
//            mp3Data = Arrays.copyOfRange(fileContent, size+counter, fileContent.length - size+counter);
//            counter = 0;
//            byte[] id3DataBytes = Arrays.copyOfRange(fileContent, 0, size);
//
//            byte[] outputFileData = new byte[mp3Data.length + id3DataBytes.length];
//            System.arraycopy(id3DataBytes, 0, outputFileData, 0, id3DataBytes.length);
//            System.arraycopy(mp3Data, 0, outputFileData, id3DataBytes.length, mp3Data.length);
//
////        File outputFile = new File("c:\\test\\testing.mp3");
//            File outputFile = new File(fileLoc);
//            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
//                outputStream.write(outputFileData);
//            } catch (Exception e) {
//                logger.error("could not write to output file: ", e);
//            }
//        }


        double estDur = 0;
        while (counter < mp3Data.length) {
            BigInteger HBI = BigInteger.valueOf(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data, counter, counter + 4)).getInt());
            mp3Frame frame = new mp3Frame(HBI);
            //error checking, not needed atm
            if (counter < 0) {
                logger.debug(size+counter);
                logger.debug(frame.getAllData());
            }

            if (!frame.isValid()) {
                int distance = mp3Data.length - counter;
                String headerString = new String(Arrays.copyOfRange(mp3Data,counter,counter+3));
                logger.debug(headerString);
                logger.debug("weird header found at "+(distance)+" before the end");
                logger.debug(size+counter);
                break; }
//            logger.debug(frame.getAllData());
            frameLen = frame.getFrameSizeInBytes();
//            logger.debug("frame length: "+frameLen);
            counter+=frameLen;
            estDur+=frame.getFrameTimeInMS();
//            logger.debug(estDur);
        }
//        logger.debug("Estimated file duration in s: " + estDur/1000);
        //logger.debug("----- ending getDuration");

        return estDur < 1000 ? 0 : (long)estDur;
    }

}
