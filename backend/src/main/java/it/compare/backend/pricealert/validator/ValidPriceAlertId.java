package it.compare.backend.pricealert.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import it.compare.backend.core.validator.ValidObjectId;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
@Constraint(validatedBy = {})
@ValidObjectId(message = "priceAlertId must be a valid ObjectId.")
public @interface ValidPriceAlertId {
    String message() default "Invalid priceAlertId format.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
