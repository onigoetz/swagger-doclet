package com.tenxerconsulting.swagger.doclet.translator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;

/**
 * The FirstNotNullTranslator represents a chain of translators.
 * It will iterate through each one in the chain and return the results of the first translator
 * that does not return null.
 * @version $Id$
 */
public class FirstNotNullTranslator implements Translator {

	private final List<Translator> chain;

	/**
	 * This creates a FirstNotNullTranslator with no links
	 */
	public FirstNotNullTranslator() {
                this.chain = new ArrayList<>();
	}

	/**
	 * This adds another translator link to the chain of translators
	 * @param link The translator to add
	 * @return this
	 */
	public FirstNotNullTranslator addNext(Translator link) {
		this.chain.add(link);
		return this;
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#typeName(com.sun.javadoc.Type, boolean, com.sun.javadoc.ClassDoc[])
	 */
	public OptionalName typeName(Type type, boolean useFqn, ClassDoc[] views) {
		return firstNotNullOf(translator -> translator.typeName(type, useFqn, views));
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#parameterTypeName(boolean, com.sun.javadoc.Parameter, com.sun.javadoc.Type, boolean,
	 *      com.sun.javadoc.ClassDoc[])
	 */
	public OptionalName parameterTypeName(boolean multipart, Parameter parameter, Type paramType, boolean useFqn, ClassDoc[] views) {
		return firstNotNullOf(translator -> translator.parameterTypeName(multipart, parameter, paramType, useFqn, views));
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#typeName(com.sun.javadoc.Type, boolean)
	 */
	public OptionalName typeName(Type type, boolean useFqn) {
		return firstNotNullOf(translator -> translator.typeName(type, useFqn));
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#fieldName(com.sun.javadoc.FieldDoc)
	 */
	public OptionalName fieldName(final FieldDoc field) {
		return firstNotNullOf(translator -> translator.fieldName(field));
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#methodName(com.sun.javadoc.MethodDoc)
	 */
	public OptionalName methodName(final MethodDoc method) {
		return firstNotNullOf(translator -> translator.methodName(method));
	}

	private OptionalName firstNotNullOf(Function<Translator, OptionalName> function) {
		OptionalName name = null;
		Iterator<Translator> iterator = this.chain.iterator();
		while ((name == null || name.isMissing()) && iterator.hasNext()) {
			name = function.apply(iterator.next());
		}
		return name;
	}

}
