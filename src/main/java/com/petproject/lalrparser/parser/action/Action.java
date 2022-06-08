package com.petproject.lalrparser.parser.action;

import com.petproject.lalrparser.shared.Mapper;

public interface Action {
    int getIndex();

    default String getType() {
        return this.getClass().getSimpleName();
    }
}
