package com.petproject.lalrparser.shared;

import com.petproject.lalrparser.grammar.Grammar;
import com.petproject.lalrparser.grammar.token.Rule;
import com.petproject.lalrparser.grammar.token.Token;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FollowSetGenerator {
    public static Set<Token> generate(Token token, Grammar grammar) {
        return findFollowOf(token, grammar, new ArrayList<>());
    }

    private static Set<Token> findFollowOf(Token token, Grammar grammar, List<Rule> except) {
        Set<Token> followSet = new HashSet<>();

        if (grammar.getEntryKey().equals(token)) {
            followSet.add(grammar.acceptToken());
        }

        /* Find all rules where 'token' is present on Right-Hand Side of rule */
        var rulesWithRHStoken = grammar.rules().stream()
                .filter(r -> r.value().stream().anyMatch(t -> t.equals(token)))
                .toList();

        for (var r : rulesWithRHStoken) {
            int index = 0;
            if (except.contains(r)) {
                continue;
            }
            for (var t : r.value()) {
                if (t.equals(token)) {
                    /* If token B is last in rule like A -> aB we should search for FollowOf(A) */
                    except.add(r);
                    if (index + 1 == r.value().size()) {
                        followSet.addAll(findFollowOf(r.key(), grammar, except));
                    } else {
                        var nextToken = r.value().get(index + 1);
                        var firstSet = FirstSetGenerator.generate(nextToken, grammar);
                        followSet.addAll(firstSet);
                        if (firstSet.contains(grammar.epsilonToken())) {
                            if (!nextToken.equals(t)) {
                                followSet.addAll(findFollowOf(nextToken, grammar, except));
                            }
                            followSet.remove(grammar.epsilonToken());
                        }
                    }
                }
                index++;
            }
        }

        log.debug("Follow of: {} : {}", token, followSet);
        return followSet;
    }
}
