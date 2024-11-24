package com.example.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

//Define the annotation as a constraint
@Constraint(validatedBy = PostcodeValidator.class) // Points to the Validator class
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Postcode {
   String message() default "Invalid postcode format.";

   Class<?>[] groups() default {};

   Class<? extends Payload>[] payload() default {};
}
