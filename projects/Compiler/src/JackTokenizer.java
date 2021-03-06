import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class JackTokenizer {
	
	private final ArrayList<Character> SYMBOLS = new ArrayList<Character>(Arrays.asList(
									'{', '}', '(', ')', '[', ']', '.', ',', ';',
									'+', '-', '*', '/', '&', '|', '<', '>', '=', '~'));
	
	private HashMap<String, String> SPECIAL_SYMBOLS = new HashMap<String, String>();
		
	private String input;
	
	/**
	 * The type of the current token.
	 */
	private TokenType currentTokenType;
	
	
	private Keyword currentKeyword;
	private String currentSymbol;
	private String currentIdentifier;
	private int currentIntValue;
	private String currentStringValue;
	
	/**
	 * Opens the given .jack file and gets ready to tokenize it.
	 * @param inputFile		The .jack file to be tokenized.
	 * @throws IOException 
	 */
	public JackTokenizer(File inputFile) throws IOException {
		SPECIAL_SYMBOLS.put("<", "&lt;");
		SPECIAL_SYMBOLS.put(">", "&gt;");
		SPECIAL_SYMBOLS.put("\"", "&quot;");
		SPECIAL_SYMBOLS.put("&", "&amp;");
		
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		
		input = "";
		String line = null;
		while ((line = reader.readLine()) != null) {
			input += line + "\n";
		}
		
		reader.close();
		
		passWhiteSpace();
	}
	
	/**
	 * Returns true if there are more tokens in the input
	 * file, false otherwise.
	 * @return	true if there are more tokens, otherwise false.
	 */
	public boolean hasMoreTokens() {
		passWhiteSpace();
		return input.length() > 0;
	}
	
	/**
	 * Determines the next token in the input, and sets
	 * this as the current token.
	 * Should only be called if hasMoreTokens() returns true.
	 * Initially there is no current token until advance() is
	 * called for the first time.
	 */
	public void advance() {
		passWhiteSpace();
		
		char firstChar = input.charAt(0);
		
		if (SYMBOLS.contains(firstChar)) {
			readSymbol();
			
		} else if (firstChar == '"') {
			readStringConstant();
			
		} else if (Character.isDigit(firstChar)) {
			readIntConstant();
			
		} else if (Character.isLetter(firstChar) || firstChar == '_') {
			readKeywordOrIdentifier();
			
		} else {
			throw new RuntimeException("Invalid token character " + firstChar);
		}
	}
	
	/**
	 * Reads past any leading whitespace and comments in the input stream.
	 */
	private void passWhiteSpace() {
		boolean done = false;
		
		while (!done) {
			input = input.trim();
			if (input.startsWith("//")) {
				input = input.substring(input.indexOf("\n") + 1);
				
			} else if (input.startsWith("/**")) {
				input = input.substring(input.indexOf("*/") + 2);
				
			} else {
				done = true;
			}
		}
	}
	
	/**
	 * Reads the next symbol from the input string and removes it from the
	 * string. Sets currentTokenType and currentSymbol to the appropriate
	 * values, using the string representation of special characters if needed.
	 * Should only be called when the first character in the input string is
	 * one of the Jack symbols.
	 */
	private void readSymbol() {
		currentTokenType = TokenType.SYMBOL;
		String symbol = input.substring(0, 1);
		input = input.substring(1);
		
		if (SPECIAL_SYMBOLS.containsKey(symbol)) {
			symbol = SPECIAL_SYMBOLS.get(symbol);
		}
		
		currentSymbol = symbol;
	}
	
	/**
	 * Reads the next string constant from the input string and removes it and its
	 * enclosing double quotes from the string. Sets currentTokenType and
	 * currentStringValue to the appropriate values.
	 * Should only be called when the first character in the input string is
	 * a double quote.
	 */
	private void readStringConstant() {
		currentTokenType = TokenType.STRING_CONSTANT;
		int closingQuoteIndex = input.indexOf("\"", 1);
		currentStringValue = input.substring(1, closingQuoteIndex);
		input = input.substring(closingQuoteIndex + 1);
	}
	
	/**
	 * Reads the next int constant from the input string and removes it from the string.
	 * Sets currentTokenType and currentIntValue to the appropriate values.
	 * Should only be called when the first character in the input string is
	 * a digit.
	 */
	private void readIntConstant() {
		int i = 0;
		while (i < input.length() && Character.isDigit(input.charAt(i))) {
			i++;
		}
		
		currentTokenType = TokenType.INT_CONSTANT;
		currentIntValue = Integer.parseInt(input.substring(0, i));
		input = input.substring(i);
		
	}
	
	/**
	 * Reads the next sequence of letters, digits and _ from the input string, and
	 * removes it from the input string. Determines whether it is a keyword or an
	 * identifier, and sets currentTokenType and currentKeyword or currentIdentifier
	 * to the appropriate values.
	 * Should only be called when the first character in the input string is a
	 * letter or an _.
	 */
	private void readKeywordOrIdentifier() {
		int i = 0;
		while (i < input.length()
			   && (Character.isLetterOrDigit(input.charAt(i))
			       || input.charAt(i) == '_')) {
			i++;
		}
		
		String token = input.substring(0, i);
		input = input.substring(i);
		
		try {
			currentKeyword = Keyword.valueOf(token.toUpperCase());
			currentTokenType = TokenType.KEYWORD;
			
		} catch (IllegalArgumentException e) {
			currentIdentifier = token;
			currentTokenType = TokenType.IDENTIFIER;
		}		
	}
	
	/**
	 * Returns the type of the current token, as a constant
	 * of TokenType.
	 * @return	The type of the current token.
	 */
	public TokenType tokenType() {
		return currentTokenType;
	}
	
	/**
	 * Returns the actual value of the keyword which is the
	 * current token, as a constant of type Keyword.
	 * Should only be called if tokenType() returns KEYWORD.
	 * @return		The value of the keyword which is the current token.
	 */
	public Keyword keyword() {
		return currentKeyword;
	}
	
	/**
	 * Returns the value of the symbol which is the current
	 * token.
	 * Should only be called when tokenType() returns SYMBOL.
	 * If symbol is a special xml character, returns the appropriate string.
	 * @return 		The value of the symbol which is the current token.
	 */
	public String symbol() {
		return currentSymbol;
	}
	
	/**
	 * Returns the value of the identifier which is the current
	 * token.
	 * Should only be called when tokenType() returns IDENTIFIER.
	 * @return 		The value of the identifier which is the current token.
	 */
	public String identifier() {
		return currentIdentifier;
	}
	
	/**
	 * Returns the value of the integer constant which is the current
	 * token.
	 * Should only be called when tokenType() returns INT_CONSTANTT.
	 * @return 		The value of the integer constant which is the current token.
	 */
	public int intValue() {
		return currentIntValue;
	}
	
	/**
	 * Returns the value of the string constant which is the current
	 * token, without the enclosing double quotes.
	 * Should only be called when tokenType() returns STRING_CONSTANT.
	 * @return 		The value of the string constant which is the current token.
	 */
	public String stringValue() {
		return currentStringValue;
	}
	
	/**
	 * Returns the value of the current token as a String, regardless of
	 * the type of the token.
	 * @return	The value of the current token.
	 */
	public String tokenValue() {
		TokenType type = tokenType();
		
		switch (type) {
			case KEYWORD:			return keyword().toString();
			case SYMBOL: 			return symbol();
			case IDENTIFIER: 		return identifier();
			case INT_CONSTANT: 		return Integer.toString(intValue());
			case STRING_CONSTANT: 	return stringValue();
			default:				throw new RuntimeException("Invalid token type");
		}
	}
	
	
	
	
	
}
