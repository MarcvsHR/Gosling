package prodo.marc.gosling.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Setter
@Getter
@Entity
@ToString
@Table(name = "song")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "artist")
    private String artist;

    @Column(name = "title")
    private String title;

    @Column(name = "album")
    private String album;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "composer")
    private String composer;

    @Column(name = "year")
    private int year;

    @Column(name = "genre")
    private String genre;

    @Column(name = "ISRC")
    private String ISRC;
}
