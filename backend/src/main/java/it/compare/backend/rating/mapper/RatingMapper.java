package it.compare.backend.rating.mapper;

import it.compare.backend.rating.dto.RatingDto;
import it.compare.backend.rating.model.Rating;
import it.compare.backend.rating.response.RatingResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RatingMapper {

    private final ModelMapper modelMapper;

    public RatingResponse toResponse(Rating rating) {
        return modelMapper.map(rating, RatingResponse.class);
    }

    public Rating toEntity(RatingDto ratingDto) {
        return modelMapper.map(ratingDto, Rating.class);
    }
}
