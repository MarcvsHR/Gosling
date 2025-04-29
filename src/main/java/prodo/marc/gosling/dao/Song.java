package prodo.marc.gosling.dao;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.log4j.Logger;
import prodo.marc.gosling.service.MyStringUtils;
import prodo.marc.gosling.service.util.Truncated;

import javax.persistence.*;
import java.time.Year;
import java.util.Objects;

@Setter
@Entity
@ToString
@NoArgsConstructor
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Truncated
    private String artist;
    @Truncated
    private String title;
    @Truncated
    private String album;
    @Truncated
    private String publisher;
    @Truncated
    private String composer;
    @Truncated
    private Year year;
    @Truncated
    private String genre;

    private String ISRC;
    private String fileLoc;
    private Boolean done;
    private String editor;
    private Integer duration;

    public Song(Song song) {
        this.id = song.getId();
        this.artist = song.getArtist();
        this.title = song.getTitle();
        this.album = song.getAlbum();
        this.publisher = song.getPublisher();
        this.composer = song.getComposer();
        this.year = song.getYear();
        this.genre = song.getGenre();
        this.ISRC = song.getISRC();
        this.fileLoc = song.getFileLoc();
        this.done = song.getDone();
        this.editor = song.getEditor();
        this.duration = song.getDuration();
    }

    private String generateDifferenceString(String propertyName, String value1, String value2) {
        return propertyName + ": " + value1 + " --- " + value2 + ";\r\n";
    }

    public String isTheSame(Song compareSong) {
        if (this == compareSong)
            return "";
        if (compareSong == null || getClass() != compareSong.getClass())
            return "one of the songs is empty";

        StringBuilder returnBuilder = new StringBuilder();
        if (!MyStringUtils.compareStrings(artist, compareSong.artist))
            returnBuilder.append(generateDifferenceString("Artist", artist, compareSong.artist));
        if (!MyStringUtils.compareStrings(title, compareSong.title))
            returnBuilder.append(generateDifferenceString("Title", title, compareSong.title));
        if (!MyStringUtils.compareStrings(album, compareSong.album))
            returnBuilder.append(generateDifferenceString("Album", album, compareSong.album));
        if (!MyStringUtils.compareStrings(publisher, compareSong.publisher))
            returnBuilder.append(generateDifferenceString("Publisher", publisher, compareSong.publisher));
        if (!MyStringUtils.compareStrings(composer, compareSong.composer))
            returnBuilder.append(generateDifferenceString("Composer", composer, compareSong.composer));
        if (!MyStringUtils.compareStrings(genre, compareSong.genre))
            returnBuilder.append(generateDifferenceString("Genre", genre, compareSong.genre));
        if (!MyStringUtils.compareStrings(ISRC, compareSong.ISRC))
            returnBuilder.append(generateDifferenceString("ISRC", ISRC, compareSong.ISRC));
        if (!Objects.equals(year, compareSong.year))
            returnBuilder.append(generateDifferenceString("Year", String.valueOf(year), String.valueOf(compareSong.year)));

        return returnBuilder.toString();
    }

    public String getDurationString() {
        int durInSecs = duration != null ? duration / 1000 : 0;
        return String.format("%02d:%02d", durInSecs / 60, durInSecs % 60);
    }

    public String getTitle() {
        return Objects.requireNonNullElse(title, "");
    }

    public String getPublisher() {
        return Objects.requireNonNullElse(publisher, "");
    }

    public String getISRC() { return Objects.requireNonNullElse(ISRC, ""); }

    public String getAlbum() {
        return Objects.requireNonNullElse(album, "");
    }

    public String getGenre() {
        return Objects.requireNonNullElse(genre, "");
    }

    public boolean getDone() {
        return Objects.requireNonNullElse(done, false);
    }

    public String getComposer() {
        return composer == null ? "" : composer;
    }

    public String getFileLoc() {
        return fileLoc;
    }

    public String getEditor() {
        return editor;
    }

    public Integer getId() {
        return id;
    }

    public Year getYear() {
        return year;
    }

    public String getArtist() {
        return Objects.requireNonNullElse(artist, "");
    }

    public Integer getDuration() {
        return duration;
    }

    @Converter(autoApply = true)
    public static class YearConverter implements AttributeConverter<Year, Short> {

        Logger log = Logger.getLogger(YearConverter.class.getSimpleName());

        @Override
        public Short convertToDatabaseColumn(Year attribute) {
            short year = (short) attribute.getValue();
            log.info("Convert Year [" + attribute + "] to short [" + year + "]");
            return year;
        }

        @Override
        public Year convertToEntityAttribute(Short dbValue) {
            Year year = Year.of(dbValue);
            log.info("Convert Short [" + dbValue + "] to Year [" + year + "]");
            return year;
        }

    }

}
