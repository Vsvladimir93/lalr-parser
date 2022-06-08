package com.petproject.lalrparser.parser.action;

import com.petproject.lalrparser.shared.Mapper;

public record ReduceAction(Integer index) implements Action {
    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return Mapper.writeValueAsString(this);
    }
}
