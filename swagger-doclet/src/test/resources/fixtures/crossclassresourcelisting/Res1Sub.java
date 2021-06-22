package fixtures.crossclassresourcelisting;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * The Res1Sub represents a sub resource
 * @version $Id$
 * @author conor.roche
 */
public class Res1Sub {

	/**
	 * @resource a
	 * @priority 1
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("/w")
	public SubData getW() {
		return null;
	}

}
