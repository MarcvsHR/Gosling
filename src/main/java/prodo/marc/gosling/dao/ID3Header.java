package prodo.marc.gosling.dao;


public final class ID3Header {
    public static final String PUBLISHER = "TPUB";
    public static final String ALBUM = "TALB";
    public static final String TITLE = "TIT2";
    public static final String YEAR = "TYER";
    public static final String TYPE = "TCON";
    public static final String ARTIST = "TPE1";
    public static final String COMPOSER = "TCOM";

    public static final String[] LIST = {
        "TPUB", "TALB", "TIT2", "TYER", "TCON", "TPE1", "TCOM"
    };

    public static boolean CHECK_LIST(String s) {
        for (String c : LIST) {
            if (s.equals(c)) return true;
        }
        return false;
    }
}
