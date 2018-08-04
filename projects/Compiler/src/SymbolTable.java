import java.util.HashMap;

public class SymbolTable {
	
	private HashMap<Kind, Integer> counts = new HashMap<Kind, Integer>();
	
	private HashMap<String, Entry> classTable = new HashMap<String, Entry>();
	
	private HashMap<String, Entry> subroutineTable = new HashMap<String, Entry>();
	
	/**
	 * Creates a new SymbolTable which manages all the identifiers
	 * for one jack class.
	 */
	public SymbolTable() {
		for (Kind kind: Kind.values()) {
			counts.put(kind, 0);
		}
	}
	
	/**
	 * Enters the scope of a new subroutine by resetting the
	 * subroutine symbol table.
	 * Called when the compilation of a new subroutine is
	 * started.
	 */
	public void startSubroutine() {
		counts.put(Kind.ARG, 0);
		counts.put(Kind.VAR, 0);
		
		subroutineTable.clear();
	}
	
	/**
	 * Adds a new variable with the given name, type and kind to
	 * the symbol table, and assigns it a running index, based on its
	 * kind. Increments the count for the appropriate variable kind.
	 * 
	 * Variables of kind STATIC or FIELD are added to the class level
	 * symbol table, while those of kind ARG or VAR are added to the
	 * subroutine level symbol table.
	 * Called when a variable declaration is compiled.
	 * 
	 * @param name	The identifier of the variable.
	 * @param type	The type of the variable (one of the primitive types
	 * 					or a class name).
	 * @param kind	One of the values STATIC, FIELD, ARG and VAR.
	 */
	public void define(String name, String type, Kind kind) {
		int index = counts.get(kind);
		counts.put(kind, index + 1);
		
		Entry entry = new Entry(type, kind, index);
		
		if (kind == Kind.STATIC || kind == Kind.FIELD) {
			classTable.put(name, entry);
		} else {
			subroutineTable.put(name, entry);
		}
	}
	
	/**
	 * Returns the number of variables of the given kind already defined
	 * in the current scope.
	 * @param kind	The kind of variable of interest.
	 * @return		The number of variables of the given kind currently defined
	 * 				in the symbol table of the current scope.
	 */
	public int varCount(Kind kind) {
		return counts.get(kind);
	}
	
	/**
	 * Returns the kind of the variable with the given name in the
	 * current scope. If the given name is not defined in the current
	 * scope, return null.
	 * @param name	The name of the variable of interest.
	 * @return		The kind of the given variable, or null if it is undefined.
	 */
	public Kind kindOf(String name) {
		if (subroutineTable.containsKey(name)) {
			return subroutineTable.get(name).getKind();
			
		} else if (classTable.containsKey(name)) {
			return classTable.get(name).getKind();
			
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the type of the variable with the given name in the
	 * current scope.
	 * Throws an error if the variable is not defined in the current scope.
	 * @param name	The name of the variable of interest.
	 * @return		The type of the given variable.
	 */
	public String typeOf(String name) {
		if (subroutineTable.containsKey(name)) {
			return subroutineTable.get(name).getType();
			
		} else if (classTable.containsKey(name)) {
			return classTable.get(name).getType();
			
		} else {
			throw new RuntimeException(String.format(
						"%s not defined in current scope",
						name));
		}	
	}
	
	/**
	 * Returns the index assigned to the variable with the given name
	 * in the current scope.
	 * Throws an exception if the variable is not defined in the current scope.
	 * @param name	The name of the variable of interest.
	 * @return		The index assigned to the given variable.
	 */
	public int indexOf(String name) {
		if (subroutineTable.containsKey(name)) {
			return subroutineTable.get(name).getIndex();
			
		} else if (classTable.containsKey(name)) {
			return classTable.get(name).getIndex();
			
		} else {
			throw new RuntimeException(String.format(
						"%s not defined in current scope",
						name));
		}
	}
	
	
	
}
