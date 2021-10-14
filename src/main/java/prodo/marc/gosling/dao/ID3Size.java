package prodo.marc.gosling.dao;


import java.nio.ByteBuffer;


public class ID3Size {

    private int size;

    public int getSize() {
        return (size);
    }

    public void setSize(int size, boolean compute) {
        if (compute) {
            this.size = computeSize(size); }
        else {
            this.size = size;
        }
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

}
