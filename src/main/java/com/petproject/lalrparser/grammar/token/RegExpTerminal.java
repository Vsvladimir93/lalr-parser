package com.petproject.lalrparser.grammar.token;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petproject.lalrparser.shared.Mapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class RegExpTerminal extends Terminal {

    private String dynamicValue;
    private String regexp;

    public RegExpTerminal(String dynamicValue, String value, String regexp) {
        super(value);
        this.dynamicValue = dynamicValue;
        this.regexp = regexp;
    }

    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    public String toString() {
        return super.getValue();
    }

    public String toJson() {
        return Mapper.writeValueAsString(this);
    }

    @Override
    @JsonIgnore
    public Integer getValueLength() {
        return dynamicValue.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RegExpTerminal terminal)) {
            return false;
        }
        return getValue().equals(terminal.getValue())
//                && getDynamicValue().equals(terminal.getDynamicValue())
                && getRegexp().equals(terminal.getRegexp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getRegexp());
    }
}
