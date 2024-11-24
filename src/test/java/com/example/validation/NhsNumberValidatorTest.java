package com.example.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class NhsNumberValidatorTest {
   private final NhsNumberValidator validator = new NhsNumberValidator();

   @Test
   void nullValid() {
      assertTrue(validator.isValid(null, null));
   }

   @ParameterizedTest
   @ValueSource(strings = {"9434765919", "9999999999"})
   void valid(String valid) {
      assertTrue(validator.isValid(valid, null));
   }

   @ParameterizedTest
   @ValueSource(strings = {"9434765918", "1234567890", "943476591", "94347659191", "94347659a9", "", " "})
   void invalid(String invalid) {
      assertFalse(validator.isValid(invalid, null));
   }
}
