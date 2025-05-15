.global main
.global _start
.text

_start: 
call _main 
movq %rax, %rdi 
movq $0x3C, %rax 
syscall

_main: 
movq $10, %rcx
movq $5, %rdx
subq %rdx, %rcx
movq %rsi, %rax 
ret
