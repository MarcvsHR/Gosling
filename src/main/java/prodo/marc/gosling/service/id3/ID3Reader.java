package prodo.marc.gosling.service.id3;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.ID3Frame;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.id3Header;
import prodo.marc.gosling.service.Popups;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class ID3Reader {

    private static final Logger logger = LogManager.getLogger(ID3Reader.class);

    public static MyID3 getTag(File file) {
        byte[] fileContent = null;
        MyID3 id3Data = new MyID3();

        if (file.exists()) {
            try {
                fileContent = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                logger.error("couldn't read file", e);
            }
        }

        if (fileContent != null) {
            String header = new String(Arrays.copyOfRange(fileContent, 0, 3));
//            logger.debug("first 3 characters: " + header);
            if (header.equals("ID3")) {
                id3Data.setVersion((byte) 2, fileContent[3], fileContent[4]);
//                logger.debug("id3 version: " + id3Data.getVersionString());
            }
            if (id3Data.getVersionString().equals("2.4.0") ||
                    id3Data.getVersionString().equals("2.3.0") ||
                    id3Data.getVersionString().equals("2.2.0")) {
                id3Data.setFlags(fileContent[5]);
                if (id3Data.getFlags() > 0) logger.debug("Flags detected!!!!   " + id3Data.getFlags());
                id3Data.setSize(ByteBuffer.wrap(Arrays.copyOfRange(fileContent, 6, 10)).getInt(), true);
                int tempSize = id3Data.getSize();

//                logger.debug("Size: " + id3Data.getSize());
//                logger.debug("size bytes in array" + Arrays.toString(Arrays.copyOfRange(fileContent, 6, 10)));
                if (!id3Data.getVersionString().equals("2.2.0")) {
                    int startFrames = 10;
                    boolean calculateFrameHeader = id3Data.getVersionString().equals("2.4.0");
                    while (startFrames < id3Data.getSize()) {
                        ID3Frame frame;
                        frame = getFrame(fileContent, startFrames, calculateFrameHeader);
                        startFrames += frame.getSize() + 10;
                        if (Objects.equals(frame.getFrameID(), "XXXX")) {
                            break;
                        } else if (frame.getFrameID().equals(id3Header.USER_DATA)) {
                            String number = String.format("%02d",id3Data.getTXXX());
                            frame.setFrameID("TXXX"+number);
                            id3Data.changeTXXX(1);
                            //logger.debug("TXXX DATA: "+ new String(frame.getContent()));
                        } else if (frame.getFrameID().equals(id3Header.COMMENT)) {
                            String number = String.format("%02d",id3Data.getCOMM());
                            frame.setFrameID("COMM"+number);
                            id3Data.changeCOMM(1);
                        } else if (frame.getFrameID().equals(id3Header.GENERAL_OBJECT)) {
                            String number = String.format("%02d",id3Data.getGEOB());
                            frame.setFrameID("GEOB"+number);
                            id3Data.changeGEOB(1);
                        } else if (frame.getFrameID().equals(id3Header.CD_ID)) {
                            String tempHeader = new String(Arrays.copyOfRange(fileContent, startFrames, startFrames + 4));
                            if (!id3Header.CHECK_LIST(tempHeader))
                                startFrames++;
                        }
                        if (!frame.getFrameID().equals(id3Header.CD_ID))
                            id3Data.addFrame(frame);
                        logger.debug(frame.getFrameID());
//                        logger.debug(frame.getSize());
                        //id3Data.getFrame(frameID).setSize(frame.getContent().length + 1, false);
//                    logger.debug("Current pos: " + startFrames);
                    }
                } else {
                    id3Data.setVersion((byte)2,(byte)4,(byte)0);
                }

                if (!id3Data.exists(id3Header.LENGTH) || id3Data.getData(id3Header.LENGTH).equals("0"))
                    id3Data.addFrame(id3Header.LENGTH, String.valueOf(ID3v2Utils.getDuration(fileContent,id3Data.getSize())));

                logger.debug("duration: "+ ID3v2Utils.getDuration(fileContent, tempSize));

                id3Data.setSize(id3Data.totalFrameSize(), false);
//                logger.debug("final size: "+id3Data.getSize());
//                logger.debug("number of frames total: "+id3Data.getFrames().size());
//                logger.debug("last frame content: "+id3Data.getFrame(id3Data.getFrames().size()-1).getContent());
//                logger.debug("calculated size: " + id3Data.totalFrameSize());
//                logger.debug("calculated bytes: " + Arrays.toString(MyID3.convertIntToBytes(id3Data.totalFrameSize())));

            }
        }

        if (id3Data.getVersionString().equals("2.5.0"))
            id3Data = new MyID3(String.valueOf(ID3v2Utils.getDuration(fileContent, 0)));

        return id3Data;
    }


    private static ID3Frame getFrame(byte[] fileContent, int start, boolean calculateSize) {
        ID3Frame frame = new ID3Frame();
        frame.setFrameID(new String(Arrays.copyOfRange(fileContent, start, start + 4)));
        String test = String.valueOf((char) 0);
        test += test;
        test += test;

        start += 4;
        frame.setSize(ByteBuffer.wrap(Arrays.copyOfRange(fileContent, start, start + 4)).getInt(), calculateSize);
        start += 4;
        frame.setFlag1(fileContent[start]);
        frame.setFlag2(fileContent[start + 1]);
        frame.setEncoding(fileContent[start + 2]);
        start += 3;
        if (frame.getFrameID().equals(test)) {
            frame.setFrameID("XXXX");
        } else {
            //logger.debug("header: "+frame.getFrameID());

            byte[] tempArr = Arrays.copyOfRange(fileContent, start, start + frame.getSize() - 1);
            if (frame.getSize()==1) {
                frame.setContent(new byte[0]);
            }
            else if (frame.getEncoding() > 0 && frame.getEncoding() < 4) {
                int encoding = frame.getEncoding();
                //System.out.println("Encoding set to: "+encoding);
                Charset utf8charset = StandardCharsets.UTF_8;
                Charset utf16Charset = StandardCharsets.UTF_16;
                Charset utf16beCharset = StandardCharsets.UTF_16BE;
                Charset iso88591charset = StandardCharsets.ISO_8859_1;
                if (tempArr[0] != 0 && encoding == 1 && tempArr[0] != (byte)255){
                    byte[] newArr = new byte[tempArr.length];
                    newArr[0] = (byte)(0);
                    System.arraycopy(tempArr,0, newArr, 1, tempArr.length-1);
                    tempArr = newArr;
                }
                ByteBuffer inputBuffer = ByteBuffer.wrap(tempArr);
                CharBuffer data = null;
//                System.out.println(Arrays.toString(inputBuffer.array()));
                if (encoding == 3) data = utf8charset.decode(inputBuffer);
                if (encoding == 2) data = utf16beCharset.decode(inputBuffer);
                if (encoding == 1) data = utf16Charset.decode(inputBuffer);
                assert data != null;
                ByteBuffer outputBuffer = iso88591charset.encode(data);
//                System.out.println(new String(outputBuffer.array()));
//                System.out.println(Arrays.toString(outputBuffer.array()));
                //logger.debug("old data: " + Arrays.toString(tempArr));
                tempArr = outputBuffer.array();
//                if (tempArr[0] == 63) tempArr = Arrays.copyOfRange(tempArr,1,tempArr.length);
//                if (tempArr[0] == 63) tempArr = Arrays.copyOfRange(tempArr,1,tempArr.length);
                if (tempArr[tempArr.length-1] == 0)
                    tempArr = Arrays.copyOf(tempArr,tempArr.length-1);
                frame.setContent(tempArr);
                //logger.debug("new data: " + Arrays.toString(tempArr));
                frame.setEncoding((byte) 0);
            }
            else {
                if (tempArr[tempArr.length - 1] == 0 && !frame.getFrameID().equals(id3Header.ALBUM_ART)) {
                    frame.setContent((Arrays.copyOf(tempArr, tempArr.length - 1)));
                } else {
                    frame.setContent((tempArr));
                }
            }
        }
        if (!id3Header.CHECK_LIST(frame.getFrameID())) {
            logger.debug("unknown header: " + frame.getFrameID());
            Popups.giveInfoAlert("ID3 import error", "found new unknown header", frame.getFrameID() + " - ");
        }
//        logger.debug("header: " + frame.getFrameID());
//        logger.debug("size: " + frame.getSize());
//        if (!frame.getFrameID().equals("XXXX")) logger.debug("content size: " + frame.getContent().length);
//        logger.debug("f1: " + frame.getFlag1());
//        logger.debug("f2: " + frame.getFlag2());
//        logger.debug("encoding: " + frame.getEncoding());
//        if (!frame.getFrameID().equals("APIC") && !frame.getFrameID().equals("XXXX"))
//            logger.debug("data: " + new String(frame.getContent()));
//        logger.debug("");

        return frame;
    }


    public static void writeFile(String fileLoc, MyID3 id3) {
        byte[] fileContent = null;
        byte[] mp3Data = new byte[0];
        byte[] id3Data = id3.getID3Data();
        byte[] outputFileData;
        try {
            fileContent = Files.readAllBytes(new File(fileLoc).toPath());
        } catch (IOException e) {
            logger.error("couldn't read file", e);
        }

        if (fileContent != null) {
            int size = 0;
            String header = new String(Arrays.copyOfRange(fileContent, 0, 3));
            if (header.equals("ID3")) {
                size = MyID3.computeSize(ByteBuffer.wrap(Arrays.copyOfRange(fileContent, 6, 10)).getInt());
                size += 10;
//                logger.debug("Found old id3 of size: "+size);
            }
//            logger.debug("first 3 chars are : - "+header);
//            logger.debug("size should be above this...");
//            logger.debug(fileContent[size]);
            mp3Data = new byte[fileContent.length - size];
            System.arraycopy(fileContent, size, mp3Data, 0, fileContent.length - size);
//            logger.debug(fileContent[fileContent.length-1]);
//            logger.debug(mp3Data[0]);
//            logger.debug(mp3Data[mp3Data.length-1]);
        }
        outputFileData = new byte[mp3Data.length + id3Data.length];
        System.arraycopy(id3Data, 0, outputFileData, 0, id3Data.length);
        System.arraycopy(mp3Data, 0, outputFileData, id3Data.length, mp3Data.length);

//        File outputFile = new File("c:\\test\\testing.mp3");
        File outputFile = new File(fileLoc);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(outputFileData);
        } catch (Exception e) {
            logger.error("could not write to output file: ", e);
        }
    }
}
