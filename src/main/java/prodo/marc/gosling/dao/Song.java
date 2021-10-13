package prodo.marc.gosling.dao;

import javafx.util.Duration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.log4j.Logger;
import prodo.marc.gosling.service.StringUtils;
import prodo.marc.gosling.service.util.Truncated;

import javax.persistence.*;
import java.time.Year;
import java.util.Objects;

@Setter
@Getter
@Entity
@ToString
@NoArgsConstructor
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Truncated
    private String artist = "";
    @Truncated
    private String title = "";
    @Truncated
    private String album = "";
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
    private Duration duration;
    //private char truncated = 'Y';


    @Override
    //TODO: there's null and everything breaks! panic!
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song that = (Song) o;
        return StringUtils.compareStrings(artist, that.artist) &&
                StringUtils.compareStrings(title, that.title) &&
                StringUtils.compareStrings(album, that.album) &&
                StringUtils.compareStrings(publisher, that.publisher) &&
                StringUtils.compareStrings(composer, that.composer) &&
                StringUtils.compareStrings(genre, that.genre) &&
                StringUtils.compareStrings(fileLoc, that.fileLoc) &&
                Objects.equals(year, that.year) &&
                //Objects.equals(duration, that.duration) &&

                Objects.equals(done, that.done);

        //         StringUtils.compareStrings(ISRC, that.ISRC);
    }

    public String getDurationString() {
        return String.format("%.0fm %.1fs",duration.toMinutes(),duration.toSeconds()%60);
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
