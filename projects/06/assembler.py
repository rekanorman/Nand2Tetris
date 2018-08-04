import sys
from hack_parser import Parser
from code import Code
from symbol_table import SymbolTable

class Assembler:
    def __init__(self, filename):
        self.asm_filename = filename
        self.hack_filename = self.asm_filename[:-3] + 'hack'
        self.outfile = open(self.hack_filename, 'w')
        
        self.parser = Parser(self.asm_filename)
        self.code = Code()
        self.symbol_table = SymbolTable()
        self.next_available_address = 16
    
    def translate(self):
        """Translates all the commands in the .asm file, writing the resulting
        binary code to the .hack file.
        """
        self.first_pass()
        self.parser.reset()
        self.second_pass()
        self.outfile.close()
    
    def first_pass(self):
        """Goes through the input file, adding all the labels to the symbol
        table.
        """
        label = self.parser.first_label()
        while not self.parser.is_done():
            self.symbol_table.add_entry(label, self.parser.label_address())
            label = self.parser.next_label()
    
    def second_pass(self):
        """Goes through the input file fo a second time, translating the
        commands into binary, and adding symbols to the symbol table as
        required.
        """
        while not self.parser.is_done():
            self.process_command()
            self.parser.next_command()
    
    def process_command(self):
        """Translates the current command of the parser, writing the
        corresponding binary command to the output file (including a newline
        character).
        """
        if self.parser.command_type() == 'C_COMMAND':
            self.process_c_command()
        elif self.parser.command_type() == 'A_COMMAND':
            self.process_a_command()            
        else:                   #Command is a label
            return
    
    def process_c_command(self):
        """Translates the current C-command of the parser, writing the
        corresponding binary command to the output file (including a newline
        character).
        """
        dest_binary = self.code.dest(self.parser.dest)
        comp_binary = self.code.comp(self.parser.comp)
        jump_binary = self.code.jump(self.parser.jump)
        binary_command = '111' + comp_binary + dest_binary + jump_binary
        self.outfile.write(binary_command + '\n')
    
    def process_a_command(self):
        """Translates the current C-command of the parser, writing the
        corresponding binary command to the output file (including a newline
        character).
        """        
        symbol = self.parser.symbol
        
        if symbol.isnumeric():
            address = int(symbol)
        elif self.symbol_table.contains(symbol):
            address = self.symbol_table.get_address(symbol)
        else:
            address = self.next_available_address
            self.symbol_table.add_entry(symbol, address)
            self.next_available_address += 1
            
        binary_command = self.binary_number(address)
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
