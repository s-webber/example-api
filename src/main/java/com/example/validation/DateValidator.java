package com.example.validation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateValidator implements ConstraintValidator<Date, String> {
   /**
    * Attempts to parse the given {@code date} to LocalDate
    *
    * @return {@code true} if {@code date} is {@code null} or represents a valid date in the format YYYY-MM-DD, else
    * returns {@code false}
    */
   @Override
   public boolean isValid(String date, ConstraintValidatorContext context) {
      if (date == null) {
         return true;
      }

      try {
         LocalDate.parse(date);
         return true;
      } catch (DateTimeParseException e) {
         return false;
      }
   }
}