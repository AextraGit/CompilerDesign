.PHONY: run clean

all: out1

out1: out1.o
	ld -o out1 out1.o

out1.o: out1.s
	as -o out1.o out1.s

out1.s: test1.c
	./run.sh test1.c out1.s

run: out1  
	-./out1
	echo "Exit Code: $$?"

clean:
	rm -f out1.o out1