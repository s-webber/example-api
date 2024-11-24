package com.example.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PostcodeValidator implements ConstraintValidator<Postcode, String> {
   private static final Pattern POSTCODE_PATTERN = Pattern.compile("^[A-Z]{1,2}\\d[A-Z\\d]? \\d[A-Z]{2}$");

   /**
    * Confirms that {@code postcode} is in a valid format for a postcode
    *
    * @return {@code true} if {@code postcode} is {@code null} or is in a valid format, else returns {@code false}
    * @see https://en.wikipedia.org/wiki/Postcodes_in_the_United_Kingdom#Formatting
    */
   @Override
   public boolean isValid(String postcode, ConstraintValidatorContext context) {
      return postcode == null || POSTCODE_PATTERN.matcher(postcode).matches();
   }
}
