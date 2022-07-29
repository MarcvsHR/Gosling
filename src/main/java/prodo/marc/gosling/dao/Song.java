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

    public String isTheSame(Song compareSong) {
        if (this == compareSong) return "";
        if (compareSong == null || getClass() != compareSong.getClass()) return "one of the songs is empty";
        String returnString = "";
        if (!MyStringUtils.compareStrings(artist,compareSong.artist))
            returnString += "Artist: "+artist + " --- " + compareSong.artist + ";\r\n";
        if (!MyStringUtils.compareStrings(title,compareSong.title))
            returnString += "Title: "+title + " --- " + compareSong.title + ";\r\n";
        if (!MyStringUtils.compareStrings(album,compareSong.album))
            returnString += "Album: "+album + " --- " + compareSong.album + ";\r\n";
        if (!MyStringUtils.compareStrings(publisher,compareSong.publisher))
            returnString += "Publisher: "+publisher + " --- " + compareSong.publisher + ";\r\n";
        if (!MyStringUtils.compareStrings(composer,compareSong.composer))
            returnString += "Composer: "+composer + " --- " + compareSong.composer + ";\r\n";
        if (!MyStringUtils.compareStrings(genre,compareSong.genre))
            returnString += "Genre: "+genre + " --- " + compareSong.genre + ";\r\n";
        if (!MyStringUtils.compareStrings(ISRC,compareSong.ISRC))
            returnString += "ISRC: "+ISRC + " --- " + compareSong.ISRC + ";\r\n";
        if (!MyStringUtils.compareStrings(fileLoc,compareSong.fileLoc))
            returnString += "File loc: "+fileLoc + " --- " + compareSong.fileLoc + ";\r\n";
        if (!Objects.equals(year, compareSong.year))
            returnString += "Year: "+year + " --- " + compareSong.year + ";\r\n";
        return returnString;
    }

    public String getDurationString() {
        if (duration == null) duration = 0;
        double seconds = Math.floor(duration / 1000.0 % 60);
        String output = String.format("%.0f", seconds);
        if (output.length() < 2) output = "0"+output;
        double minutes = Math.floor(duration / 1000.0/60);
        output = String.format("%.0f:", minutes) + output;
        if (duration / 1000 < 600) output = "0"+output;
        return output;
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
