package com.tenxerconsulting.swagger.doclet.sample;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

@Provider
public class MyObjectMapperProvider implements ContextResolver<ObjectMapper> {

	final ObjectMapper defaultObjectMapper;
	final ObjectMapper combinedObjectMapper;

	public MyObjectMapperProvider() {
		this.defaultObjectMapper = createDefaultMapper();
		this.combinedObjectMapper = createCombinedObjectMapper();
	}

	@Override
	public ObjectMapper getContext(final Class<?> type) {
		return this.combinedObjectMapper;
	}

	private static ObjectMapper createCombinedObjectMapper() {
		return new ObjectMapper().configure(SerializationFeature.WRAP_ROOT_VALUE, false).configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false)
				.setAnnotationIntrospector(createJaxbJacksonAnnotationIntrospector());
	}

	private static ObjectMapper createDefaultMapper() {
		final ObjectMapper result = new ObjectMapper();
		result.enable(SerializationFeature.INDENT_OUTPUT);
		return result;
	}

	private static AnnotationIntrospector createJaxbJacksonAnnotationIntrospector() {
		final AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
		final AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
		return AnnotationIntrospector.pair(jacksonIntrospector, jaxbIntrospector);
	}
}
