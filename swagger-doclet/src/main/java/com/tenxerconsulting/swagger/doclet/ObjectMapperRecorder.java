package com.tenxerconsulting.swagger.doclet;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.oas.models.OpenAPI;

/**
 * The ObjectMapperRecorder represents a mapper for writing swagger objects to files
 */
public class ObjectMapperRecorder implements Recorder {
	final ObjectMapper mapper;

	public ObjectMapperRecorder(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * {@inheritDoc}
	 * @see com.tenxerconsulting.swagger.doclet.Recorder#record(java.io.File, io.swagger.oas.models.OpenAPI)
	 */
	@Override
	public void record(File file, OpenAPI openapi) throws IOException {
		this.mapper.writeValue(file, openapi);
	}
}
