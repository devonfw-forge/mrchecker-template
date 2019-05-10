package com.capgemini.mrchecker.webapi.core.base.driver;

import com.capgemini.mrchecker.test.core.BaseTest;
import com.capgemini.mrchecker.webapi.core.base.runtime.RuntimeParameters;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;


public class DriverManagerTest_standaloneWiremock extends BaseTest {

	private static Process process_wiremock = null;
	private static final String WIREMOCK_STANDALONE_PORT = "8100";

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testStandaloneWireMockStartDedicatedPortHttp() throws UnknownHostException {
		InetAddress inetAddress = InetAddress.getLocalHost();
		String hostAddress = inetAddress.getHostAddress();
		System.out.println("IP Address:- " + hostAddress);
		String hostName = inetAddress.getHostName();
		System.out.println("Host Name:- " + hostName);



		System.setProperty("mock_http_host", hostAddress);
		RuntimeParameters.MOCK_HTTP_HOST.refreshParameterValue();
		assertEquals("System parameters for 'mock_http_host'", hostAddress, RuntimeParameters.MOCK_HTTP_HOST.getValue());
		
		System.setProperty("mock_http_port", WIREMOCK_STANDALONE_PORT);
		RuntimeParameters.MOCK_HTTP_PORT.refreshParameterValue();
		assertEquals("Runtime Parameter for MOCK_HTTP_PORT is incorrect", "8100", RuntimeParameters.MOCK_HTTP_PORT.getValue());
		
		WireMock driver = DriverManager.getDriverVirtualService();
		assertEquals("Mock server for http does not run o specified port", WIREMOCK_STANDALONE_PORT, String.valueOf(DriverManager.getHttpPort()));
		assertEquals("Mock server for http does not run o specific hostname", hostAddress, DriverManager.getHttpHost());
	}
	
	@Test
	public void testStandaloneWireMockWrongHostname() {
		System.setProperty("mock_http_host", "http://wrongHost");
		RuntimeParameters.MOCK_HTTP_HOST.refreshParameterValue();
		
		System.setProperty("mock_http_port", "8100");
		RuntimeParameters.MOCK_HTTP_PORT.refreshParameterValue();
		
		thrown.expect(java.net.UnknownHostException.class);
		thrown.expectMessage(startsWith("http"));
		WireMock driver = DriverManager.getDriverVirtualService();
	}
	
	@Test
	public void testStandaloneWireMockStartRandomPortHttp() {
		System.setProperty("mock_http_host", "http://127.0.0.1");
		RuntimeParameters.MOCK_HTTP_HOST.refreshParameterValue();
		
		System.setProperty("mock_http_port", "1000");
		RuntimeParameters.MOCK_HTTP_PORT.refreshParameterValue();
		
		thrown.expect(java.net.UnknownHostException.class);
		thrown.expectMessage(startsWith("http"));
		WireMock driver = DriverManager.getDriverVirtualService();
	}

	@BeforeClass
	public static void beforeAll() throws IOException {
		startWiremockStandalone();

	}



	@AfterClass
	public static void afterAll(){
		try {
			process_wiremock.destroyForcibly();
		}catch (Exception e){
			//No need to catch Exception
		}

	}

	@Override
	public void setUp() {
		DriverManager.closeDriverVirtualServer();




	}
	
	@Override
	public void tearDown() {
		DriverManager.closeDriverVirtualServer();
	}


	private static void startWiremockStandalone() throws IOException {
		String path = System.getProperty("user.dir") + Paths.get("/src/test/resources/wiremock/wiremock-standalone-2.21.0.jar");
		File file = new File(path);

		ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", file.getAbsolutePath(), "--port", WIREMOCK_STANDALONE_PORT);
		processBuilder.redirectOutput(Redirect.INHERIT);
		processBuilder.redirectError(Redirect.INHERIT);

		process_wiremock = processBuilder.start();
	}
}