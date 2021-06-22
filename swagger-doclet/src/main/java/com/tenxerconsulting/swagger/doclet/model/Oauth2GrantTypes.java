package com.tenxerconsulting.swagger.doclet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

/**
 * The Oauth2GrantTypes represents different oauth2 grant types
 * @version $Id$
 * @author conor.roche
 */
@EqualsAndHashCode
public class Oauth2GrantTypes {

	private Oauth2AuthorizationCodeGrantType authorizationCode;
	private Oauth2ImplicitGrantType implicit;

	// TODO when swagger spec updated for other oauth2 flows ammend

	/**
	 * This creates a Oauth2GrantTypes
	 */
	public Oauth2GrantTypes() {
	}

	/**
	 * This creates a Oauth2GrantTypes
	 * @param authorizationCode
	 */
	public Oauth2GrantTypes(Oauth2AuthorizationCodeGrantType authorizationCode) {
		super();
		this.authorizationCode = authorizationCode;
	}

	/**
	 * This creates a Oauth2GrantTypes
	 * @param implicit
	 */
	public Oauth2GrantTypes(Oauth2ImplicitGrantType implicit) {
		super();
		this.implicit = implicit;
	}

	/**
	 * This creates a Oauth2GrantTypes
	 * @param authorizationCode
	 * @param implicit
	 */
	public Oauth2GrantTypes(Oauth2AuthorizationCodeGrantType authorizationCode, Oauth2ImplicitGrantType implicit) {
		super();
		this.authorizationCode = authorizationCode;
		this.implicit = implicit;
	}

	/**
	 * This gets the authorizationCode
	 * @return the authorizationCode
	 */
	@JsonProperty("authorization_code")
	public Oauth2AuthorizationCodeGrantType getAuthorizationCode() {
		return this.authorizationCode;
	}

	/**
	 * This gets the implicit
	 * @return the implicit
	 */
	public Oauth2ImplicitGrantType getImplicit() {
		return this.implicit;
	}


}
