public enum TokenType {
	KEYWORD("keyword"),
	SYMBOL("symbol"),
	IDENTIFIER("identifier"),
	INT_CONSTANT("integerConstant"),
	STRING_CONSTANT("stringConstant");
	
	private String string;
	
	TokenType(String string) {
		this.string = string;
	}
	
	public String toString() {
		return string;
	}
}
