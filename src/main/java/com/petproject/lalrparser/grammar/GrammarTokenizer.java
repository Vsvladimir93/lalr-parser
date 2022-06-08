package com.petproject.lalrparser.grammar;

import com.petproject.lalrparser.grammar.exception.GrammarParseException;
import com.petproject.lalrparser.grammar.token.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
class GrammarTokenizer {

    private static final String KEY_VALUE_SEPARATOR = " : ";
    private static final String RULE_KEY_SEPARATOR = "->";
    private static final String RULE_VALUE_SEPARATOR = "|";

    enum TokenType {Terminals, RegexpTerminal, NonTerminals, Rule, Epsilon}

    public Grammar parseGrammar(List<String> grammarDefinitions) {
        log.debug("Parse grammar from definitions {}", grammarDefinitions);

        var terminals = parseTerminals(grammarDefinitions);
        var nonTerminals = parseNonTerminals(grammarDefinitions);
        var epsilon = parseEpsilon(grammarDefinitions);
        var rules = parseRules(grammarDefinitions, terminals, nonTerminals, epsilon);


        log.debug("Terminals: {}", terminals);
        log.debug("NonTerminals: {}", nonTerminals);
        log.debug("Rules: {}", rules);

        var grammar = new Grammar(terminals, nonTerminals, rules, false, new AcceptToken("$"),
                epsilon);

        log.debug("Augment grammar");

        return augmentGrammar(grammar);
    }

    private EpsilonToken parseEpsilon(List<String> grammarDefinitions) {
        return grammarDefinitions.stream()
                .filter(d -> d.startsWith(TokenType.Epsilon.name()))
                .map(d -> d.split(KEY_VALUE_SEPARATOR)[1].trim())
                .map(EpsilonToken::new)
                .findFirst()
                .orElseThrow();
    }

    private List<Terminal> parseTerminals(List<String> grammarDefinitions) {
        var terminal = parseToken(grammarDefinitions, TokenType.Terminals, Terminal.class);
        var regExpTerminals = parseRegExpTerminals(grammarDefinitions);
        System.out.println(regExpTerminals);
        return Stream.concat(terminal.stream(), regExpTerminals.stream()).toList();

    }

    private List<NonTerminal> parseNonTerminals(List<String> grammarDefinitions) {
        return parseToken(grammarDefinitions, TokenType.NonTerminals, NonTerminal.class);
    }

    private List<Rule> parseRules(List<String> grammarDefinitions, List<Terminal> terminals, List<NonTerminal> nonTerminals,
                                  EpsilonToken epsilon) {
        return grammarDefinitions.stream()
                .filter(d -> d.startsWith(TokenType.Rule.name()))
                .map(d -> d.split(KEY_VALUE_SEPARATOR)[1])
                .map(r -> r.split(RULE_KEY_SEPARATOR))
                .map(r -> parseRule(r, terminals, nonTerminals, epsilon))
                .toList();
    }

    private List<RegExpTerminal> parseRegExpTerminals(List<String> grammarDefinitions) {
        return grammarDefinitions.stream()
                .filter(d -> d.startsWith(TokenType.RegexpTerminal.name()))
                .map(d -> d.split(KEY_VALUE_SEPARATOR)[1])
                .map(r -> r.split(RULE_KEY_SEPARATOR))
                .map(r -> new RegExpTerminal(null, r[0], r[1].trim()))
                .toList();
    }

    private <T extends Token> List<T> parseToken(List<String> grammarDefinitions, TokenType tokenType, Class<T> clazz) {
        return grammarDefinitions.stream()
                .filter(d -> d.startsWith(tokenType.name()))
                .map(d -> d.split(KEY_VALUE_SEPARATOR)[1].split(" "))
                .map(d -> Arrays.stream(d).toList())
                .flatMap(Collection::stream)
                .map(t -> {
                    try {
                        return clazz.getConstructor(String.class).newInstance(t);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Rule parseRule(String[] ruleTuple, List<Terminal> terminals, List<NonTerminal> nonTerminals, EpsilonToken epsilon) {
        var ruleValue = new StringBuilder(ruleTuple[1].replace(" ", ""));
        List<Token> tokens = new ArrayList<>();

        Predicate<Token> nonEmptyToken = t -> t.getValue() != null && !t.getValue().isBlank();

        terminals = terminals.stream().filter(nonEmptyToken).toList();
        nonTerminals = nonTerminals.stream().filter(nonEmptyToken).toList();

        tokens.add(epsilon);
        tokens.addAll(terminals);
        tokens.addAll(nonTerminals);

        System.out.println(tokens);

        List<Token> result = new ArrayList<>();

        while (ruleValue.length() > 0) {
            var token = tokens.stream()
                    .filter(t -> ruleValue.toString().startsWith(t.getValue()))
                    .findFirst();

            if (token.isEmpty()) {
                throw new GrammarParseException(
                        "Can't parse rule: " + Arrays.toString(ruleTuple) + " Can't find token " + ruleValue);
            }

            result.add(token.get());

            ruleValue.delete(0, token.get().getValue().length());
        }

        return new Rule(new NonTerminal(ruleTuple[0]), result);
    }

    public static Grammar augmentGrammar(Grammar grammar) {
        var primaryRule = grammar.rules().stream().findFirst();

        if (primaryRule.isEmpty()) {
            throw new GrammarParseException("Can't augment grammar. Rules is empty.");
        }

        var primaryKey = primaryRule.get().key();
        var augmentedPrimaryKey = new NonTerminal(primaryKey.value().concat("'"));

        List<NonTerminal> augmentedKeys = new ArrayList<>();
        augmentedKeys.add(augmentedPrimaryKey);
        augmentedKeys.addAll(grammar.nonTerminals());

        var rule = new Rule(augmentedPrimaryKey, List.of(primaryKey));
        List<Rule> augmentedRules = new ArrayList<>();
        augmentedRules.add(rule);
        augmentedRules.addAll(grammar.rules());

        return new Grammar(grammar.terminals(), augmentedKeys, augmentedRules, true, grammar.acceptToken(),
                grammar.epsilonToken());
    }
}
