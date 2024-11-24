package com.example.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DateValidatorTest {
   private final DateValidator validator = new DateValidator();

   @Test
   void nullValid() {
      assertTrue(validator.isValid(null, null));
   }

   @ParameterizedTest
   @ValueSource(strings = {"2024-12-25", "2024-02-29"})
   void valid(String valid) {
      assertTrue(validator.isValid(valid, null));
   }

   @ParameterizedTest
   @ValueSource(strings = {"2023-02-29", "2024-11-31", "2024-13-01", "2024-11-31", "2024-11-20T12:00:00", "", " "})
   void invalid(String invalid) {
      assertFalse(validator.isValid(invalid, null));
   }
}
