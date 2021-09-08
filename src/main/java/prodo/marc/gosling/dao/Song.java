package prodo.marc.gosling.dao;

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

}
