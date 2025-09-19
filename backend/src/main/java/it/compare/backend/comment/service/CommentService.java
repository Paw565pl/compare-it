package it.compare.backend.comment.service;

import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.model.Role;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.auth.util.AuthUtil;
import it.compare.backend.comment.dto.CommentRequestDto;
import it.compare.backend.comment.dto.CommentResponseDto;
import it.compare.backend.comment.mapper.CommentMapper;
import it.compare.backend.comment.model.Comment;
import it.compare.backend.comment.repository.CommentRepository;
import it.compare.backend.product.service.ProductService;
import it.compare.backend.rating.repository.RatingRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Size;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
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
    private final RatingRepository ratingRepository;

    private static final String RATINGS_FIELD = "ratings";
    private static final String SINGLE_RATING_FIELD = "rating";
    private static final String AUTHOR_FIELD = "author";
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String POSITIVE_RATINGS_COUNT_FIELD = "positiveRatingsCount";
    private static final String NEGATIVE_RATINGS_COUNT_FIELD = "negativeRatingsCount";
    private static final String IS_POSITIVE_RATING_FIELD = "isRatingPositive";

    public Comment findCommentOrThrow(String id) {
        return commentRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    public Page<CommentResponseDto> findAllByProductId(
            @Nullable OAuthUserDetails oAuthUserDetails, String productId, Pageable pageable) {
        productService.findProductOrThrow(productId);

        var criteria = Criteria.where("product.$id").is(new ObjectId(productId));
        var total = mongoTemplate.count(Query.query(criteria), Comment.class);
        if (total == 0) return Page.empty(pageable);

        var operations = new ArrayList<AggregationOperation>();

        operations.add(Aggregation.match(criteria));
        operations.addAll(getRatingsCountsAggregationOperations());

        var userId = Optional.ofNullable(oAuthUserDetails).map(OAuthUserDetails::getId);
        userId.ifPresent(userIdValue -> operations.add(getUserRatingAggregationOperation(userIdValue)));

        operations.add(Aggregation.project().andExclude(RATINGS_FIELD));

        var validSortProperties = Set.of(POSITIVE_RATINGS_COUNT_FIELD, NEGATIVE_RATINGS_COUNT_FIELD, CREATED_AT_FIELD);
        var sortOrders = pageable.getSort()
                .filter(order -> validSortProperties.contains(order.getProperty()))
                .toList();
        operations.add(Aggregation.sort(Sort.by(sortOrders)));

        operations.add(Aggregation.skip(pageable.getOffset()));
        operations.add(Aggregation.limit(pageable.getPageSize()));

        var project = Aggregation.project(
                        "id",
                        "text",
                        CREATED_AT_FIELD,
                        POSITIVE_RATINGS_COUNT_FIELD,
                        NEGATIVE_RATINGS_COUNT_FIELD,
                        IS_POSITIVE_RATING_FIELD)
                .and("author.username")
                .as(AUTHOR_FIELD);
        operations.add(project);

        var aggregation = Aggregation.newAggregation(Comment.class, operations);
        var results =
                mongoTemplate.aggregate(aggregation, CommentResponseDto.class).getMappedResults();

        return new PageImpl<>(results, pageable, total);
    }

    private List<AggregationOperation> getRatingsCountsAggregationOperations() {
        var ratingsLookup = Aggregation.lookup(RATINGS_FIELD, "_id", "comment.$id", RATINGS_FIELD);
        var authorLookup = Aggregation.lookup("users", "author.$id", "_id", AUTHOR_FIELD);

        var positiveRatingsCountFilter = Filter.filter(RATINGS_FIELD)
                .as(SINGLE_RATING_FIELD)
                .by(Eq.valueOf("$$rating.isPositive").equalToValue(true));
        var negativeRatingsCountFilter = Filter.filter(RATINGS_FIELD)
                .as(SINGLE_RATING_FIELD)
                .by(Eq.valueOf("$$rating.isPositive").equalToValue(false));
        var addFields = Aggregation.addFields()
                .addFieldWithValue(POSITIVE_RATINGS_COUNT_FIELD, Size.lengthOfArray(positiveRatingsCountFilter))
                .addFieldWithValue(NEGATIVE_RATINGS_COUNT_FIELD, Size.lengthOfArray(negativeRatingsCountFilter))
                .build();

        return List.of(ratingsLookup, authorLookup, addFields);
    }

    private AddFieldsOperation getUserRatingAggregationOperation(String userId) {
        var filterRatingsByUserId = Filter.filter(RATINGS_FIELD)
                .as(SINGLE_RATING_FIELD)
                .by(Eq.valueOf("$$rating.author.$id").equalToValue(userId));
        var findFirstRating = ArrayOperators.First.firstOf(filterRatingsByUserId);

        var nullValue = ObjectOperators.getValueOf("non-existing-field");
        var calculateIsRatingPositiveField = ConditionalOperators.ifNull(
                        ObjectOperators.GetField.getField("isPositive").of(findFirstRating))
                .thenValueOf(nullValue);

        return Aggregation.addFields()
                .addFieldWithValue(IS_POSITIVE_RATING_FIELD, calculateIsRatingPositiveField)
                .build();
    }

    public CommentResponseDto findById(
            @Nullable OAuthUserDetails oAuthUserDetails, String productId, String commentId) {
        productService.findProductOrThrow(productId);
        var operations = new ArrayList<AggregationOperation>();

        var criteria = Criteria.where("product.$id")
                .is(new ObjectId(productId))
                .and("_id")
                .is(new ObjectId(commentId));
        operations.add(Aggregation.match(criteria));
        operations.addAll(getRatingsCountsAggregationOperations());

        var userId = Optional.ofNullable(oAuthUserDetails).map(OAuthUserDetails::getId);
        userId.ifPresent(userIdValue -> operations.add(getUserRatingAggregationOperation(userIdValue)));

        var project = Aggregation.project(
                        "id",
                        "text",
                        CREATED_AT_FIELD,
                        POSITIVE_RATINGS_COUNT_FIELD,
                        NEGATIVE_RATINGS_COUNT_FIELD,
                        IS_POSITIVE_RATING_FIELD)
                .and("author.username")
                .as(AUTHOR_FIELD);
        operations.add(project);

        var aggregation = Aggregation.newAggregation(Comment.class, operations);
        var commentResponse = mongoTemplate
                .aggregate(aggregation, Comment.class, CommentResponseDto.class)
                .getUniqueMappedResult();

        if (commentResponse == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");

        return commentResponse;
    }

    @Transactional
    public CommentResponseDto create(
            OAuthUserDetails oAuthUserDetails, String productId, CommentRequestDto commentRequestDto) {
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var product = productService.findProductOrThrow(productId);

        var comment = commentMapper.toEntity(commentRequestDto);
        comment.setAuthor(user);
        comment.setProduct(product);
        var savedComment = commentRepository.save(comment);

        return commentMapper.toResponseDto(savedComment, 0L, 0L, null);
    }

    @Transactional
    public CommentResponseDto update(
            OAuthUserDetails oAuthUserDetails,
            String productId,
            String commentId,
            CommentRequestDto commentRequestDto) {
        var userId = oAuthUserDetails.getId();
        productService.findProductOrThrow(productId);
        var comment = findCommentOrThrow(commentId);

        var canUpdate = userId.equals(comment.getAuthor().getId());
        if (!canUpdate) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        comment.setText(commentRequestDto.text());
        commentRepository.save(comment);

        return findById(oAuthUserDetails, productId, commentId);
    }

    @Transactional
    public void deleteById(OAuthUserDetails oAuthUserDetails, String productId, String commentId) {
        var userId = oAuthUserDetails.getId();
        productService.findProductOrThrow(productId);
        var comment = findCommentOrThrow(commentId);

        var canDelete = userId.equals(comment.getAuthor().getId())
                || AuthUtil.hasRole(oAuthUserDetails.getAuthorities(), Role.ADMIN);
        if (!canDelete) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        ratingRepository.deleteAllByCommentId(commentId);
        commentRepository.deleteById(commentId);
    }
}
