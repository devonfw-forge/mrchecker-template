package com.capgemini.mrchecker.playwright.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.ResourceLock;

import com.capgemini.mrchecker.playwright.core.newDrivers.DriverManager;
import com.capgemini.mrchecker.playwright.core.pages.TestPage;
import com.capgemini.mrchecker.playwright.tags.UnitTest;
import com.capgemini.mrchecker.test.core.utils.PageFactory;

@UnitTest
@ResourceLock(value = "SingleThread")
public class DefaultDriverBrowserTest {
	@BeforeAll
	public static void setUpBeforeClass() {
	}
	
	@AfterAll
	public static void tearDownAfterClass() {
	}
	
	@BeforeEach
	public void setUp() {
		
	}
	
	@AfterEach
	public void tearDown() {
	}
	
	@Test
	public void testParameterGetChrome() {
		TestPage testPage = PageFactory.getPageInstance(TestPage.class);
		testPage.load();
		String driverName = DriverManager.getDriver()
				.browser()
				.browserType()
				.name();
		assertEquals("chromium", driverName, "Wrong browser name");
	}
	
}
