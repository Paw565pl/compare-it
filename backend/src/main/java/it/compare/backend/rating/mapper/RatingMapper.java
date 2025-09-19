package it.compare.backend.rating.mapper;

import it.compare.backend.rating.dto.RatingRequestDto;
import it.compare.backend.rating.dto.RatingResponseDto;
import it.compare.backend.rating.model.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RatingMapper {

    RatingResponseDto toResponseDto(Rating rating);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "comment", ignore = true)
    Rating toEntity(RatingRequestDto ratingRequestDto);
}
