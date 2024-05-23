package com.capgemini.mrchecker.playwright.core.pages;

import com.capgemini.mrchecker.playwright.core.BasePage;

public class TestPage extends BasePage {
	@Override
	public boolean isLoaded() {
		return true;
	}
	
	@Override
	public void load() {
		loadPage("https://google.com/");
	}
	
	@Override
	public String pageTitle() {
		return getActualPageTitle();
	}
}
