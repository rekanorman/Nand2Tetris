import sys
import os

from Parser import Parser
from CodeWriter import CodeWriter


class VMTranslator:
    def __init__(self, input_name):
        """Constructs the Parser and CodeWriter objects to be used in
        translating the file and supplies them with the appropriate filenames.
        """
        if input_name.endswith('.vm'):
            vmfilename = input_name
            self.vmfilenames = [vmfilename]
            self.asmfilename = vmfilename[:-3] + '.asm'
            write_init = False
        else:
            directory = input_name
            self.vmfilenames = self.get_vmfilenames(directory)
            self.asmfilename = '{}\{}'.format(directory,
                                              directory.split('\\')[-1] + '.asm')
            write_init = True
            
        self.code_writer = CodeWriter(self.asmfilename)
        
        if write_init:
            self.code_writer.write_init()
    
    def get_vmfilenames(self, directory):
        """Takes the path of a directory, starting from the current directory,
        and returns a list of all the VM filenames in that directory,
        including their paths from the current directory.
        """
        filenames = os.listdir(os.getcwd() + '\\' + directory)
        return [directory + '\\' + filename for filename in filenames if
                filename.endswith('.vm')]
    
    def translate(self):
        """Translates all the given VM files"""
        for vmfilename in self.vmfilenames:
            self.translate_file(vmfilename)
        
        self.code_writer.close_outfile()
    
    def translate_file(self, vmfilename):
        """Translates the given VM file, creating a parser for the file, and 
        writing the corresponding assembly code to the output file.
        """
        self.parser = Parser(vmfilename)
        self.code_writer.set_filename(vmfilename.split('\\')[-1][:-3])
        
        self.parser.advance()
        while self.parser.has_more_commands():
            self.translate_current_command()
            self.parser.advance()
    
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
        elif command_type == 'LABEL':
            self.code_writer.write_label(arg1)
        elif command_type == 'GOTO':
            self.code_writer.write_goto(arg1)
        elif command_type == 'IF':
            self.code_writer.write_if(arg1)
        elif command_type == 'FUNCTION':
            self.code_writer.set_function_name(arg1)            
            self.code_writer.write_function(arg1, int(arg2))
        elif command_type == 'CALL':
            self.code_writer.write_call(arg1, int(arg2))
        elif command_type == 'RETURN':
            self.code_writer.write_return()
        else:
            raise ValueError('Incorrect command type.')
    
    
def main():
    input_name = sys.argv[1]
    vmtranslator = VMTranslator(input_name)
    vmtranslator.translate()

main()

#vmt = VMTranslator('ProgramFlow\BasicLoop\BasicLoop.vm')
#vmt = VMTranslator('ProgramFlow\FibonacciSeries\FibonacciSeries.vm')
#vmt = VMTranslator('FunctionCalls\SimpleFunction\SimpleFunction.vm')

#vmt.translate()