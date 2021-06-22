package com.tenxerconsulting.swagger.doclet.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The ApiBasicAuthorization represents the
 * @version $Id$
 * @author conor.roche
 */
@EqualsAndHashCode
@ToString
public class ApiBasicAuthorization {

	private final String type;

	/**
	 * This creates a ApiBasicAuthorization
	 */
	public ApiBasicAuthorization() {
		this.type = "basicAuth";
	}

	/**
	 * This gets the type of the authorization
	 * @return basicAuth
	 */
	public String getType() {
		return this.type;
	}
}
