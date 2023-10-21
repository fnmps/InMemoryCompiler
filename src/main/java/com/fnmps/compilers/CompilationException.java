package com.fnmps.compilers;

public class CompilationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CompilationException(Exception e) {
		super(e);
	}

	public CompilationException(String message) {
		super(message);
	}
	
	public CompilationException(String message, Exception e) {
		super(message, e);
	}

}
