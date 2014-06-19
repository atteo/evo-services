#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/time")
@Produces("text/plain")
public class TimeResource {
	@GET
	public String getTime() {
		return new Date().toString();
	}
}
