class Parser:
    def __init__(self, filename):
        """Opens and reads the given input file. Sets current_command to the
        first valid command in the file, and sets done to False.
        """        
        self.filename = filename
        self.file = open(self.filename)
        self.done = False
        self.instruction_number = -1
        
        self.symbol = None
        self.dest = None
        self.comp = None
        self.jump = None
        self.label = None
        
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
        """Parses the current command, setting symbol, dest,
        comp, jump and label to their appropriate values.
        """
        self.strip_command()
        if self.command_type() == 'A_COMMAND':
            self.parse_a_command()
            self.instruction_number += 1
        elif self.command_type() == 'C_COMMAND':
            self.parse_c_command()
            self.instruction_number += 1
        else:
            self.parse_label()
    
    def parse_a_command(self):
        """Takes an A-command stripped of surrounding whitespace and comments,
        and parses it, setting symbol to the appropriate value.
        """
        self.symbol = self.current_command[1:].strip()
    
    def parse_c_command(self):
        """Takes C-command stripped of surrounding whitespace and comments,
        and parses it, setting dest, comp and jump to their appropriate values.
        """        
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
    
    def parse_label(self):
        """Takes a label stripped of surrounding whitespace and comments,
        and parses it, setting label to the appropriate value.
        """
        self.label = self.current_command[1:-1]
    
    def strip_command(self):
        """Removes any in-line comments and surrounding whitespace from the
        current command.
        """
        self.current_command = self.current_command.split('//')[0]
        self.current_command = self.current_command.strip()
    
    def next_label(self):
        """Finds and returns the next label in the input file, otherwise sets
        done to True and returns None.
        """
        self.next_command()
        while self.command_type() != 'LABEL' and not self.is_done():
            self.next_command()
        
        if not self.is_done():
            return self.label
    
    def first_label(self):
        """Returns the first label starting with the current command,
        otherwise returns None and sets done to True.
        """
        if self.command_type() == 'LABEL':
            return self.label
        else:
            return self.next_label()
    
    def label_address(self):
        """Returns the address to which the current label refers."""
        return self.instruction_number + 1
    
    def command_type(self):
        """Returns the type of the current command, either A_COMMAND,
        C_COMMAND, or LABEL. Used once the command has been stripped of
        whitespace and comments.
        """
        if self.current_command.startswith('('):
            return 'LABEL'
        elif self.current_command.startswith('@'):
            return 'A_COMMAND'
        else:
            return 'C_COMMAND'
    
    def reset(self):
        """Resets the parser after the first pass, preparing it for the
        second pass.
        """
        self.__init__(self.filename)