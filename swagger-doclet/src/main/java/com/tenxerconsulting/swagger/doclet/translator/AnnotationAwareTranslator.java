package com.tenxerconsulting.swagger.doclet.translator;

import static com.tenxerconsulting.swagger.doclet.translator.Translator.OptionalName.ignored;
import static com.tenxerconsulting.swagger.doclet.translator.Translator.OptionalName.presentOrMissing;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Type;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.parser.AnnotationParser;
import com.tenxerconsulting.swagger.doclet.parser.ParserHelper;

/**
 * The AnnotationAwareTranslator represents a translator that can source the names from various
 * annotations like jaxb and json ones.
 * @version $Id$
 */
public class AnnotationAwareTranslator implements Translator {

	private String ignore;
	private String element;
	private String elementProperty;
	private String rootElement;
	private String rootElementProperty;
	private DocletOptions options;

	/**
	 * This creates a AnnotationAwareTranslator that uses the given doclet options
	 * @param options the doclet options
	 */
	public AnnotationAwareTranslator(DocletOptions options) {
		this.options = options;
	}

	/**
	 * This adds an ignore annotation to this translator
	 * @param qualifiedAnnotationType The FQN of the annotation that if present means a field is ignored e.g. JsonIgnore, XmlTransient
	 * @return this
	 */
	public AnnotationAwareTranslator ignore(String qualifiedAnnotationType) {
		this.ignore = qualifiedAnnotationType;
		return this;
	}

	/**
	 * This adds an element annotation to this translator, these ones are used for field and method names
	 * @param qualifiedAnnotationType The FQN of the annotation that if present means a field/method uses this annotation's property for the name
	 * @param property The property name of the annotation to use for the field/method name
	 * @return this
	 */
	public AnnotationAwareTranslator element(String qualifiedAnnotationType, String property) {
		this.element = qualifiedAnnotationType;
		this.elementProperty = property;
		return this;
	}

	/**
	 * This adds an root annotation to this translator, these ones are used for class type names
	 * @param qualifiedAnnotationType The FQN of the annotation that if present means a class type uses uses this annotation's property for the name
	 * @param property The property name of the annotation to use for the class type name
	 * @return this
	 */
	public AnnotationAwareTranslator rootElement(String qualifiedAnnotationType, String property) {
		this.rootElement = qualifiedAnnotationType;
		this.rootElementProperty = property;
		return this;
	}

	public OptionalName typeName(Type type, boolean useFqn, ClassDoc[] views) {
		OptionalName name = typeName(type, useFqn);

		if (views != null && views.length > 0 && name != null && name.isPresent() && !ParserHelper.isPrimitive(type, this.options)) {
			StringBuilder nameWithViews = new StringBuilder(name.value()).append("-");
			for (ClassDoc view : views) {
				nameWithViews.append(view.name());
			}
			name = presentOrMissing(nameWithViews.toString(), name.getFormat());
		}
		return name;
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
				OptionalName res = presentOrMissing("internalFileSchema");
				return res;
			}
		}

		return typeName(paramType, useFqn, views);
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#typeName(com.sun.javadoc.Type, boolean)
	 */
	public OptionalName typeName(Type type, boolean useFqn) {
		return typeName(new QualifiedType("typeName", type));
	}

	private OptionalName typeName(QualifiedType type) {
		if (ParserHelper.isPrimitive(type.getType(), this.options) || type.getType().asClassDoc() == null) {
			return null;
		}

		OptionalName name = null;
		if (ParserHelper.isArray(type.getType())) {
			name = presentOrMissing("array");
		} else {
			name = nameFor(this.rootElement, this.rootElementProperty, type.getType().asClassDoc(), false);
		}
		return name;
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#fieldName(com.sun.javadoc.FieldDoc)
	 */
	public OptionalName fieldName(FieldDoc field) {
		return nameFor(this.element, this.elementProperty, field, true);
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.translator.Translator#methodName(com.sun.javadoc.MethodDoc)
	 */
	public OptionalName methodName(MethodDoc method) {
		return nameFor(this.element, this.elementProperty, method, true);
	}

	private OptionalName nameFor(String annotation, String property, ProgramElementDoc doc, boolean processIgnore) {
		AnnotationParser element = new AnnotationParser(doc, this.options);
		if (processIgnore && element.isAnnotatedBy(this.ignore)) {
			return ignored();
		}
		String name = element.getAnnotationValue(annotation, property);
		return presentOrMissing(name);
	}

}
