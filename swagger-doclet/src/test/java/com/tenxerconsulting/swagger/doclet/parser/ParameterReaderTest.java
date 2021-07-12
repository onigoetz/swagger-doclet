package com.tenxerconsulting.swagger.doclet.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * The ParameterReaderTest represents a test case for the parameter reader
 * @version $Id$
 * @author conor.roche
 */
class ParameterReaderTest {

	/**
	 * This tests the add path params method can successfull
	 * extract parameters from an expression
	 */
	@Test
        void testAddPathParams() {
		ParameterReader r = new ParameterReader(null, null);

		List<String> items = new ArrayList<>();

		List<String> expected = Arrays.asList("a");
		r.addPathParams("/{a}/test", items);
		assertEquals(expected, items);

		items.clear();
		r.addPathParams("{a}", items);
		assertEquals(expected, items);

		items.clear();
		expected = Arrays.asList("a", "b");
		r.addPathParams("/{a}/{b}", items);
		assertEquals(expected, items);

		items.clear();
		r.addPathParams("/{a: [0-9]+}/{b}", items);
		assertEquals(expected, items);

		items.clear();
		r.addPathParams("/{a: [0-9]+}/{b: [A-Za-z0-9]+}", items);
		assertEquals(expected, items);

	}

}
