package com.tenxerconsulting.swagger.doclet.model;

import java.util.List;

import com.tenxerconsulting.swagger.doclet.parser.ParserHelper;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;
import io.swagger.oas.models.responses.ApiResponses;
import io.swagger.oas.models.security.SecurityRequirement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Method {
	private HttpMethod method;
	private String methodName;
	private String path;
	private List<Parameter> apiParameters;
	private String summary;
	private String description;
	private ApiResponses responses;
	private List<String> consumes;
	private List<String> produces;
	private List<SecurityRequirement> security;
	private boolean deprecated;
	private RequestBody requestBody;

	public Method(HttpMethod method, String methodName, String path, List<Parameter> apiParameters,
				  String summary, String description, ApiResponses responses,
				  List<String> consumes, List<String> produces, List<SecurityRequirement> security, boolean deprecated, RequestBody body) {
		this.method = method;
		this.methodName = methodName;
		this.path = ParserHelper.sanitizePath(path);
		this.apiParameters = apiParameters;
		this.summary = summary;
		this.description = description;

		this.responses = responses;

		this.consumes = consumes;
		this.produces = produces;
		this.security = security;
		this.deprecated = deprecated;
		this.requestBody = body;
	}

	public boolean isSubResource() {
		return this.method == null;
	}

	/**
	 * This gets the deprecated
	 * @return the deprecated
	 */
	public boolean isDeprecated() {
		return this.deprecated;
	}
}
