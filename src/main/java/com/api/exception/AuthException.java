package com.api.exception;

/**
 * Class AuthException
 *
 * Custom exception class for handling unsuccessful authentication cases.
 * This exception is thrown when the client sends a wrong password etc.
 */
public class AuthException extends RuntimeException {

    /**
     * Constructor for creating a new instance of AuthException.
     *
     * @param errorMessage The message that explains the error.
     */
    public AuthException(String errorMessage) {
        super(errorMessage);
    }
}
