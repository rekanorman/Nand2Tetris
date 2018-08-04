public enum Command {
	ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT;
	
	public String toString() {
		return this.name().toLowerCase();
	}
}
