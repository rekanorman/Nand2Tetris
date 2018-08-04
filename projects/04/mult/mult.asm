// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.

	// Set x = RAM[0]
	@R0
	D=M
	@x
	M=D
	
	// Set y = RAM[1]
	@R1
	D=M
	@y
	M=D
	
	// Set product = 0
	@product
	M=0
	
(LOOP)
	// If y = 0, goto STOP
	@y
	D=M
	@STOP
	D; JEQ
	
	// product = product + x
	@x
	D=M
	@product
	M=M+D
	
	// y = y - 1
	@y
	M=M-1
	
	// goto LOOP
	@LOOP
	0; JMP

(STOP)
	// Set RAM[2] = product
	@product
	D=M
	@R2
	M=D

(END)
	@END
	0; JMP

	