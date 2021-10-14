package prodo.marc.gosling.dao;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MyID3 {
    private final byte[] version = new byte[3];
    private byte flags;
    private int size;
    private final ArrayList<ID3Frame> frames = new ArrayList<>();

    public void addFrame(ID3Frame frame) {
        this.frames.add(frame);
    }

    public ArrayList<ID3Frame> getFrames() {
        return frames;
    }

    public ID3Frame getFrame(int i) {
        return frames.get(i);
    }

    public void removeFrame() {
        frames.remove(frames.size()-1);
    }

    public void setVersion(int a, int b, int c) {
        this.version[0] = (byte)a;
        this.version[1] = (byte)b;
        this.version[2] = (byte)c;
    }

    public static class ID3Frame {
        private String frameID;
        private int size;
        private byte flag1;
        private byte flag2;
        private byte encoding;
        private String content;

        public byte getEncoding() {
            return encoding;
        }

        public void setEncoding(byte encoding) {
            this.encoding = encoding;
        }

        public String getFrameID() {
            return frameID;
        }

        public void setFrameID(String frameID) {
            this.frameID = frameID;
        }

        public int getSize() {
            return (size);
        }

        public void setSize(int size) {
            this.size = MyID3.computeSize(size);
        }

        public byte getFlag1() {
            return flag1;
        }

        public void setFlag1(byte flag1) {
            this.flag1 = flag1;
        }

        public byte getFlag2() {
            return flag2;
        }

        public void setFlag2(byte flag2) {
            this.flag2 = flag2;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public String getVersionString() {
        return version[0]+"."+version[1]+"."+version[2];
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

    public int getSize() {
        return this.size;

    }

    public static int computeSize(int size) {
        byte[] outArray = ByteBuffer.allocate(4).putInt(size).array();
        String binaryString = "0000";
        binaryString += String.format("%7s",Integer.toBinaryString(outArray[0])).replace(' ','0');
        binaryString += String.format("%7s",Integer.toBinaryString(outArray[1])).replace(' ','0');
        binaryString += String.format("%7s",Integer.toBinaryString(outArray[2])).replace(' ','0');
        binaryString += String.format("%7s",Integer.toBinaryString(outArray[3])).replace(' ','0');
        return Integer.parseInt(binaryString,2);
    }

    public void setSize(int size, boolean compute) {
        if (compute) {
        this.size = computeSize(size); }
        else {
            this.size = size;
        }
    }

    public static byte[] convertIntToBytes(int size) {
        String binary = Integer.toBinaryString(size);
        if (binary.length()>7)
            binary = new StringBuilder(binary).insert(binary.length()-7,"0").toString();
        if (binary.length()>15)
            binary = new StringBuilder(binary).insert(binary.length()-15,"0").toString();
        if (binary.length()>23)
            binary = new StringBuilder(binary).insert(binary.length()-23,"0").toString();
        int value = Integer.parseInt(binary,2);

        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public int totalFrameSize() {
        int total = 0;
        for (ID3Frame frame : frames) {
            total += frame.getSize()+10;
        }
        return total;
    }

    public byte[] getID3Data() {
        byte[] output = new byte[this.size+10];
        output[0] = (byte)'I';
        output[1] = (byte)'D';
        output[2] = (byte)'3';
        output[3] = version[1];
        output[4] = version[2];
        output[5] = flags;
        byte[] tempArr = convertIntToBytes(this.size);
        System.arraycopy(tempArr, 0, output, 6, 4);
        int pos = 10;

        //handle frames
        for (ID3Frame frame : frames) {
            tempArr = frame.getFrameID().getBytes();
            System.arraycopy(tempArr,0,output,pos,4);
            pos += 4;
            tempArr = convertIntToBytes(frame.getSize());
            System.arraycopy(tempArr,0,output,pos,4);
            pos +=4;
            output[pos] = frame.getFlag1();
            output[pos+1] = frame.getFlag2();
            output[pos+2] = frame.getEncoding();
            pos +=3;
            tempArr = frame.getContent().getBytes();
            System.arraycopy(tempArr,0,output,pos,tempArr.length);
            pos += tempArr.length;
        }

        return output;
    }

}
