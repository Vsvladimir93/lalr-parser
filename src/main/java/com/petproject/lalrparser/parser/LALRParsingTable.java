package com.petproject.lalrparser.parser;

import com.petproject.lalrparser.grammar.Grammar;
import com.petproject.lalrparser.grammar.token.AcceptToken;
import com.petproject.lalrparser.grammar.token.NonTerminal;
import com.petproject.lalrparser.grammar.token.Terminal;
import com.petproject.lalrparser.grammar.token.Token;
import com.petproject.lalrparser.collection.State;
import com.petproject.lalrparser.collection.Item;
import com.petproject.lalrparser.parser.action.*;
import com.petproject.lalrparser.parser.exception.ReduceReduceConflictException;
import com.petproject.lalrparser.parser.exception.ShiftReduceConflictException;
import com.petproject.lalrparser.shared.FollowSetGenerator;
import com.petproject.lalrparser.shared.Mapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Slf4j
public class LALRParsingTable {

    public static final AcceptToken acceptToken = new AcceptToken("$");

    public Map<Token, Action[]> actionTable = new HashMap<>();

    public Map<NonTerminal, Integer[]> goToTable = new HashMap<>();

    public LALRParsingTable(Grammar grammar, List<State> canonicalCollection) {
        init(grammar, canonicalCollection);
        log.info("Initiated :{} ", actionTable.size());

        log.info("Initiated :{} ", Mapper.writeValueAsString(actionTable));
    }

    private void init(Grammar grammar, List<State> canonicalCollection) {
        var size = canonicalCollection.size();
        grammar.terminals().forEach(t -> actionTable.put(t, new Action[size + 1]));
        actionTable.put(acceptToken, new Action[size + 1]);
        grammar.nonTerminals().forEach(nt -> goToTable.put(nt, new Integer[size + 1]));

        fillShiftActionTable(canonicalCollection);
        fillGoToTable(canonicalCollection);
        fillReduceActionTable(canonicalCollection, grammar);
    }

    /**
     * To fill action table with SHIFT action we need to find a Terminals that followed by the cursor in CC
     *
     * @param cc Canonical Collection of LR(0) items
     */
    private void fillShiftActionTable(List<State> cc) {
        cc.forEach(state -> {
            for (var item : state.items()) {
                if (item.getTokenAfterCursor().isEmpty())
                    continue;

                var token = item.getTokenAfterCursor().get();
                if (token instanceof Terminal) {
                    var shiftReference = resolve(item, cc);

                    if (shiftReference.isEmpty()) {
                        log.error("Can't find SHIFT TO item for state: {} item: {} token: {}", state, item, token);
                        continue;
                    }

                    actionTable.get(token)[state.stateIndex()] = new ShiftAction(shiftReference.get().stateIndex());
                }
            }
        });
    }

    /**
     * To fill action table with REDUCE action we need to find a Terminals that followed by the cursor in CC
     *
     * @param cc Canonical Collection of LR(0) items
     */
    private void fillReduceActionTable(List<State> cc, Grammar grammar) {
        final NonTerminal augmentedKey = grammar.getAugmentedRule().orElseThrow().key();
        log.debug("Start to fill reduce action table");

        cc.forEach(state -> {
            for (var item : state.items()) {
                if (item.getTokenAfterCursor().isEmpty()) {
                    if (item.key().equals(augmentedKey)) {
                        checkShiftReduceConflict(acceptToken, state.stateIndex());
                        checkReduceReduceConflict(acceptToken, state.stateIndex());
                        actionTable.get(acceptToken)[state.stateIndex()] = new AcceptAction(state.stateIndex());
                    } else {
                        var set = FollowSetGenerator.generate(item.key(), grammar);

                        set.forEach(token -> {
                            try {
                                var ruleIndex = findRuleIndexByItem(item, grammar);
                                checkShiftReduceConflict(token, state.stateIndex());
                                checkReduceReduceConflict(token, state.stateIndex());
                                actionTable.get(token)[state.stateIndex()] = new ReduceAction(ruleIndex);
                            } catch (Exception e) {
                                log.error("", e);
                            }
                        });
                    }
                }
            }
        });
    }

    private void checkShiftReduceConflict(Token token, Integer index) {
        if (actionTable.get(token)[index] != null && actionTable.get(token)[index] instanceof ShiftAction) {
            throw new ShiftReduceConflictException(format("SHIFT REDUCE conflict on %s token at %d index.",
                    token.getValue(), index));
        }
    }

    private void checkReduceReduceConflict(Token token, Integer index) {
        if (actionTable.get(token)[index] != null && actionTable.get(token)[index] instanceof ReduceAction) {
            throw new ReduceReduceConflictException(format("REDUCE REDUCE conflict on %s token at %d index.",
                    token.getValue(), index));
        }
    }

    private int findRuleIndexByItem(Item item, Grammar grammar) {
        int increment = 0;
        for (var rule : grammar.rules()) {
            if (rule.key().equals(item.key()) && rule.value().equals(item.values())) {
                return increment;
            }
            increment++;
        }
        return -1;
    }

    /**
     * To fill go to table we need to find a NonTerminals that followed by the cursor in CC
     *
     * @param cc Canonical Collection of LR(0) items
     */
    private void fillGoToTable(List<State> cc) {
        cc.forEach(state -> {
            for (var item : state.items()) {
                if (item.getTokenAfterCursor().isEmpty())
                    continue;

                var token = item.getTokenAfterCursor().get();
                if (token instanceof NonTerminal) {
                    var goToReference = resolve(item, cc);
                    if (goToReference.isEmpty()) {
                        log.error("Can't find GO TO item for state: {} item: {} token: {}", state, item, token);
                        continue;
                    }

                    goToTable.get(token)[state.stateIndex()] = goToReference.get().stateIndex();
                }
            }
        });
    }

    private Optional<State> resolve(Item item, List<State> cc) {
        var itemWithShiftedCursor = Item.shiftCursor(item);
        return cc.stream().filter(state -> state.items().get(0).equals(itemWithShiftedCursor)).findFirst();
    }
}
