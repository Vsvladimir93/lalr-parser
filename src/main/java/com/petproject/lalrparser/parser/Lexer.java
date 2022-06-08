package com.petproject.lalrparser.parser;

import com.petproject.lalrparser.grammar.Grammar;
import com.petproject.lalrparser.grammar.token.RegExpTerminal;
import com.petproject.lalrparser.grammar.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class Lexer {
    public static List<Token> splitByTokens(String input, Grammar grammar) {
        var sb = new StringBuilder(input.replaceAll(" ", ""));
        List<Token> tokens = new ArrayList<>();
        var position = 0;

        var terminalAccepted = false;
        while (!sb.isEmpty()) {
            terminalAccepted = false;
            for (var terminal : grammar.terminals()) {
                // Check for RegExp
                if (terminal instanceof RegExpTerminal rt) {
                    // Match start of the string
                    Pattern p = Pattern.compile("^" + rt.getRegexp());
                    Matcher m = p.matcher(sb.toString());
                    // Matches regexp
                    if (m.find(0)) {
                        var substr = sb.toString().replaceFirst(p.pattern(), "");
                        var diffLength = sb.length() - substr.length();
                        var dynamicValue = sb.substring(0, diffLength);
                        tokens.add(new RegExpTerminal(dynamicValue, rt.getValue(), rt.getRegexp()));
                        sb.delete(0, diffLength);
                        position += diffLength;
                        terminalAccepted = true;
                        break;
                    }
                } else if (sb.toString().startsWith(terminal.getValue())) {
                    tokens.add(terminal);
                    sb.delete(0, terminal.getValue().length());
                    position += terminal.getValue().length();
                    terminalAccepted = true;
                    break;
                }


            }

            if (!terminalAccepted) {
                throw new RuntimeException(format("Can't split input by tokens. Position: %d Rest of the input: %s",
                        position, sb));
            }
        }

        return tokens;
    }
}
