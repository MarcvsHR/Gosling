package prodo.marc.gosling.dao;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;


public class MyID3 extends ID3Size {
    private final byte[] version = new byte[3];
    private final HashMap<String, ID3Frame> frames = new HashMap<>();
    private byte flags;
    private int TXXX_Counter = 0;
    private int COMM_Counter = 0;
    private int GEOB_Counter = 0;

    public MyID3(String frame) {
        this.flags = 0;
        this.version[0] = 2;
        this.version[1] = 4;
        addFrame(id3Header.LENGTH, frame);
        setSize(totalFrameSize(), false);
    }

    public MyID3() {
        this.flags = 0;
        this.version[0] = 2;
        this.version[1] = 5;
        setSize(totalFrameSize(), false);
    }

    public ArrayList<String> listFrames() {
        ArrayList<String> list = new ArrayList<>();
        String breakLine = "";
        for (ID3Frame frame : frames.values()) {
            //System.out.println(Arrays.toString(frame.getContent()));
            if (!frame.getFrameID().equals(id3Header.ALBUM_ART)) {
                list.add(breakLine + frame.getFrameID() + " - " + new String(frame.getContent()));
            }
            breakLine = "\n";
        }
        return list;
    }

    public ArrayList<String> checkFrames() {
        ArrayList<String> list = new ArrayList<>();
        String breakLine = "";
        for (ID3Frame frame : frames.values()) {
            //System.out.println(Arrays.toString(frame.getContent()));
            if (!frame.getFrameID().equals(id3Header.ALBUM_ART) && frame.getFrameID().length() == 4) {
                if (id3Header.LIST_NOT_CONTAINS(frame.getFrameID())) {
                    list.add(breakLine + frame.getFrameID() + " - " + new String(frame.getContent()));
                }
            }
            breakLine = "\n";
        }
        return list;
    }

    public void addFrame(ID3Frame frame) {
        this.frames.put(frame.getFrameID(), frame);
        frames.get(frame.getFrameID()).setSize(frames.get(frame.getFrameID()).getContent().length + 1, false);
    }

    public void addFrame(String header, String data) {
        if (data != null) {
            this.frames.put(header, new ID3Frame(header, data));
        }
        setSize(totalFrameSize(), false);
    }

    public String getVersionString() {
        return version[0] + "." + version[1] + "." + version[2];
    }

    public void setVersion(byte a, byte b, byte c) {
        this.version[0] = a;
        this.version[1] = b;
        this.version[2] = c;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public int totalFrameSize() {
        int total = 0;
        for (ID3Frame frame : frames.values()) {
            total += frame.getSize() + 10;
        }
        return total;
    }

    public byte[] getID3Data() {
        byte[] output = new byte[this.getSize() + 10];
        output[0] = (byte) 'I';
        output[1] = (byte) 'D';
        output[2] = (byte) '3';
        output[3] = version[1];
        output[4] = version[2];
        output[5] = flags;
        byte[] tempArr = convertIntToBytes(this.getSize());
//        System.out.println(this.getSize());
//        System.out.println("");
        System.arraycopy(tempArr, 0, output, 6, 4);
        int pos = 10;

        for (ID3Frame frame : frames.values()) {
            if (frame.getFrameID().startsWith(id3Header.USER_DATA)) {
                tempArr = id3Header.USER_DATA.getBytes();
            } else if (frame.getFrameID().startsWith(id3Header.COMMENT)) {
                tempArr = id3Header.COMMENT.getBytes();
            } else {
                tempArr = frame.getFrameID().getBytes();
            }
            System.arraycopy(tempArr, 0, output, pos, 4);
            pos += 4;
            if (version[1] == 4) {
                tempArr = ID3Size.convertIntToBytes(frame.getSize());
            } else {
                tempArr = ByteBuffer.allocate(4).putInt(frame.getSize()).array();
            }

            System.arraycopy(tempArr, 0, output, pos, 4);
            pos += 4;
            output[pos] = frame.getFlag1();
            output[pos + 1] = frame.getFlag2();
            output[pos + 2] = frame.getEncoding();
            pos += 3;
            tempArr = frame.getContent();
//            System.out.println(new String(tempArr));
//            System.out.println(Arrays.toString(tempArr));
            System.arraycopy(tempArr, 0, output, pos, tempArr.length);
            pos += tempArr.length;

//            if (!frame.getFrameID().equals(ideHeader.ALBUM_ART)) System.out.println(new String(frame.getContent()));
//            System.out.println(frame.getSize());
//            System.out.println(pos);
//            System.out.println("");
        }
//        System.out.println(Arrays.toString(output));
        return output;
    }

    public String getData(String s) {
        if (frames.containsKey(s)) return new String(frames.get(s).getContent());
        return "";
    }

    public void setFrame(String header, String content) {
        if (frames.containsKey(header)) {
            frames.get(header).setContent(content.getBytes());
            frames.get(header).setSize(content.length() + 1, false);
            setSize(totalFrameSize(), false);
        } else {
            addFrame(header, content);
        }
    }

    public void changeTXXX(int i) {
        TXXX_Counter += i;
    }

    public int getTXXX() {
        return TXXX_Counter;
    }

    public void changeCOMM(int i) {
        COMM_Counter += i;
    }

    public int getCOMM() {
        return COMM_Counter;
    }

    public void changeGEOB(int i) {
        GEOB_Counter += i;
    }

    public int getGEOB() {
        return GEOB_Counter;
    }

    public boolean exists(String frame_id) {
        return frames.get(frame_id) != null;
    }

    public void removeFrame(String frame_id) {
        frames.remove(frame_id);
    }

    public ID3Frame getFrame(String string) {
        return frames.get(string);
    }
}

