package prodo.marc.gosling.service;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.MyID3;

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

    private static final Logger logger = LogManager.getLogger(ID3v2Utils.class);


    public static MyID3 getTag(File file) {
        byte[] fileContent = null;
        MyID3 id3Data = new MyID3();
        try {
            fileContent = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            logger.error("couldn't read file", e);
        }

        if (fileContent != null) {
            String header = new String(Arrays.copyOfRange(fileContent, 0, 3));
//            logger.debug("first 3 characters: " + header);
            if (header.equals("ID3")) {
                id3Data.setVersion((byte) 2, fileContent[3], fileContent[4]);
//                logger.debug("id3 version: " + id3Data.getVersionString());
            }
            if (id3Data.getVersionString().equals("2.4.0") || id3Data.getVersionString().equals("2.3.0")) {
                id3Data.setFlags(fileContent[5]);
                id3Data.setVersion(2, 4, 0);
                if (id3Data.getFlags() > 0) logger.debug("Flags detected!!!!   " + id3Data.getFlags());
                id3Data.setSize(ByteBuffer.wrap(Arrays.copyOfRange(fileContent, 6, 10)).getInt(), true);
//                logger.debug("Size: " + id3Data.getSize());
//                logger.debug("size bytes in array" + Arrays.toString(Arrays.copyOfRange(fileContent, 6, 10)));
                int startFrames = 10;
                int i = 0;
                while (startFrames < id3Data.getSize()) {
                    id3Data.addFrame(getFrame(fileContent, startFrames));
                    startFrames += id3Data.getFrame(i).getSize() + 10;
//                    logger.debug("Current pos: " + startFrames);
                    if (Objects.equals(id3Data.getFrame(i).getFrameID(), "XXXX")) {
                        id3Data.removeFrame();
                        break;
                    }
                    if (id3Data.getFrame(i).getEncoding() == 1) {
                        id3Data.getFrame(i).setSize(id3Data.getFrame(i).getContent().length() + 1);
                        id3Data.getFrame(i).setEncoding((byte) 0);
                    }
                    i++;
                }
                id3Data.setSize(id3Data.totalFrameSize(), false);
//                logger.debug("number of frames total: "+id3Data.getFrames().size());
//                logger.debug("last frame content: "+id3Data.getFrame(id3Data.getFrames().size()-1).getContent());
//                logger.debug("calculated size: " + id3Data.totalFrameSize());
//                logger.debug("calculated bytes: " + Arrays.toString(MyID3.convertIntToBytes(id3Data.totalFrameSize())));

                compareID3s(fileContent, id3Data);

            }
        }

        return id3Data;
    }

    private static void compareID3s(byte[] fileContent, MyID3 id3Data) {
        byte[] newArray = id3Data.getID3Data();

        int errors = 0;
        for (int i = 0; i < newArray.length; i++) {
//            logger.debug("place: "+i+", orig: "+fileContent[i]+" - new: "+newArray[i]);
            if (fileContent[i] != newArray[i]) {
//                logger.debug("byte check failed!!!");
                errors++;
            }
        }
//        logger.debug("total byte mismatch: " + errors);
    }

    private static MyID3.ID3Frame getFrame(byte[] fileContent, int start) {
        MyID3.ID3Frame frame = new MyID3.ID3Frame();
        frame.setFrameID(new String(Arrays.copyOfRange(fileContent, start, start + 4)));
        String test = String.valueOf((char) 0);
        test += test;
        test += test;

        start += 4;
        frame.setSize(ByteBuffer.wrap(Arrays.copyOfRange(fileContent, start, start + 4)).getInt());
        start += 4;
        frame.setFlag1(fileContent[start]);
        frame.setFlag2(fileContent[start + 1]);
        frame.setEncoding(fileContent[start + 2]);
        start += 3;
        if (frame.getFrameID().equals(test)) {
            frame.setContent("");
            frame.setFrameID("XXXX");
        } else {
            byte[] tempArr = Arrays.copyOfRange(fileContent, start, start + frame.getSize() - 1);
            if (frame.getEncoding() == 1) {
                Charset utf8charset = StandardCharsets.UTF_16;
                Charset iso88591charset = StandardCharsets.ISO_8859_1;
                ByteBuffer inputBuffer = ByteBuffer.wrap(tempArr);
                CharBuffer data = utf8charset.decode(inputBuffer);
                ByteBuffer outputBuffer = iso88591charset.encode(data);
//                logger.debug("old data: " + Arrays.toString(tempArr));
                tempArr = outputBuffer.array();
                frame.setContent(new String(Arrays.copyOf(tempArr, tempArr.length - 1)));
            } else {
                if (tempArr[tempArr.length - 1] == 0) {
                    frame.setContent(new String(Arrays.copyOf(tempArr, tempArr.length - 1)));
                } else {
                    frame.setContent(new String(tempArr));
                }
            }
        }
//        logger.debug("header: " + frame.getFrameID());
//        logger.debug("size: " + frame.getSize());
//        logger.debug("content size: " + frame.getContent().length());
//        logger.debug("f1: " + frame.getFlag1());
//        logger.debug("f2: " + frame.getFlag2());
//        logger.debug("encoding: " + frame.getEncoding());
//        logger.debug("data: " + frame.getContent());

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
            int size = MyID3.computeSize(ByteBuffer.wrap(Arrays.copyOfRange(fileContent, 6, 10)).getInt());
//            logger.debug(size);
            size += 10;
//            logger.debug(fileContent[size]);
            mp3Data = new byte[fileContent.length-size];
            System.arraycopy(fileContent,size,mp3Data,0,fileContent.length-size);
//            logger.debug(fileContent[fileContent.length-1]);
//            logger.debug(mp3Data[0]);
//            logger.debug(mp3Data[mp3Data.length-1]);
        }
        outputFileData = new byte[mp3Data.length+id3Data.length];
        System.arraycopy(id3Data,0,outputFileData,0,id3Data.length);
        System.arraycopy(mp3Data,0,outputFileData,id3Data.length,mp3Data.length);

        File outputFile = new File("c:\\users\\glazb\\downloads\\testing.mp3");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(outputFileData);
        } catch (Exception e) {
            logger.error("could not write to output file: ",e);
        }
    }
}
