package edu.kit.kastel.vads.compiler.backend.aasm;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;
import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class CodeGenerator {

    public String generateCodeFr(List<IrGraph> program){
        StringBuilder builder = new StringBuilder();
        for(IrGraph graph : program) {              //Zuerst hier die .global direktiven in die .asm einfügen
            builder.append(".global ")
                .append(graph.name())
                .append("\n");
        }
        builder.append(".text")
            .repeat("\n", 2);                    //dann die .text direktive

        builder.append("main: \n")
                .append("call _main \n")
                .append("movq %rax, %rdi \n")
                .append("movq $0x3C, %rax \n")
                .append("syscall")
                .repeat("\n", 2);

        for(IrGraph graph : program){               //dann weiter mit den Routinendefs
            AasmRegisterAllocator allocator = new AasmRegisterAllocator();
            Map<Node, Register> registers = allocator.allocateRegisters(graph);
            builder.append("_")
                .append(graph.name())
                .append(": \n");
            
            

            generateForGraphSkibidi(graph, builder, registers);
        }
        return builder.toString();
    }

    private void generateForGraphSkibidi(IrGraph graph, StringBuilder builder, Map<Node, Register> registers){
        Set<Node> visited = new HashSet<>();                                                                        //actually keine Ahnung was wir hier machen omega
        skibidiScan(graph.endBlock(), visited, builder, registers);
    }

    private void skibidiScan(Node node, Set<Node> visited, StringBuilder builder, Map<Node, Register> registers){
        for (Node predecessor : node.predecessors()){
            if (visited.add(predecessor)){                                                                          //TODO: Vielleicht mal anschauen was das visitor pattern ist
                skibidiScan(predecessor, visited, builder, registers);
            }
        }
        switch(node) {
            case AddNode add -> betaBinary(builder, registers, add, "addq");
            case SubNode sub -> betaBinary(builder, registers, sub, "subq");
            case MulNode mul -> betaBinary(builder, registers, mul, "imulq");
            case DivNode div -> betaBinary(builder, registers, div, "idiv");
            case ModNode mod -> betaBinary(builder, registers, mod, "MOD TODO");
            case ReturnNode r -> builder.repeat(" ", 2).append("ret ")
                .append(registers.get(predecessorSkipProj(r, ReturnNode.RESULT)));
            case ConstIntNode c -> builder.append("movq ")
                .append(c.value())
                .append(", ")
                .append(registers.get(c));
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
        builder.append("\n");
    }

    private void betaBinary(StringBuilder builder, Map<Node, Register> registers, BinaryOperationNode node, String opcode){
        builder.append(opcode)
            .append(" ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)))
            .append(", ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT)));
    }

    public String generateCode(List<IrGraph> program) {
        StringBuilder builder = new StringBuilder();
        for (IrGraph graph : program) {
            AasmRegisterAllocator allocator = new AasmRegisterAllocator();
            Map<Node, Register> registers = allocator.allocateRegisters(graph);
            builder.append("function ")
                .append(graph.name())
                .append(" {\n");
            generateForGraph(graph, builder, registers);
            builder.append("}");
        }
        return builder.toString();
    }

    private void generateForGraph(IrGraph graph, StringBuilder builder, Map<Node, Register> registers) {
        Set<Node> visited = new HashSet<>();
        scan(graph.endBlock(), visited, builder, registers);
    }

    private void scan(Node node, Set<Node> visited, StringBuilder builder, Map<Node, Register> registers) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited, builder, registers);
            }
        }

        switch (node) {
            case AddNode add -> binary(builder, registers, add, "add");
            case SubNode sub -> binary(builder, registers, sub, "sub");
            case MulNode mul -> binary(builder, registers, mul, "mul");
            case DivNode div -> binary(builder, registers, div, "div");
            case ModNode mod -> binary(builder, registers, mod, "mod");
            case ReturnNode r -> builder.repeat(" ", 2).append("ret ")
                .append(registers.get(predecessorSkipProj(r, ReturnNode.RESULT)));
            case ConstIntNode c -> builder.repeat(" ", 2)
                .append(registers.get(c))
                .append(" = const ")
                .append(c.value());
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
        builder.append("\n");
    }

    private static void binary(
        StringBuilder builder,
        Map<Node, Register> registers,
        BinaryOperationNode node,
        String opcode
    ) {
        builder.repeat(" ", 2).append(registers.get(node))
            .append(" = ")
            .append(opcode)
            .append(" ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)))
            .append(" ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT)));
    }
}
