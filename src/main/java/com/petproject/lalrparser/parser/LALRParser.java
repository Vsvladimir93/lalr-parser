package com.petproject.lalrparser.parser;

import com.petproject.lalrparser.grammar.Grammar;
import com.petproject.lalrparser.grammar.Node;
import com.petproject.lalrparser.grammar.token.NonTerminal;
import com.petproject.lalrparser.grammar.token.RegExpTerminal;
import com.petproject.lalrparser.grammar.token.Rule;
import com.petproject.lalrparser.grammar.token.Token;
import com.petproject.lalrparser.parser.action.AcceptAction;
import com.petproject.lalrparser.parser.action.Action;
import com.petproject.lalrparser.parser.action.ReduceAction;
import com.petproject.lalrparser.parser.action.ShiftAction;
import com.petproject.lalrparser.shared.Mapper;
import com.petproject.lalrparser.shared.Regexp;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class LALRParser implements Parser {

    private final Grammar grammar;
    private final LALRParsingTable table;

    @Override
    public List<Node> parse(String input) {
        var tokens = Lexer.splitByTokens(input, grammar);
        var pointer = 0;
        log.info("lexer. split input by tokens: {}", tokens);

        Deque<Object> stack = new ArrayDeque<>();
        Queue<Token> inputTokens = new LinkedList<>(tokens);
        inputTokens.add(grammar.acceptToken());

        List<Node> output = new ArrayList<>();

        log.debug("Start parsing");
        stack.push(0);
        while (true) {
            int state = (int) stack.peek();
            Action action = table.actionTable.get(inputTokens.peek())[state];
//            log.info("Action: {}", action);
            if (action == null) {
                System.out.println(input);
                log.error("Input is incorrect. Error at position: {} of input string: {}", pointer,
                        new StringBuilder(input).insert(pointer, "!"));
                break;
            }

//            log.debug("{} for state: {} token: {}", action.getType(), state, inputTokens.peek());

            if (action instanceof ShiftAction) {
                var token = inputTokens.remove();
                pointer += token.getValueLength();
                stack.push(token);
                stack.push(action.getIndex());
            } else if (action instanceof ReduceAction) {
                Rule rule = grammar.rules().get(action.getIndex());
                List<String> outTokens = new ArrayList<>();
                for (int i = 0; i < rule.value().size() * 2; i++) {
                    var out = stack.pop();
                    if (out instanceof Token token) {
                        outTokens.add(Mapper.writeValueAsString(token));
                    }
                }
                output.add(new Node(rule, outTokens));
                int prevState = (int) stack.peek();
                stack.push(rule.key());
                stack.push(table.goToTable.get(rule.key())[prevState]);
            } else if (action instanceof AcceptAction) {
                log.info("Input accepted");
                break;
            }
        }

        log.debug("Output: {}", output);

        return output;
    }

    @AllArgsConstructor
    public class Pair<F, S> {
        public F token;
        public S second;

        @Override
        public String toString() {
            return Mapper.writeValueAsString(this);
        }
    }

//    public class Output {
//        F first;
//        S second;
//
//        @Override
//        public String toString() {
//            return Mapper.writeValueAsString(this);
//        }
//    }

}
