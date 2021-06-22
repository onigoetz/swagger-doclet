package fixtures.genericresponse;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.JResponse;

/**
 * The GenericResponseResource represents a test class for generic responses
 * @version $Id$
 * @author conor.roche
 */
@SuppressWarnings("javadoc")
@Path("/genericresponse")
public class GenericResponseResource {

	@GET
	@Path("getParameterized")
	public Parameterized<String, Integer> getParameterized() {
		return new Parameterized<String, Integer>();
	}

	@GET
	@Path("getJResponse")
	public JResponse<String> getJResponse() {
		return new JResponse<String>(200, null, "");
	}

	@GET
	@Path("getOptional")
	public Response getOptional(@QueryParam("name") com.google.common.base.Optional<String> name) {
		return null;
	}

	@GET
	@Path("getOptional2")
	public Response getOptional2(@QueryParam("name") jersey.repackaged.com.google.common.base.Optional<Integer> name) {
		return null;
	}

	@GET
	@Path("getIntMap")
	public Map<String, Integer> getIntMap() {
		return Collections.emptyMap();
	}

	/**
	 * @returnType fixtures.genericresponse.Parameterized2<java.lang.Integer>
	 */
	@GET
	@Path("getParameterized2")
	public Response getParameterized2() {
		return null;
	}

	@GET
	@Path("getBatch")
	public Batch<Item> getBatch() {
		return null;
	}

	/**
	 * @returnType fixtures.genericresponse.Batch<fixtures.genericresponse.Item>
	 */
	@GET
	@Path("getBatch2")
	public Response getBatch2() {
		return null;
	}
}
