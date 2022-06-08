package com.petproject.lalrparser.grammar.exception;

public class GrammarProcessingException extends RuntimeException {
    public GrammarProcessingException(String message) {
        super(message);
    }

    public GrammarProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
