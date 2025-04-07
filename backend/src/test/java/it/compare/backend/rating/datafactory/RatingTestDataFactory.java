package it.compare.backend.rating.datafactory;

import it.compare.backend.auth.model.User;
import it.compare.backend.comment.datafactory.CommentTestDataFactory;
import it.compare.backend.comment.model.Comment;
import it.compare.backend.core.config.FakerConfig;
import it.compare.backend.core.datafactory.TestDataFactory;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.rating.model.Rating;
import it.compare.backend.rating.repository.RatingRepository;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;

@TestComponent
@Import({CommentTestDataFactory.class, FakerConfig.class})
public class RatingTestDataFactory implements TestDataFactory<Rating> {

    private final RatingRepository ratingRepository;
    private final CommentTestDataFactory commentTestDataFactory;
    private final ProductTestDataFactory productTestDataFactory;
    private final UserTestDataFactory userTestDataFactory;

    public RatingTestDataFactory(
            RatingRepository ratingRepository,
            CommentTestDataFactory commentTestDataFactory,
            ProductTestDataFactory productTestDataFactory,
            UserTestDataFactory userTestDataFactory) {
        this.ratingRepository = ratingRepository;
        this.commentTestDataFactory = commentTestDataFactory;
        this.productTestDataFactory = productTestDataFactory;
        this.userTestDataFactory = userTestDataFactory;
    }

    @Override
    public Rating generate() {
        var comment = commentTestDataFactory.createOne();
        return new Rating(true, comment);
    }

    @Override
    public Rating createOne() {
        return ratingRepository.save(generate());
    }

    @Override
    public List<Rating> createMany(int count) {
        var ratings = new ArrayList<Rating>();
        for (int i = 0; i < count; i++) {
            ratings.add(generate());
        }

        return ratingRepository.saveAll(ratings);
    }

    @Override
    public void clear() {
        userTestDataFactory.clear();
        productTestDataFactory.clear();
        commentTestDataFactory.clear();
        ratingRepository.deleteAll();
    }

    public Rating createRatingForComment(Comment comment, User author, boolean isPositive) {
        var rating = new Rating(isPositive, comment);
        rating.setAuthor(author);
        return ratingRepository.save(rating);
    }
}
