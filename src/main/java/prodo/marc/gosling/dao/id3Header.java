package prodo.marc.gosling.dao;

import java.util.Set;

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
    public static final String ALBUM_ART = "APIC";
    public static final String COMMENT = "COMM"; //can have multiple, implemented
    public static final String USER_DATA = "TXXX"; //can have multiple, implemented
    public static final String CD_ID = "MCDI";
    public static final String GENERAL_OBJECT = "GEOB"; //can have multiple, implemented
    public static final String RECORDING_DATE = "TDRC";
    public static final String ALBUM_ARTIST = "TPE2";
    public static final String DISC_NUMBER = "TPOS";
    public static final String RELEASE_DATE = "TDRL";
    public static final String TRACK = "TRCK";
    public static final String BPM = "TBPM";
    public static final String TIME = "TIME";
    public static final String DONE = "DONE";
    public static final String SOFTWARE = "TSSE";
    public static final String ENCODER = "TENC";
    public static final String UNSYNCED_LYRICS = "USLT";
    public static final String PRIVATE = "PRIV";
    public static final String DATE = "TDAT";
    public static final String COPYRIGHT_MSG = "TCOP";
    public static final String TEXT_WRITER = "TEXT";
    public static final String REMIXED_BY = "TPE4";
    public static final String LANGUAGE = "TLAN";
    public static final String CONTENT_GROUP = "TIT1";
    public static final String SUBTITLE_INFO = "TIT3";
    public static final String URL_CUSTOM = "WXXX";
    public static final String ORIGINAL_ARTIST = "TOPE";
    public static final String MEDIA_TYPE = "TMED";
    public static final String UNIQUE_FILE_ID = "UFID";
    public static final String POPULARIMETER = "POPM";
    public static final String TAGGING_DATE = "TDTG";
    public static final String ORIGINAL_ALBUM = "TOAL";
    public static final String CONDUCTOR = "TPE3";
    public static final String ORIGINAL_LYRICIST = "TOLY";
    public static final String RADIO_STATION = "TRSN";
    public static final String ENCODING_DATE = "TDEN";
    public static final String MOOD = "TMOO";
    public static final String MUSICIAN_CREDITS = "TMCL";
    public static final String PEOPLE_INVOLVED = "TIPL";
    public static final String FILE_TYPE = "TFLT";
    public static final String INVOLVED_PEOPLE_OLD = "IPLS";
    public static final String RECORDING_DATES = "TRDA";
    public static final String COPYRIGHT = "WCOP";
    public static final String TABLE_OF_CONTENT = "CTOC";
    public static final String CHAPTER = "CHAP";
    public static final String COMPOSER_SORT = "TSOC";
    public static final String ITUNES_COMPILATION = "TCMP";
    public static final String FILE_URL = "WOAF";

    public static final String DELETE = "XXXX";

    public static final Set<String> LIST = Set.of(
            "TPUB", "TALB", "TIT2", "TYER", "TCON", "TPE1", "TPE2",
            "TKEY", "COMM", "TPOS", "TDRL", "TRCK", "TXXX", "TBPM",
            "APIC", "TDRC", "TIME", "DONE", "TSRC", "TSSE", "TLEN",
            "TENC", "USLT", "MCDI", "PRIV", "TDAT", "TCOP", "TEXT",
            "TPE4", "GEOB", "TLAN", "TIT3", "WXXX", "TOPE", "TMED",
            "TIT1", "UFID", "POPM", "TDTG", "TOAL", "TPE3", "WCOP",
            "TOLY", "TRSN", "TDEN", "TMOO", "TMCL", "TIPL", "TCOM",
            "TFLT", "IPLS", "TRDA", "CTOC", "CHAP", "TSOC", "TCMP",
            "WOAF",
            "XXXX"
    );

    public static boolean LIST_NOT_CONTAINS(String s) {
        return !LIST.contains(s);
    }
}
