package prodo.marc.gosling.dao;


public enum ID3Header {
    PUBLISHER("TPUB"),
    ALBUM("TALB"),
    TITLE("TIT2"),
    YEAR("TYER"),
    TYPE("TCON"),
    ARTIST("TPE1"),
    COMPOSER("TCOM");

    private final String name;

    ID3Header(String n){
        this.name = n;
    }

    public String getName() {
        return this.name;
    }

}
