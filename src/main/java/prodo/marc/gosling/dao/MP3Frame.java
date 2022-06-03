package prodo.marc.gosling.dao;

import java.math.BigInteger;

public class MP3Frame {

    int layer;
    boolean padding;
    int frequency;
    /**
    * kilobits per second
     */
    int bitrate;
    int samplesPerFrame;
    boolean valid = true;

    public MP3Frame(BigInteger bigIntHeader) {

        for (int i = 0; i<13;i++) {
            if (!bigIntHeader.testBit(20+i)) {
                System.out.println("error at bit: " + (19 + i));
                valid = false;
                break;
            }
        }

        if (valid)
        {
            layer = 4 - ((bigIntHeader.testBit(17) ? 1 : 0) + (bigIntHeader.testBit(18) ? 2 : 0));
            if (layer == 1) samplesPerFrame = 384;
            else samplesPerFrame = 1152;

            padding = bigIntHeader.testBit(9);

            if (bigIntHeader.testBit(10)) frequency = 48000;
            else if (bigIntHeader.testBit(11)) frequency = 32000;
            else frequency = 44100;

            int tempBitrate = (bigIntHeader.testBit(12) ? 1 : 0) +
                    (bigIntHeader.testBit(13) ? 2 : 0) +
                    (bigIntHeader.testBit(14) ? 4 : 0) +
                    (bigIntHeader.testBit(15) ? 8 : 0);
            bitrate = checkBitrate(tempBitrate, layer);
        }

    }

    private int checkBitrate(int tempBitrate, int layer) {
        int place = (layer*16-16) + tempBitrate;
        //System.out.println(tempBitrate+"  "+layer);
//        System.out.println(place);

        int[] bitrateArray = new int[]{
                0, 32, 64 ,96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 0,
                0, 32, 48, 56,  64,  80,  96, 112, 128, 160, 192, 224, 256, 320, 384, 0,
                0, 32, 40, 48,  56,  64,  80,  96, 112, 128, 160, 192, 224, 256, 320, 0,
        };
        return bitrateArray[place];
    }

    public int getLayer() {
        return layer;
    }

    public boolean isPadding() {
        return padding;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getBitrate() {
        return bitrate;
    }

    public double getFrameTimeInMS() {
        return samplesPerFrame * 1000.0 / frequency;
    }

    public int getFrameSizeInBytes() {
        int byteRate = bitrate / 8 * 1000;
        return (samplesPerFrame * byteRate / frequency) + (padding ? 1 : 0);
    }

    public String getAllData() {
        return "layer of frame: "+this.getLayer()+", frequency: "+this.getFrequency()+
                ", has padding: "+this.isPadding()+", bitrate: "+this.getBitrate()+
                ", ms duration: "+this.getFrameTimeInMS()+", byte size: "+this.getFrameSizeInBytes()+
                ", valid: "+this.valid;
    }

    public boolean isValid() {
        return valid;
    }
}
