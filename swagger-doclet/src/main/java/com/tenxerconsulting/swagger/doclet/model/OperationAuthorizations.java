package com.tenxerconsulting.swagger.doclet.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The OperationAuthorizations represents the authorizations element for the API
 * @version $Id$
 * @author conor.roche
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationAuthorizations {
	private List<Oauth2Scope> oauth2Scopes;
}
