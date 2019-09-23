package se325.assignment01.concert.service.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;

    @Column(name = "CONCERT_ID")
    @JoinColumn(name = "CONCERT_ID")
    private long concertId;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name= "SEAT")
    @CollectionTable(name = "BOOKING_SEATS")
    private List<Seat> seats = new ArrayList<Seat>();

    @Column(name = "DATE")
    private LocalDateTime date;

    @JoinColumn(name = "USER_ID")
    private long userId;


    public Booking (){}

    public Booking(long concertId, List<Seat> seats, LocalDateTime date, long userId){
        this.concertId = concertId;
        this.seats = seats;
        this.date = date;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getConcertId() {
        return concertId;
    }

    public void setConcertId(long concertId) {
        this.concertId = concertId;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public LocalDateTime getDate() { return date; }

    public void setDate(LocalDateTime date) { this.date = date; }

    public long getUserId() { return userId; }

    public void setUserId(long userId) { this.userId = userId; }
}
