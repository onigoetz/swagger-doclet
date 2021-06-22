package fixtures.producesconsumes;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The ProducesConsumesResource represents a jaxrs resource that uses the produces and consumes mimetypes
 * @version $Id$
 * @author conor.roche
 */
@Path("/producesconsumes")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
@SuppressWarnings("javadoc")
public class ProducesConsumesResource {

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Data getData() {
		return new Data("test");
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("postData")
	public Data postData(Data data) {
		return data;
	}

	@POST
	@Path("postData2")
	public Data postData2(Data data) {
		return new Data("test2");
	}

}
