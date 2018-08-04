public enum Kind {
	STATIC, FIELD, ARG, VAR;
	
	public String toString() {
		return this.name().toLowerCase();
	}
}
