package it.compare.backend.comment.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import it.compare.backend.core.validator.ValidObjectId;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
@Constraint(validatedBy = {})
@NotBlank(message = "commentId cannot be empty.") @ValidObjectId(message = "commentId must be a valid ObjectId.")
public @interface ValidCommentId {
    String message() default "Invalid commentId format.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
