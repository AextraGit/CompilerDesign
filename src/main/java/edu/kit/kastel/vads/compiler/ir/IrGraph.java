package edu.kit.kastel.vads.compiler.ir;

import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;

public class IrGraph {
    private final Map<Node, SequencedSet<Node>> successors = new IdentityHashMap<>();
    private final Block startBlock;
    private final Block endBlock;
    private final String name;

    public IrGraph(String name) {
        this.name = name;
        this.startBlock = new Block(this);
        this.endBlock = new Block(this);
    }

    public void registerSuccessor(Node node, Node successor) {
        this.successors.computeIfAbsent(node, _ -> new LinkedHashSet<>()).add(successor);
    }

    public void removeSuccessor(Node node, Node oldSuccessor) {
        this.successors.computeIfAbsent(node, _ -> new LinkedHashSet<>()).remove(oldSuccessor);
    }

    /// {@return the set of nodes that have the given node as one of their inputs}
    public Set<Node> successors(Node node) {
        SequencedSet<Node> successors = this.successors.get(node);
        if (successors == null) {
            return Set.of();
        }
        return Set.copyOf(successors);
    }

    public Block startBlock() {
        return this.startBlock;
    }

    public Block endBlock() {
        return this.endBlock;
    }

    /// {@return the name of this graph}
    public String name() {
        return name;
    }

    @Override
    public String toString(){
        int counter = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("IR Representation for function ").append(this.name).append("\n");

        for(Map.Entry<Node, SequencedSet<Node>> entry : successors.entrySet()){
            if(entry.getKey().id == -1)
                entry.getKey().id = counter++;
            SequencedSet<Node> toSet = entry.getValue();
            for(Node ent : toSet){
                if(ent.id == -1) ent.id = counter++;
            }
        }

        for(Map.Entry<Node, SequencedSet<Node>> entry : successors.entrySet()){
            Node from = entry.getKey();
            SequencedSet<Node> toSet = entry.getValue();

            sb.append(" ").append(from.toString()).append(" -> ");

            if(toSet.isEmpty()){
                sb.append("∅");
            }else{
                sb.append(toSet.stream()
                    .map(Object::toString)
                    .reduce((a,b) -> a + ", " + b)
                    .orElse(""));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
