package prodo.marc.gosling.dao;

import java.math.BigInteger;

public class mp3Frame {

    int layer;
    boolean padding;
    int frequency;
    int bitrate;
    int samplesPerFrame;
    boolean valid = true;

    public mp3Frame(BigInteger hbi) {

        for (int i = 0; i<13;i++) {
            if (!hbi.testBit(20+i)) {
                System.out.println("error at bit: " + (19 + i));
                valid = false;
                break;
            }
        }

        if (valid)
        {
            int layerBit1 = hbi.testBit(17) ? 1 : 0;
            int layerBit2 = hbi.testBit(18) ? 2 : 0;

            layer = 4 - (layerBit1 + layerBit2);
            if (layer == 1) samplesPerFrame = 384;
            else samplesPerFrame = 1152;

            padding = hbi.testBit(9);

            if (hbi.testBit(10)) frequency = 48000;
            else if (hbi.testBit(11)) frequency = 32000;
            else frequency = 44100;

            int tempBitrate = (hbi.testBit(12) ? 1 : 0) +
                    (hbi.testBit(13) ? 2 : 0) +
                    (hbi.testBit(14) ? 4 : 0) +
                    (hbi.testBit(15) ? 8 : 0);
            bitrate = checkBitrate(tempBitrate, layer);
        }

    }

    private int checkBitrate(int tempBitrate, int layer) {
        int place = (layer*16-16) + tempBitrate;
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
        return (144000 * bitrate / frequency) + (padding ? 1 : 0);
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
