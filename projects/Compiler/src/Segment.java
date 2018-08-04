public enum Segment {
	CONSTANT, ARGUMENT, LOCAL, STATIC, THIS, THAT, POINTER, TEMP;
	
	public String toString() {
		return this.name().toLowerCase();
	}
}
