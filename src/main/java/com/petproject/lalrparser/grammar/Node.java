package com.petproject.lalrparser.grammar;

import com.petproject.lalrparser.grammar.token.Rule;
import com.petproject.lalrparser.grammar.token.Token;
import com.petproject.lalrparser.parser.LALRParser;

import java.util.List;

public record Node(Rule rule, List<String> tokens) {
}
