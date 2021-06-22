package fixtures.crossclassresourcelisting;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@SuppressWarnings("javadoc")
@Path("/v1")
public class Res1 {

	/**
	 * @resource a
	 * @priority 2
	 * @resourceDescription A
	 * @apiDescription A
	 */
	@GET
	@Path("/x")
	public void getX() {
		// noop
	}

	/**
	 * @resource b
	 * @resourcePriority 1
	 */
	@GET
	@Path("/y")
	public void getY() {
		// noop
	}

	@Path("/x/sub")
	public Res1Sub getXSub() {
		return null;
	}

}
