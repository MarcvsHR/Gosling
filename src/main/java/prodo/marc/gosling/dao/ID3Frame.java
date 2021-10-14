package prodo.marc.gosling.dao;


public class ID3Frame extends ID3Size {
    private String frameID;
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