package com.tenxerconsulting.swagger.doclet.translator;

import com.sun.javadoc.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * The QualifiedType represents a type with a qualifier.
 * @version $Id$
 * @author conor.roche
 */
@EqualsAndHashCode
public class QualifiedType {
	@Getter
	private final String qualifier;
	@Getter
	private final Type type;
	@Getter
	private final String typeName;

	/**
	 * This creates a QualifiedType
	 * @param qualifier
	 * @param type
	 */
	public QualifiedType(String qualifier, Type type) {
		super();
		this.qualifier = qualifier;
		this.type = type;
		this.typeName = type == null ? null : this.type.qualifiedTypeName();
	}

	/**
	 * This creates a QualifiedType
	 * @param type
	 */
	public QualifiedType(Type type) {
		this(null, type);
	}
}
