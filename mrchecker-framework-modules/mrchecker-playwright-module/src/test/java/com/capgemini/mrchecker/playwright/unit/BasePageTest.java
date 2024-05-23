package com.capgemini.mrchecker.playwright.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.capgemini.mrchecker.playwright.core.pages.TestPage;
import com.capgemini.mrchecker.playwright.tags.UnitTest;
import com.capgemini.mrchecker.test.core.BaseTest;
import com.capgemini.mrchecker.test.core.utils.PageFactory;

@UnitTest
class BasePageTest extends BaseTest {
	
	@Test
	void testGetPageTitle() {
		TestPage testPage = PageFactory.getPageInstance(TestPage.class);
		testPage.load();
		Assertions.assertEquals("Google", testPage.pageTitle(), "Wrong page title");
	}
}
