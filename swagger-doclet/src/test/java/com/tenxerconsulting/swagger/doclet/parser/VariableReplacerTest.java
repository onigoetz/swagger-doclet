package com.tenxerconsulting.swagger.doclet.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * The VariableReplacerTest represents
 * @version $Id$
 * @author conor.roche
 */
public class VariableReplacerTest {

	/**
	 * This tests simple e.g. single level replacement
	 */
	@Test
	public void testSimpleReplacement() {
		Properties props = new Properties();
		props.setProperty("a", "aval");
		props.setProperty("b", "bval");

		String val = "a ${a}";
		assertEquals("a aval", VariableReplacer.replaceVariables(props, val));
		val = "${a}";
		assertEquals("aval", VariableReplacer.replaceVariables(props, val));
		val = "$a";
		assertEquals("$a", VariableReplacer.replaceVariables(props, val));
		val = "${a}${b}";
		assertEquals("avalbval", VariableReplacer.replaceVariables(props, val));
		val = "${a} $${b}";
		assertEquals("aval $bval", VariableReplacer.replaceVariables(props, val));

	}

	/**
	 * This tests multi level replacement where one value refers to other variables
	 */
	@Test
	public void testMultiLevelReplacement() {
		Properties props = new Properties();
		props.setProperty("a", "aval");
		props.setProperty("b", "${a}");
		props.setProperty("c", "c${b}");
		props.setProperty("d", "a:${a} b:${b} c:${c} e:${e}");

		String val = "res${d}";
		assertEquals("resa:aval b:aval c:caval e:${e}", VariableReplacer.replaceVariables(props, val));
	}

}
