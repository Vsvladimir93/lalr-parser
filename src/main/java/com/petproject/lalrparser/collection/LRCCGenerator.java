package com.petproject.lalrparser.collection;

import com.petproject.lalrparser.grammar.Grammar;
import com.petproject.lalrparser.grammar.token.NonTerminal;
import com.petproject.lalrparser.grammar.token.Token;
import com.petproject.lalrparser.shared.FirstSetGenerator;
import com.petproject.lalrparser.shared.Mapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
public final class LRCCGenerator {

    public static List<State> generate(Grammar grammar) {
        var initialState = generateInitialState(grammar);
        var nextIndex = new AtomicInteger(initialState.stateIndex() + 1);

        List<State> collection = new ArrayList<>();
        collection.add(initialState);

        var processedStates = new HashSet<Integer>();
        processedStates.add(initialState.stateIndex());

        populateCollectionWithGoToOnState(initialState, collection, grammar, nextIndex);

        var hasUnprocessedStates = true;
        Predicate<State> unprocessedState = l -> !processedStates.contains(l.stateIndex());

        do {
            var newStates = collection.stream()
                    .filter(unprocessedState)
                    .toList();

            for (var state : newStates) {
                processedStates.add(state.stateIndex());
                populateCollectionWithGoToOnState(state, collection, grammar, nextIndex);
            }

            hasUnprocessedStates = hasUnprocessedStates(collection, processedStates);
        } while (hasUnprocessedStates);

        log.debug("Processed states: {}", processedStates);

        log.debug("States size: {}", collection.size());

        log.debug("States: {}", Mapper.writeValueAsString(collection.stream()
                .collect(Collectors.toMap(State::stateIndex, s -> s))));

        return collection;
    }

    private static State generateInitialState(Grammar grammar) {
        List<Item> items = new ArrayList<>();
        var r = grammar.getPrimaryRule();
        items.add(new Item(r.key(), r.value(), Set.of(grammar.acceptToken()), 0));

        var s = new State(grammar.getPrimaryRule().key(), items, 0, items.get(0));
        return closure(s, grammar);
    }

    private static Set<Token> generateLookaheads(Item item, Grammar grammar) {
        Set<Token> lookaheads;
        if (item.values().size() > item.cursorIndex() + 1) {
            var nextToken = item.values().get(item.cursorIndex() + 1);
            lookaheads = FirstSetGenerator.generate(nextToken, grammar);
        } else {
            lookaheads = item.lookaheads();
        }
        return lookaheads;
    }


    private static boolean hasUnprocessedStates(List<State> collection, Set<Integer> processedStates) {
        Predicate<State> unprocessedState = l -> !processedStates.contains(l.stateIndex());
        return collection.stream().anyMatch(unprocessedState);
    }

    private static void populateCollectionWithGoToOnState(
            State state, List<State> collection, Grammar grammar, AtomicInteger nextIndex) {
        log.debug("GoTo for state with number: {} new state number: {}", state.stateIndex(), nextIndex.get() + 1);

        var alreadyProcessedTokens = new HashSet<Token>();

        for (var item : state.items()) {
            if (collection.subList(1, collection.size())
                    .stream()
                    .anyMatch(s ->
                            s.fromItem().getTokensAroundCursor()
                    .equals(item.getTokensAroundCursor())
                            && s.fromItem().lookaheads().equals(item.lookaheads())
                    )) {
                log.warn("Item already calculated: {}", item);
                continue;
            }

//            if (collection.stream().anyMatch(s -> s.fromItem().equals(item))) {
//                log.warn("Item already calculated: {}", item);
//                continue;
//            }

            if (item.getTokenAfterCursor().isEmpty()) {
                log.warn("Token after cursor isEmpty for initial items! Item: {}", item);
                continue;
            }

            var tokenAfterCursor = item.getTokenAfterCursor().get();

            if (alreadyProcessedTokens.contains(tokenAfterCursor)) {
                log.debug("Item with token: {} already processed for state number: {}",
                        tokenAfterCursor, state.stateIndex());
                continue;
            }

            var newState = goTo(
                    nextIndex.getAndIncrement(),
                    tokenAfterCursor,
                    state, item);

            newState = closure(newState, grammar);

            collection.add(newState);
            alreadyProcessedTokens.add(tokenAfterCursor);
        }
    }


    private static State closure(State state, Grammar grammar) {
        var items = new ArrayList<>(state.items());

        // If has NonTerminal after cursor - add items for them
        var additionalItems = findAdditionalItems(items, Collections.emptyList(), grammar);

        // Repeat while have new NonTerminals after cursor
        while (!additionalItems.isEmpty()) {
            items.addAll(additionalItems);

            additionalItems = findAdditionalItems(additionalItems, items, grammar);
        }

        return new State(state.token(), items, state.stateIndex(), state.fromItem());
    }

    private static State goTo(Integer nextIndex, Token token, State state, Item fromItem) {
        // Get all items from state where the "token" is followed by the cursor
        // Shift cursor by one position to the right
        // Return new state

        var itemsWhereTokenFollowedByCursor = state.items().stream()
                .filter(item -> item.getTokenAfterCursor().isPresent())
                .filter(item -> item.getTokenAfterCursor().get().equals(token))
                .map(Item::shiftCursor)
                .toList();
        log.debug("Check item number : {} it has : {} items where token followed by the cursor",
                state.stateIndex(), itemsWhereTokenFollowedByCursor.size());
        return new State(token, itemsWhereTokenFollowedByCursor, nextIndex, fromItem);
    }

    private static List<Item> findAdditionalItems(List<Item> items, List<Item> except, Grammar grammar) {
        Predicate<Item> notPresentIn = r -> except.stream().noneMatch(ex -> ex.equals(r));

        Predicate<Item> nonTerminalAfterCursor =
                i -> i.getTokenAfterCursor().isPresent() && i.getTokenAfterCursor().get() instanceof NonTerminal;

        var itemsWithNonTerminalsAfterCursor = items.stream()
                .filter(nonTerminalAfterCursor)
//                .filter(notPresentIn)
                .toList();

        List<Item> newItems = new ArrayList<>();

        for (var item : itemsWithNonTerminalsAfterCursor) {
            var nt = (NonTerminal) item.getTokenAfterCursor().orElseThrow();
            var grammarRulesByKey = grammar.getRulesByKey(nt);

            grammarRulesByKey.forEach(r -> newItems.add(new Item(nt, r.value(), generateLookaheads(item, grammar), 0)));
        }

        return newItems.stream().filter(notPresentIn).toList();
    }
}
