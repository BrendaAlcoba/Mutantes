package org.example.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidDnaSequenceValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDnaSequence {
    String message() default "ADN inválido: debe ser NxN y contener solo A,T,C,G";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
