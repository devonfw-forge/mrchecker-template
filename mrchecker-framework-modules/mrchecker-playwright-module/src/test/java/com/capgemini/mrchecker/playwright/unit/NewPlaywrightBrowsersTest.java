package com.capgemini.mrchecker.playwright.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.capgemini.mrchecker.playwright.core.newDrivers.NewPlaywright;
import com.capgemini.mrchecker.playwright.tags.UnitTest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

@UnitTest
public class NewPlaywrightBrowsersTest {
	private final NewPlaywright playwright = new NewPlaywright(Playwright.create());
	
	@Test
	public void shouldBrowserNameBeFirefox() {
		Browser browser = playwright.firefox()
				.launch();
		assertEquals("firefox", browser.browserType()
				.name(), "Wrong browser name");
	}
	
	@Test
	public void shouldBrowserNameBeChromium() {
		Browser browser = playwright.chromium()
				.launch();
		assertEquals("chromium", browser.browserType()
				.name(), "Wrong browser name");
	}
	
	@Test
	public void shouldBrowserNameBeWebkit() {
		Browser browser = playwright.webkit()
				.launch();
		assertEquals("webkit", browser.browserType()
				.name(), "Wrong browser name");
	}
}
