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

}
