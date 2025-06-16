package edu.kit.kastel.vads.compiler.backend.x86asm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class PhysicalRegister implements Register{
    private static final String[] registers = {"rbx", "rcx", "rsi", "rdi", "rbp", "r8", "r9", "r10", "r11", "r12", "r13", "r14", "r15"}; //Rax not allowed as allocation register for now, rdx too for division
    private final int id;
    private static final int MAX_AMOUNT = registers.length;

    public PhysicalRegister(int id){
        this.id = id;
    }

    public static int getMaxAmount(){
        return MAX_AMOUNT;
    }

    @Override
    public String toString() {
        return "%" + registers[this.id];
    }
}
