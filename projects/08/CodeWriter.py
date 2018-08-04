#---------------------------------------------------------------------------
# Assembly code for arithmetic VM commands
#---------------------------------------------------------------------------

ADD = ['// add',
       '@SP',
       'AM=M-1',
       'D=M',
       'A=A-1',
       'M=M+D']

SUB = ['// sub',
       '@SP',
       'AM=M-1',
       'D=M',
       'A=A-1',
       'M=M-D']

NEG = ['// neg',
       '@SP',
       'A=M-1',
       'M=-M']

EQ = ['// eq',
      '@SP',
      'AM=M-1',
      'D=M',
      'A=A-1',
      'D=M-D',
      '@EQ_TRUE_{0}',
      'D;JEQ',
      'D=0',
      '@EQ_END_{0}',
      '0;JMP',
      '(EQ_TRUE_{0})',
      'D=-1',
      '(EQ_END_{0})',
      '@SP',
      'A=M-1',
      'M=D']

GT = ['// gt',
      '@SP',
      'AM=M-1',
      'D=M',
      'A=A-1',
      'D=M-D',
      '@GT_TRUE_{0}',
      'D;JGT',
      'D=0',
      '@GT_END_{0}',
      '0;JMP',
      '(GT_TRUE_{0})',
      'D=-1',
      '(GT_END_{0})',
      '@SP',
      'A=M-1',
      'M=D']

LT = ['// lt',
      '@SP',
      'AM=M-1',
      'D=M',
      'A=A-1',
      'D=M-D',
      '@LT_TRUE_{0}',
      'D;JLT',
      'D=0',
      '@LT_END_{0}',
      '0;JMP',
      '(LT_TRUE_{0})',
      'D=-1',
      '(LT_END_{0})',
      '@SP',
      'A=M-1',
      'M=D']

AND = ['// and',
       '@SP',
       'AM=M-1',
       'D=M',
       'A=A-1',
       'M=M&D']

OR = ['// or',
       '@SP',
       'AM=M-1',
       'D=M',
       'A=A-1',
       'M=M|D']

NOT = ['// not',
       '@SP',
       'A=M-1',
       'M=!M']

COMMAND_DICT = {'add' : ADD,
                'sub' : SUB,
                'neg' : NEG,
                'eq'  : EQ,
                'gt'  : GT,
                'lt'  : LT,
                'and' : AND,
                'or'  : OR,
                'not' : NOT}

#--------------------------------------------------------------------------
# Assembly code for pushe/pop VM commands
#--------------------------------------------------------------------------

# Insert one of LCL, ARG, THIS, THAT to set addr to the appropriate address
SET_ADDR = ['@{}',
            'D=A',
            '@{}',
            'D=D+M',
            '@addr',
            'M=D']

# Insert 5 for temp or THIS for pointer
SET_ADDR_TEMP_POINTER = ['@{}',
                         'D=A',
                         '@{}',
                         'D=D+A',
                         '@addr',
                         'M=D']

# Pushes/pops the value stored in RAM[addr] to/from the stack.
# Assumes that the variable addr contains the correct address.
PUSH_ADDR = ['@addr',
             'A=M',
             'D=M',
             '@SP',
             'A=M',
             'M=D',
             '@SP',
             'M=M+1']

POP_ADDR = ['@SP',
            'AM=M-1',
            'D=M',
            '@addr',
            'A=M',
            'M=D']

PUSH_CONSTANT = ['@{}',
                 'D=A',
                 '@SP',
                 'A=M',
                 'M=D',
                 '@SP',
                 'M=M+1']

PUSH_STATIC = ['@{}.{}',
               'D=M',
               '@SP',
               'A=M',
               'M=D',
               '@SP',
               'M=M+1']

POP_STATIC = ['@SP',
              'AM=M-1',
              'D=M',
              '@{}.{}',
              'M=D']


