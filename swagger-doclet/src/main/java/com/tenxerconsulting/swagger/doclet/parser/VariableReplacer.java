package com.tenxerconsulting.swagger.doclet.parser;

import java.util.Properties;

/**
 * The VariableReplacer represents a util class for replacing variable references
 * in text with variable values from a properties file
 * @version $Id$
 * @author conorroche
 */
public final class VariableReplacer {

        private VariableReplacer() {}

	/**
	 * This does a replacement of variable references in the given value
	 * where references should be in the form ${propertyname}. It supports dereferencing the
	 * result too, e.g. if ${a} resolves to test${b} it would also replace the ${b} variable reference too.
	 * @param properties The properties used for replacement
	 * @param value The value to replace
	 * @return The replaced value
	 */
	public static final String replaceVariables(Properties properties, String value) {
                if (value == null || !value.contains("${")) {
			return value;
		}
		String prev;
		while (true) {
			prev = value;
                        value = innerReplaceVariables(properties, value);
                        if (prev.equals(value) || !value.contains("${")) {
				break;
			}
		}

		return value;
	}

	/**
	 * This does a single pass replacement of variable references in the given value
	 * where references should be in the form ${propertyname}
	 * @param properties The properties used for replacement
	 * @param value The value to replace
	 * @return The replaced value
	 */
        private static final String innerReplaceVariables(Properties properties, String value) {
		// this does a single pass over the value replacing any properties it finds
		if (value != null && value.length() > 3) {
			boolean inVar = false;
			StringBuilder buf = new StringBuilder();
			StringBuilder varName = new StringBuilder();
			char prev = value.charAt(0);
			for (int i = 1; i < value.length(); i++) {
				char c = value.charAt(i);
				if (c == '{' && prev == '$') {
					inVar = true;
				} else if (inVar) {

					if (c == '}') {
						// handle reaching the end of the variable
						inVar = false;
						String propVal = properties.getProperty(varName.toString());
						if (propVal == null) {
							buf.append("${").append(varName.toString()).append('}');
						} else {
							buf.append(propVal);
						}
						varName.delete(0, varName.length());
					} else {
						varName.append(c);
					}
				} else {
					// if prev was a dollar and we aren't at a variable start add it
					// or if first char that isnt start of variable add it
					if (i == 1 || prev == '$') {
						buf.append(prev);
					}
					// dont append a $ here as we need to check next char unless its last
					if (c != '$' || i == value.length() - 1) {
						buf.append(c);
					}
				}
				prev = c;
			}
			return buf.toString();
		}
		return value;
	}

}
