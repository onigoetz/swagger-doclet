package com.tenxerconsulting.swagger.doclet.translator;

import static com.tenxerconsulting.swagger.doclet.translator.Translator.OptionalName.presentOrMissing;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.parser.ParserHelper;

/**
 * The NameBasedTranslator represents a translator that looks up names of items based on their
 * javadoc signature
 * @version $Id$
 */
public class NameBasedTranslator implements Translator {

	private DocletOptions options;

	/**
	 * This creates a NameBasedTranslator
	 * @param options
	 */
	public NameBasedTranslator(DocletOptions options) {
		super();
		this.options = options;
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#parameterTypeName(boolean, com.sun.javadoc.Parameter, com.sun.javadoc.Type, boolean,
	 *      com.sun.javadoc.ClassDoc[])
	 */
	public OptionalName parameterTypeName(boolean multipart, Parameter parameter, Type paramType, boolean useFqn, ClassDoc[] views) {
		if (paramType == null) {
			paramType = parameter.type();
		}
		// look for File data types
		if (multipart) {
			boolean isFileDataType = ParserHelper.isFileParameterDataType(parameter, this.options);
			if (isFileDataType) {
                                return presentOrMissing("internalFileSchema");
			}
		}

		return typeName(paramType, useFqn, views);
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#typeName(com.sun.javadoc.Type, boolean, com.sun.javadoc.ClassDoc[])
	 */
	public OptionalName typeName(Type type, boolean useFqn, ClassDoc[] views) {
		String[] typeFormat = ParserHelper.typeOf(type, useFqn, this.options);

		if (views != null && views.length > 0 && !ParserHelper.isPrimitive(type, this.options) && !"array".equals(typeFormat[0])) {
			StringBuilder nameWithView = new StringBuilder(typeFormat[0]).append("-");
			for (ClassDoc view : views) {
				nameWithView.append(view.name());
			}
			return presentOrMissing(nameWithView.toString(), typeFormat[1]);
		}

		return presentOrMissing(typeFormat[0], typeFormat[1]);
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#typeName(com.sun.javadoc.Type, boolean)
	 */
	public OptionalName typeName(Type type, boolean useFqn) {
		String[] typeFormat = ParserHelper.typeOf(type, useFqn, this.options);
		return presentOrMissing(typeFormat[0], typeFormat[1]);
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#fieldName(com.sun.javadoc.FieldDoc)
	 */
	public OptionalName fieldName(FieldDoc field) {
		return presentOrMissing(field.name());
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#methodName(com.sun.javadoc.MethodDoc)
	 */
	public OptionalName methodName(MethodDoc method) {
		String name = null;
		if ((method.name().startsWith("get") || method.name().startsWith("set")) && method.name().length() > 3) {
			name = method.name().substring(3);
			name = name.substring(0, 1).toLowerCase() + (name.length() > 1 ? name.substring(1) : "");
		} else if (method.name().startsWith("is") && method.name().length() > 2) {
			// verify return type is boolean
			String[] typeFormat = ParserHelper.primitiveTypeOf(method.returnType(), this.options);
			if (typeFormat != null && "boolean".equals(typeFormat[0])) {
				name = method.name().substring(2);
				name = name.substring(0, 1).toLowerCase() + (name.length() > 1 ? name.substring(1) : "");
			}
		}
		return presentOrMissing(name);
	}

}
