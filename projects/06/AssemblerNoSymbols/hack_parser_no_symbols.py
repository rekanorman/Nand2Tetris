class Parser:
    def __init__(self, filename):
        """Opens and reads the given input file. Sets current_command to the
        first valid command in the file, and sets done to False.
        """        
        self.file = open(filename)
        self.done = False
        
        self.command_type = None
        self.symbol = None
        self.dest = None
        self.comp = None
        self.jump = None
        
        self.current_command = self.file.readline()
        if not self.is_valid_command():
            self.next_command()
        else:
            self.parse_command()
    
    def is_done(self):
        """Returns True if there are no more commands to process,
        False otherwise.
        """
        return self.done
    
    def next_command(self):
        """Looks for the next command of the input file. If a valid command
        is found, parses that command, otherwise sets done to True.
        """
        self.current_command = self.file.readline()
        while not self.is_valid_command():
            self.current_command = self.file.readline()
        
        if self.current_command == '':
            self.done = True
            self.file.close()
        else:
            self.parse_command()
    
    def is_valid_command(self):
        """Returns True if the current command is a valid command or an empty
        string (no newline character), False otherwise.
        """
        return not (self.current_command.isspace()
                    or self.current_command.strip().startswith('//'))
    
    def parse_command(self):
        """Parses the current command, setting command_type, symbol, dest,
        comp, and jump to their appropriate values.
        """
        self.strip_command()
        if self.current_command.startswith('@'):
            self.parse_a_command()
        else:
            self.parse_c_command()
    
    def parse_a_command(self):
        self.command_type = 'A_COMMAND'
        self.symbol = self.current_command[1:].strip()
    
    def parse_c_command(self):
        self.command_type = 'C_COMMAND'
        command = self.current_command
        
        if ';' in command:
            dest_comp, jump = command.split(';')
            self.jump = jump.strip()
        else:
            dest_comp = command
            self.jump = 'null'
        
        if '=' in dest_comp:
            dest, comp = dest_comp.split('=')
        else:
            comp = dest_comp
            dest = 'null'
        
        self.dest = ''.join(dest.split())
        self.comp = ''.join(comp.split())
    
    def strip_command(self):
        """Removes any in-line comments and surrounding whitespace from the
        current command.
        """
        self.current_command = self.current_command.split('//')[0]
        self.current_command = self.current_command.strip()
