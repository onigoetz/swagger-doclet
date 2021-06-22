package com.tenxerconsulting.swagger.doclet.model;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The ApiOauth2Authorization represents the oauth 2 authorization for the api authorization
 * @version $Id$
 * @author conor.roche
 */
@EqualsAndHashCode
@ToString
public class ApiOauth2Authorization {

	private String type;
	private List<Oauth2Scope> scopes;
	private Oauth2GrantTypes grantTypes;

	/**
	 * This creates a ApiOauth2Authorization
	 */
	public ApiOauth2Authorization() {
		this.type = "oauth2";
	}

	/**
	 * This creates a ApiOauth2Authorization
	 * @param scopes
	 * @param grantTypes
	 */
	public ApiOauth2Authorization(List<Oauth2Scope> scopes, Oauth2GrantTypes grantTypes) {
		super();
		this.type = "oauth2";
		this.scopes = scopes;
		this.grantTypes = grantTypes;
	}

	/**
	 * This gets the type of the authorization
	 * @return oauth2
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * This gets the scopes
	 * @return the scopes
	 */
	public List<Oauth2Scope> getScopes() {
		return this.scopes;
	}

	/**
	 * This gets the grantTypes
	 * @return the grantTypes
	 */
	public Oauth2GrantTypes getGrantTypes() {
		return this.grantTypes;
	}
}
