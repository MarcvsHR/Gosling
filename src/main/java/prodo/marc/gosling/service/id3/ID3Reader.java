package prodo.marc.gosling.service.id3;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.ID3Frame;
import prodo.marc.gosling.dao.MyID3;
import prodo.marc.gosling.dao.id3Header;

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
            //logger.debug("first 3 characters: " + header);
            if (header.equals("ID3")) {
                id3Data.setVersion((byte) 2, fileContent[3], fileContent[4]);
                //logger.debug("id3 version: " + id3Data.getVersionString());
            }
            if (id3Data.getVersionString().equals("2.4.0") ||
                    id3Data.getVersionString().equals("2.3.0") ||
                    id3Data.getVersionString().equals("2.2.0")) {
                id3Data.setFlags(fileContent[5]);
                if (id3Data.getFlags() > 0) logger.debug("Flags detected!!!!   " + id3Data.getFlags());

                id3Data.setSize(ByteBuffer.wrap(Arrays.copyOfRange(fileContent, 6, 10)).getInt(), true);
                //logger.debug("id3 size: " + id3Data.getSize());

                if (!id3Data.getVersionString().equals("2.2.0")) {
                    int startFrames = 10;
                    boolean calculateFrameHeader = id3Data.getVersionString().equals("2.4.0");
                    while (startFrames < id3Data.getSize()) {
                        ID3Frame frame;
                        frame = getFrame(fileContent, startFrames, calculateFrameHeader);
                        startFrames += frame.getSize() + 10;
                        if (Objects.equals(frame.getFrameID(), id3Header.DELETE)) {
                            break;
                        } else if (frame.getFrameID().equals(id3Header.USER_DATA)) {
                            String number = String.format("%02d", id3Data.getTXXX());
                            frame.setFrameID(id3Header.USER_DATA + number);
                            id3Data.changeTXXX(1);
                        } else if (frame.getFrameID().equals(id3Header.COMMENT)) {
                            String number = String.format("%02d", id3Data.getCOMM());
                            frame.setFrameID(id3Header.COMMENT + number);
                            id3Data.changeCOMM(1);
                        } else if (frame.getFrameID().equals(id3Header.GENERAL_OBJECT)) {
                            String number = String.format("%02d", id3Data.getGEOB());
                            frame.setFrameID(id3Header.GENERAL_OBJECT + number);
                            id3Data.changeGEOB(1);
                        } else if (frame.getFrameID().equals(id3Header.CD_ID)) {
                            String tempHeader = new String(Arrays.copyOfRange(fileContent, startFrames, startFrames + 4));
                            if (id3Header.LIST_NOT_CONTAINS(tempHeader))
                                startFrames++;
                        }
                        if (!frame.getFrameID().equals(id3Header.CD_ID)) {
                            id3Data.addFrame(frame);
                            //logger.debug("Frame added: " + frame.getFrameID());
                        }
                    }
                } else {
                    id3Data.setVersion((byte) 2, (byte) 4, (byte) 0);
                }

                String duration = String.valueOf(ID3v2Utils.getDuration(fileContent, id3Data.getSize(), file.getName()));

                if (!id3Data.exists(id3Header.LENGTH) || id3Data.getData(id3Header.LENGTH).equals("0"))
                    id3Data.addFrame(id3Header.LENGTH, duration);
                else if (!id3Data.getData(id3Header.LENGTH).equals(duration)) {

                    //TODO: eventually need to go through the database and change the duration of all songs in the database and in the file

                    int difference = Integer.parseInt(id3Data.getData(id3Header.LENGTH)) - Integer.parseInt(duration);
                    if (difference > 1000) {
                        logger.debug("Duration changed from originally " + id3Data.getData(id3Header.LENGTH) + " to " + duration + ", " +
                                " this is longer by " + difference + " ms");
                    }
                    id3Data.setFrame(id3Header.LENGTH, duration);
                }
//                else
//                    logger.debug("duration: " + Integer.parseInt(id3Data.getData(id3Header.LENGTH)) / 1000 + " seconds");

//                if (id3Data.exists(id3Header.ALBUM_ART)) {
//                    logger.debug("--Album art found--");
//                }

                id3Data.setSize(id3Data.totalFrameSize(), false);

            }
        }

        if (id3Data.getVersionString().equals("2.5.0")) {
            logger.debug("ID3 v2.5.0... how?");
            id3Data = new MyID3(String.valueOf(ID3v2Utils.getDuration(fileContent, 0, file.getName())));
        }

        return id3Data;
    }


    private static ID3Frame getFrame(byte[] fileContent, int start, boolean calculateSize) {
        ID3Frame frame = new ID3Frame();
        frame.setFrameID(new String(Arrays.copyOfRange(fileContent, start, start + 4)));
        String paddingString = String.valueOf((char) 0);
        paddingString += paddingString;
        paddingString += paddingString;

        start += 4;
        frame.setSize(ByteBuffer.wrap(Arrays.copyOfRange(fileContent, start, start + 4)).getInt(), calculateSize);
        start += 4;
        frame.setFlag1(fileContent[start]);
        frame.setFlag2(fileContent[start + 1]);
        frame.setEncoding(fileContent[start + 2]);
        start += 3;
        if (frame.getFrameID().equals(paddingString)) {
            frame.setFrameID(id3Header.DELETE);
            //logger.debug("found id3 padding!");
        } else {
            //logger.debug("header: "+frame.getFrameID());

            byte[] frameByteArray = Arrays.copyOfRange(fileContent, start, start + frame.getSize() - 1);
            if (frame.getSize() == 1) {
                frame.setContent(new byte[0]);
            } else if (frame.getEncoding() > 0 && frame.getEncoding() < 4) {
                int encoding = frame.getEncoding();
                //System.out.println("Encoding set to: "+encoding);
                Charset utf8charset = StandardCharsets.UTF_8;
                Charset utf16Charset = StandardCharsets.UTF_16;
                Charset utf16beCharset = StandardCharsets.UTF_16BE;
                Charset iso88591charset = StandardCharsets.ISO_8859_1;
                if (frameByteArray[0] != 0 && encoding == 1 && frameByteArray[0] != (byte) 255) {
                    byte[] newArr = new byte[frameByteArray.length];
                    newArr[0] = (byte) (0);
                    System.arraycopy(frameByteArray, 0, newArr, 1, frameByteArray.length - 1);
                    frameByteArray = newArr;
                }
                ByteBuffer inputBuffer = ByteBuffer.wrap(frameByteArray);
                CharBuffer data = null;
//                System.out.println(Arrays.toString(inputBuffer.array()));
                if (encoding == 3) data = utf8charset.decode(inputBuffer);
                if (encoding == 2) data = utf16beCharset.decode(inputBuffer);
                if (encoding == 1) data = utf16Charset.decode(inputBuffer);
                assert data != null;
                ByteBuffer outputBuffer = iso88591charset.encode(data);
                frameByteArray = outputBuffer.array();
                if (frameByteArray[frameByteArray.length - 1] == 0)
                    frameByteArray = Arrays.copyOf(frameByteArray, frameByteArray.length - 1);
                frame.setContent(frameByteArray);
                //logger.debug("new data: " + Arrays.toString(frameByteArray));
                frame.setEncoding((byte) 0);
            } else {
                if (frameByteArray[frameByteArray.length - 1] == 0 && !frame.getFrameID().equals(id3Header.ALBUM_ART)) {
                    frame.setContent((Arrays.copyOf(frameByteArray, frameByteArray.length - 1)));
                } else {
                    frame.setContent((frameByteArray));
                }
            }
        }
        if (id3Header.LIST_NOT_CONTAINS(frame.getFrameID())) {
            logger.debug("unknown header: " + frame.getFrameID());
        }

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
            mp3Data = new byte[fileContent.length - size];
            System.arraycopy(fileContent, size, mp3Data, 0, fileContent.length - size);
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
