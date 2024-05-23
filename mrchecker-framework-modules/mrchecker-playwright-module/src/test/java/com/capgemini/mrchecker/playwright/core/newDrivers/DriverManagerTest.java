package com.capgemini.mrchecker.playwright.core.newDrivers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;

import com.capgemini.mrchecker.playwright.core.base.properties.PropertiesPlaywright;
import com.capgemini.mrchecker.playwright.tags.UnitTest;
import com.capgemini.mrchecker.test.core.base.properties.PropertiesSettingsModule;
import com.google.inject.Guice;

@UnitTest
public class DriverManagerTest {
	private DriverManager driverManager;
	
	@BeforeAll
	public static void setUpBeforeClass() {
	}
	
	@AfterAll
	public static void tearDownAfterClass() {
	}
	
	@BeforeEach
	public void setUp() {
		PropertiesPlaywright propertiesPlaywright = Guice.createInjector(PropertiesSettingsModule.init())
				.getInstance(PropertiesPlaywright.class);
		
		driverManager = new DriverManager(propertiesPlaywright);
		driverManager.start();
	}
	
	@AfterEach
	public void tearDown() {
		driverManager.stop();
	}
	
	@Test
	public void shouldDriverBeCreated() {
		assertTrue(DriverManager.wasDriverCreated(), "Driver was not created");
	}
	
}
