package com.tenxerconsulting.swagger.doclet;

import java.io.File;
import java.io.IOException;

import io.swagger.oas.models.OpenAPI;

public interface Recorder {
	void record(File file, OpenAPI swagger) throws IOException;
}
