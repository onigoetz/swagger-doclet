package com.tenxerconsulting.swagger.doclet.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The ApiKeyAuthorization represents the API Key authorization type
 * @version $Id$
 * @author conor.roche
 */
@EqualsAndHashCode
@ToString
public class ApiKeyAuthorization {

	private String type;

	private String passAs;
	private String keyname;

	/**
	 * This creates a ApiKeyAuthorization
	 */
	public ApiKeyAuthorization() {
		this.type = "apiKey";
	}

	/**
	 * This creates a ApiKeyAuthorization
	 * @param passAs either header or query
	 * @param keyname
	 */
	public ApiKeyAuthorization(String passAs, String keyname) {
		super();
		this.type = "apiKey";
		this.passAs = passAs;
		this.keyname = keyname;
	}

	/**
	 * This gets the passAs
	 * @return the passAs
	 */
	public String getPassAs() {
		return this.passAs;
	}

	/**
	 * This sets the passAs
	 * @param passAs the passAs to set
	 */
	public void setPassAs(String passAs) {
		this.passAs = passAs;
	}

	/**
	 * This gets the keyname
	 * @return the keyname
	 */
	public String getKeyname() {
		return this.keyname;
	}

	/**
	 * This sets the keyname
	 * @param keyname the keyname to set
	 */
	public void setKeyname(String keyname) {
		this.keyname = keyname;
	}

	/**
	 * This gets the type
	 * @return the type
	 */
	public String getType() {
		return this.type;
	}
}
