package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.ConcertDTO;
import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Concert;
import se325.assignment01.concert.service.domain.Performer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConcertMapper {

    public static Concert toDomainModel(ConcertDTO dto){

        // Converting performerDTOs inside concertDTO to performer objects
        Set<Performer> performers = new HashSet<>();
        for (PerformerDTO performerDTO : dto.getPerformers()){
            Performer performer = PerformerMapper.toDomainModel(performerDTO);
            performers.add(performer);
        }



        Concert concertDomainModel = new Concert(dto.getId(),
                                                dto.getTitle(),
                                                dto.getImageName(),
                                                dto.getBlurb(),
                                                new HashSet(dto.getDates()),
                                                performers);

        return concertDomainModel;
    }

    public static ConcertDTO toDto(Concert concert){

        //Converting performer to performer DTOs.
        List<PerformerDTO> performers = new ArrayList<>();
        for(Performer performer : concert.getPerformers()){
            PerformerDTO pDTO = PerformerMapper.toDto(performer);
            performers.add(pDTO);
        }

        ConcertDTO dto = new ConcertDTO(concert.getId(),
                                        concert.getTitle(),
                                        concert.getImage_name(),
                                        concert.getBlurb(),
                                        new ArrayList( concert.getDates()),
                                        performers);

        return dto;
    }
}
