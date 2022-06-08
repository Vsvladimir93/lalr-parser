package com.petproject.lalrparser.collection;

import com.petproject.lalrparser.grammar.token.Token;

import java.util.List;

public record State(Token token, List<Item> items, Integer stateIndex, Item fromItem) {
}
