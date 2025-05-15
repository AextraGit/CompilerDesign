package edu.kit.kastel.vads.compiler.backend.x86asm;

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
        builder.append(".global _start\n")
            .append(".text")
            .repeat("\n", 2);                    //dann die .text direktive

        builder.append("_start: \n")
                .append("call _main \n")
                .append("movq %rax, %rdi \n")
                .append("movq $0x3C, %rax \n")
                .append("syscall")
                .repeat("\n", 2);

        for(IrGraph graph : program){               //dann weiter mit den Routinendefs
            AsmRegisterAllocator allocator = new AsmRegisterAllocator();
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
            case DivNode div -> divide(builder, registers, div, "rax");
            case ModNode mod -> divide(builder, registers, mod, "rdx");
            case ReturnNode r -> ret(builder, registers, r, "ret");
                                   /* .append("movq ")
                                    .append(registers.get(predecessorSkipProj(r, ReturnNode.RESULT)))
                                    .append(", %rax \n")
                                    .append("ret");*/
               
            case ConstIntNode c -> assignConstant(builder, registers, c);
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
        builder.append("\n");
    }

    private void assignConstant(StringBuilder builder, Map<Node, Register> registers, ConstIntNode node){
        builder.append("movq ")
                .append("$")
                .append(node.value())
                .append(", ")
                .append(registers.get(node));
        moveToRax(builder, registers, node);
    }

    private void ret(StringBuilder builder, Map<Node, Register> registers, ReturnNode r, String opcode){
        builder.append(opcode);
        //System.err.println("fucking ret " + registers.get(predecessorSkipProj(r, ReturnNode.RESULT)));
    }

    private void divide(StringBuilder builder, Map<Node, Register> registers, BinaryOperationNode node, String registerToRead){
        builder.append("movq ")                                                             //mov dividend (the upper one) to %rax
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)))
            .append(" , %rax\n")
            .append("cqo\n")                                                                //cqo for sign
            .append("idiv ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT)));   //actual division
           /* .append("\n")
            .append("movq ")
            .append(registerToRead)
            .append(", ")
            .append(registers.get(prede));   */
        if(registerToRead.equals("rdx")) builder.append("\nmovq %rdx, %rax");
        //System.err.println("divide " + registers.get(predecessorSkipProj(node, ReturnNode.RESULT)));
    }

    private void betaBinary(StringBuilder builder, Map<Node, Register> registers, BinaryOperationNode node, String opcode){
        builder.append(opcode)
            .append(" ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT)))
            .append(", ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)))
            .append("\n");
        moveToRax(builder, registers, node);
    }

    private void moveToRax(StringBuilder builder, Map<Node, Register> registers, Node node){
        builder.append("movq ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)))     //looks like the result is stored in the left binop node NOT in the RESULT node wtf
            .append(", %rax");
    }
}
