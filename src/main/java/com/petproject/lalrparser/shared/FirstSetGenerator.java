package com.petproject.lalrparser.shared;

import com.petproject.lalrparser.grammar.Grammar;
import com.petproject.lalrparser.grammar.token.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirstSetGenerator {
    public static Set<Token> generate(Token token, Grammar grammar) {
        return findFirstOf(token, grammar, new HashMap<>());
    }

    private static Set<Token> findFirstOf(Token token, Grammar grammar, Map<NonTerminal, Set<Token>> memory) {
        if (token instanceof Terminal t) {
            return Set.of(t);
        }

        if (token instanceof EpsilonToken t) {
            return Set.of(t);
        }

        if (token instanceof AcceptToken t) {
            return Set.of(t);
        }

        NonTerminal nt = (NonTerminal) token;

        memory.put(nt, new HashSet<>());

        var tokenRules = grammar.getRulesByKey(nt);

        Set<Token> fs = new HashSet<>();

        for (var r : tokenRules) {
            Set<Token> ruleSet = new HashSet<>();
            int epsilonValuesCounter = 0;
            for (var value : r.value()) {
                if (value.equals(token)) {
                    continue;
                }

                Set<Token> set = new HashSet<>();

                if (value instanceof NonTerminal && memory.containsKey(value) && !memory.get(value).isEmpty()) {
                    set.addAll(memory.get(value));
                } else {
                    set.addAll(findFirstOf(value, grammar, memory));
                }

                if (set.contains(grammar.epsilonToken())) {
                    epsilonValuesCounter++;
                }

                if (!set.contains(grammar.epsilonToken())) {
                    ruleSet.addAll(set);
                    break;
                } else if (set.size() > 1) {
                    set.remove(grammar.epsilonToken());
                }
                ruleSet.addAll(set);
            }

            if (epsilonValuesCounter == r.value().size()) {
                fs.add(grammar.epsilonToken());
            }

            fs.addAll(ruleSet);
        }

        log.debug("First of: {} : {}", token, fs);
        return fs;
    }
}