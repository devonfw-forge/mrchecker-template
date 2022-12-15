package com.capgemini.mrchecker.selenium.core.newDrivers;

import com.capgemini.mrchecker.selenium.core.base.properties.PropertiesSelenium;
import com.capgemini.mrchecker.selenium.core.base.runtime.RuntimeParametersSelenium;
import com.capgemini.mrchecker.selenium.core.enums.ResolutionEnum;
import com.capgemini.mrchecker.selenium.core.exceptions.BFSeleniumGridNotConnectedException;
import com.capgemini.mrchecker.selenium.core.utils.OperationsOnFiles;
import com.capgemini.mrchecker.selenium.core.utils.ResolutionUtils;
import com.capgemini.mrchecker.test.core.logger.BFLogger;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DriverManager {
    private static final ThreadLocal<INewWebDriver> DRIVERS = new ThreadLocal<>();
    //private static final Browser BROWSER = getBrowser();
    private static final ResolutionEnum DEFAULT_RESOLUTION = ResolutionEnum.w1920;
    private static final int IMPLICITLY_WAIT = 2;
    private static final String DOWNLOAD_DIR = System.getProperty("java.io.tmpdir");
    private static boolean driverDownloadedChrome = false;
    private static boolean driverDownloadedFirefox = false;
    private static boolean driverDownloadedMicrosoftEdge = false;
    private static boolean driverDownloadedInternetExplorer = false;
    private static PropertiesSelenium propertiesSelenium;

//    private static Browser getBrowser() {
//        List<Browser> supportedBrowsers = new ArrayList<>();
//        supportedBrowsers.add(Browser.CHROME);
//        supportedBrowsers.add(Browser.EDGE);
//        supportedBrowsers.add(Browser.FIREFOX);
//        supportedBrowsers.add(Browser.IE);
//        String browserParam = RuntimeParametersSelenium.BROWSER.getValue();
//        for (Browser browser : supportedBrowsers) {
//            if (browser.is(browserParam)) {
//                return browser;
//            }
//        }
//        throw new RuntimeException("Unable to setup [" + browserParam + "]. Browser not recognized.");
//    }

    @Inject
    public DriverManager(@Named("properties") PropertiesSelenium propertiesSelenium) {
        if (Objects.isNull(DriverManager.propertiesSelenium)) {
            DriverManager.propertiesSelenium = propertiesSelenium;
        }
        this.start();
    }

    public void start() {
        DriverManager.getDriver();
    }

    public void stop() {
        try {
            closeDriver();
            BFLogger.logDebug("Closing Driver in stop()");
            BFLogger.logInfo(String.format("All clicks took %.2fs", 1.0 * NewRemoteWebElement.dropClickTimer() / 1000));
        } catch (Exception e) {
            // TODO: handle that
        }
    }

    public static INewWebDriver getDriver() {
        INewWebDriver driver = DRIVERS.get();
        if (Objects.isNull(driver)) {
            driver = createDriver();
            DRIVERS.set(driver);
            BFLogger.logDebug("Driver:" + driver);
        }
        return driver;
    }

    //If driver object is not null but there is no session or window it means that driver crashed
    public static boolean hasDriverCrushed() {
        if (wasDriverCreated()) {
            INewWebDriver driver = DRIVERS.get();
            try {
                return driver.toString().contains("(null)") || driver.getWindowHandles().isEmpty();
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    public static boolean wasDriverCreated() {
        return Objects.nonNull(DRIVERS.get());
    }

    public static void closeDriver() {
        INewWebDriver driver = DRIVERS.get();
        if (Objects.isNull(driver)) {
            BFLogger.logDebug("closeDriver() was called but there was no driver for this thread.");
        } else {
            try {
                BFLogger.logDebug("Closing WebDriver for this thread. " + RuntimeParametersSelenium.BROWSER.getValue());
                driver.quit();
            } catch (WebDriverException e) {
                BFLogger.logDebug("Ooops! Something went wrong while closing the driver: ");
                e.printStackTrace();
            } finally {
                DRIVERS.remove();
            }
        }
    }

    /**
     * Method sets desired 'driver' depends on chosen parameters
     */
    private static INewWebDriver createDriver() {
        BFLogger.logDebug("Creating new " + RuntimeParametersSelenium.BROWSER.getValue() + " WebDriver.");
        INewWebDriver driver;
        String seleniumGridParameter = RuntimeParametersSelenium.SELENIUM_GRID.getValue();
        driver = isEmpty(seleniumGridParameter) ? setupBrowser() : setupGrid();
        driver.manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);
        ResolutionUtils.setResolution(driver, DriverManager.DEFAULT_RESOLUTION);
        NewRemoteWebElement.setClickTimer();
        return driver;
    }

    private static boolean isEmpty(String seleniumGridParameter) {
        return seleniumGridParameter == null || seleniumGridParameter.trim().isEmpty();
    }

    /**
     * Method sets Selenium Grid
     */
    private static INewWebDriver setupGrid() {
        try {
            return Driver.SELENIUMGRID.getDriver();
        } catch (WebDriverException e) {
            throw new BFSeleniumGridNotConnectedException(e);
        }
    }

    /**
     * Method sets desired 'driver' depends on chosen parameters
     */
    private static INewWebDriver setupBrowser() {
        String browser = RuntimeParametersSelenium.BROWSER.getValue();
        switch (browser) {
            case BrowserType.CHROME:
                return Driver.CHROME.getDriver();
            case BrowserType.EDGE:
            case "edge":
            case "msedge":
                return Driver.EDGE.getDriver();
            case BrowserType.FIREFOX:
                return Driver.FIREFOX.getDriver();
            case BrowserType.IE:
            case "ie":
                return Driver.IE.getDriver();
            default:
                throw new IllegalStateException("Unsupported browser: " + browser);
        }
    }

    private enum Driver {
        CHROME {
            @Override
            public INewWebDriver getDriver() {
                return DriverManager.getDriver(getChromeOptions());
            }
        },
        EDGE {
            @Override
            public INewWebDriver getDriver() {
                return DriverManager.getDriver(getEdgeOptions());
            }
        },
        FIREFOX {
            @Override
            public INewWebDriver getDriver() {
                return DriverManager.getDriver(getFirefoxOptions());
            }
        },
        IE {
            @Override
            public INewWebDriver getDriver() {
                return DriverManager.getDriver(getInternetExplorerOptions());
            }
        },
        SELENIUMGRID {
            @Override
            public INewWebDriver getDriver() {
                String browser = RuntimeParametersSelenium.BROWSER.getValue();
                switch (browser) {
                    case BrowserType.CHROME:
                        return setupGrid(getChromeOptions());
                    case BrowserType.EDGE:
                    case "edge":
                    case "msedge":
                        return setupGrid(getEdgeOptions());
                    case BrowserType.FIREFOX:
                        return setupGrid(getFirefoxOptions());
                    default:
                        throw new IllegalStateException("Unsupported browser: " + browser);
                }
            }
        };

        protected abstract INewWebDriver getDriver();
    }

    private static <T extends RemoteWebDriver> void downloadNewestOrGivenVersionOfWebDriver(Class<T> webDriverType) {
        String proxy = DriverManager.propertiesSelenium.getProxy();
        String webDriversPath = DriverManager.propertiesSelenium.getWebDrivers();
        try {
            System.setProperty("wdm.targetPath", webDriversPath);
            System.setProperty("wdm.useBetaVersions", "false");

            WebDriverManager.getInstance(webDriverType)
                    .proxy(proxy)
                    .setup();
            BFLogger.logDebug("Downloaded version of driver=" + WebDriverManager.getInstance(webDriverType).getDownloadedDriverVersion());

        } catch (WebDriverManagerException e) {
            BFLogger.logInfo("Unable to download driver automatically. "
                    + "Please try to set up the proxy in properties file. "
                    + "If you want to download them manually, go to the "
                    + "http://www.seleniumhq.org/projects/webdriver/ site.");
        }
    }

    private static ChromeOptions getChromeOptions() {
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("download.default_directory", DOWNLOAD_DIR);
        chromePrefs.put("profile.content_settings.pattern_pairs.*.multiple-automatic-downloads", 1);
        chromePrefs.put("profile.default_content_setting_values.notifications", 2);

        RuntimeParametersSelenium.BROWSER_OPTIONS.getValues().forEach((key, value) -> {
            BFLogger.logInfo("Add to Chrome prefs: " + key + " = " + value.toString());
            chromePrefs.put(key, value.toString());
        });

        ChromeOptions options = new ChromeOptions();
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS);
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.setExperimentalOption("prefs", chromePrefs);
        options.setAcceptInsecureCerts(true);
        options.setCapability("acceptSslCerts", true);
        options.addArguments("--test-type");
        options.addArguments("window-size=1920x1080");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--allow-insecure-localhost");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-popup-blocking");
        options.setHeadless(Boolean.parseBoolean(System.getProperty("headless", "false")));

        RuntimeParametersSelenium.BROWSER_OPTIONS.getValues().forEach((key, value) -> {
            BFLogger.logInfo("Add to Chrome options: " + key + " = " + value.toString());
            String item = (value.toString().isEmpty()) ? key : key + "=" + value;
            options.addArguments(item);
            options.setCapability(key, value.toString());
        });

        return options;
    }

    public static INewWebDriver getDriver(ChromeOptions chromeOptions) {
        INewWebDriver driver = DRIVERS.get();
        if (Objects.isNull(driver)) {
            String seleniumGridParameter = RuntimeParametersSelenium.SELENIUM_GRID.getValue();
            if (!isEmpty(seleniumGridParameter)) {
                return setupGrid(chromeOptions);
            }
            String browserPath = DriverManager.propertiesSelenium.getSeleniumChrome();
            boolean isDriverAutoUpdateActivated = DriverManager.propertiesSelenium.getDriverAutoUpdateFlag();
            synchronized (DOWNLOAD_DIR) {
                if (isDriverAutoUpdateActivated && !driverDownloadedChrome) {
                    if (!DriverManager.propertiesSelenium.getChromeDriverVersion().isEmpty()) {
                        System.setProperty("wdm.chromeDriverVersion", DriverManager.propertiesSelenium.getChromeDriverVersion());
                    }
                    downloadNewestOrGivenVersionOfWebDriver(ChromeDriver.class);
                    OperationsOnFiles.moveWithPruneEmptydirectories(WebDriverManager.getInstance(ChromeDriver.class).getDownloadedDriverPath(), browserPath);
                }
                driverDownloadedChrome = true;
            }
            System.setProperty("webdriver.chrome.driver", browserPath);
            return new NewChromeDriver(chromeOptions);
        }
        return driver;
    }

    private static EdgeOptions getEdgeOptions() {
        HashMap<String, Object> edgePrefs = new HashMap<>();
        edgePrefs.put("download.default_directory", DOWNLOAD_DIR);
        edgePrefs.put("profile.content_settings.pattern_pairs.*.multiple-automatic-downloads", 1);
        edgePrefs.put("profile.default_content_setting_values.notifications", 2);

        RuntimeParametersSelenium.BROWSER_OPTIONS.getValues().forEach((key, value) -> {
            BFLogger.logInfo("Add to Edge prefs: " + key + " = " + value.toString());
            edgePrefs.put(key, value.toString());
        });

        EdgeOptions options = new EdgeOptions();
        //options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS);
        //options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL.toString());
        //options.setExperimentalOption("prefs", edgePrefs);
        //options.setAcceptInsecureCerts(true);
        options.setCapability("acceptSslCerts", true);
        //options.addArguments("--test-type");
        //options.addArguments("window-size=1920x1080");
        //options.addArguments("--ignore-certificate-errors");
        //options.addArguments("--allow-insecure-localhost");
        //options.addArguments("--allow-running-insecure-content");
        //options.addArguments("--disable-popup-blocking");
        //options.setHeadless(Boolean.parseBoolean(System.getProperty("headless", "false")));

        RuntimeParametersSelenium.BROWSER_OPTIONS.getValues().forEach((key, value) -> {
            BFLogger.logInfo("Add to Edge options: " + key + " = " + value.toString());
            String item = (value.toString().isEmpty()) ? key : key + "=" + value;
            //options.addArguments(item);
            options.setCapability(key, value.toString());
        });

        return options;
    }

    public static INewWebDriver getDriver(EdgeOptions edgeOptions) {
        INewWebDriver driver = DRIVERS.get();
        if (Objects.isNull(driver)) {
            String seleniumGridParameter = RuntimeParametersSelenium.SELENIUM_GRID.getValue();
            if (!isEmpty(seleniumGridParameter)) {
                return setupGrid(edgeOptions);
            }
            String browserPath = DriverManager.propertiesSelenium.getSeleniumEdge();
            boolean isDriverAutoUpdateActivated = DriverManager.propertiesSelenium.getDriverAutoUpdateFlag();
            synchronized (DOWNLOAD_DIR) {
                if (isDriverAutoUpdateActivated && !driverDownloadedMicrosoftEdge) {
                    if (!DriverManager.propertiesSelenium.getEdgeDriverVersion().isEmpty()) {
                        System.setProperty("wdm.edgeVersion", DriverManager.propertiesSelenium.getEdgeDriverVersion());
                    }
                    downloadNewestOrGivenVersionOfWebDriver(EdgeDriver.class);
                    OperationsOnFiles.moveWithPruneEmptydirectories(WebDriverManager.getInstance(EdgeDriver.class).getDownloadedDriverPath(), browserPath);
                }
                driverDownloadedMicrosoftEdge = true;
            }
            System.setProperty("webdriver.edge.driver", browserPath);
            return new NewEdgeDriver(edgeOptions);
        }
        return driver;
    }

    private static FirefoxOptions getFirefoxOptions() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setAcceptUntrustedCertificates(true);
        profile.setAssumeUntrustedCertificateIssuer(false);
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.dir", DOWNLOAD_DIR);
        profile.setPreference("browser.download.useDownloadDir", true);
        profile.setPreference("security.ssl.enable_ocsp_stapling", false);
        profile.setPreference("dom.disable_beforeunload", true);
        profile.setPreference("dom.webnotifications.enabled", false);
        profile.setPreference("dom.push.enabled", false);
        profile.setPreference("browser.cache.disk.enable", false);
        profile.setPreference("browser.cache.memory.enable", false);
        profile.setPreference("browser.cache.offline.enable", false);
        profile.setPreference("browser.startup.homepage_override.mstone", "ignore");
        profile.setPreference("security.tls.version.min", 1);
        profile.setPreference("intl.accept_languages", "en-us");
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/comma-separated-values, application/vnd.ms-excel, application/msword, application/csv, application/ris, text/csv, image/png, application/pdf, text/html, text/plain, application/zip, application/x-zip, application/x-zip-compressed, application/download, application/octet-stream, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        profile.setPreference("browser.download.manager.showWhenStarting", false);
        profile.setPreference("browser.helperApps.alwaysAsk.force", false);
        profile.setPreference("pdfjs.disabled", false);
        profile.setPreference("network.http.use-cache", false);

        RuntimeParametersSelenium.BROWSER_OPTIONS.getValues().forEach((key, value) -> {
            BFLogger.logInfo("Add to Firefox profile: " + key + " = " + value.toString());
            profile.setPreference(key, value.toString());
        });

        FirefoxOptions options = new FirefoxOptions().setProfile(profile);
        options.setCapability("security.ssl.enable_ocsp_stapling", false);
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS);
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.setAcceptInsecureCerts(true);
        options.setCapability("acceptSslCerts", true);
        options.setCapability("handleAlerts", true);
        options.setCapability("network.http.use-cache", false);
        options.setCapability("security.cert_pinning.enforcement_level", 0);
        options.setCapability("security.enterprise_roots.enabled", true);
        options.setHeadless(Boolean.parseBoolean(System.getProperty("headless", "false")));

        RuntimeParametersSelenium.BROWSER_OPTIONS.getValues().forEach((key, value) -> {
            BFLogger.logInfo("Add to Firefox options: " + key + " = " + value.toString());
            String item = (value.toString().isEmpty()) ? key : key + "=" + value;
            options.addArguments(item);
            options.setCapability(key, value.toString());
        });

        return options;
    }

    public static INewWebDriver getDriver(FirefoxOptions firefoxOptions) {
        INewWebDriver driver = DRIVERS.get();
        if (Objects.isNull(driver)) {
            String seleniumGridParameter = RuntimeParametersSelenium.SELENIUM_GRID.getValue();
            if (!isEmpty(seleniumGridParameter)) {
                return setupGrid(firefoxOptions);
            }
            String browserPath = DriverManager.propertiesSelenium.getSeleniumFirefox();
            boolean isDriverAutoUpdateActivated = DriverManager.propertiesSelenium.getDriverAutoUpdateFlag();
            synchronized (DOWNLOAD_DIR) {
                if (isDriverAutoUpdateActivated && !driverDownloadedFirefox) {
                    if (!DriverManager.propertiesSelenium.getGeckoDriverVersion().isEmpty()) {
                        System.setProperty("wdm.geckoDriverVersion", DriverManager.propertiesSelenium.getGeckoDriverVersion());
                    }
                    downloadNewestOrGivenVersionOfWebDriver(FirefoxDriver.class);
                    OperationsOnFiles.moveWithPruneEmptydirectories(WebDriverManager.getInstance(FirefoxDriver.class).getDownloadedDriverPath(), browserPath);
                }
                driverDownloadedFirefox = true;
            }
            System.setProperty("webdriver.gecko.driver", browserPath);
            System.setProperty("webdriver.firefox.logfile", "logs\\firefox_logs.txt");
            return new NewFirefoxDriver(firefoxOptions);
        }
        return driver;
    }

    private static InternetExplorerOptions getInternetExplorerOptions() {
        InternetExplorerOptions options = new InternetExplorerOptions();
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS);
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        //options.setAcceptInsecureCerts(true);
        options.setCapability("acceptSslCerts", true);

        RuntimeParametersSelenium.BROWSER_OPTIONS.getValues().forEach((key, value) -> {
            BFLogger.logInfo("Add to Internet Explorer options: " + key + " = " + value.toString());
            String item = (value.toString().isEmpty()) ? key : key + "=" + value;
            options.setCapability(key, value.toString());
        });

        return options;
    }

    public static INewWebDriver getDriver(InternetExplorerOptions internetExplorerOptions) {
        INewWebDriver driver = DRIVERS.get();
        if (Objects.isNull(driver)) {
            String seleniumGridParameter = RuntimeParametersSelenium.SELENIUM_GRID.getValue();
            if (!isEmpty(seleniumGridParameter)) {
                return setupGrid(internetExplorerOptions);
            }
            String browserPath = DriverManager.propertiesSelenium.getSeleniumIE();
            boolean isDriverAutoUpdateActivated = DriverManager.propertiesSelenium.getDriverAutoUpdateFlag();
            synchronized (DOWNLOAD_DIR) {
                if (isDriverAutoUpdateActivated && !driverDownloadedInternetExplorer) {
                    if (!DriverManager.propertiesSelenium.getInternetExplorerDriverVersion()
                            .equals("")) {
                        System.setProperty("wdm.internetExplorerDriverVersion", DriverManager.propertiesSelenium.getInternetExplorerDriverVersion());
                    }
                    downloadNewestOrGivenVersionOfWebDriver(InternetExplorerDriver.class);
                    OperationsOnFiles.moveWithPruneEmptydirectories(WebDriverManager.getInstance(InternetExplorerDriver.class).getDownloadedDriverPath()
                            , browserPath);
                }
                driverDownloadedInternetExplorer = true;
            }
            System.setProperty("webdriver.ie.driver", browserPath);
            return new NewInternetExplorerDriver(internetExplorerOptions);
        }
        return driver;
    }

    private static INewWebDriver setupGrid(MutableCapabilities options) {
        final String SELENIUM_GRID_URL = RuntimeParametersSelenium.SELENIUM_GRID.getValue();
        BFLogger.logDebug("Connecting to the selenium grid: " + SELENIUM_GRID_URL);
        DesiredCapabilities capabilities = new DesiredCapabilities();
        String operatingSystem = RuntimeParametersSelenium.OS.getValue();

        switch (operatingSystem) {
            case "windows":
                capabilities.setPlatform(Platform.WINDOWS);
                break;
            case "vista":
                capabilities.setPlatform(Platform.VISTA);
                break;
            case "mac":
                capabilities.setPlatform(Platform.MAC);
                break;
            default:
                capabilities.setPlatform(Platform.LINUX);
        }

        String browser = RuntimeParametersSelenium.BROWSER.getValue();
        capabilities.setVersion(RuntimeParametersSelenium.BROWSER_VERSION.getValue());
        switch (browser) {
            case BrowserType.CHROME:
                capabilities.setCapability(ChromeOptions.CAPABILITY, options);
                break;
            case BrowserType.EDGE:
            case "edge":
            case "msedge":
                //capabilities.setCapability(EdgeOptions.CAPABILITY, options);
                break;
            case BrowserType.FIREFOX:
                capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
                break;
            default:
                throw new IllegalStateException("Unsupported browser: " + browser);
        }
        capabilities.setBrowserName(browser);

        RuntimeParametersSelenium.BROWSER_OPTIONS.getValues().forEach((key, value) -> {
            BFLogger.logInfo("Browser option: " + key + " " + value.toString());
            capabilities.setCapability(key, value.toString());
        });

        NewRemoteWebDriver newRemoteWebDriver = null;
        try {
            newRemoteWebDriver = new NewRemoteWebDriver(new URL(SELENIUM_GRID_URL), capabilities);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("Unable to find selenium grid URL: " + SELENIUM_GRID_URL);
        }
        return newRemoteWebDriver;
    }

    @SuppressWarnings("removal")
    @Override
    // TODO: handle that finalize should never been called - it's unreliable. Intoduce Autoclose()
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            closeDriver();
            BFLogger.logDebug("Closing Driver in finalize()");
        } catch (Exception e) {
            // TODO: handle that
        }
    }
}