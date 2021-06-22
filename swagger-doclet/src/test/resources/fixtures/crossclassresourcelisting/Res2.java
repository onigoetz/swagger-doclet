package fixtures.crossclassresourcelisting;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@SuppressWarnings("javadoc")
@Path("/v1")
public class Res2 {

	/**
	 * @resource c
	 * @priority 3
	 * @resourceDescription C
	 */
	@GET
	@Path("/c")
	public void getC() {
		// noop
	}

	/**
	 * @parentEndpointName b
	 * @resourceDescription B
	 */
	@GET
	@Path("/b")
	public void getB() {
		// noop
	}

}
