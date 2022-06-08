package com.petproject.lalrparser.collection;

import com.petproject.lalrparser.grammar.token.NonTerminal;
import com.petproject.lalrparser.grammar.token.Token;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record Item(NonTerminal key, List<Token> values, Set<Token> lookaheads, Integer cursorIndex) {
    public Optional<Token> getTokenAfterCursor() {
        if (cursorIndex >= values.size())
            return Optional.empty();

        return Optional.of(values.get(cursorIndex));
    }

//    public static Item ruleToItem(Rule rule) {
//        return new Item(rule.key(), rule.value(), 0);
//    }

    public static Item shiftCursor(Item item) {
        return new Item(item.key, item.values, item.lookaheads,item.cursorIndex + 1);
    }

    public List<Token> getTokensAroundCursor() {
        var end = cursorIndex + 1;

        if (end >= values.size())
            end = values.size();

        return values.subList(0, end);
    }
}
