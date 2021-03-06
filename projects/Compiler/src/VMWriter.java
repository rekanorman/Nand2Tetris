import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class VMWriter {
	
	private PrintWriter out;
	
	/**
	 * Creates a new VMWriter which writes VM code to the given
	 * output file.
	 * @param outputFile	The output file to write VM code to.
	 * @throws FileNotFoundException 
	 */
	public VMWriter(File outputFile) throws FileNotFoundException {
		this.out = new PrintWriter(outputFile);
	}
	
	/**
	 * Closes the output stream used for writing output to the output
	 * file. Should be called once the compilation of the class is complete.
	 */
	public void closeOutputFile() {
		out.close();
	}
	
	/**
	 * Writes a push command using the given memory segment and index
	 * to the output file.
	 * @param segment	The memory segment to be used in the push command.
	 * @param index		The memory index to be used in the push command.
	 */
	public void writePush(Segment segment, int index) {
		out.println(String.format("push %s %s", segment, index));
	}
	
	/**
	 * Writes a pop command using the given memory segment and index
	 * to the output file.
	 * @param segment	The memory segment to be used in the pop command.
	 * @param index		The memory index to be used in the pop command.
	 */
	public void writePop(Segment segment, int index) {
		out.println(String.format("pop %s %s", segment, index));
	}
	
	/**
	 * Writes the given VM arithmetic or logical command to the output file.
	 * @param command	The command to write.
	 */
	public void writeArithmetic(Command command) {
		out.println(command);
	}
	
	/**
	 * Writes a VM label command using the given label.
	 * @param label	The label to be used in the label command.
	 */
	public void writeLabel(String label) {
		out.println(String.format("label %s", label));
	}
	
	/**
	 * Writes a VM goto command using the given label.
	 * @param label		The label to be used in the goto command.
	 */
	public void writeGoto(String label) {
		out.println(String.format("goto %s", label));
	}
	
	/**
	 * Writes a VM if-goto command using the given label.
	 * @param label		The label to be used in the if-goto command.
	 */
	public void writeIf(String label) {
		out.println(String.format("if-goto %s", label));
	}
	
	/**
	 * Writes a VM call command using the given function name and
	 * number of arguments.
	 * @param name		The name of the function being called.
	 * @param numArgs	The number of arguments being passed to the function.
	 */
	public void writeCall(String name, int numArgs) {
		out.println(String.format("call %s %s", name, numArgs));
	}
	
	/**
	 * Writes a VM function command for a function with the given name
	 * and number of local variables.
	 * @param name		The name of the function.
	 * @param numLocals	The number of local variables of the function.
	 */
	public void writeFunction(String name, int numLocals) {
		out.println(String.format("function %s %s", name, numLocals));
	}
	
	/**
	 * Writes a VM return command.
	 */
	public void writeReturn() {
		out.println("return");
	}
	
	
	
	
	
	
}
