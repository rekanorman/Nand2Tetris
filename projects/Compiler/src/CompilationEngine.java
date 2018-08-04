import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Has compile method for each non-terminal rule in the Jack grammar,
 * except for the following:
 *  - type
 *  - className
 *  - subroutineName
 *  - varName
 *  - statement
 *  - subroutineCall
 */
public class CompilationEngine {
	
	private final ArrayList<String> OPS = new ArrayList<String>(
													Arrays.asList(
					"+", "-", "*", "/", "&amp;", "|", "&lt;", "&gt;", "="));
	
	private final ArrayList<String> UNARY_OPS = new ArrayList<String>(
													Arrays.asList(
													"-", "~"));
	
	private final ArrayList<Keyword> KEYWORD_CONSTANTS = new ArrayList<Keyword>(
												Arrays.asList(
					Keyword.TRUE, Keyword.FALSE, Keyword.NULL, Keyword.THIS));
	
	private JackTokenizer tokenizer;
	private SymbolTable table;
	private VMWriter writer;
	
	private String className;
	private String currentSubroutineName;
	
	private int ifCounter;
	private int whileCounter;
	
	/**
	 * Creates a new compilation engine which uses input
	 * from the given JackTokeniser, and writes output to
	 * the given .xml file.
	 * Each compilexxx method writes the appropriate xml for that construct
	 * to the output file.
	 * @param tokenizer		The JackTokenizer to read input from.
	 * @param outputFile	The xml file to write the parsed output to.
	 * @throws FileNotFoundException
	 */
	public CompilationEngine(JackTokenizer tokenizer, File outputFile) throws FileNotFoundException {
		this.tokenizer = tokenizer;
		this.table = new SymbolTable();
		this.writer = new VMWriter(outputFile);
	}
	
	/**
	 * Closes the VMWriter used to write output to the vm file.
	 * Should be called once the compilation is finished.
	 */
	public void close() {
		writer.closeOutputFile();
	}
	
	/**
	 * Takes a string representation of a binary operator
	 * and returns the corresponding VM command.
	 * @param operator	A String representation of the binary operator.
	 */
	private Command commandBinary(String operator) {
		switch (operator) {
			case "+":		return Command.ADD;
			case "-": 		return Command.SUB;
			case "=":		return Command.EQ;
			case "&lt;":	return Command.LT;
			case "&gt;":	return Command.GT;
			case "&amp;":	return Command.AND;
			case "|":		return Command.OR;
			default:
				throw new RuntimeException("Invalid binary operator");
		}
	}
	
	/**
	 * Takes a string representation of a unary operator
	 * and returns the corresponding VM command.
	 * @param operator	A String representation of the unary operator.
	 */
	private Command commandUnary(String operator) {
		switch (operator) {
			case "-": 		return Command.NEG;
			case "~":		return Command.NOT;
			default:
				throw new RuntimeException("Invalid unary operator");
		}
	}
	
	/**
	 * Takes a variable kind, and returns the memory segment where
	 * variable of that kind are stored.
	 * @param kind	The kind of the variable.
	 * @return		The corresponding memory segment.
	 */
	private Segment segmentFromKind(Kind kind) {
		switch (kind) {
			case STATIC: 	return Segment.STATIC;
			case FIELD: 	return Segment.THIS;
			case ARG:		return Segment.ARGUMENT;
			case VAR: 		return Segment.LOCAL;
			default:
				throw new RuntimeException("Invalid variable kind");
		}
	}
	
	/**
	 * Writes the appropriate VM commands to push a new string
	 * with the value of the given string constant to the stack.
	 * @param string	The string value to push to the stack.
	 */
	private void pushStringConstant(String string) {
		writer.writePush(Segment.CONSTANT, string.length());
		writer.writeCall("String.new", 1);
		for (int c: string.toCharArray()) {
			writer.writePush(Segment.CONSTANT, c);
			writer.writeCall("String.appendChar", 2);
		}
	}
	
