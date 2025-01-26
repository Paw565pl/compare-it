package it.compare.backend.comment.service;

import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.model.Role;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.auth.util.AuthUtil;
import it.compare.backend.comment.dto.CommentDto;
import it.compare.backend.comment.mapper.CommentMapper;
import it.compare.backend.comment.model.Comment;
import it.compare.backend.comment.repository.CommentRepository;
import it.compare.backend.comment.response.CommentResponse;
import it.compare.backend.product.service.ProductService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Size;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final MongoTemplate mongoTemplate;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ProductService productService;
    private final UserRepository userRepository;

    private static final String RATINGS_COLLECTION = "ratings";
    private static final String POSITIVE_RATINGS_COUNT_FIELD = "positiveRatingsCount";
    private static final String NEGATIVE_RATINGS_COUNT_FIELD = "negativeRatingsCount";
    private static final String CREATED_AT_FIELD = "createdAt";

    public Comment findCommentOrThrow(String id) {
        return commentRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    private List<AggregationOperation> getCommentRatingsAggregationOperations() {
        var ratingsLookup = Aggregation.lookup(RATINGS_COLLECTION, "_id", "comment.$id", RATINGS_COLLECTION);
        var authorLookup = Aggregation.lookup("users", "author.$id", "_id", "author");

        var positiveRatingsCountFilter = Filter.filter(RATINGS_COLLECTION)
                .as("rating")
                .by(Eq.valueOf("rating.isPositive").equalToValue(true));
        var negativeRatingsCountFilter = Filter.filter(RATINGS_COLLECTION)
                .as("rating")
                .by(Eq.valueOf("rating.isPositive").equalToValue(false));
        var addFields = Aggregation.addFields()
                .addFieldWithValue(POSITIVE_RATINGS_COUNT_FIELD, Size.lengthOfArray(positiveRatingsCountFilter))
                .addFieldWithValue(NEGATIVE_RATINGS_COUNT_FIELD, Size.lengthOfArray(negativeRatingsCountFilter))
                .build();

        var project = Aggregation.project(
                        "id", "text", CREATED_AT_FIELD, POSITIVE_RATINGS_COUNT_FIELD, NEGATIVE_RATINGS_COUNT_FIELD)
                .and("author.username")
                .as("author");

        return List.of(ratingsLookup, authorLookup, addFields, project);
    }

    public Page<CommentResponse> findAllByProductId(String productId, Pageable pageable) {
        productService.findProductOrThrow(productId);

        var criteria = Criteria.where("product.$id").is(productId);
        var total = mongoTemplate.count(Query.query(criteria), Comment.class);

        var operations = new ArrayList<>(getCommentRatingsAggregationOperations());

        var match = Aggregation.match(criteria);
        operations.addFirst(match);

        var sortOrders = new ArrayList<Sort.Order>();
        var validSortProperties = Set.of(POSITIVE_RATINGS_COUNT_FIELD, NEGATIVE_RATINGS_COUNT_FIELD, CREATED_AT_FIELD);

        pageable.getSort().forEach(order -> {
            var property = order.getProperty();
            if (validSortProperties.contains(property)) sortOrders.add(new Sort.Order(order.getDirection(), property));
        });
        var sort = Aggregation.sort(Sort.by(sortOrders));
        operations.add(sort);

        var skip = Aggregation.skip(pageable.getOffset());
        var limit = Aggregation.limit(pageable.getPageSize());
        operations.add(skip);
        operations.add(limit);

        var aggregation = Aggregation.newAggregation(Comment.class, operations);
        var results =
                mongoTemplate.aggregate(aggregation, CommentResponse.class).getMappedResults();

        return new PageImpl<>(results, pageable, total);
    }

    public CommentResponse findById(String productId, String commentId) {
        productService.findProductOrThrow(productId);

        var criteria = Criteria.where("product.$id").is(productId).and("_id").is(commentId);
        var match = Aggregation.match(criteria);

        var operations = new ArrayList<>(getCommentRatingsAggregationOperations());
        operations.addFirst(match);

        var aggregation = Aggregation.newAggregation(Comment.class, operations);
        var result = mongoTemplate
                .aggregate(aggregation, Comment.class, CommentResponse.class)
                .getUniqueMappedResult();

        if (result == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");

        return result;
    }

    @Transactional
    public CommentResponse create(OAuthUserDetails oAuthUserDetails, String productId, CommentDto commentDto) {
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var product = productService.findProductOrThrow(productId);

        var comment = commentMapper.toEntity(commentDto);
        comment.setAuthor(user);
        comment.setProduct(product);
        var savedComment = commentRepository.save(comment);

        var commentResponse = commentMapper.toResponse(savedComment);
        commentResponse.setPositiveRatingsCount(0L);
        commentResponse.setNegativeRatingsCount(0L);

        return commentResponse;
    }

    @Transactional
    public CommentResponse update(
            OAuthUserDetails oAuthUserDetails, String productId, String commentId, CommentDto commentDto) {
        var userId = oAuthUserDetails.getId();
        productService.findProductOrThrow(productId);
        var comment = findCommentOrThrow(commentId);

        var canUpdate = userId.equals(comment.getAuthor().getId());
        if (!canUpdate) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        comment.setText(commentDto.text());
        var savedComment = commentRepository.save(comment);

        return commentMapper.toResponse(savedComment);
    }

    @Transactional
    public void deleteById(OAuthUserDetails oAuthUserDetails, String productId, String commentId) {
        var userId = oAuthUserDetails.getId();
        productService.findProductOrThrow(productId);
        var comment = findCommentOrThrow(commentId);

        var canDelete = userId.equals(comment.getAuthor().getId())
                || AuthUtil.hasRole(oAuthUserDetails.getAuthorities(), Role.ADMIN);
        if (!canDelete) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        commentRepository.deleteById(commentId);
    }
}
