package com.capgemini.mrchecker.playwright.core.base.properties;

import com.capgemini.mrchecker.test.core.base.driver.DriverCloseLevel;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.microsoft.playwright.options.Proxy;

public class PropertiesPlaywright {
	private Proxy				proxy;
	private String				channel;
	private String				browsersPath		= "./lib/playwright";
	private int					skipBrowserDownload	= 0;
	private boolean				allowStaticPage		= false;
	private DriverCloseLevel	driverCloseLevel	= DriverCloseLevel.CLASS;
	
	@Inject(optional = true)
	@SuppressWarnings("unused")
	private void setProxy(@Named("playwright.browser.proxy") String proxy) {
		if (!proxy.isEmpty())
			this.proxy = new Proxy(proxy);
	}
	
	public Proxy getProxy() {
		return proxy;
	}
	
	@Inject(optional = true)
	@SuppressWarnings("unused")
	private void setChannel(@Named("playwright.browser.channel") String channel) {
		this.channel = channel;
	}
	
	public String getChannel() {
		return channel;
	}
	
	@Inject(optional = true)
	@SuppressWarnings("unused")
	private void setBrowsersPath(@Named("playwright.browser.downloadPath") String browsersPath) {
		this.browsersPath = browsersPath;
	}
	
	public String getBrowsersPath() {
		return browsersPath;
	}
	
	@Inject(optional = true)
	@SuppressWarnings("unused")
	private void setSkipBrowserDownload(@Named("playwright.browser.skipDownload") int value) {
		this.skipBrowserDownload = value;
	}
	
	public int getSkipBrowserDownload() {
		return skipBrowserDownload;
	}
	
	@Inject(optional = true)
	@SuppressWarnings("unused")
	private void setAllowStaticPage(@Named("playwright.allowStaticPage") boolean value) {
		this.allowStaticPage = value;
	}
	
	public boolean getAllowStaticPage() {
		return allowStaticPage;
	}
	
	@Inject(optional = true)
	@SuppressWarnings("unused")
	private void setDriverCloseLevel(@Named("playwright.driverCloseLevel") String level) {
		driverCloseLevel = DriverCloseLevel.fromText(level);
	}
	
	public DriverCloseLevel getDriverCloseLevel() {
		return driverCloseLevel;
	}
}