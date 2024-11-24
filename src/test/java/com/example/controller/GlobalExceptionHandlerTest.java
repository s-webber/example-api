package com.example.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class GlobalExceptionHandlerTest {
   private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

   @Test
   void test() {
      List<FieldError> errors = Arrays.asList(new FieldError("dummyObjectName", "nhsNumber", "Invalid format for NHS number"),
                  new FieldError("dummyObjectName", "dateOfBirth", "Incorrect date format"), new FieldError("dummyObjectName", "name", "Too short"));
      BindingResult bindingResult = mock(BindingResult.class);
      when(bindingResult.getFieldErrors()).thenReturn(errors);
      MethodArgumentNotValidException e = new MethodArgumentNotValidException(null, bindingResult);

      ResponseEntity<Object> response = handler.handleFailedValidation(e);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertEquals(Arrays.asList("dateOfBirth: Incorrect date format", "name: Too short", "nhsNumber: Invalid format for NHS number"), response.getBody());
   }
}
