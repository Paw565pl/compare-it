package it.compare.backend.comment.datafactory;

import it.compare.backend.auth.model.User;
import it.compare.backend.comment.model.Comment;
import it.compare.backend.comment.repository.CommentRepository;
import it.compare.backend.core.config.FakerConfig;
import it.compare.backend.core.datafactory.TestDataFactory;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.model.Product;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import java.util.ArrayList;
import java.util.List;
import net.datafaker.Faker;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;

@TestComponent
@Import({ProductTestDataFactory.class, FakerConfig.class})
public class CommentTestDataFactory implements TestDataFactory<Comment> {

    public final CommentRepository commentRepository;
    public final ProductTestDataFactory productTestDataFactory;
    private final Faker faker;
    private final UserTestDataFactory userTestDataFactory;

    public CommentTestDataFactory(
            CommentRepository commentRepository,
            ProductTestDataFactory productTestDataFactory,
            UserTestDataFactory userTestDataFactory,
            Faker faker) {
        this.commentRepository = commentRepository;
        this.productTestDataFactory = productTestDataFactory;
        this.userTestDataFactory = userTestDataFactory;
        this.faker = faker;
    }

    @Override
    public Comment generate() {
        return new Comment(faker.lorem().sentence(), productTestDataFactory.generate());
    }

    @Override
    public Comment createOne() {
        return commentRepository.save(generate());
    }

    @Override
    public List<Comment> createMany(int count) {
        var comments = new ArrayList<Comment>();
        for (int i = 0; i < count; i++) {
            comments.add(generate());
        }

        return commentRepository.saveAll(comments);
    }

    @Override
    public void clear() {
        userTestDataFactory.clear();
        productTestDataFactory.clear();
        commentRepository.deleteAll();
    }

    public Comment createCommentForProduct(Product product) {
        var comment = new Comment(faker.lorem().sentence(), product);
        return commentRepository.save(comment);
    }

    public List<Comment> createMultipleCommentsForProduct(Product product, User author, int count) {
        var comments = new ArrayList<Comment>();
        for (int i = 0; i < count; i++) {
            var comment = new Comment(faker.lorem().sentence(), product);
            comment.setAuthor(author);
            comments.add(commentRepository.save(comment));
        }

        return comments;
    }
}
