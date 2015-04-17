package depspace.extension;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import depspace.general.DepSpaceConfiguration;
import depspace.general.DepSpaceException;


public class EDSExtensionCompiler {

	private final ExtensionSource source;
	private ExtensionBinary extensionBinary;

	
	public EDSExtensionCompiler(String extensionName, String extensionCode) {
		this.source = new ExtensionSource(extensionName, extensionCode);		
	}
	

	// #################
	// # CODE ANALYSIS #
	// #################

	private static final Pattern FOR_EACH_PATTERN = Pattern.compile("for\\([a-zA-Z0-9]+ [a-zA-Z0-9]+: [a-zA-Z0-9]+\\)");
	
	
	private void analyzeExtension() throws DepSpaceException {
		// Check extension size
		if(source.getCode().length() > DepSpaceConfiguration.MAX_EXTENSION_CODE_SIZE) throw new DepSpaceException("An extension's source code may comprise at most " + DepSpaceConfiguration.MAX_EXTENSION_CODE_SIZE + " characters: found " + source.getCode().length() + " characters");
		
		// Analyze extension line by line
		try {
			BufferedReader reader = new BufferedReader(new StringReader(source.getCode()));
			for(String line = reader.readLine(); line != null; line = reader.readLine()) {
				// Ignore empty lines
				if(line.isEmpty()) continue;
				
				// Ignore line comments
				String trimmedLine = line.trim();
				if(trimmedLine.startsWith("//")) continue;
				
				// Do not allow new objects to be created
				if(trimmedLine.contains("new")) throw new DepSpaceException("An extension must not create own objects: \"new\" pattern found in line \"" + trimmedLine + "\"");
				
				// Do not allow loops other than for-each loops
				if(trimmedLine.contains("while")) throw new DepSpaceException("An extension is only allowed to use for-each loops: \"while\" pattern found in line \"" + trimmedLine + "\"");
				if(trimmedLine.contains("for")) {
					Matcher matcher = FOR_EACH_PATTERN.matcher(trimmedLine);
					if(!matcher.find()) throw new DepSpaceException("An extension is only allowed to use for-each loops matching the pattern \"" + FOR_EACH_PATTERN + "\": bad for loop found in line \"" + trimmedLine + "\"");
				}
				
				// Do not allow nondeterministic methods to be called
				if(trimmedLine.contains("hashCode()")) throw new DepSpaceException("An extension is not allowed to use the Object.hashCode() method: \"hashCode()\" pattern found in line \"" + trimmedLine + "\"");
				
				// Do not allow blocking methods to be called
				if(trimmedLine.contains("wait()")) throw new DepSpaceException("An extension is not allowed to use the Object.wait() method: \"wait()\" pattern found in line \"" + trimmedLine + "\"");
			}
			reader.close();
		} catch(IOException ioe) {
			throw new DepSpaceException("Error during extension-code analysis: " + ioe);
		}
	}

	
	// ###############
	// # COMPILATION #
	// ###############

	public byte[] compileExtension() throws DepSpaceException {
		// Analyze source code
		if(DepSpaceConfiguration.ENABLE_SANDBOX) analyzeExtension();
		
		// Compile extension
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		ExtensionFileManager fileManager = new ExtensionFileManager(compiler);
		Iterable<ExtensionSource> compilationUnits = Arrays.asList(source);
		CompilationTask compilationTask = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
		Boolean success = compilationTask.call();
		if((success == null) || !success) throw new DepSpaceException("Error during extension compilation");
		
		// Return extension binary
		if((extensionBinary == null) || (extensionBinary.getBinary() == null)) throw new DepSpaceException("Error during extension binary creation");
		return extensionBinary.getBinary();
	}
	
	
	// ##################
	// # HELPER CLASSES #
	// ##################

	/*
	 * Helper class for compiling code represented as a String object.
	 */
	private static class ExtensionSource extends SimpleJavaFileObject {
		
		private final String code;
		
		
		public ExtensionSource(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}
		
		
		public String getCode() {
			return code;
		}
		
		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return code;
		}
	
	}

	
	/* 
	 * Helper class for writing a class binary to a byte array.
	 */
	private static class ExtensionBinary extends SimpleJavaFileObject {
		
		private final ByteArrayOutputStream stream;
		private byte[] binary;
		
		
		public ExtensionBinary(String className, Kind kind) {
			super(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind);
			this.stream = new ByteArrayOutputStream();
		}
		
		
		public byte[] getBinary() {
			return binary;
		}
		
		@Override
		public OutputStream openOutputStream() throws IOException {
			return stream;
		}
		
		public void complete() {
			try {
				stream.close();
				binary = stream.toByteArray();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
	}

	
	/* 
	 * Helper class for forcing the compiler to use an ExtensionBinary object as output.
	 */
	private class ExtensionFileManager implements JavaFileManager {

		private final JavaFileManager fileManager;
		
		
		public ExtensionFileManager(JavaCompiler compiler) {
			this.fileManager = compiler.getStandardFileManager(null, null, null);
		}
		
		
		@Override
		public boolean hasLocation(Location location) {
			return fileManager.hasLocation(location);
		}

		@Override
		public ClassLoader getClassLoader(Location location) {
			return fileManager.getClassLoader(location);
		}

		@Override
		public String inferBinaryName(Location location, JavaFileObject file) {
			return fileManager.inferBinaryName(location, file);
		}

		@Override
		public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
			return fileManager.list(location, packageName, kinds, recurse);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
			extensionBinary = new ExtensionBinary(className, kind);
			return extensionBinary;
		}

		@Override
		public void flush() throws IOException {
			if(extensionBinary != null) extensionBinary.complete();
		}

		@Override
		public int isSupportedOption(String option) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isSameFile(FileObject a, FileObject b) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean handleOption(String current, Iterator<String> remaining) {
			throw new UnsupportedOperationException();
		}

		@Override
		public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() throws IOException {
			throw new UnsupportedOperationException();
		}
		
	}
	
}
