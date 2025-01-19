package it.compare.backend.comment.mapper;

import it.compare.backend.comment.dto.CommentDto;
import it.compare.backend.comment.model.Comment;
import it.compare.backend.comment.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final ModelMapper modelMapper;

    public CommentResponse toResponse(Comment comment) {
        var response = modelMapper.map(comment, CommentResponse.class);
        if (comment.getAuthor() != null) response.setAuthor(comment.getAuthor().getUsername());

        return response;
    }

    public Comment toEntity(CommentDto commentDto) {
        return modelMapper.map(commentDto, Comment.class);
    }
}
