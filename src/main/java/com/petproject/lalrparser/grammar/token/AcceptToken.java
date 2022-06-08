package com.petproject.lalrparser.grammar.token;

public record AcceptToken(String value) implements Token {
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public Integer getValueLength() {
        return value.length();
    }
}
