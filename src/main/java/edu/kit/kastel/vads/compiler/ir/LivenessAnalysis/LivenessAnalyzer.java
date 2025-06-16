package edu.kit.kastel.vads.compiler.ir.LivenessAnalysis;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;

public class LivenessAnalyzer {
    private final Map<Node, Set<Node>> liveIn = new IdentityHashMap<>();
    private final Map<Node, Set<Node>> liveOut = new IdentityHashMap<>();
    private final Map<Node, Set<Node>> gen = new IdentityHashMap<>();
    private final Map<Node, Set<Node>> kill = new IdentityHashMap<>();

    public void analyze(IrGraph graph){
        generateKillGen(graph.endBlock());
        liveOut.put(graph.endBlock(), null);
        generateLive(graph);
    }

    //theoretisch kann man hier visited löschen, könnte sein, dass das mehr performance gönnt

    private boolean generateLiveOut(IrGraph graph){
        boolean hasChanged = false;
        Node startNode = graph.endBlock();
        Queue<Node> worklist = new ArrayDeque<>();
        Set<Node> visited = new HashSet<>();
        worklist.add(startNode);
        do{
            Node currentNode = worklist.remove();
            if(!(currentNode instanceof BinaryOperationNode || currentNode instanceof ConstIntNode)) continue;
            Set<Node> outSet = new HashSet<>();
            if(!visited.add(currentNode)) continue;
            
            for(Node node : currentNode.predecessors()){
                worklist.add(node);
            }
            
            for(Node succ : graph.successors(currentNode)){
                if(outSet.addAll(liveIn.get(succ))){
                    worklist.addAll(currentNode.predecessors());
                    hasChanged = true;
                }
            }

            Set<Node> inSet = gen.get(currentNode);
            if(inSet.addAll(
                outSet.stream()
                            .filter(n -> ! kill.get(currentNode).contains(n))
                            .collect(Collectors.toSet())
            )){
                worklist.addAll(currentNode.predecessors());
                hasChanged = true;
            }

            liveIn.put(currentNode, inSet);
            liveOut.put(currentNode, outSet);
        }while(worklist.peek() != null);
        return hasChanged;
    }

    private void generateLive(IrGraph graph){
        while(generateLiveOut(graph)){}
    }

    private void generateKillGen(Node startNode){
        Queue<Node> worklist = new ArrayDeque<>();
        Set<Node> visited = new HashSet<>();
        worklist.add(startNode);
        do{
            Set<Node> genSet = new HashSet<>();
            Set<Node> killSet = new HashSet<>();
            Node currentNode = worklist.remove();
            if(!visited.add(currentNode)) continue;
            
            for(Node node : currentNode.predecessors()){
                worklist.add(node);
                if(node instanceof BinaryOperationNode || node instanceof ConstIntNode)
                    genSet.add(node);
            }

            if(currentNode instanceof BinaryOperationNode || currentNode instanceof ConstIntNode)
                killSet.add(currentNode);

            gen.put(currentNode, genSet);
            kill.put(currentNode, killSet);
        }while(worklist.peek() != null);
    }

    private Set<Node> toSet(List<? extends Node> nodes){
        Set<Node> nodeSet = new HashSet<>();
        for(Node nude : nodes){
            nodeSet.add(nude);
        }
        return nodeSet;
    }

    public Set<Node> liveOut(Node node){
        return liveOut.get(node);
    }
}
