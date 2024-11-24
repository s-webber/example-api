package com.example.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PostcodeValidatorTest {
   private final PostcodeValidator validator = new PostcodeValidator();

   @Test
   void nullValid() {
      assertTrue(validator.isValid(null, null));
   }

   @ParameterizedTest
   @ValueSource(strings = {"EC1A 1BB", "W1A 0AX", "M1 1AE", "B33 8TH", "CR2 6XH", "DN55 1PT"})
   void valid(String valid) {
      assertTrue(validator.isValid(valid, null));
   }

   @ParameterizedTest
   @ValueSource(strings = {"W1A1AA", "123 ABC", "ZZZZ ZZZ", "", " "})
   void invalid(String invalid) {
      assertFalse(validator.isValid(invalid, null));
   }
}