	/**
	 * Checks that the current token of the tokenizer matches the
	 * given keyword, and advances the tokenizer past it.
	 * Writes the appropriate xml to the output file.
	 * @param keyword	The keyword to be consumed.
	 */
	private void consumeKeyword(Keyword keyword) {
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyword() == keyword) {
			tokenizer.advance();
			
		} else {
			throw new RuntimeException("Invalid syntax, expected keyword "
											+ keyword.toString());
		}
	}
	
	/**
	 * Consumes one of the four keyword constants, writing the VM command
	 * to push the appropriate value and advancing the tokenizer past
	 * the keyword.
	 */
	private void consumeKeywordConstant() {		
		Keyword keyword = tokenizer.keyword();
		
		if (keyword == Keyword.THIS) {
			writer.writePush(Segment.POINTER, 0);
			
		} else if (keyword == Keyword.NULL || keyword == Keyword.FALSE) {
			writer.writePush(Segment.CONSTANT, 0);
			
		} else if (keyword == Keyword.TRUE) {
			writer.writePush(Segment.CONSTANT, 0);
			writer.writeArithmetic(Command.NOT);
			
		} else {
			throw new RuntimeException("Expected keyword constant, got "
										+ keyword.toString());
		}
		
		tokenizer.advance();
	}
	
	/**
	 * Checks that the current token of the tokenizer is the given
	 * symbol, and advances the tokenizer past it.
	 * Writes the appropriate xml to the output file, using the string
	 * representation for special characters.
	 * @param symbol	The symbol to be consumed, using the string representation
	 * 					for special characters, or null if the symbol
	 * 					to be consumed is unknown.
	 */
	private void consumeSymbol(String symbol) {
		if (symbol == null
				|| (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals(symbol))) {
			
			tokenizer.advance();
			
		} else {
			throw new RuntimeException(String.format(
									"Invalid syntax, expected symbol %s got %s",
									symbol,
									tokenizer.symbol()));
		}
	}
	
	/**
	 * Checks that the current token of the tokenizer is an identifier,
	 * and advances the tokenizer past it.
	 * Sets the className property to the identifier consumed.
	 */
	private void consumeClassName() {
		if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
			this.className = tokenizer.identifier();
			tokenizer.advance();
			
		} else {
			throw new RuntimeException("Invalid syntax, expected class name got "
										+ tokenizer.tokenType().toString());
		}
	}
	
	/**
	 * Checks that the current token of the tokenizer is an identifier,
	 * and advances the tokenizer past it.
	 * Sets the currentSubroutineName to the identifier consumed.
	 */
	private void consumeSubroutineName() {
		if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
			this.currentSubroutineName = tokenizer.identifier();
			tokenizer.advance();
			
		} else {
			throw new RuntimeException("Invalid syntax, expected subroutine name got "
										+ tokenizer.tokenType().toString());
		}
	}
	
	/**
	 * Checks that the current token of the tokenizer is an identifier,
	 * and advances the tokenizer past it.
	 * Adds the identifier to the symbol table.
	 * @param kind	The kind of the variable being declared.
	 * @param type	The type of the variable being declared.
	 */
	private void addVarToTable(Kind kind, String type) {
		if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
			String name = tokenizer.identifier();
			table.define(name, type, kind);
			
			tokenizer.advance();
			
		} else {
			throw new RuntimeException(String.format(
										"Invalid syntax, expected %s name got %s",
										kind,
										tokenizer.tokenType().toString()));
		}
	}
	
	/**
	 * Consumes either an INT, CHAR, or BOOLEAN keyword, or an identifier,
	 * depending on the next token, and returns a string representation
	 * of the type consumed.
	 * @return A string representation of the type consumed.
	 */
	private String consumeType() {
		if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
			String className = tokenizer.identifier();
			tokenizer.advance();
			return className;
			
		} else if (tokenizer.keyword() == Keyword.INT) {
			consumeKeyword(Keyword.INT);
			return "int";
			
		} else if (tokenizer.keyword() == Keyword.CHAR) {
			consumeKeyword(Keyword.CHAR);
			return "char";
			
		} else if (tokenizer.keyword() == Keyword.BOOLEAN) {
			consumeKeyword(Keyword.BOOLEAN);
			return "boolean";
			
		} else {
			throw new RuntimeException("Invalid syntax, expected type");
		}
	}
	
	/**
	 * Compiles a complete class.
	 * Should be called to start the compilation process once the
	 * compilation engine has been created.
	 */
	public void compileClass() {
		tokenizer.advance();
		
		consumeKeyword(Keyword.CLASS);
		consumeClassName();
		consumeSymbol("{");
		
		while (tokenizer.tokenType() == TokenType.KEYWORD
				&& (tokenizer.keyword() == Keyword.STATIC
				|| tokenizer.keyword() == Keyword.FIELD)) {
			
			compileClassVarDec();
		}
		
		while (tokenizer.tokenType() == TokenType.KEYWORD
				&& (tokenizer.keyword() == Keyword.CONSTRUCTOR
				|| tokenizer.keyword() == Keyword.FUNCTION
				|| tokenizer.keyword() == Keyword.METHOD)) {
			
			compileSubroutineDec();
		}
		
//		consumeSymbol("}");
		}
	
	/**
	 * Compiles a single static variable declaration
	 * or field declaration.
	 */
	private void compileClassVarDec() {	
		Kind kind;
		if (tokenizer.keyword() == Keyword.STATIC) {
			consumeKeyword(Keyword.STATIC);
			kind = Kind.STATIC;
			
		} else {
			consumeKeyword(Keyword.FIELD);
			kind = Kind.FIELD;
		}
		
		String type = consumeType();
		
		addVarToTable(kind, type);
		
		while (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals(",")) {
			consumeSymbol(",");
			addVarToTable(kind, type);
		}
		
		consumeSymbol(";");
	}
	
	/**
	 * Compiles a complete method, function or constructor,
	 * including header and body.
	 */
	private void compileSubroutineDec() {		
		table.startSubroutine();
		ifCounter = 0;
		whileCounter = 0;
				
		Keyword subroutineType = tokenizer.keyword();
		tokenizer.advance();
		
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyword() == Keyword.VOID) {
			consumeKeyword(Keyword.VOID);
		} else {
			consumeType();
		}
		
		if (subroutineType == Keyword.METHOD) {
			table.define("this", className, Kind.ARG);
		}
		
		consumeSubroutineName();
		consumeSymbol("(");
		compileParameterList();
		consumeSymbol(")");
		
		compileSubroutineBody(subroutineType);
	}
	
	/**
	 * Compiles a (possibly empty) parameter list,
	 * not including the enclosing parentheses.
	 */
	private void compileParameterList() {		
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& (tokenizer.keyword() == Keyword.INT
				|| tokenizer.keyword() == Keyword.CHAR
				|| tokenizer.keyword() == Keyword.BOOLEAN)
				|| tokenizer.tokenType() == TokenType.IDENTIFIER) {
			
			String type = consumeType();
			addVarToTable(Kind.ARG, type);
			
			while (tokenizer.tokenType() == TokenType.SYMBOL
					&& tokenizer.symbol().equals(",")) {
				consumeSymbol(",");
				
				type = consumeType();
				addVarToTable(Kind.ARG, type);
			}
		}
	}
	
	/**
	 * Compiles the body of a subroutine.
	 * @param One of FUNCTION, METHOD or CONSTRUCTOR.
	 */
	private void compileSubroutineBody(Keyword subroutineType) {		
		consumeSymbol("{");
		
		while (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyword() == Keyword.VAR) {
			compileVarDec();
		}
		
		writer.writeFunction(className + "." + currentSubroutineName,
								table.varCount(Kind.VAR));
		
		if (subroutineType == Keyword.METHOD) {			
			writer.writePush(Segment.ARGUMENT, 0);
			writer.writePop(Segment.POINTER, 0);
			
		} else if (subroutineType == Keyword.CONSTRUCTOR) {
			writer.writePush(Segment.CONSTANT, table.varCount(Kind.FIELD));
			writer.writeCall("Memory.alloc", 1);
			writer.writePop(Segment.POINTER, 0);
		}
		
		compileStatements();
		consumeSymbol("}");
	}
	
	/**
	 * Compiles a local variable declaration, adding the appropriate
	 * identifiers to the symbol table.
	 */
	private void compileVarDec() {		
		consumeKeyword(Keyword.VAR);
		String type = consumeType();
		addVarToTable(Kind.VAR, type);
		
		while (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals(",")) {
			
			consumeSymbol(",");
			addVarToTable(Kind.VAR, type);
		}
		
		consumeSymbol(";");
	}
	
	/**
	 * Compiles a sequence of zero or more statements,
	 * not including the enclosing curly braces.
	 */
	private void compileStatements() {
		
		while (tokenizer.tokenType() == TokenType.KEYWORD
				&& (tokenizer.keyword() == Keyword.LET
					|| tokenizer.keyword() == Keyword.IF
					|| tokenizer.keyword() == Keyword.WHILE
					|| tokenizer.keyword() == Keyword.DO
					|| tokenizer.keyword() == Keyword.RETURN)) {
			
			if (tokenizer.keyword() == Keyword.LET) {
				compileLet();
			} else if (tokenizer.keyword() == Keyword.IF) {
				compileIf();
			} else if (tokenizer.keyword() == Keyword.WHILE) {
				compileWhile();
			} else if (tokenizer.keyword() == Keyword.DO) {
				compileDo();
			} else if (tokenizer.keyword() == Keyword.RETURN) {
				compileReturn();
			}
		}
		
	}
	
	/**
	 * Compiles a let statement.
	 */
	private void compileLet() {
		consumeKeyword(Keyword.LET);
		
		String variableName = tokenizer.identifier();
		tokenizer.advance();
		
		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals("[")) {
			// variableName is an array reference
			
			writer.writePush(segmentFromKind(table.kindOf(variableName)),
								table.indexOf(variableName));
			
			consumeSymbol("[");
			compileExpression();
			consumeSymbol("]");
			
			writer.writeArithmetic(Command.ADD);
			
			consumeSymbol("=");
			compileExpression();
			consumeSymbol(";");
			
			writer.writePop(Segment.TEMP, 0);
			writer.writePop(Segment.POINTER, 1);
			writer.writePush(Segment.TEMP, 0);
			writer.writePop(Segment.THAT, 0);
			
		} else {
			consumeSymbol("=");
			compileExpression();
			consumeSymbol(";");
			
			writer.writePop(segmentFromKind(table.kindOf(variableName)),
							table.indexOf(variableName));
		}
	}
	
	/**
	 * Compiles an if statement, possibly including an
	 * else clause.
	 */
	private void compileIf() {		
		String trueLabel = "IF_TRUE" + ifCounter;
		String falseLabel = "IF_FALSE" + ifCounter;
		String endLabel = "IF_END" + ifCounter;
		ifCounter++;
		
		consumeKeyword(Keyword.IF);
		
		consumeSymbol("(");
		compileExpression();		
		consumeSymbol(")");
		
		writer.writeIf(trueLabel);
		writer.writeGoto(falseLabel);
		writer.writeLabel(trueLabel);
		
		consumeSymbol("{");
		compileStatements();
		consumeSymbol("}");
		
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyword() == Keyword.ELSE) {
			
			writer.writeGoto(endLabel);
			writer.writeLabel(falseLabel);
			
			consumeKeyword(Keyword.ELSE);
			consumeSymbol("{");
			compileStatements();
			consumeSymbol("}");
			
			writer.writeLabel(endLabel);
			
		} else {
			writer.writeLabel(falseLabel);
		}
		
	}
	
	/**
	 * Compiles a while statement.
	 */
	private void compileWhile() {
		String expLabel = "WHILE_EXP" + whileCounter;
		String endLabel = "WHILE_END" + whileCounter;
		whileCounter++;
		
		consumeKeyword(Keyword.WHILE);
		
		writer.writeLabel(expLabel);
		
		consumeSymbol("(");
		compileExpression();
		consumeSymbol(")");
		
		writer.writeArithmetic(Command.NOT);
		writer.writeIf(endLabel);
		
		consumeSymbol("{");
		compileStatements();
		consumeSymbol("}");
		
		writer.writeGoto(expLabel);
		writer.writeLabel(endLabel);
	}
	
	/**
	 * Compiles a do statement.
	 */
	private void compileDo() {
		consumeKeyword(Keyword.DO);
		
		String identifier = tokenizer.identifier();
		tokenizer.advance();
		
		String className;
		String subroutineName;
		int numArgs = 0;
		
		if (tokenizer.symbol().equals(".")) {
			if (table.kindOf(identifier) == null) {
				// identifier is a class name, so subroutine is a function or
				// constructor in this class or another
				className = identifier;

			} else {
				// identifier is an object name (some type of variable)
				// so subroutine must be a method
				className = table.typeOf(identifier);
				writer.writePush(segmentFromKind(table.kindOf(identifier)),
									table.indexOf(identifier));
				numArgs++;
			}
			
			consumeSymbol(".");
			
			subroutineName = tokenizer.identifier();
			tokenizer.advance();
			
		} else {
			// identifier is the name of a method in the current class
			// method is called on this
			className = this.className;
			subroutineName = identifier;
			
			writer.writePush(Segment.POINTER, 0);
			numArgs++;
		}
		
		consumeSymbol("(");
		numArgs += compileExpressionList();
		consumeSymbol(")");
		consumeSymbol(";");
		
		writer.writeCall(className + "." + subroutineName, numArgs);
		writer.writePop(Segment.TEMP, 0);
	}
	
	/**
	 * Compiles a return statement.
	 */
	private void compileReturn() {		
		consumeKeyword(Keyword.RETURN);
		
		if (!(tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals(";"))) {
			// not a void subroutine
			compileExpression();
			
		} else {
			// void subroutine
			writer.writePush(Segment.CONSTANT, 0);
		}
		
		writer.writeReturn();
		
		consumeSymbol(";");
	}
	
	/**
	 * Compiles an expression.
	 */
	private void compileExpression() {
		compileTerm();
		
		while (tokenizer.tokenType() == TokenType.SYMBOL
				&& OPS.contains(tokenizer.symbol())) {
			
			String operator = tokenizer.symbol();
			tokenizer.advance();
			
			compileTerm();
			
			if (operator.equals("*")) {
				writer.writeCall("Math.multiply", 2);
				
			} else if (operator.equals("/")) {
				writer.writeCall("Math.divide", 2);
				
			} else {
				writer.writeArithmetic(commandBinary(operator));
			}
			
		}
		
	}
	
	
	/**
	 * Compiles a term of an expression.
	 * If the current token is an identifier, it distinguishes
	 * between a variable, an array entry, or a subroutine call.
	 * This is done by checking if the next token is "[", "(", or ".".
	 * Any other token is not part of this term, and should not
	 * be advanced over.
	 */
	private void compileTerm() {		
		if (tokenizer.tokenType() == TokenType.INT_CONSTANT) {
			writer.writePush(Segment.CONSTANT, tokenizer.intValue());
			tokenizer.advance();
			
		} else if (tokenizer.tokenType() == TokenType.STRING_CONSTANT) {
			pushStringConstant(tokenizer.stringValue());
			tokenizer.advance();
			
		} else if (tokenizer.tokenType() == TokenType.KEYWORD
				&& KEYWORD_CONSTANTS.contains(tokenizer.keyword())) {
			consumeKeywordConstant();
			
		} else if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals("(")) {
			consumeSymbol("(");
			compileExpression();
			consumeSymbol(")");
			
		} else if (tokenizer.tokenType() == TokenType.SYMBOL
				&& UNARY_OPS.contains(tokenizer.symbol())) {
			String operator = tokenizer.symbol();
			tokenizer.advance();
			compileTerm();
			writer.writeArithmetic(commandUnary(operator));
			
		} else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
			String identifier = tokenizer.identifier();
			tokenizer.advance();
			
			if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals("[")) {
				// identifier is an array reference
				writer.writePush(segmentFromKind(table.kindOf(identifier)),
									table.indexOf(identifier));				
				consumeSymbol("[");
				compileExpression();
				consumeSymbol("]");
				
				writer.writeArithmetic(Command.ADD);
				writer.writePop(Segment.POINTER, 1);
				writer.writePush(Segment.THAT, 0);
				
			} else if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals("(")) {
				// identifier is the name of a method in the current class,
				// so the object it is called on is this.
				writer.writePush(Segment.POINTER, 0);
				consumeSymbol("(");
				int numArgs = compileExpressionList() + 1;
				consumeSymbol(")");
				
				writer.writeCall(className + "." + identifier, numArgs);
				
			} else if (tokenizer.tokenType() == TokenType.SYMBOL
					&& tokenizer.symbol().equals(".")) {
				
				if (table.kindOf(identifier) == null) {
					//identifier is a class name
					consumeSymbol(".");
					String subroutineName = tokenizer.identifier();
					tokenizer.advance();
					consumeSymbol("(");
					int numArgs = compileExpressionList();
					consumeSymbol(")");
					
					writer.writeCall(identifier + "." + subroutineName, numArgs);
					
				} else {
					//identifier is an object name
					String className = table.typeOf(identifier);
					writer.writePush(segmentFromKind(table.kindOf(identifier)),
							table.indexOf(identifier));
					
					consumeSymbol(".");
					
					String subroutineName = tokenizer.identifier();
					tokenizer.advance();
					
					consumeSymbol("(");
					int numArgs = compileExpressionList() + 1;
					consumeSymbol(")");
					
					writer.writeCall(className + "." + subroutineName, numArgs);
				}
				
			} else {
				// identifier just a single variable name
				writer.writePush(segmentFromKind(table.kindOf(identifier)),
									table.indexOf(identifier));
			}
		}
		
	}
	
	/**
	 * Compiles a (possibly empty) comma-separated list of
	 * expressions.
	 * Returns the number of expressions in the list, to be used
	 * when determined how many arguments are passed to a subroutine call.
	 * Produces vm code which will result in the results of each expression
	 * being placed on the stack in the order in which they occur in the list.
	 * @return The number of expressions in the list.
	 */
	private int compileExpressionList() {
		int numExpressions = 0;
		
		if (!(tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals(")"))) {
			
			compileExpression();
			numExpressions++;
			
			while (tokenizer.tokenType() == TokenType.SYMBOL
					&& tokenizer.symbol().equals(",")) {
				
				consumeSymbol(",");
				compileExpression();
				numExpressions++;
			}
		}
		
		return numExpressions;
	}
	
	
	
	
}
