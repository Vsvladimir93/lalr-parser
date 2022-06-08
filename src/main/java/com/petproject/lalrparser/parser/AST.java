package com.petproject.lalrparser.parser;

import com.petproject.lalrparser.grammar.Node;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AST {

    public ASTNode node;

    public AST(List<Node> nodes) {

    }

    private ASTNode mergeNodes(List<Node> nodes) {
        Collections.reverse(nodes);

        ASTNode node = new ASTNode(nodes.get(0).rule().key().getValue(), new ArrayList<>());

        for (var n : nodes.subList(1, nodes.size())) {

        }

        return null;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ASTNode {
        public String type;
        public List<ASTNode> values;
    }
}
