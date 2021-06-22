package com.tenxerconsulting.swagger.doclet.translator;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;

public interface Translator {

	OptionalName typeName(Type type, boolean useFqn, ClassDoc[] views);

	OptionalName typeName(Type type, boolean useFqn);

	OptionalName parameterTypeName(boolean multipart, Parameter parameter, Type paramType, boolean useFqn, ClassDoc[] views);

	OptionalName fieldName(FieldDoc field);

	OptionalName methodName(MethodDoc method);

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
			if (!Strings.isNullOrEmpty(name)) {
				return new OptionalName(Status.PRESENT, name, null);
			} else {
				return new OptionalName(Status.MISSING, null, null);
			}
		}

		public static OptionalName presentOrMissing(String name, String format) {
			if (!Strings.isNullOrEmpty(name)) {
				return new OptionalName(Status.PRESENT, name, format);
			} else {
				return new OptionalName(Status.MISSING, null, format);
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

		private static enum Status {
			PRESENT,
			IGNORED,
			MISSING
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			OptionalName that = (OptionalName) o;
			return Objects.equal(this.status, that.status) && Objects.equal(this.name, that.name);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.status, this.name);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("status", this.status).add("name", this.name).toString();
		}
	}

}
