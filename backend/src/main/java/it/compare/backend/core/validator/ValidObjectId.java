package it.compare.backend.core.validator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.bson.types.ObjectId;

@Retention(RUNTIME)
@Target({FIELD, PARAMETER, ANNOTATION_TYPE})
@Constraint(validatedBy = ObjectIdValidator.class)
public @interface ValidObjectId {
    String message() default "Invalid ObjectId format.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class ObjectIdValidator implements ConstraintValidator<ValidObjectId, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return true;

        try {
            new ObjectId(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
