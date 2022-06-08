package com.petproject.lalrparser.parser.exception;

public class ReduceReduceConflictException extends RuntimeException {
    public ReduceReduceConflictException(String message) {
        super(message);
    }

    public ReduceReduceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
