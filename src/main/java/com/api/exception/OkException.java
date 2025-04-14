package com.api.exception;

/**
 * Class OkException
 *
 * Custom exception class for handling OK status errors.
 * This exception can be used when an operation completes successfully,
 * but you want to return some specific information or behavior through the exception.
 */
public class OkException extends RuntimeException{

    /**
     * Constructor for creating a new instance of OkException.
     *
     * @param errorMessage The message that explains the context or details of the successful operation.
     */
    public OkException(String errorMessage){
        super(errorMessage);
    }
}
