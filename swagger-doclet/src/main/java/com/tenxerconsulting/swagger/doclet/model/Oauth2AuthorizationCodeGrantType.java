package com.tenxerconsulting.swagger.doclet.model;

import lombok.*;

/**
 * The Oauth2AuthorizationCodeGrantType represents
 * @version $Id$
 * @author conor.roche
 */
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Oauth2AuthorizationCodeGrantType {

	/**
	 * The TokenRequestEndpoint represents the url for the request token endpoint
	 * @version $Id$
	 * @author conor.roche
	 */
	@Data
	@NoArgsConstructor
	public static class TokenRequestEndpoint {
		private String url;
		private String clientIdName = "client_id";
		private String clientSecretName = "client_secret";

		/**
		 * This creates a TokenRequestEndpoint
		 * @param url
		 */
		public TokenRequestEndpoint(String url) {
			super();
			this.url = url;
		}
	}

	/**
	 * The TokenEndpoint represents the token endpoint
	 * @version $Id$
	 * @author conor.roche
	 */
	@Data
	@NoArgsConstructor
	public static class TokenEndpoint {

		private String url;
		private String tokenName = "access_token";

		/**
		 * This creates a TokenEndpoint
		 * @param url
		 */
		public TokenEndpoint(String url) {
			super();
			this.url = url;
		}
	}

	@Getter
	private TokenRequestEndpoint tokenRequestEndpoint;

	@Getter
	private TokenEndpoint tokenEndpoint;

	/**
	 * This creates a Oauth2AuthorizationCodeGrantType
	 * @param tokenRequestEndpoint
	 * @param tokenEndpoint
	 */
	public Oauth2AuthorizationCodeGrantType(TokenRequestEndpoint tokenRequestEndpoint, TokenEndpoint tokenEndpoint) {
		super();
		this.tokenRequestEndpoint = tokenRequestEndpoint;
		this.tokenEndpoint = tokenEndpoint;
	}

	/**
	 * This creates a Oauth2AuthorizationCodeGrantType with the given endpoint urls
	 * @param tokenRequestEndpointUrl The url to the token request endpoint
	 * @param tokenEndpointUrl The url to the token endpoint
	 */
	public Oauth2AuthorizationCodeGrantType(String tokenRequestEndpointUrl, String tokenEndpointUrl) {
		this.tokenRequestEndpoint = new TokenRequestEndpoint(tokenRequestEndpointUrl);
		this.tokenEndpoint = new TokenEndpoint(tokenEndpointUrl);
	}
}
