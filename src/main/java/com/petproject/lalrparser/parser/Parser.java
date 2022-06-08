package com.petproject.lalrparser.parser;

import com.petproject.lalrparser.grammar.Node;

import java.util.List;

public interface Parser {
    List<Node> parse(String input);
}
