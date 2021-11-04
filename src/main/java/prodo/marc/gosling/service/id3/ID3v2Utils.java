package prodo.marc.gosling.service.id3;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.dao.id3Header;
import prodo.marc.gosling.hibernate.repository.SongRepository;
import prodo.marc.gosling.service.MyStringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
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
        if (id3Data.getData(id3Header.LENGTH) == null) id3Data.addFrame(id3Header.LENGTH, "0");
        testSong.setDuration(Integer.parseInt(id3Data.getData(id3Header.LENGTH).replaceAll(" ms", "")));
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

    public static long getDuration(byte[] fileContent, int size, String fileLoc) {

        //logger.debug("----- Executing getDuration");
        if (size != 0) size += 10;

        byte[] mp3Data = Arrays.copyOfRange(fileContent, size, fileContent.length - size);
        String header;
        double frameLen;

        int counter = 0;
        int frames = 0;

        header = Integer.toBinaryString(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data, counter, counter+4)).getInt());
//        logger.debug("first header: "+header);
        String headerString = new String(Arrays.copyOfRange(mp3Data,counter,counter+3));
        if (headerString.equals("ID3")) {
            logger.debug("-----------------------2 id3s found! panic!-----------------------");
            logger.debug("file: "+fileLoc);
            MyID3 id3Data = new MyID3();
            logger.debug(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data,6,10)).getInt());
            id3Data.setSize(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data, 6, 10)).getInt(), true);
            logger.debug("delete size should be: "+id3Data.getSize());
            counter += id3Data.getSize()+10;
            header = Integer.toBinaryString(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data, counter, counter+4)).getInt());
            logger.debug("retrying second header at "+(counter+size)+": "+header);
            mp3Data = Arrays.copyOfRange(fileContent, size+counter, fileContent.length - size+counter);
            counter = 0;
            byte[] id3DataBytes = Arrays.copyOfRange(fileContent, 0, size);

            byte[] outputFileData = new byte[mp3Data.length + id3DataBytes.length];
            System.arraycopy(id3DataBytes, 0, outputFileData, 0, id3DataBytes.length);
            System.arraycopy(mp3Data, 0, outputFileData, id3DataBytes.length, mp3Data.length);

//        File outputFile = new File("c:\\test\\testing.mp3");
            File outputFile = new File(fileLoc);
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(outputFileData);
            } catch (Exception e) {
                logger.error("could not write to output file: ", e);
            }
        }

        double multi = 1152;
        if (header.startsWith("01", 20))
            multi = multi/48;
        else if (header.startsWith("10", 20))
            multi = multi/32;
        else
            multi = multi/44.1;

        while (counter < mp3Data.length) {
            header = Integer.toBinaryString(ByteBuffer.wrap(Arrays.copyOfRange(mp3Data, counter, counter+4)).getInt());
//            logger.debug(header);
            if (!header.startsWith("111111111111101")) {
                int distance = mp3Data.length - counter;
                headerString = new String(Arrays.copyOfRange(mp3Data,counter,counter+3));
                logger.debug(headerString);
                logger.debug("weird header found at "+(distance)+" before the end: "+header);
                break; }
            frameLen = getFrameLen(header);
            counter+=frameLen;
            frames++;
        }
        double estDur = frames*multi;
//        logger.debug("Estimated file duration in s: " + estDur/1000);

        //logger.debug("----- ending getDuration");

        return (long)estDur;
    }

    private static double getFrameLen(String header) {
        //TODO: one day this should return an object with all the proper frame info but for now it handles most mp3s
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

//        logger.debug("bitrate: "+bitrate);
//        logger.debug("sampling: "+sampling);

        return Math.floor(144000 * (float)bitrate / (float)sampling) + padding;
    }


}
