package fixtures.modelinheritanceusingsubtypes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * The ModelInheritanceResource represents a resource for testing model classes that are subtypes of
 * a class. They should not include fields from their parent. They should be generated even if they
 * don't have any additional properties.
 * @version $Id$
 * @author conor.roche
 */
@Path("/modelinheritanceusingsubtypes")
public class ModelInheritanceUsingSubTypesResource {

	@SuppressWarnings("javadoc")
	@GET
	@Path("getSmallestVehicle")
	public Vehicle getSmallestVehicle() {
		return new Car("Fiat", "127");
	}
        
        @SuppressWarnings("javadoc")
	@GET
	@Path("getMotorcycle")
	public Motorcycle getMotorcycle() {
		return new Motorcycle("Yamaha", "2000");
	}

}
