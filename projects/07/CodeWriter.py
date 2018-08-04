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
    def __init__(self, filename):
        """Opens the given output file for writing."""
        self.outfile = open(filename, 'w')
        self.label_counter = 0
        self.filename = filename.split('\\')[-1].split('.')[0]
        
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
      
    def close_outfile(self):
        """Closes the output file."""
        self.outfile.close()
