import sys
from hack_parser import Parser
from code import Code

class Assembler:
    def __init__(self, filename):
        self.asm_filename = filename
        self.hack_filename = self.asm_filename[:-3] + 'hack'
        self.outfile = open(self.hack_filename, 'w')
        
        self.parser = Parser(self.asm_filename)
        self.code = Code()
    
    def translate(self):
        """Translates all the commands in the .asm file, writing the resulting
        binary code to the .hack file.
        """
        while not self.parser.is_done():
            self.process_command()
            self.parser.next_command()
        self.outfile.close()
    
    def process_command(self):
        """Translates the current command of the parser, writing the
        corresponding binary command to the output file (including a newline
        character).
        """
        if self.parser.command_type == 'C_COMMAND':
            dest_binary = self.code.dest(self.parser.dest)
            comp_binary = self.code.comp(self.parser.comp)
            jump_binary = self.code.jump(self.parser.jump)
            binary_command = '111' + comp_binary + dest_binary + jump_binary
        else:
            binary_command = self.binary_number(self.parser.symbol)
        
        self.outfile.write(binary_command + '\n')
    
    def binary_number(self, symbol):
        """Takes a decimal number in the form of a string and converts
        it to a 16-bit binary number in the form of a string.
        """
        binary = bin(int(symbol))
        return binary[2:].zfill(16)
    

def main():
    filename = sys.argv[1]
    assembler = Assembler(filename)
    assembler.translate()

main()