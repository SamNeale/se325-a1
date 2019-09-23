package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Performer;

public class PerformerMapper {

    public static Performer toDomainModel(PerformerDTO dto){
        Performer performerDomainModel = new Performer( dto.getId(),
                                                        dto.getName(),
                                                        dto.getImageName(),
                                                        dto.getGenre(),
                                                        dto.getBlurb());
        return performerDomainModel;
    }

    public static PerformerDTO toDto(Performer performer){
        PerformerDTO dto = new PerformerDTO(performer.getId(),
                                            performer.getName(),
                                            performer.getImage_name(),
                                            performer.getGenre(),
                                            performer.getBlurb());

        return dto;
    }
}