class CodeWriter:
    def __init__(self, asmfilename):
        """Opens the given output file for writing."""
        self.outfile = open(asmfilename, 'w')
        self.label_counter = 0
        self.return_counter = 0
        self.filename = asmfilename.split('\\')[-1].split('.')[0]
        self.function_name = ''
    
    def set_filename(self, filename):
        """Sets the filename to that of the VM file currently being translated.
        """
        self.filename = filename
        
    def set_function_name(self, function_name):
        """Sets function_name to the name of the the function currently being
        processed in the VM file. Function name is usually in the format
        filename.function_name. Called when a functoin declaration command
        is being translated. Resets the return_counter to 0.
        """
        self.function_name = function_name
        self.return_counter = 0
    
    def write_init(self):
        """Writes the assembly commands for the bootstrap code to the
        output file. Only called if a directory is being translated.
        """
        self.outfile.write('// Bootstrap code\n')
        
        # SP = 256
        self.outfile.write('@256\nD=A\n@SP\nM=D\n')
        
        # call Sys.init
        #self.outfile.write('@Sys.init\n0;JMP\n')
        
        # push return address
        self.outfile.write('@{}$ret.{}\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n'.format(
            self.function_name,
            self.return_counter))
        
        # push LCL, ARG, THIS, THAT
        for pointer in ('LCL', 'ARG', 'THIS', 'THAT'):
            self.outfile.write('@{}\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n'.format(
                pointer))
            
        # ARG = SP - 5 - num_args
        self.outfile.write('@SP\nD=M\n@5\nD=D-A\n@0\nD=D-A\n@ARG\nM=D\n')
        
        # LCL = SP
        self.outfile.write('@SP\nD=M\n@LCL\nM=D\n')
        
        # goto called_function_name
        self.outfile.write('@Sys.init\n0;JMP\n')
        
        # (return_address)
        self.outfile.write('({}$ret.{})\n'.format(self.function_name,
                                               self.return_counter))        
        
    def write_arithmetic(self, command):
        """Takes the arithmetic command in the form of a string (eg. 'add')
        and writes the corresponding assembly commands to the output file.
        """
        asm_code = '\n'.join(COMMAND_DICT[command]).format(self.label_counter)
        self.outfile.write(asm_code + '\n')
        self.label_counter += 1
    
    def write_push_pop(self, command_type, segment, index):
        """Takes a PUSH or POP command, with the memory segment (string), an
        the memory index (int), and writes the corresponding assembly commands
        to the output file.
        """
        self.outfile.write('// {} {} {}\n'.format(command_type.lower(),
                                                  segment,
                                                  index))
        if command_type == 'POP':
            if segment == 'local':
                asm_code = '\n'.join(SET_ADDR + POP_ADDR).format(index, 'LCL')
            elif segment == 'argument':
                asm_code = '\n'.join(SET_ADDR + POP_ADDR).format(index, 'ARG')
            elif segment == 'this':
                asm_code = '\n'.join(SET_ADDR + POP_ADDR).format(index, 'THIS')
            elif segment == 'that':
                asm_code = '\n'.join(SET_ADDR + POP_ADDR).format(index, 'THAT')
            elif segment == 'static':
                asm_code = '\n'.join(POP_STATIC).format(self.filename, index)
            elif segment == 'temp':
                asm_code = '\n'.join(SET_ADDR_TEMP_POINTER + POP_ADDR).format(index,
                                                                              5)
            elif segment == 'pointer':
                asm_code = '\n'.join(SET_ADDR_TEMP_POINTER + POP_ADDR).format(index,
                                                                            'THIS')
        elif command_type == 'PUSH':
            if segment == 'local':
                asm_code = '\n'.join(SET_ADDR + PUSH_ADDR).format(index, 'LCL')
            elif segment == 'argument':
                asm_code = '\n'.join(SET_ADDR + PUSH_ADDR).format(index, 'ARG')
            elif segment == 'this':
                asm_code = '\n'.join(SET_ADDR + PUSH_ADDR).format(index, 'THIS')
            elif segment == 'that':
                asm_code = '\n'.join(SET_ADDR + PUSH_ADDR).format(index, 'THAT')
            elif segment == 'constant':
                asm_code = '\n'.join(PUSH_CONSTANT).format(index)
            elif segment == 'static':
                asm_code = '\n'.join(PUSH_STATIC).format(self.filename, index)
            elif segment == 'temp':
                asm_code = '\n'.join(SET_ADDR_TEMP_POINTER + PUSH_ADDR).format(index,
                                                                              5)
            elif segment == 'pointer':
                asm_code = '\n'.join(SET_ADDR_TEMP_POINTER + PUSH_ADDR).format(index,
                                                                            'THIS')
        self.outfile.write(asm_code + '\n')
    
    def write_label(self, label):
        self.outfile.write('// label {}\n'.format(label))
        self.outfile.write('({}${})\n'.format(self.function_name, label))
    
    def write_goto(self, label):
        self.outfile.write('// goto {}\n'.format(label))
        self.outfile.write('@{}${}\n'.format(self.function_name, label))
        self.outfile.write('0;JMP\n')
    
    def write_if(self, label):
        self.outfile.write('// if-goto {}\n'.format(label))
        self.outfile.write('@SP\n')
        self.outfile.write('AM=M-1\n')
        self.outfile.write('D=M\n')
        self.outfile.write('@{}${}\n'.format(self.function_name, label))
        self.outfile.write('D;JNE\n')
    
    def write_function(self, function_name, num_local):
        self.outfile.write('// function {} {}\n'.format(function_name, num_local))
        
        self.outfile.write('({})\n'.format(function_name))
        for i in range(num_local):
            self.outfile.write('@SP\nA=M\nM=0\n@SP\nM=M+1\n')
    
    def write_call(self, called_function_name, num_args):
        self.outfile.write('// call {} {}\n'.format(called_function_name, num_args))
        
        # push return address
        self.outfile.write('@{}$ret.{}\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n'.format(
            self.function_name,
            self.return_counter))
        
        # push LCL, ARG, THIS, THAT
        for pointer in ('LCL', 'ARG', 'THIS', 'THAT'):
            self.outfile.write('@{}\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n'.format(
                pointer))
            
        # ARG = SP - 5 - num_args
        self.outfile.write('@SP\nD=M\n@5\nD=D-A\n@{}\nD=D-A\n@ARG\nM=D\n'.format(
            num_args))
        
        # LCL = SP
        self.outfile.write('@SP\nD=M\n@LCL\nM=D\n')
        
        # goto called_function_name
        self.outfile.write('@{}\n0;JMP\n'.format(called_function_name))
        
        # (return_address)
        self.outfile.write('({}$ret.{})\n'.format(self.function_name,
                                               self.return_counter))
        self.return_counter += 1
    
    def write_return(self):
        self.outfile.write('// return\n')
        
        # end_frame = LCL
        self.outfile.write('@LCL\nD=M\n@end_frame\nM=D\n')
        
        # return_address = *(end_frame - 5)
        self.outfile.write(
            '@end_frame\nD=M\n@5\nD=D-A\nA=D\nD=M\n@return_address\nM=D\n')
        
        # *ARG = top value of stack
        self.outfile.write('@SP\nA=M-1\nD=M\n@ARG\nA=M\nM=D\n')
        
        # SP = ARG + 1
        self.outfile.write('@ARG\nD=M+1\n@SP\nM=D\n')
        
        # THAT = *(end_frame - 1), etc
        for pointer, num in [('THAT', 1), ('THIS', 2), ('ARG', 3), ('LCL', 4),]:
            self.outfile.write(
                '@end_frame\nD=M\n@{}\nD=D-A\nA=D\nD=M\n@{}\nM=D\n'.format(
                    num, pointer))
        
        # goto return_address
        self.outfile.write('@return_address\nA=M\n0;JMP\n')
        
    def close_outfile(self):
        """Closes the output file."""
        self.outfile.close()
