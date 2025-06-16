package edu.kit.kastel.vads.compiler.backend.x86asm;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.LivenessAnalysis.LivenessAnalyzer;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;

public class AsmRegisterAllocator implements RegisterAllocator {
    private boolean firstRun = true;
    private final Queue<Register> freeRegisters = new ArrayDeque<>();
    private final Map<Node, Register> registers = new HashMap<>();
    private final LivenessAnalyzer anal = new LivenessAnalyzer();

    @Override
    public Map<Node, Register> allocateRegisters(IrGraph graph) {
        if(firstRun)prepare();
        Set<Node> visited = new HashSet<>();
        visited.add(graph.endBlock());
        anal.analyze(graph);
        scan(graph.endBlock(), visited);
        return this.registers != null ? Map.copyOf(this.registers) : null;
    }

    private void scan(Node node, Set<Node> visited) {
        checkRegisters(anal.liveOut(node));
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited);
            }
        }
        if (!needsRegister(node))
            return;

        if (node instanceof ConstIntNode constNode) {
            for (Map.Entry<Node, Register> entry : registers.entrySet()) {
                if (entry.getKey() instanceof ConstIntNode other &&
                    other.value() == constNode.value()) {
                    registers.put(node, entry.getValue());
                    return;
                }
            }
        }

        registers.put(node, getRegister());
    }

    private Register getRegister(){
        if(!freeRegisters.isEmpty()){
            return freeRegisters.remove();
        }else{
            return null; // TODO: spill
        }
    }

    private void checkRegisters(Set<Node> nodes){
        if(nodes == null)return;
        for(Map.Entry<Node, Register> entry : registers.entrySet()){
            if(!nodes.contains(entry.getKey())){
                freeRegister(entry.getKey());
            }
        }
    }

    private void freeRegister(Node node){
        freeRegisters.add(registers.get(node));
        registers.remove(node);
        System.out.println("Yay it spilt (removed one)");
    }

    private static boolean needsRegister(Node node) {
        return !(node instanceof ProjNode || node instanceof StartNode || node instanceof Block || node instanceof ReturnNode);
    }

    private void prepare(){
        for(int i = 0; i < PhysicalRegister.getMaxAmount(); i++){
            this.freeRegisters.add(new PhysicalRegister(i));
        }
    }
}
