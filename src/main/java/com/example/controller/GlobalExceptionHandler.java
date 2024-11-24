package com.example.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.exception.ResourceAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

   /**
    * Invoked when the values in the body of the request fail validation.
    * <p>
    * Returns a 400 status code and a list of reasons for failure.
    */
   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<Object> handleFailedValidation(MethodArgumentNotValidException e) {
      BindingResult result = e.getBindingResult();
      List<FieldError> fieldErrors = result.getFieldErrors();

      List<String> errors = fieldErrors.stream().map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()).sorted().collect(Collectors.toList());

      return ResponseEntity.badRequest().body(errors);
   }

   @ExceptionHandler(ResourceNotFoundException.class)
   public ResponseEntity<Object> handleNotFound(ResourceNotFoundException e) {
      return ResponseEntity.notFound().build();
   }

   @ExceptionHandler(ResourceAlreadyExistsException.class)
   public ResponseEntity<Object> handleNotFound(ResourceAlreadyExistsException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
   }

   @ExceptionHandler(HttpMessageNotReadableException.class)
   public ResponseEntity<Object> handleMessageNotReadable(HttpMessageNotReadableException e) {
      return ResponseEntity.badRequest().build();
   }

   @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
   public ResponseEntity<Object> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
      return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
   }

   @ExceptionHandler(NoResourceFoundException.class)
   public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException e) {
      return ResponseEntity.notFound().build();
   }

   @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
   public ResponseEntity<Object> hanldeMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
      return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
   }

   /** Catch-all for exceptions not handled elsewhere. */
   @ExceptionHandler(Exception.class)
   public ResponseEntity<Object> handleInternalServerException(Exception e) {
      LOGGER.error("internal server error", e);
      return ResponseEntity.internalServerError().build();
   }
}