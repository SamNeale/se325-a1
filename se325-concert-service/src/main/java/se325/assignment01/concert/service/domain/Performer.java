package se325.assignment01.concert.service.domain;

import se325.assignment01.concert.common.types.Genre;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PERFORMERS")
public class Performer {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "IMAGE_NAME")
    private String image_name;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column(name = "BLURB", length = 1024)
    private String blurb;

    @ManyToMany(mappedBy = "performers")
    private Set<Concert> concerts = new HashSet<Concert>();



    public Performer(){}

    public Performer(long id, String name, String image_name, Genre genre, String blurb){
        this.id = id;
        this.name = name;
        this.image_name = image_name;
        this.genre = genre;
        this.blurb = blurb;
        this.concerts = new HashSet<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public Set<Concert> getConcerts() {
        return concerts;
    }

    public void setConcerts(Set<Concert> concerts) {
        this.concerts = concerts;
    }
}
