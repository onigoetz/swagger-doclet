package fixtures.responsemodel;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * The ResponseModelResource represents a jaxrs resource for testing custom response models
 * @version $Id$
 * @author conor.roche
 */
@Path("/responsemodel")
public class ResponseModelResource {

	@SuppressWarnings("javadoc")
	@GET
	@Path("unspecifiedResponse")
	public Response unspecifiedResponse() {
		return Response.ok().build();
	}

	@SuppressWarnings("javadoc")
	@GET
	@Path("responseDefinedViaReturn")
	public Response1 responseDefinedViaReturn() {
		return new Response1();
	}

	/**
	 * @responseType fixtures.responsemodel.Response2
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseDefinedViaTag")
	public Response responseDefinedViaTag() {
		return Response.ok().entity(new Response2()).build();
	}

	/**
	 * @responseType java.util.List<fixtures.responsemodel.Response2>
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseDefinedViaTagForList")
	public Response responseDefinedViaTagForList() {
		return Response.ok().entity(new ArrayList<Response2>()).build();
	}

	/**
	 * @responseType java.util.List<String>
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseDefinedViaTagForPrimitiveList")
	public Response responseDefinedViaTagForPrimitiveList() {
		return Response.ok().entity(new ArrayList<String>()).build();
	}

	/**
	 * @responseType string
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseString")
	public Response responseString() {
		return Response.ok().build();
	}

	/**
	 * @responseType java.lang.String
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseStringObject")
	public Response responseStringObject() {
		return Response.ok().build();
	}

	/**
	 * @responseType int
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseInt")
	public Response responseInt() {
		return Response.ok().build();
	}

	/**
	 * @responseType java.lang.Integer
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseIntObject")
	public Response responseIntObject() {
		return Response.ok().build();
	}

	/**
	 * @responseType java.math.BigInteger
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseBigIntObject")
	public Response responseBigIntObject() {
		return Response.ok().build();
	}

	/**
	 * @responseType java.math.BigDecimal
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseBigDecObject")
	public Response responseBigDecObject() {
		return Response.ok().build();
	}

	/**
	 * @responseMessage 200 if ok `fixtures.responsemodel.Response1
	 * @responseMessage 404 if no result found `fixtures.responsemodel.Response2
	 * @responseMessage 500 if an internal error occurred
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseDefinedViaResponseCode")
	public Response responseDefinedViaResponseCode() {
		return Response.ok().entity(new Response2()).build();
	}

	/**
	 * @responseType fixtures.responsemodel.Response2
	 * @responseMessage 404 if no result found `fixtures.responsemodel.Response3
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("responseMix")
	public Response responseMix() {
		return Response.ok().entity(new Response3()).build();
	}

	@GET
	@SuppressWarnings("javadoc")
	@Path("interfaceResponse")
	public Response4 interfaceResponse() {
		return new Response4() {
			public String getValue() {
				return "test";
			}
		};
	}

	/**
	 * @responseType fixtures.responsemodel.Response4
	 */
	@SuppressWarnings("javadoc")
	@GET
	@Path("interfaceResponseViaTag")
	public Response interfaceResponseViaTag() {
		return Response.ok().entity(new Response4() {
			public String getValue() {
				return "test";
			}
		}).build();
	}

}
