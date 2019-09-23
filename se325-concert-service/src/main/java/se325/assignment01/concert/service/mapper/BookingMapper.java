package se325.assignment01.concert.service.mapper;


import se325.assignment01.concert.common.dto.BookingDTO;
import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.service.domain.Booking;
import se325.assignment01.concert.service.domain.Seat;

import java.util.ArrayList;
import java.util.List;

public class BookingMapper {

    public static BookingDTO toDto(Booking booking){
        List<SeatDTO> dtoSeats = new ArrayList<SeatDTO>();
        for(Seat s : booking.getSeats()){
            SeatDTO dtoSeat = SeatMapper.toDto(s);
            dtoSeats.add(dtoSeat);
        }
        BookingDTO dto = new BookingDTO(booking.getConcertId(),
                                        booking.getDate(),
                                        dtoSeats);

        return dto;
    }
}
