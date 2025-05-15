.global main
.global _start
.text

_start: 
call _main 
movq %rax, %rdi 
movq $0x3C, %rax 
syscall

_main: 
movq $10, %rbx
movq $3, %rcx
movq %rbx , %rax
cqo
idiv %rcx
 movq %rdx, %rax
ret
