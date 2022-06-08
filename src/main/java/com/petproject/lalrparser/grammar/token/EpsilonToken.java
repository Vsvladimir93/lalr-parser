package com.petproject.lalrparser.grammar.token;

public record EpsilonToken(String value) implements Token {
    @Override
    public String getValue() {
        return this.value;
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
