package com.tenxerconsulting.swagger.doclet.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * The PathSanitizingTest represents a test case of api and resource path sanitizing
 * @version $Id$
 * @author conor.roche
 */
public class PathSanitizingTest {

	/**
	 * This tests the api path sanitization
	 */
	@Test
	public void testSanitizePath() {

		assertEquals("/api/test/{id}", ParserHelper.sanitizePath("/api/test/{id }"));

		assertEquals("/api", ParserHelper.sanitizePath("/api"));
		assertEquals("/api/test", ParserHelper.sanitizePath("/api/test"));
		assertEquals("/api/test{id}", ParserHelper.sanitizePath("/api/test{id}"));
		assertEquals("/api/test/{id}", ParserHelper.sanitizePath("/api/test/{id}"));
		assertEquals("/api/test/{id}", ParserHelper.sanitizePath("/api/test/{id }"));
		assertEquals("/api/test/{id}", ParserHelper.sanitizePath("/api/test/{id:}"));
		assertEquals("/api/test/{id}", ParserHelper.sanitizePath("/api/test/{id:[0-9]+}"));
		assertEquals("/api/test/{id}", ParserHelper.sanitizePath("/api/test/{id: [0-9]+}"));
		assertEquals("/api/test/{id}/2", ParserHelper.sanitizePath("/api/test/{id: [0-9]+}/2"));
		assertEquals("/api/test/{id}/{id2}", ParserHelper.sanitizePath("/api/test/{id: [0-9]+}/{id2: [0-9]+}"));
		assertEquals("/api/test/{id}/{id2}/test", ParserHelper.sanitizePath("/api/test/{id: [0-9]+}/{id2: [0-9]+}/test"));
	}

	/**
	 * This tests the file name for the resource listing is generated correctly from a resource path
	 */
	@Test
	public void testGenerateResourceFilename() {

		assertEquals("api", ParserHelper.generateResourceFilename("/api"));
		assertEquals("api_test", ParserHelper.generateResourceFilename("/api/test"));
		assertEquals("api_test_id", ParserHelper.generateResourceFilename("/api/test{id}"));
		assertEquals("api_test_id", ParserHelper.generateResourceFilename("/api/test/{id}"));
		assertEquals("api_test_id", ParserHelper.generateResourceFilename("/api/test/{id }"));
		assertEquals("api_test_id", ParserHelper.generateResourceFilename("/api/test/{id:}"));
		assertEquals("api_test_id", ParserHelper.generateResourceFilename("/api/test/{id:[0-9]+}"));
		assertEquals("api_test_id", ParserHelper.generateResourceFilename("/api/test/{id: [0-9]+}"));
		assertEquals("api_test_id_2", ParserHelper.generateResourceFilename("/api/test/{id: [0-9]+}/2"));
		assertEquals("api_test_id_id2", ParserHelper.generateResourceFilename("/api/test/{id: [0-9]+}/{id2: [0-9]+}"));
		assertEquals("api_test_id_id2_test", ParserHelper.generateResourceFilename("/api/test/{id: [0-9]+}/{id2: [0-9]+}/test"));
	}

}
