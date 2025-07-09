package com.ecommerce.sbecom.exceptions;

// This custom exception is designed to work with a global exception handler.
// To provide consistent, meaningful error responses across your application when resources cannot be found.
// The stored fields allow the exception handler to create detailed error responses with specific information about
//  what resource was being looked for and what search criteria were used.
public class ResourceNotFoundException extends RuntimeException {

    int fieldId;
    long id;
    String fieldName;
    String field;
    String resourceName;

    public ResourceNotFoundException() {
    }


    public ResourceNotFoundException(String resourceName, String field, String fieldName) {
        super(String.format("%s not found with %s: %s", resourceName, field, fieldName));
        this.fieldName = fieldName;
        this.field = field;
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException( String resourceName, String field, int fieldId ) {
        super(String.format("%s not found with %s: %d", resourceName, field, fieldId));
        this.fieldId = fieldId;
        this.field = field;
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException( String resourceName, String field, long id ) {
        super(String.format("%s not found with %s: %d", resourceName, field, id));
        this.id = id;
        this.field = field;
        this.resourceName = resourceName;
    }
}
