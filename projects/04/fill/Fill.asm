// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

(MAIN_LOOP)
	// If RAM[KBD] == 0, goto CHECK_WHITE (no key is pressed)
	@KBD
	D=M
	@CHECK_WHITE
	D; JEQ
	
	// Else, goto CHECK_BLACK (a key is pressed)
	@CHECK_BLACK
	0; JMP
	
(CHECK_WHITE)
	// If RAM[SCREEN] == 0, goto MAIN_LOOP
	// (first word of screen memory map is 0, so screen is already white)
	@SCREEN
	D=M
	@MAIN_LOOP
	D; JEQ
	
	// Else, goto SET_WHITE
	@SET_WHITE
	0; JMP

(CHECK_BLACK)
	// If RAM[SCREEN] != 0, goto MAIN_LOOP
	// (first word of screen memory map is not 0, so screen is already black)
	@SCREEN
	D=M
	@MAIN_LOOP
	D; JNE
	
	// Else, goto SET_BLACK
	@SET_BLACK
	0; JMP

(SET_WHITE)
	// Set address = SCREEN
	@SCREEN
	D=A
	@address
	M=D
	
(SET_WHITE_LOOP)
	// If address = KBD, goto LOOP (reached end of screen memory map)
	@address
	D=M
	@KBD
	D=D-A
	@MAIN_LOOP
	D; JEQ
	
	// Set RAM[address] = 0
	@address
	A=M
	M=0
	
	// address ++
	@address
	M=M+1
	
	// goto SET_WHITE_LOOP
	@SET_WHITE_LOOP
	0; JMP

(SET_BLACK)
	// Set address = SCREEN
	@SCREEN
	D=A
	@address
	M=D
	
(SET_BLACK_LOOP)
	// If address = KBD, goto LOOP (reached end of screen memory map)
	@address
	D=M
	@KBD
	D=D-A
	@MAIN_LOOP
	D; JEQ
	
	// Set RAM[address] = -1
	@address
	A=M
	M=-1
	
	// address ++
	@address
	M=M+1
	
	// goto SET_BLACK_LOOP
	@SET_BLACK_LOOP
	0; JMP
	
	
	
	
	
	
	
	
	
	
	