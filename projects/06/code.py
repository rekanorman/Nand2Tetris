DEST_DICT = {'null' : '000',
             'M'    : '001',
             'D'    : '010',
             'MD'   : '011',
             'A'    : '100',
             'AM'   : '101',
             'AD'   : '110',
             'AMD'  : '111'}

COMP_DICT = {'0'   : '101010',
             '1'   : '111111',
             '-1'  : '111010',
             'D'   : '001100',
             'A'   : '110000',
             '!D'  : '001101',
             '!A'  : '110001',
             '-D'  : '001111',
             '-A'  : '110011',
             'D+1' : '011111',
             'A+1' : '110111',
             'D-1' : '001110',
             'A-1' : '110010',
             'D+A' : '000010',
             'D-A' : '010011',
             'A-D' : '000111',
             'D&A' : '000000',
             'D|A' : '010101'}

JUMP_DICT = {'null' : '000',
             'JGT'  : '001',
             'JEQ'  : '010',
             'JGE'  : '011',
             'JLT'  : '100',
             'JNE'  : '101',
             'JLE'  : '110',
             'JMP'  : '111'}


class Code:
    def __init__(self):
        pass
    
    def dest(self, dest_asm):
        """Takes a dest mnenonic and returns the corresponding 3-bit binary
        code.
        """
        return DEST_DICT[dest_asm]
    
    def comp(self, comp_asm):
        """Takes a comp mnemonic and returns the corresponding 7-bit binary
        code.
        """
        if 'M' in comp_asm:
            a = '1'
            comp_asm = comp_asm.replace('M', 'A')
        else:
            a = '0'
        
        return a + COMP_DICT[comp_asm]
    
    def jump(self, jump_asm):
        """Takes a jump mnemonic and returns the corresponding 3-bit binary
        code.
        """
        return JUMP_DICT[jump_asm]

    