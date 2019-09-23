package se325.assignment01.concert.service.domain;

import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import se325.assignment01.concert.common.jackson.LocalDateTimeDeserializer;
import se325.assignment01.concert.common.jackson.LocalDateTimeSerializer;

@Entity
@Table(name = "CONCERTS")
public class Concert {


    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "IMAGE_NAME")
    private String image_name;

    @Column(name = "BLURB", length = 1024)
    private String blurb;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name= "DATE")
    @CollectionTable(name = "CONCERT_DATES")
    private Set<LocalDateTime> dates = new HashSet<>();


    // https://www.baeldung.com/jpa-many-to-many Section 2.2 used for annotation logic.
    // https://www.youtube.com/watch?v=BO-Gy4XC6QE
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "CONCERT_PERFORMER", joinColumns = {@JoinColumn(name = "CONCERT_ID")}, inverseJoinColumns = {@JoinColumn(name = "PERFORMER_ID")})
    private Set<Performer> performers = new HashSet<>();

    public Concert(){}

    public Concert(long id, String title, String image_name , String blurb, Set<LocalDateTime> dates, Set<Performer> performers) {
        this.id = id;
        this.title = title;
        this.image_name = image_name;
        this.blurb = blurb;
        this.dates = dates;
        this.performers = performers;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage_name() {
        return image_name;
    }

    public Set<Performer> getPerformers() {
        return performers;
    }

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = dates;
    }

    public Set<LocalDateTime> getDates() {
        return dates;
    }
}
