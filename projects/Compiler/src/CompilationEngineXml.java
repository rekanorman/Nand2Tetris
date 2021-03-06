import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
public class CompilationEngineXml {
	
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
	
	private PrintWriter out;
	
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
	public CompilationEngineXml(JackTokenizer tokenizer, File outputFile) throws FileNotFoundException {
		this.tokenizer = tokenizer;
		this.out = new PrintWriter(outputFile);
	}
	
	/**
	 * Closes the output stream used to write output to the xml file.
	 * Should be called once the compilation is finished.
	 */
	public void closeOutputStream() {
		out.close();
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
			
			out.println(String.format("<keyword> %s </keyword>", keyword));
			tokenizer.advance();
			
		} else {
			throw new RuntimeException("Invalid syntax, expected keyword "
											+ keyword.toString());
		}
	}
	
	/**
	 * Version of consumeKeyword for when the keyword to be consumed
	 * is unknown. Writes the current keyword of the tokenizer to the
	 * output file.
	 */
	private void consumeKeyword() {
		out.println(String.format("<keyword> %s </keyword>",
										tokenizer.keyword()));
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
			
			out.println(String.format("<symbol> %s </symbol>",
												tokenizer.symbol()));
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
	 * Writes the appropriate xml to the output file.
	 */
	private void consumeIdentifier() {
		if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
			out.println(String.format("<identifier> %s </identifier>",
										tokenizer.identifier()));
			tokenizer.advance();
			
		} else {
			throw new RuntimeException("Invalid syntax, expected identifier got "
										+ tokenizer.tokenType().toString());
		}
	}
	
	/**
	 * Checks that the current token of the tokenizer is an int constant,
	 * and advances the tokenizer past it.
	 * Writes the appropriate xml to the output file.
	 */
	private void consumeIntConstant() {
		if (tokenizer.tokenType() == TokenType.INT_CONSTANT) {
			out.println(String.format("<integerConstant> %s </integerConstant>",
										tokenizer.intValue()));
			tokenizer.advance();
			
		} else {
			throw new RuntimeException("Invalid syntax, expected int constant got "
										+ tokenizer.tokenType().toString());
		}
	}
	
	/**
	 * Checks that the current token of the tokenizer is a string constant,
	 * and advances the tokenizer past it.
	 * Writes the appropriate xml to the output file.
	 */
	private void consumeStringConstant() {
		if (tokenizer.tokenType() == TokenType.STRING_CONSTANT) {
			out.println(String.format("<stringConstant> %s </stringConstant>",
										tokenizer.stringValue()));
			tokenizer.advance();
			
		} else {
			throw new RuntimeException("Invalid syntax, expected string constant got "
										+ tokenizer.tokenType().toString());
		}
	}
	
	/**
	 * Consumes either an INT, CHAR, or BOOLEAN keyword, or an identifier,
	 * depending on the next token.
	 */
	private void consumeType() {
		if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
			consumeIdentifier();
			
		} else if (tokenizer.keyword() == Keyword.INT) {
			consumeKeyword(Keyword.INT);
			
		} else if (tokenizer.keyword() == Keyword.CHAR) {
			consumeKeyword(Keyword.CHAR);
			
		} else if (tokenizer.keyword() == Keyword.BOOLEAN) {
			consumeKeyword(Keyword.BOOLEAN);
			
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
		
		out.println("<class>");
		consumeKeyword(Keyword.CLASS);
		consumeIdentifier();
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
		
		out.println("<symbol> } </symbol>");
//		consumeSymbol("}");
		
		out.println("</class>");
	}
	
	/**
	 * Compiles a single static variable declaration
	 * or field declaration.
	 */
	private void compileClassVarDec() {
		out.println("<classVarDec>");
		
		if (tokenizer.keyword() == Keyword.STATIC) {
			consumeKeyword(Keyword.STATIC);
		} else {
			consumeKeyword(Keyword.FIELD);
		}
		
		consumeType();
		consumeIdentifier();
		
		while (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals(",")) {
			consumeSymbol(",");
			consumeIdentifier();
		}
		
		consumeSymbol(";");
		
		out.println("</classVarDec>");
	}
	
	/**
	 * Compiles a complete method, function or constructor,
	 * including header and body.
	 */
	private void compileSubroutineDec() {
		out.println("<subroutineDec>");
		
		if (tokenizer.keyword() == Keyword.CONSTRUCTOR) {
			consumeKeyword(Keyword.CONSTRUCTOR);
			
		} else if (tokenizer.keyword() == Keyword.FUNCTION) {
			consumeKeyword(Keyword.FUNCTION);
			
		} else {
			consumeKeyword(Keyword.METHOD);
		}
		
		
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyword() == Keyword.VOID) {
			consumeKeyword(Keyword.VOID);
		} else {
			consumeType();
		}
		
		consumeIdentifier();
		consumeSymbol("(");
		compileParameterList();
		consumeSymbol(")");
		compileSubroutineBody();
		
		out.println("</subroutineDec>");
	}
	
	/**
	 * Compiles a (possibly empty) parameter list,
	 * not including the enclosing parentheses.
	 */
	private void compileParameterList() {
		out.println("<parameterList>");
		
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& (tokenizer.keyword() == Keyword.INT
				|| tokenizer.keyword() == Keyword.CHAR
				|| tokenizer.keyword() == Keyword.BOOLEAN)
				|| tokenizer.tokenType() == TokenType.IDENTIFIER) {
			
			consumeType();
			consumeIdentifier();
			
			while (tokenizer.tokenType() == TokenType.SYMBOL
					&& tokenizer.symbol().equals(",")) {
				consumeSymbol(",");
				consumeType();
				consumeIdentifier();
			}
		}
			
		out.println("</parameterList>");
	}
	
	/**
	 * Compiles and body of a subroutine.
	 */
	private void compileSubroutineBody() {
		out.println("<subroutineBody>");
		
		consumeSymbol("{");
		
		while (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyword() == Keyword.VAR) {
			compileVarDec();
		}
		
		compileStatements();
		consumeSymbol("}");
		
		out.println("</subroutineBody>");
	}
	
	/**
	 * Compiles a local variable declaration.
	 */
	private void compileVarDec() {
		out.println("<varDec>");
		
		consumeKeyword(Keyword.VAR);
		consumeType();
		consumeIdentifier();
		
		while (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals(",")) {
			
			consumeSymbol(",");
			consumeIdentifier();
		}
		
		consumeSymbol(";");
		
		out.println("</varDec>");
	}
	
	/**
	 * Compiles a sequence of zero or more statements,
	 * not including the enclosing curly braces.
	 */
	private void compileStatements() {
		out.println("<statements>");
		
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
		
		out.println("</statements>");
	}
	
	/**
	 * Compiles a let statement.
	 */
	private void compileLet() {
		out.println("<letStatement>");
		
		consumeKeyword(Keyword.LET);
		consumeIdentifier();
		
		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals("[")) {
			
			consumeSymbol("[");
			compileExpression();
			consumeSymbol("]");
		}
		
		consumeSymbol("=");
		compileExpression();
		consumeSymbol(";");
		
		out.println("</letStatement>");
	}
	
	/**
	 * Compiles an if statement, possibly including an
	 * else clause.
	 */
	private void compileIf() {
		out.println("<ifStatement>");
		
		consumeKeyword(Keyword.IF);
		consumeSymbol("(");
		compileExpression();
		consumeSymbol(")");
		consumeSymbol("{");
		compileStatements();
		consumeSymbol("}");
		
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyword() == Keyword.ELSE) {
			
			consumeKeyword(Keyword.ELSE);
			consumeSymbol("{");
			compileStatements();
			consumeSymbol("}");
		}
		
		out.println("</ifStatement>");
	}
	
	/**
	 * Compiles a while statement.
	 */
	private void compileWhile() {
		out.println("<whileStatement>");
		
		consumeKeyword(Keyword.WHILE);
		consumeSymbol("(");
		compileExpression();
		consumeSymbol(")");
		consumeSymbol("{");
		compileStatements();
		consumeSymbol("}");
		
		out.println("</whileStatement>");
	}
	
	/**
	 * Compiles a do statement.
	 */
	private void compileDo() {
		out.println("<doStatement>");
		
		consumeKeyword(Keyword.DO);
		consumeIdentifier();
		
		if (tokenizer.symbol().equals(".")) {
			consumeSymbol(".");
			consumeIdentifier();
		}
		
		consumeSymbol("(");
		compileExpressionList();
		consumeSymbol(")");
		consumeSymbol(";");
		
		out.println("</doStatement>");
	}
	
	/**
	 * Compiles a return statement.
	 */
	private void compileReturn() {
		out.println("<returnStatement>");
		
		consumeKeyword(Keyword.RETURN);
		
		if (!(tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals(";"))) {
			
			compileExpression();
		}
		
		consumeSymbol(";");
		
		out.println("</returnStatement>");
	}
	
	/**
	 * Compiles an expression.
	 */
	private void compileExpression() {
		out.println("<expression>");
		
		compileTerm();
		
		while (tokenizer.tokenType() == TokenType.SYMBOL
				&& OPS.contains(tokenizer.symbol())) {
			
			consumeSymbol(null);
			compileTerm();
		}
		
		out.println("</expression>");
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
		out.println("<term>");
		
		if (tokenizer.tokenType() == TokenType.INT_CONSTANT) {
			consumeIntConstant();
			
		} else if (tokenizer.tokenType() == TokenType.STRING_CONSTANT) {
			consumeStringConstant();
			
		} else if (tokenizer.tokenType() == TokenType.KEYWORD
				&& KEYWORD_CONSTANTS.contains(tokenizer.keyword())) {
			consumeKeyword();
			
		} else if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals("(")) {
			consumeSymbol("(");
			compileExpression();
			consumeSymbol(")");
			
		} else if (tokenizer.tokenType() == TokenType.SYMBOL
				&& UNARY_OPS.contains(tokenizer.symbol())) {
			consumeSymbol(null);
			compileTerm();
			
		} else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
			consumeIdentifier();
			
			if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals("[")) {
				consumeSymbol("[");
				compileExpression();
				consumeSymbol("]");
				
			} else if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals("(")) {
				consumeSymbol("(");
				compileExpressionList();
				consumeSymbol(")");
				
			} else if (tokenizer.tokenType() == TokenType.SYMBOL
					&& tokenizer.symbol().equals(".")) {
				consumeSymbol(".");
				consumeIdentifier();
				consumeSymbol("(");
				compileExpressionList();
				consumeSymbol(")");
			}
		}
		
		out.println("</term>");
	}
	
	/**
	 * Compiles a (possibly empty) comma-separated list of
	 * expressions.
	 */
	private void compileExpressionList() {
		out.println("<expressionList>");
		
		if (!(tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol().equals(")"))) {
			
			compileExpression();
			
			while (tokenizer.tokenType() == TokenType.SYMBOL
					&& tokenizer.symbol().equals(",")) {
				
				consumeSymbol(",");
				compileExpression();
			}
		}
		
		out.println("</expressionList>");
	}
	
	
	
	
}
