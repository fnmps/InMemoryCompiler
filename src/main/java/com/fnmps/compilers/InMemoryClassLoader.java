package com.fnmps.compilers;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

/**
 * ClassLoader that loads .class bytes from memory.
 */
final class MemoryClassLoader extends URLClassLoader {
	private Map<String, byte[]> classBytes;

	public MemoryClassLoader(Map<String, byte[]> classBytes) {
		super(new URL[0], ClassLoader.getSystemClassLoader());
		this.classBytes = classBytes;
	}

	public Class<?> load(String className) throws ClassNotFoundException {
		return loadClass(className);
	}

	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException {
		byte[] buf = classBytes.get(className);
		if (buf != null) {
			// clear the bytes in map -- we don't need it anymore
			classBytes.put(className, null);
			return defineClass(className, buf, 0, buf.length);
		} else {
			return super.findClass(className);
		}
	}

}