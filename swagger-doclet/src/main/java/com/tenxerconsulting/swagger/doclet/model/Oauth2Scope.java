package com.tenxerconsulting.swagger.doclet.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Oauth2Scope represents an oauth2 scope
 * @version $Id$
 * @author conor.roche
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Oauth2Scope {
	private String scope;
	private String description;
}
