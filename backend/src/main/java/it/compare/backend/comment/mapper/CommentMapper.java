package it.compare.backend.comment.mapper;

import it.compare.backend.comment.dto.CommentRequestDto;
import it.compare.backend.comment.dto.CommentResponseDto;
import it.compare.backend.comment.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {
    CommentResponseDto toResponseDto(
            Comment comment,
            @NonNull Long positiveRatingsCount,
            @NonNull Long negativeRatingsCount,
            @Nullable Boolean isRatingPositive);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "product", ignore = true)
    Comment toEntity(CommentRequestDto commentRequestDto);
}
