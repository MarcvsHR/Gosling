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
//TODO: need to handle nulls better so there isn't a shitload of null checks all over the place
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
    //private char truncated = 'Y';


    public boolean isTheSame(Song compareSong) {
        if (this == compareSong) return true;
        if (compareSong == null || getClass() != compareSong.getClass()) return false;
        return MyStringUtils.compareStrings(artist, compareSong.artist) &&
                MyStringUtils.compareStrings(title, compareSong.title) &&
                MyStringUtils.compareStrings(album, compareSong.album) &&
                MyStringUtils.compareStrings(publisher, compareSong.publisher) &&
                MyStringUtils.compareStrings(composer, compareSong.composer) &&
                MyStringUtils.compareStrings(genre, compareSong.genre) &&
                MyStringUtils.compareStrings(fileLoc, compareSong.fileLoc) &&
                MyStringUtils.compareStrings(ISRC, compareSong.ISRC) &&
                Objects.equals(year, compareSong.year) &&
                Objects.equals(done, compareSong.done);
        //Objects.equals(duration, that.duration) &&
        //MyStringUtils.compareStrings(ISRC, that.ISRC);

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

    public String getComposer() {
        return composer;
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

    public boolean getDone() {
        return done;
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
