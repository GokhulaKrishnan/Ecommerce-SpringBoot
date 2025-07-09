package com.ecommerce.sbecom.exceptions;

// Here we are going to handle the exceptions in this application

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
public class MyGlobalExceptionHandler {

    // Here we are going to handle the MethodArgumentNotValidException.
    // This exception is thrown when the data passed to the controller is not a valid data.
    // Here we are going to return a hashmap of error with the message.

    // The below annotation is used to tell spring that this is the method to be invoked if methodArgument exception
    //      arises.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HashMap<String, String>> MethodArgumentNotValidException(MethodArgumentNotValidException e) {

        // To store the errors
        HashMap<String, String> errors = new HashMap<>();

        // Getting the error
        e.getBindingResult().getAllErrors().forEach(err -> {
            // Getting the name
            String fieldName = ((FieldError) err).getField();
            // Getting the error message
            String errorMessage = err.getDefaultMessage();
            // Adding it into the hashmap
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<HashMap<String, String>>(errors, HttpStatus.BAD_REQUEST);
    }


    // Here we are creating a new exception handler for resource not found which uses the custom exception class
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> myResourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    // Exception handler for API
    @ExceptionHandler(APIExceptionHandler.class)
    public ResponseEntity<APIResponse> myAPIException(APIExceptionHandler apiExceptionHandler) {
        String message = apiExceptionHandler.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
}
