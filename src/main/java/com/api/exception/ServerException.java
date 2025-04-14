package com.api.exception;

/**
 * Class ServerException
 *
 * Custom exception class for handling server-related errors.
 * This exception is thrown when an unexpected error occurs on the server side.
 */
public class ServerException extends RuntimeException{

    /**
     * Constructor for creating a new instance of ServerException.
     *
     * @param errorMessage The message that explains the server error.
     */
    public ServerException(String errorMessage){
        super(errorMessage);
    }
}
