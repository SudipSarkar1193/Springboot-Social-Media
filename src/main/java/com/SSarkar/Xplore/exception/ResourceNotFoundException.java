package com.SSarkar.Xplore.exception;

// This is an unchecked exception because it extends RuntimeException
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}