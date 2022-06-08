package com.petproject.lalrparser;

import com.petproject.lalrparser.collection.LRCCGenerator;
import com.petproject.lalrparser.collection.State;
import com.petproject.lalrparser.grammar.GrammarParser;
import com.petproject.lalrparser.grammar.token.RegExpTerminal;
import com.petproject.lalrparser.parser.LALRParser;
import com.petproject.lalrparser.parser.LALRParsingTable;
import com.petproject.lalrparser.parser.action.AcceptAction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class LALRParserApplication {

    public static void main(String[] args) {

//        String s =  "(111+1)";
//        var r = new RegExpTerminal(null, "NUMBERS", "\\d+");
//
//        String regStart = "^".concat(r.getRegexp());
//
//        Pattern p = Pattern.compile("^\\d*");
//
//        Matcher m = p.matcher(s);
//
//        System.out.println(m.find(0));
//
//       var s1 =  s.replaceFirst(p.pattern(), "");
//        System.out.println(s1);

        log.info("Upload grammar");
        var grammar = GrammarParser.parseGrammar("grammar");

        log.debug("Grammar: {}", grammar);

        List<State> canonicalCollection = LRCCGenerator.generate(grammar);

        printCanonicalCollection(canonicalCollection);

        var parsingTable = new LALRParsingTable(grammar, canonicalCollection);

        LALRParser parser = new LALRParser(grammar, parsingTable);

        parser.parse("ccccdcd");

    }

    private static void printCanonicalCollection(List<State> canonicalCollection) {
        canonicalCollection.forEach(state -> {
            log.info("State: {}", state.stateIndex());
            state.items().forEach(item -> {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < item.values().size(); i++) {
                    if (i == item.cursorIndex()) {
                        sb.append("@ ");
                    }
                    sb.append(item.values().get(i).getValue().concat(" "));
                }
                var str = sb.toString();

                if (!str.contains("@ ")) {
                    str = str.concat(" @");
                }
                log.info("\t- {} -> {}, {}", item.key(), str, item.lookaheads().toString());
            });
        });
    }

}
