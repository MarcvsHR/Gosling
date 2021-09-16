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

    public static Song getFromID3(ID3v24Tag id3, String fileLoc) {
        Song song = new Song();

        song.setArtist(id3.getArtist());
        song.setTitle(id3.getTitle());
        song.setAlbum(id3.getAlbum());
        song.setPublisher(id3.getPublisher());
        song.setComposer(id3.getComposer());
        String year = id3.getYear();
        if (year == null) {year = "0";}
        song.setYear(Integer.valueOf(year));
        song.setGenre(id3.getGenreDescription());
        song.setDone(id3.getKey().equals("true"));
        //song.setISRC(id3.getISRC());
        song.fileLoc = fileLoc;

        return song;
    }

}
