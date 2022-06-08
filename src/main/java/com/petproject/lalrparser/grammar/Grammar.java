package com.petproject.lalrparser.grammar;

import com.petproject.lalrparser.grammar.exception.GrammarProcessingException;
import com.petproject.lalrparser.grammar.token.*;

import java.util.List;
import java.util.Optional;

public record Grammar(List<Terminal> terminals,
                      List<NonTerminal> nonTerminals,
                      List<Rule> rules,
                      Boolean isAugmented,
                      AcceptToken acceptToken,
                      EpsilonToken epsilonToken) {

    public Optional<Rule> getAugmentedRule() {
        if (isAugmented)
            return Optional.of(rules.get(0));

        return Optional.empty();
    }


    public Rule getPrimaryRule() {
        return rules.stream()
                .findFirst()
                .orElseThrow(
                        () -> new GrammarProcessingException("Can't get primary rule")
                );
    }

    public NonTerminal getEntryKey() {
        return rules.get(0).key();
    }

    public List<Rule> getRulesByKey(NonTerminal key) {
        return rules.stream()
                .filter(r -> r.key().equals(key))
                .toList();
    }

    public Grammar getSimpleGrammar() {
        if (!isAugmented)
            return this;

        var agToken = getAugmentedRule().orElseThrow().key();
        return new Grammar(terminals, nonTerminals.stream().filter(nt -> !nt.equals(agToken)).toList(),
                rules.stream().filter(r -> !r.key().equals(agToken)).toList(),
                false, acceptToken, epsilonToken);
    }
}
