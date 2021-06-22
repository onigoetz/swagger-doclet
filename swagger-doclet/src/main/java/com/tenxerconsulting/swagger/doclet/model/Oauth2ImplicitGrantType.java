package com.tenxerconsulting.swagger.doclet.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The Oauth2ImplicitGrantType represents the definition of the implicit flow
 * @version $Id$
 * @author conor.roche
 */
@Data
@NoArgsConstructor
public class Oauth2ImplicitGrantType {

	/**
	 * The LoginEndpoint represents the login endpoint used for the implicit flow
	 * @version $Id$
	 * @author conor.roche
	 */
	@Data
	@AllArgsConstructor
	public static class LoginEndpoint {
		private String url;
	}

	private String tokenName = "access_token";
	private LoginEndpoint loginEndpoint;

	/**
	 * This creates a Oauth2ImplicitGrantType
	 * @param loginEndpointUrl
	 */
	public Oauth2ImplicitGrantType(String loginEndpointUrl) {
		super();
		this.loginEndpoint = new LoginEndpoint(loginEndpointUrl);
	}
}
