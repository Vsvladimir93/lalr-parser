package com.petproject.lalrparser.parser.exception;

public class ShiftReduceConflictException extends RuntimeException {
    public ShiftReduceConflictException(String message) {
        super(message);
    }

    public ShiftReduceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
