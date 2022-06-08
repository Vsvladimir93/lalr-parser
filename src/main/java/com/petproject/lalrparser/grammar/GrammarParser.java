package com.petproject.lalrparser.grammar;

import com.petproject.lalrparser.grammar.exception.GrammarParseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GrammarParser {

    public static Grammar parseGrammar(List<String> grammarDefinitions) {
        return new GrammarTokenizer().parseGrammar(grammarDefinitions);
    }

    /**
     * @param grammarFilePath - absolute path to grammar file
     * @return Grammar
     */
    public static Grammar parseGrammar(Path grammarFilePath) {
        var grammarFile = grammarFilePath.toFile();
        if (!grammarFile.exists())
            throw new GrammarParseException("Can't find grammar file by path: " + grammarFilePath);

        try (Stream<String> stream = Files.lines(grammarFilePath)) {
            return parseGrammar(stream.collect(Collectors.toList()));
        } catch (IOException e) {
            throw new GrammarParseException("Can't parse grammar.", e);
        }
    }

    public static Grammar parseGrammar(String resourceFileName) {
        var is = GrammarParser.class.getClassLoader()
                .getResourceAsStream(resourceFileName);
        if (is == null)
            throw new GrammarParseException("Can't find resource file with name: " + resourceFileName);

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        try (Stream<String> stream = reader.lines()) {
            return parseGrammar(stream.collect(Collectors.toList()));
        }
    }
}
