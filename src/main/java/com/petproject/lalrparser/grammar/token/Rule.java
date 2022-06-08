package com.petproject.lalrparser.grammar.token;

import java.util.List;

public record Rule(NonTerminal key, List<Token> value) {}
