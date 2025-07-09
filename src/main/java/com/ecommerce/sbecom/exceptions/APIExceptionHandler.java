package com.ecommerce.sbecom.exceptions;

// This is the exception class which is used to throw error when same category is uploaded.
public class APIExceptionHandler extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public APIExceptionHandler() {
    }

    public APIExceptionHandler(String message) {
        super(message);
    }
}
