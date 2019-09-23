package se325.assignment01.concert.service.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "SEATS")
public class Seat {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private long id;

	@Column(name = "LABEL")
	private String label;

	@Column(name = "IS_BOOKED")
	private boolean isBooked;

	@Column(name = "DATE")
	private LocalDateTime date;

	@Column(name = "PRICE")
	private BigDecimal price;

	@Version
    private long version;


    public Seat() {}

    public Seat(String label, BigDecimal price) {
    	this.label = label;
    	this.price = price;
	}

	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal price) {
		this.label = label;
		this.isBooked = isBooked;
		this.date = date;
		this.price = price;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isBooked() {
		return isBooked;
	}

	public void setBooked(boolean booked) {
		isBooked = booked;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

    public long getVersion() { return version; }

    public void setVersion(long version) { this.version = version; }

}
