package it.compare.backend.rating.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import it.compare.backend.rating.repository.RatingRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RatingRepositoryTest extends RatingTest {

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    void shouldReturnEmptyOptionalWhenNoRatingExistsForAuthorAndComment() {
        var result = ratingRepository.findByAuthorIdAndCommentId(user.getId(), testComment.getId());

        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void shouldNotDeleteAnyRatingsWhenCommentIdDoesNotExist() {
        var rating = ratingTestDataFactory.createRatingForComment(testComment, user, true);

        assertThat(ratingRepository.count(), is(1L));

        ratingRepository.deleteAllByCommentId(new ObjectId().toString());

        assertThat(ratingRepository.count(), is(1L));
        assertThat(ratingRepository.findById(rating.getId()).isPresent(), is(true));
    }

    @Test
    void shouldDeleteAllRatingsForComment() {
        var user2 = userTestDataFactory.createOne();
        var user3 = userTestDataFactory.createOne();

        var rating1 = ratingTestDataFactory.createRatingForComment(testComment, user, true);
        var rating2 = ratingTestDataFactory.createRatingForComment(testComment, user2, false);
        var rating3 = ratingTestDataFactory.createRatingForComment(testComment, user3, true);

        var otherComment = commentTestDataFactory.createCommentForProduct(testProduct);
        var otherRating = ratingTestDataFactory.createRatingForComment(otherComment, user, false);

        assertThat(ratingRepository.count(), is(4L));

        ratingRepository.deleteAllByCommentId(testComment.getId());

        assertThat(ratingRepository.count(), is(1L));
        assertThat(ratingRepository.findById(otherRating.getId()).isPresent(), is(true));
        assertThat(ratingRepository.findById(rating1.getId()).isEmpty(), is(true));
        assertThat(ratingRepository.findById(rating2.getId()).isEmpty(), is(true));
        assertThat(ratingRepository.findById(rating3.getId()).isEmpty(), is(true));
    }

    @Test
    void shouldReturnCorrectRatingWhenMultipleRatingsExist() {
        var anotherUser = userTestDataFactory.createOne();
        var anotherComment = commentTestDataFactory.createCommentForProduct(testProduct);

        var rating1 = ratingTestDataFactory.createRatingForComment(testComment, user, true);
        var rating2 = ratingTestDataFactory.createRatingForComment(anotherComment, user, false);
        var rating3 = ratingTestDataFactory.createRatingForComment(testComment, anotherUser, false);

        var result1 = ratingRepository.findByAuthorIdAndCommentId(user.getId(), testComment.getId());
        var result2 = ratingRepository.findByAuthorIdAndCommentId(user.getId(), anotherComment.getId());
        var result3 = ratingRepository.findByAuthorIdAndCommentId(anotherUser.getId(), testComment.getId());

        assertThat(result1.isPresent(), is(true));
        assertThat(result1.get().getId(), equalTo(rating1.getId()));
        assertThat(result1.get().getIsPositive(), equalTo(true));

        assertThat(result2.isPresent(), is(true));
        assertThat(result2.get().getId(), equalTo(rating2.getId()));
        assertThat(result2.get().getIsPositive(), equalTo(false));

        assertThat(result3.isPresent(), is(true));
        assertThat(result3.get().getId(), equalTo(rating3.getId()));
        assertThat(result3.get().getIsPositive(), equalTo(false));
    }

    @Test
    void shouldReturnRatingWhenExistsForAuthorAndComment() {
        var rating = ratingTestDataFactory.createRatingForComment(testComment, user, true);

        var result = ratingRepository.findByAuthorIdAndCommentId(user.getId(), testComment.getId());

        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getId(), equalTo(rating.getId()));
        assertThat(result.get().getIsPositive(), equalTo(true));
    }
}
