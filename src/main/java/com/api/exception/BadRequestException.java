package com.api.exception;

/**
 * Class BadRequestException
 *
 * Custom exception class for handling bad request errors.
 * This exception is thrown when the client sends a request that
 * the server cannot process, typically due to invalid input or missing parameters.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Constructor for creating a new instance of BadRequestException.
     *
     * @param errorMessage The message that explains the error.
     */
    public BadRequestException (String errorMessage){
        super(errorMessage);
    }
}
