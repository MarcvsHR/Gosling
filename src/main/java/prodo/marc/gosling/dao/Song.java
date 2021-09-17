package prodo.marc.gosling.dao;

import com.mpatric.mp3agic.ID3v24Tag;
import javafx.scene.control.CheckBox;
import lombok.*;

import javax.persistence.*;

@Setter
@Getter
@Entity
@ToString
@NoArgsConstructor
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String artist;
    private String title;
    private String album;
    private String publisher;
    private String composer;
    private Integer year;
    private String genre;
    private String ISRC;
    private String fileLoc;
    private Boolean done;

    @Override
    //TODO: there's null and everything breaks! panic!
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song that = (Song) o;
        return artist.equals(that.artist) &&
                title.equals(that.title) &&
                album.equals(that.album) &&
                publisher.equals(that.publisher) &&
                composer.equals(that.composer) &&
                year.equals(that.year) &&
                genre.equals(that.genre) &&
                //ISRC.equals(that.ISRC) &&
                fileLoc.equals(that.fileLoc) &&
                done.equals(that.done);
    }
}
