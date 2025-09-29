package it.compare.backend.comment.mapper;

import it.compare.backend.comment.dto.CommentRequestDto;
import it.compare.backend.comment.dto.CommentResponseDto;
import it.compare.backend.comment.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {
    @Mapping(target = "author", source = "comment.author.username")
    CommentResponseDto toResponseDto(
            Comment comment, long positiveRatingsCount, long negativeRatingsCount, @Nullable Boolean isRatingPositive);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentRequestDto commentRequestDto);
}
