package com.petproject.lalrparser.shared;

import com.petproject.lalrparser.grammar.token.RegExpTerminal;
import com.petproject.lalrparser.grammar.token.Terminal;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regexp {
    public static Optional<RegExpTerminal> matchesStart(String s, List<Terminal> terminals) {
        return terminals.stream()
                .filter(RegExpTerminal.class::isInstance)
                .map(RegExpTerminal.class::cast)
                .filter(r -> {
                    Pattern p = Pattern.compile("^" + ((RegExpTerminal) r).getRegexp());
                    Matcher m = p.matcher(s);
                    return m.find(0);
                })
                .findFirst();
    }
}
