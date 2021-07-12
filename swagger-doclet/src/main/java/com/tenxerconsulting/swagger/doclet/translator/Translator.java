package com.tenxerconsulting.swagger.doclet.translator;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public interface Translator {

	OptionalName typeName(Type type, boolean useFqn, ClassDoc[] views);

	OptionalName typeName(Type type, boolean useFqn);

	OptionalName parameterTypeName(boolean multipart, Parameter parameter, Type paramType, boolean useFqn, ClassDoc[] views);

	OptionalName fieldName(FieldDoc field);

	OptionalName methodName(MethodDoc method);

        @EqualsAndHashCode(of = {"status", "name"})
        @ToString
	class OptionalName {
		private final Status status;
		private final String name;
		private final String format;

		private OptionalName(Status status, String name, String format) {
			this.status = status;
			this.name = name;
			this.format = format;
		}

		public static OptionalName presentOrMissing(String name) {
                        if ( name == null || name.isEmpty()) {
				return new OptionalName(Status.MISSING, null, null);
                        } else {
                                return new OptionalName(Status.PRESENT, name, null);
			}
		}

		public static OptionalName presentOrMissing(String name, String format) {
                        if ( name == null || name.isEmpty()) {
				return new OptionalName(Status.MISSING, null, format);
                        } else {
                                return new OptionalName(Status.PRESENT, name, format);
			}
		}

		public static OptionalName ignored() {
			return new OptionalName(Status.IGNORED, null, null);
		}

		public String value() {
			return this.name;
		}

		/**
		 * This gets the format
		 * @return the format
		 */
		public String getFormat() {
			return this.format;
		}

		public boolean isPresent() {
			return this.status == Status.PRESENT;
		}

		public boolean isMissing() {
			return this.status == Status.MISSING;
		}

                private enum Status {
			PRESENT,
			IGNORED,
			MISSING
		}
	}

}
