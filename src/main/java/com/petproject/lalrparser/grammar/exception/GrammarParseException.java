package com.petproject.lalrparser.grammar.exception;

public class GrammarParseException extends RuntimeException {
    public GrammarParseException(String message) {
        super(message);
    }

    public GrammarParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
