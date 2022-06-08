package com.petproject.lalrparser.grammar.token;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
public class Terminal implements Token {

    private String value;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Terminal terminal)) {
            return false;
        }
        return getValue().equals(terminal.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
