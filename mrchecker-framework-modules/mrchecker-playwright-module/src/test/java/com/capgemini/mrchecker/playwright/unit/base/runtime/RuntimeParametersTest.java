package com.capgemini.mrchecker.playwright.unit.base.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import com.capgemini.mrchecker.playwright.core.base.runtime.RuntimeParametersPlaywright;

@ResourceLock(value = "SingleThread")
public class RuntimeParametersTest {
	
	private static final Map<String, String> STARTUP_PARAMETERS_VALUES = new LinkedHashMap<String, String>() {
		{
			put("browser", "chromedriver");
			put("browserVersion", "11.0");
			put("seleniumGrid", "something");
			put("os", "linux");
			put("browserOptions",
					"headless;window-size=1200x600;testEquals=FirstEquals=SecondEquals;--testMe;acceptInsecureCerts=true;maxInstances=3");
			put("headless", "false");
		}
	};
	
	public static final String	DEFAULT_BROWSER_VALUE		= "chrome";
	public static final String	BROWSER_OPTIONS_TEST_KEY	= "browserOptionsTestKey";
	
	@BeforeEach
	public void setUp() {
		STARTUP_PARAMETERS_VALUES.forEach(System::setProperty);
		refreshAllParameters();
	}
	
	@AfterEach
	public void tearDown() {
		STARTUP_PARAMETERS_VALUES.forEach((k, v) -> System.clearProperty(k));
		refreshAllParameters();
	}
	
	private static void refreshAllParameters() {
		doForEachParam(RuntimeParametersPlaywright::refreshParameterValue);
	}
	
	private static void doForEachParam(Consumer<RuntimeParametersPlaywright> applyFunction) {
		Arrays.stream(RuntimeParametersPlaywright.values())
				.forEach(applyFunction);
	}
	
	@Test
	public void shouldGetAllStartupProperties() {
		doForEachParam((e) -> assertThat(e.toString() + " failed!", e.getValue(), is(equalTo(STARTUP_PARAMETERS_VALUES.get(e.getKey())))));
	}
	
	@Test
	public void shouldGetReturnOriginBrowserValue() {
		System.setProperty("browser", STARTUP_PARAMETERS_VALUES.get("browser"));
		
		refreshAllParameters();
		String browserValue = RuntimeParametersPlaywright.BROWSER.getValue();
		
		assertThat(browserValue, is(equalTo(STARTUP_PARAMETERS_VALUES.get(RuntimeParametersPlaywright.BROWSER.getKey()))));
	}
	
	@Test
	public void shouldGetReturnDefaultBrowserValue() {
		Arrays.stream(new String[] { "", "null" })
				.forEach((s) -> {
					System.setProperty("browser", s);
					
					refreshAllParameters();
					String browserValue = RuntimeParametersPlaywright.BROWSER.getValue();
					
					assertThat("Failed for '" + s + "'!", browserValue, is(equalTo(DEFAULT_BROWSER_VALUE)));
				});
	}
	
	@Test
	public void shouldGetReturnDefaultBrowserValueWhenNoSystemProperty() {
		System.clearProperty("browser");
		
		refreshAllParameters();
		String browserValue = RuntimeParametersPlaywright.BROWSER.getValue();
		
		assertThat(browserValue, is(equalTo(DEFAULT_BROWSER_VALUE)));
	}
	
	@Test
	public void shouldBrowserOptionBeBoolean() {
		shouldBrowserOptionBeOfClass("true", Boolean.class);
	}
	
	@Test
	public void shouldBrowserOptionBeInteger() {
		shouldBrowserOptionBeOfClass("123", Integer.class);
	}
	
	@Test
	public void shouldBrowserOptionBeIntegerFloat() {
		shouldBrowserOptionBeOfClass("123.321", Float.class);
	}
	
	@Test
	public void shouldBrowserOptionBeIntegerString() {
		shouldBrowserOptionBeOfClass("blue", String.class);
	}
	
	private void shouldBrowserOptionBeOfClass(String value, Class<?> clazz) {
		System.setProperty("browserOptions", BROWSER_OPTIONS_TEST_KEY + "=" + value);
		
		refreshAllParameters();
		Map<String, Object> browserOptionsValues = RuntimeParametersPlaywright.BROWSER_OPTIONS.getValues();
		Object optionValue = browserOptionsValues.get(BROWSER_OPTIONS_TEST_KEY);
		
		assertThat(optionValue, is(instanceOf(clazz)));
	}
	
	@Test
	public void shouldGetProperBrowserOptionsValuesSize() {
		final String[] browserOptions = STARTUP_PARAMETERS_VALUES.get(RuntimeParametersPlaywright.BROWSER_OPTIONS.getKey())
				.split(";");
		
		int browserOptionsCount = RuntimeParametersPlaywright.BROWSER_OPTIONS.getValues()
				.size();
		
		assertThat(browserOptionsCount, is(equalTo(browserOptions.length)));
	}
	
	@Test
	public void shouldGetProperBrowserOptionsValuesContents() {
		final String[] browserOptions = STARTUP_PARAMETERS_VALUES.get(RuntimeParametersPlaywright.BROWSER_OPTIONS.getKey())
				.split(";");
		
		Map<String, Object> browserOptionsValues = RuntimeParametersPlaywright.BROWSER_OPTIONS.getValues();
		
		browserOptionsValues.forEach((key, value) -> {
			try {
				String browserOption = Arrays.stream(browserOptions)
						.filter(s -> s.startsWith(key))
						.findFirst()
						.orElseThrow(Exception::new);
				
				assertThat(browserOption.endsWith(value.toString()), is(equalTo(true)));
			} catch (Exception e) {
				fail(key + " does NOT exist");
			}
		});
	}
	
	@Test
	public void shouldGetProperBrowserOptionsValuesBeNull() {
		Map<String, Object> browserOptionsValues = RuntimeParametersPlaywright.BROWSER.getValues();
		
		assertThat(browserOptionsValues, is(nullValue()));
	}
	
	@Test
	public void shouldToStringReturnKeyValue() {
		String toStringValue = RuntimeParametersPlaywright.BROWSER.toString();
		
		assertThat(toStringValue, is(equalTo("browser=" + STARTUP_PARAMETERS_VALUES.get("browser"))));
	}
}
