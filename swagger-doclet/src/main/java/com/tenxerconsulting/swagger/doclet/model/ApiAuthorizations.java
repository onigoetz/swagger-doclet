package com.tenxerconsulting.swagger.doclet.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The ApiAuthorizations represents the authorizations element for the API
 * @version $Id$
 * @author conor.roche
 */
@EqualsAndHashCode
@ToString
public class ApiAuthorizations {

	private ApiOauth2Authorization oauth2;
	private ApiBasicAuthorization basicAuth;
	private ApiKeyAuthorization apiKey;

	/**
	 * This creates an empty Authorizations
	 */
	public ApiAuthorizations() {
	}

	/**
	 * This creates an Authorizations for oauth2
	 * @param oauth2 The oauth2 authorization
	 */
	public ApiAuthorizations(ApiOauth2Authorization oauth2) {
		super();
		this.oauth2 = oauth2;
	}

	/**
	 * This creates a ApiAuthorizations for basic auth
	 * @param basicAuth The basic authorization
	 */
	public ApiAuthorizations(ApiBasicAuthorization basicAuth) {
		super();
		this.basicAuth = basicAuth;
	}

	/**
	 * This creates a ApiAuthorizations for api key type auth
	 * @param apiKey The API key authorization details
	 */
	public ApiAuthorizations(ApiKeyAuthorization apiKey) {
		super();
		this.apiKey = apiKey;
	}

	/**
	 * This gets the oauth2
	 * @return the oauth2
	 */
	public ApiOauth2Authorization getOauth2() {
		return this.oauth2;
	}

	/**
	 * This gets the basicAuth
	 * @return the basicAuth
	 */
	public ApiBasicAuthorization getBasicAuth() {
		return this.basicAuth;
	}

	/**
	 * This gets the apiKey
	 * @return the apiKey
	 */
	public ApiKeyAuthorization getApiKey() {
		return this.apiKey;
	}
}
