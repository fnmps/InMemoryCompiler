package com.fnmps.compilers;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fnmps.compilers.InMemoryCompiler.InMemoryCompilerSource;

class InMemoryCompilerTest {

	InMemoryCompiler classUnderTest = new InMemoryCompiler();

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testCompileMultiple() {

		List<InMemoryCompilerSource> sources = new ArrayList<>();
		sources.add(new InMemoryCompilerSource("Test", "package com.fnmps; public class Test {}"));
		sources.add(new InMemoryCompilerSource("Test1", "package com.fnmps; public class Test1 {}"));
		sources.add(new InMemoryCompilerSource("Test", "package com.fnmps.tests; public class Test {}"));

		List<Class<?>> actual = classUnderTest.compile(sources);
		
		Assertions.assertEquals(3, actual.size());
		actual.forEach(c -> {
			try {
				c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				Assertions.fail("Exception thrown: " + e.getMessage());
			}
		});
	}

}
