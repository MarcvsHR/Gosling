package prodo.marc.gosling.dao;


public final class id3Header {
    public static final String ARTIST = "TPE1";
    public static final String TITLE = "TIT2";
    public static final String ALBUM = "TALB";
    public static final String YEAR = "TYER";
    public static final String GENRE = "TCON";
    public static final String PUBLISHER = "TPUB";
    public static final String COMPOSER = "TCOM";
    public static final String ISRC = "TSRC";
    public static final String LENGTH = "TLEN";
    public static final String KEY = "TKEY";
    public static final String ALBUM_ARTIST = "TPE2";
    public static final String ALBUM_ART = "APIC";
    public static final String COMMENT = "COMM";
    public static final String DISC_NUMBER = "TPOS";
    public static final String RELEASE_TIME = "TDRL";
    public static final String TRACK = "TRCK";
    public static final String USER_DATA = "TXXX";
    public static final String BPM = "TBPM";
    public static final String RECORDING_TIME = "TDRC";
    public static final String TIME = "TIME";
    public static final String DONE = "DONE";
    public static final String SOFTWARE = "TSSE";
    public static final String ENCODER = "TENC";
    public static final String UNSYNCED_LYRICS = "USLT";
    public static final String CD_IDENT = "MCDI";
    public static final String PRIVATE = "PRIV";

    public static final String DELETE = "XXXX";

    public static final String[] LIST = {
        "TPUB", "TALB", "TIT2", "TYER", "TCON", "TPE1", "TPE2", "TCOM",
            "TKEY", "COMM", "TPOS", "TDRL", "TRCK", "TXXX", "TBPM",
            "APIC", "TDRC", "TIME", "DONE", "TSRC", "TSSE", "TLEN",
            "TENC", "USLT", "MCDI", "PRIV",
            "XXXX",
    };

    public static boolean CHECK_LIST(String s) {
        for (String c : LIST) {
            if (s.equals(c)) return true;
        }
        return false;
    }
}
