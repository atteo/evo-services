
package org.atteo.moonshine.jetty;

import java.nio.ByteBuffer;
import javax.inject.Inject;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.http.HttpTester.Request;
import org.eclipse.jetty.http.HttpTester.Response;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.util.B64Code;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

@MoonshineConfiguration("/security.xml")
public class ConstraintSecurityTest extends MoonshineTest {
	@Inject
	private LocalConnector localConnector;

	@Test
	public void testSecurity() throws Exception {
		Request request = HttpTester.newRequest();
		request.setHeader("Host", "tester");
		request.setMethod("GET");
		request.setURI("/");

		// test without authentication
		ByteBuffer responseString = localConnector.getResponses(request.generate());
		Response response = HttpTester.parseResponse(responseString);
		assertEquals(HttpStatus.UNAUTHORIZED_401, response.getStatus());
		assertTrue(response.contains(HttpHeader.WWW_AUTHENTICATE, "Basic realm=\"Security Test\""));

		// test successful authentication
		request.setHeader(HttpHeader.AUTHORIZATION.toString(), "Basic " + B64Code.encode("alice:alicepassword"));
		responseString = localConnector.getResponses(request.generate());
		response = HttpTester.parseResponse(responseString);
		assertEquals(HttpStatus.OK_200, response.getStatus());

		// test missing roles
		request.setHeader(HttpHeader.AUTHORIZATION.toString(), "Basic " + B64Code.encode("bob:bobpassword"));
		responseString = localConnector.getResponses(request.generate());
		response = HttpTester.parseResponse(responseString);
		assertEquals(HttpStatus.FORBIDDEN_403, response.getStatus());
	}
}
