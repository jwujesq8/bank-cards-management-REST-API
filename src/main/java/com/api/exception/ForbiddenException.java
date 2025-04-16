package com.api.exception;

/**
 * Class AuthException
 *
 * Custom exception class for handling authentication errors.
 * This exception is thrown when authentication-related issues occur,
 * such as invalid credentials, token errors, or unauthorized access attempts.
 */
public class ForbiddenException extends RuntimeException{
    /**
     * Constructor for creating a new instance of AuthException.
     *
     * @param errorMessage The message that explains the error.
     */
    public ForbiddenException(String errorMessage){
        super(errorMessage);
    }
}
