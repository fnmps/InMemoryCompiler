package com.fnmps.compilers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InMemoryCompiler {

	private static final Logger LOG = LogManager.getLogger(InMemoryCompiler.class);

	private JavaCompiler tool;
	private StandardJavaFileManager stdManager;

	public InMemoryCompiler() {
		tool = ToolProvider.getSystemJavaCompiler();
		if (tool == null) {
			throw new CompilationException(
					"Could not get Java compiler. Please, ensure that JDK is used instead of JRE.");
		}
		stdManager = tool.getStandardFileManager(null, null, null);
	}

	public List<Class<?>> compile(List<InMemoryCompilerSource> javaSources) {
		Map<String, byte[]> compilationResult = compileSources(javaSources);
		if (compilationResult == null) {
			throw new CompilationException("Nothing to compile");
		}
		List<Class<?>> result = new ArrayList<>(javaSources.size());
		try (MemoryClassLoader classLoader = new MemoryClassLoader(compilationResult)) {
			for (String className : compilationResult.keySet()) {
				result.add(classLoader.loadClass(className));
			}
			return result;
		} catch (IOException | ClassNotFoundException e) {
			throw new CompilationException(e);
		}
	}

	private Map<String, byte[]> compileSources(final List<InMemoryCompilerSource> javaSources) {
		InMemoryFileManager fileManager = new InMemoryFileManager(stdManager);

		List<JavaFileObject> compUnits = new ArrayList<>();
		javaSources.stream().forEach(entry -> compUnits
				.add(InMemoryFileManager.makeStringSource(entry.getClassName() + ".java", entry.getSourceCode())));
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

		CompilationTask task = tool.getTask(null, fileManager, diagnostics, null, null, compUnits);

		if (!task.call()) {
			for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
				switch (diagnostic.getKind()) {
				case ERROR:
				case MANDATORY_WARNING:
					LOG.error(diagnostic.getMessage(null));
					break;
				case NOTE:
				case OTHER:
				case WARNING:
				default:
					LOG.warn(diagnostic.getMessage(null));
				}
			}
		}

		Map<String, byte[]> classBytes = fileManager.getClassBytes();
		try {
			fileManager.close();
		} catch (IOException e) {
			throw new CompilationException(e);
		}

		return classBytes;
	}

	public static class InMemoryCompilerSource {
		private String className;
		private String sourceCode;

		public InMemoryCompilerSource(String className, String sourceCode) {
			this.className = className;
			this.sourceCode = sourceCode;
		}

		public String getClassName() {
			return className;
		}

		public String getSourceCode() {
			return sourceCode;
		}
	}
}