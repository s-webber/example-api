package com.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NhsNumberValidator implements ConstraintValidator<NhsNumber, String> {
   /**
    * Confirms that {@code nhsNumber} is in a valid format for a NHS number
    *
    * @return {@code true} if {@code nhsNumber} is {@code null} or is in a valid format, else returns {@code false}
    * @see https://en.wikipedia.org/wiki/NHS_number#Format,_number_ranges,_and_check_characters
    */
   @Override
   public boolean isValid(String nhsNumber, ConstraintValidatorContext context) {
      if (nhsNumber == null) {
         return true;
      }

      if (!nhsNumber.matches("\\d{10}")) {
         return false;
      }

      int[] digits = nhsNumber.chars().map(Character::getNumericValue).toArray();
      int checkDigit = digits[9];

      int sum = 0;
      for (int i = 0; i < 9; i++) {
         sum += digits[i] * (10 - i);
      }

      int remainder = sum % 11;
      int calculatedCheckDigit = (11 - remainder) % 11;

      return calculatedCheckDigit == checkDigit;
   }
}
