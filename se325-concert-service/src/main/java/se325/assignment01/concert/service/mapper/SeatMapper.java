package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.service.domain.Seat;

public class SeatMapper {
    public static Seat toDomainModel(SeatDTO seatDTO) {
        Seat fullSeat = new Seat(seatDTO.getLabel(), seatDTO.getPrice());
        return fullSeat;
    }
    public static SeatDTO toDto(Seat seatDomain) {
        SeatDTO seatDTO = new SeatDTO(seatDomain.getLabel(), seatDomain.getPrice());
        return seatDTO;
    }
}
