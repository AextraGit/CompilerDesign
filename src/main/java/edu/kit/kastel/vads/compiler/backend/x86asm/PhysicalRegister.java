package edu.kit.kastel.vads.compiler.backend.x86asm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class PhysicalRegister implements Register{
    private final String[] registers = {"rbx", "rcx", "rsi", "rdi", "rbp", "r8", "r9", "r10", "r11", "r12", "r13", "r14", "r15"}; //Rax not allowed as allocation register for now, rdx too for division
    private int id;
    public PhysicalRegister(int id){
        this.id = id;
    }

    @Override
    public String toString() {
        return "%" + this.registers[this.id];
    }
}
