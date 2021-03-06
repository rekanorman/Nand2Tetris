import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class JackCompiler {
	
	/**
	 * Takes a File object representing a single xxx.jack file, creates a
	 * JackTokenizer from the file, and outputs all the tokens into a
	 * new file called myxxxT.xml in the same directory as the jack file.
	 * @param jackFile	The .jack file to tokenize.
	 */
	private static void generateTokenFile(File jackFile) {		
		try {
			JackTokenizer tokenizer = new JackTokenizer(jackFile);
			
			String outputFilename = "my" + jackFile.getName().replace(".jack", "T.xml");
			File outputFile = new File(jackFile.getParentFile(), outputFilename);
			
			PrintWriter out = new PrintWriter(outputFile);
			
			out.println("<tokens>");
			
			while (tokenizer.hasMoreTokens()) {
				tokenizer.advance();
				TokenType type = tokenizer.tokenType();
				String value = tokenizer.tokenValue();
				out.println(String.format("<%s> %s </%s>", type, value, type));
			}
			
			out.println("</tokens>");
			
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}
	
	/**
	 * Takes a File object representing a single xxx.jack file, creates a
	 * JackTokenizer from the file, and parses the file, outputing the generated
	 * xml into a new file called myxxx.xml in the same directory as the jack file.
	 * @param jackFile	The .jack file to tokenize and parse.
	 */
	private static void generateXmlFile(File jackFile) {
		try {
			JackTokenizer tokenizer = new JackTokenizer(jackFile);
			
			String outputFilename = "my" + jackFile.getName().replace(".jack", ".xml");
			File outputFile = new File(jackFile.getParentFile(), outputFilename);
			
			CompilationEngine compilationEngine = new CompilationEngine(tokenizer, outputFile);
			compilationEngine.compileClass();
			compilationEngine.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Takes a File object representing a single xxx.jack file, and uses
	 * the JackTokenizer, SymbolTable, CompilationEngine and VMWriter to
	 * generate the appropriate VM code, storing it in a new file called
	 * xxx.vm in the same directory.
	 * @param jackFile		The .jack file to compile.
	 */
	private static void generateVMFile(File jackFile) {
		try {
			JackTokenizer tokenizer = new JackTokenizer(jackFile);
			
			String outputFilename = jackFile.getName().replace(".jack", ".vm");
			File outputFile = new File(jackFile.getParentFile(), outputFilename);
			
			CompilationEngine compilationEngine = new CompilationEngine(tokenizer, outputFile);
			compilationEngine.compileClass();
			compilationEngine.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Takes a .jack filename, or a directory containing zero
	 * or more .jack files as a command-line argument.
	 * For each .jack file:
	 * - creates a JackTokenizer for the file
	 * - creates a new .xml file with the same name in the
	 * 		same directory and prepares it for writing
	 * - creates and uses a CompilationEngine to compile the
	 * 		input from the tokenizer into the output .xml file. 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new RuntimeException("Argument must be a single file or directory name");
		}
		
		String fileOrDirectory = args[0];
		ArrayList<File> jackFiles = new ArrayList<File>();
		
		if (fileOrDirectory.endsWith(".jack")) {
			String filename = fileOrDirectory;
			jackFiles.add(new File(filename));
			
		} else {
			File directory = new File(fileOrDirectory);
			for (File file: directory.listFiles()) {
				if (file.getName().endsWith(".jack")) {
					jackFiles.add(file);
				}
			}
		}
		
		for (File jackFile: jackFiles) {
			generateVMFile(jackFile);
		}

	}
	
	
	
	
	
	
}
