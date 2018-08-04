COMMAND_TYPES = {'add'      : 'ARITHMETIC',
                 'sub'      : 'ARITHMETIC',
                 'neg'      : 'ARITHMETIC',
                 'eq'       : 'ARITHMETIC',
                 'gt'       : 'ARITHMETIC',
                 'lt'       : 'ARITHMETIC',
                 'and'      : 'ARITHMETIC',
                 'or'       : 'ARITHMETIC',
                 'not'      : 'ARITHMETIC',
                 'pop'      : 'POP',
                 'push'     : 'PUSH',
                 'label'    : 'LABEL',
                 'goto'     : 'GOTO',
                 'if-goto'  : 'IF',
                 'function' : 'FUNCTION',
                 'call'     : 'CALL',
                 'return'   : 'RETURN'}


class Parser:
    def __init__(self, filename):
        """Opens the given VM file for reading and initialises the instance
        variables
        """
        self.infile = open(filename)
        
        self.current_command = None
        self._has_more_commands = True
        self._command_type = None
        self._arg1 = None
        self._arg2 = None
    
    def advance(self):
        """Reads the next valid VM command and parses it, setting command_type,
        arg1 and arg2. Sets has_more_commands to False if there are no more
        valid commands.
        """
        line = self.infile.readline()
        while line != '' and self.is_whitespace(line):
            line = self.infile.readline()
        
        if line == '':
            self._has_more_commands = False
        else:
            self.current_command = self.remove_whitespace(line)
            self.parse_current_command()
        
    def parse_current_command(self):
        """Parses the current command (with whitespace and comments removed),
        setting command_type, arg1 and arg2 to the appropriate values.
        """
        components = self.current_command.split()
        self._command_type = COMMAND_TYPES[components[0]]
        
        if self.command_type() == 'ARITHMETIC':
            self._arg1 = components[0]
        elif self.command_type() in ('LABEL', 'GOTO', 'IF'):
            self._arg1 = components[1]            
        elif self.command_type() in ('PUSH', 'POP', 'FUNCTION', 'CALL'):
            self._arg1, self._arg2 = components[1:3]
        elif self.command_type() == 'RETURN':
            pass
        else:
            raise ValueError('Unknown command type.')        
    
    def remove_whitespace(self, line):
        """Takes a line from the input file and removes all surrounding
        whitespace and inline comments, returning the command.
        """
        return line.split('//')[0].strip() 
    
    def is_whitespace(self, line):
        """Takes a line from the input file and returns True if the line
        consists only of whitespace and comments.
        """
        return line.strip().startswith('//') or line.isspace()
    
    def has_more_commands(self):
        """Returns True if there are more commands in the file to parse,
        False otherwise.
        """
        return self._has_more_commands
    
    def command_type(self):
        """Returns a string constant representing the type of the current
        command (eg. 'ARITHMETIC')
        """
        return self._command_type
    
    def arg1(self):
        """Returns the first argument of the current command.
        For arithmetic commands (ARITHMETIC), the command itself is returned.
        """
        return self._arg1
    
    def arg2(self):
        """Returns the second argument of the current command (if it exists).
        """
        return self._arg2


#p = Parser('MemoryAccess\BasicTest\BasicTest.vm')
#while p.has_more_commands():
    #print(p.command_type(), p.arg1(), p.arg2())
    #p.advance()