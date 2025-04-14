package com.api.exception;


import jakarta.validation.ConstraintViolationException;

/**
 * Class ValidException
 *
 * Custom exception class for handling validation errors.
 * This exception is thrown when validation constraints are violated during processing.
 */
public class ValidException extends RuntimeException {

    private final ConstraintViolationException constraintViolationException;

    /**
     * Constructor for creating a new instance of ValidException.
     *
     * @param constraintViolationException The original ConstraintViolationException
     *                                      that holds detailed validation error information.
     */
    public ValidException(ConstraintViolationException constraintViolationException) {
        super(constraintViolationException.getMessage());
        this.constraintViolationException = constraintViolationException;
    }

    /**
     * Gets the original ConstraintViolationException that contains the validation errors.
     *
     * @return The ConstraintViolationException instance.
     */
    public ConstraintViolationException getConstraintViolationException() {
        return constraintViolationException;
    }
}

