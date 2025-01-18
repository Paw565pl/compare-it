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
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Size;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq;
import org.springframework.data.mongodb.core.query.Criteria;
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

    public Comment findCommentOrThrow(String id) {
        return commentRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    public Page<CommentResponse> findAll(String productId, Pageable pageable) {
        record CountResult(long total) {}

        productService.findProductOrThrow(productId);

        final var RATINGS_COLLECTION = "ratings";
        final var POSITIVE_RATINGS_COUNT = "positiveRatingsCount";
        final var NEGATIVE_RATINGS_COUNT = "negativeRatingsCount";

        var criteria = Criteria.where("product.$id").is(productId);

        var countMatch = Aggregation.match(criteria);
        var countOperation = Aggregation.count().as("total");
        var countAggregation = Aggregation.newAggregation(countMatch, countOperation);
        var countResults = mongoTemplate.aggregate(countAggregation, "comments", CountResult.class);
        var total = countResults.getMappedResults().isEmpty()
                ? 0
                : countResults.getMappedResults().getFirst().total();

        var match = Aggregation.match(criteria);
        var ratingsLookup = Aggregation.lookup(RATINGS_COLLECTION, "_id", "comment.$id", RATINGS_COLLECTION);
        var authorLookup = Aggregation.lookup("users", "author.$id", "_id", "author");

        var positiveRatingsCountFilter = Filter.filter(RATINGS_COLLECTION)
                .as("rating")
                .by(Eq.valueOf("rating.isPositiveRating").equalToValue(true));
        var negativeRatingsCountFilter = Filter.filter(RATINGS_COLLECTION)
                .as("rating")
                .by(Eq.valueOf("rating.isPositiveRating").equalToValue(false));
        var addFields = Aggregation.addFields()
                .addFieldWithValue(POSITIVE_RATINGS_COUNT, Size.lengthOfArray(positiveRatingsCountFilter))
                .addFieldWithValue(NEGATIVE_RATINGS_COUNT, Size.lengthOfArray(negativeRatingsCountFilter))
                .build();

        var project = Aggregation.project("id", "text", "createdAt", POSITIVE_RATINGS_COUNT, NEGATIVE_RATINGS_COUNT)
                .and("author.username")
                .as("author");

        var sortOrders = new ArrayList<Sort.Order>();
        var validSortProperties = new HashSet<>(Set.of(POSITIVE_RATINGS_COUNT, NEGATIVE_RATINGS_COUNT, "createdAt"));
        pageable.getSort().forEach(order -> {
            var property = order.getProperty();
            if (validSortProperties.contains(property)) sortOrders.add(new Sort.Order(order.getDirection(), property));
        });
        var sort = Aggregation.sort(Sort.by(sortOrders));

        var skip = Aggregation.skip(pageable.getOffset());
        var limit = Aggregation.limit(pageable.getPageSize());

        var aggregation = Aggregation.newAggregation(
                Comment.class, match, ratingsLookup, authorLookup, addFields, project, sort, skip, limit);
        var results =
                mongoTemplate.aggregate(aggregation, CommentResponse.class).getMappedResults();

        return new PageImpl<>(results, pageable, total);
    }

    public CommentResponse findById(String productId, String commentId) {
        productService.findProductOrThrow(productId);

        var match = Aggregation.match(
                Criteria.where("product.$id").is(productId).and("_id").is(commentId));
        var ratingsLookup = Aggregation.lookup("ratings", "_id", "comment.$id", "ratings");
        var authorLookup = Aggregation.lookup("users", "author.$id", "_id", "author");

        var positiveRatingsCountFilter = Filter.filter("ratings")
                .as("rating")
                .by(Eq.valueOf("rating.isPositiveRating").equalToValue(true));
        var negativeRatingsCountFilter = Filter.filter("ratings")
                .as("rating")
                .by(Eq.valueOf("rating.isPositiveRating").equalToValue(false));

        var addFields = Aggregation.addFields()
                .addFieldWithValue("positiveRatingsCount", Size.lengthOfArray(positiveRatingsCountFilter))
                .addFieldWithValue("negativeRatingsCount", Size.lengthOfArray(negativeRatingsCountFilter))
                .build();

        var project = Aggregation.project("id", "text", "createdAt", "positiveRatingsCount", "negativeRatingsCount")
                .and("author.username")
                .as("author");

        var aggregation =
                Aggregation.newAggregation(Comment.class, match, ratingsLookup, authorLookup, addFields, project);
        var result = mongoTemplate
                .aggregate(aggregation, Comment.class, CommentResponse.class)
                .getUniqueMappedResult();

        // TODO: throw exception if result is null
        return result != null ? result : commentMapper.toResponse(findCommentOrThrow(commentId));
    }

    @Transactional
    public CommentResponse create(OAuthUserDetails oAuthUserDetails, String productId, CommentDto commentDto) {
        // TODO: manually set calculated fields to 0 when creating new comment
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var product = productService.findProductOrThrow(productId);

        try {
            var comment = commentMapper.toEntity(commentDto);
            comment.setAuthor(user);
            comment.setProduct(product);
            var savedComment = commentRepository.save(comment);

            return commentMapper.toResponse(savedComment);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("You have already commented on this product.");
        }
    }

    @Transactional
    public void deleteById(OAuthUserDetails oAuthUserDetails, String productId, String commentId) {
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        productService.findProductOrThrow(productId);
        var comment = findCommentOrThrow(commentId);

        var canDelete = user.getId().equals(comment.getAuthor().getId())
                || AuthUtil.hasRole(oAuthUserDetails.getAuthorities(), Role.ADMIN);
        if (!canDelete) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        commentRepository.deleteById(commentId);
    }
}