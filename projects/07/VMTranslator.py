import sys
from Parser import Parser
from CodeWriter import CodeWriter


class VMTranslator:
    def __init__(self, vmfilename):
        """Constructs the Parser and CodeWriter objects to be used in
        translating the file and supplies them with the appropriate filenames.
        """
        asmfilename = vmfilename.split('.')[0] + '.asm'
        
        self.parser = Parser(vmfilename)
        self.code_writer = CodeWriter(asmfilename)
    
    def translate(self):
        """Translates the given VM file, writing the corresponding assembly
        code to the output file.
        """
        self.parser.advance()
        while self.parser.has_more_commands():
            self.translate_current_command()
            self.parser.advance()
        
        self.code_writer.close_outfile()
    
    def translate_current_command(self):
        """Translates the current command of the Parser, using the CodeWriter
        to write the corresponding assembly code to the output file.
        """
        command_type = self.parser.command_type()
        arg1 = self.parser.arg1()
        arg2 = self.parser.arg2()
        
        if command_type == 'ARITHMETIC':
            self.code_writer.write_arithmetic(arg1)
        elif command_type in ('PUSH', 'POP'):
            self.code_writer.write_push_pop(command_type, arg1, int(arg2))
        else:
            raise ValueError('Incorrect command type.')
    
    
def main():
    vmfilename = sys.argv[1]
    vmtranslator = VMTranslator(vmfilename)
    vmtranslator.translate()

main()

#vmt = VMTranslator('StackArithmetic\SimpleAdd\SimpleAdd.vm')