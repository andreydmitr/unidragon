package qa.unidragon.base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.opencsv.CSVReader;
import io.qameta.allure.Attachment;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.qameta.allure.Allure.step;
import static org.openqa.selenium.logging.LogType.BROWSER;


public class TestsBase {

    // base url
    static final String baseURLDef = "https://unidragon.ru";
    static URL baseURL;// err
    final static String err = "ERROR: ";

    // selenoid
    static String selenoidIp;
    static String selenoidProtocol = "http://";
    static String selenoidIpTest = "selenoid.autotests.cloud";
    static String selenoidIpTestPass = "user1:1234@";

    // system properties from command line
    public static final String sysPropSelenoidIp = "SELENOID_IP";
    public static final String sysPropBaseURL = "BASE_URL";
    public static final String sysPropBrowser = "BROWSER";
    public static final String sysPropBrowserVersion = "BROWSER_VERSION";
    public static final String sysPropBrowserSize = "BROWSER_SIZE";


    // path with config info
    final static String testsConfigPath = "config/";

    // name of file with browsers test list
    final static String testsBrowsersFile1 = "testsBrowsers1.csv";
    final static String testsBrowsersFile2 = "testsBrowsers2.csv";

    // format of testsBrowsersFile
    public enum testsBrowsersFileCol {
        colBrowser,
        colBrowserVersion,
        colBrowserResolution
    }

    // data from testsBrowsersFile
    public static List<String[]> testsBrowsersFileLines;


    // name of file with excluded tests list
    final static String testsExcludedFile1 = "testsExcluded1.csv";
    final static String testsExcludedFile2 = "testsExcluded2.csv";
    // data from testsExcludedFile
    public static List<String[]> testsExcludedFileLines;
    // TODO one test can be run
    // gradle clean --tests testclassname


    // config string
    static String configString = "";

    // random generator
    static SecureRandom randomGen;


    @BeforeAll
    static void beforeAllTests() {
        //System.out.println("*** BEFOREALL");


        // init random
        byte byteArray[] = {
                (byte) System.currentTimeMillis(),
                (byte) System.currentTimeMillis()
        };
        randomGen = new SecureRandom(byteArray);


        // get selenoid ip
        selenoidIp = System.getProperty(sysPropSelenoidIp, "127.0.0.1");
        Configuration.remote = selenoidProtocol + selenoidIp + ":4444/wd/hub";
        System.out.println("Configuration.remote: " + Configuration.remote);


        // get base url
        String url = System.getProperty(sysPropBaseURL, "");
        if (url.contentEquals("")) {
            Configuration.baseUrl = baseURLDef;
        } else {
            URI uri;
            try {
                uri = new URI(url);
                baseURL = uri.toURL();
                Configuration.baseUrl = baseURL.toString();

            } catch (Exception e) {
                throw new RuntimeException(err + "baseURL is wrong: " + url);
            }
        }
        System.out.println("BaseURL: " + Configuration.baseUrl);

        //
        Configuration.fastSetValue = true;

        //Configuration.pageLoadTimeout=60000;
        // allure
        SelenideLogger.addListener("allure", new AllureSelenide());

        // video
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("enableVideo", true);
        Configuration.browserCapabilities = capabilities;


        String filePath;
        URL res;



        // read list of tests to exclude from testsExcludeFile
        filePath = testsConfigPath + testsExcludedFile1;
        res = TestsBase.class.getClassLoader().getResource(filePath);
        if (res == null) {
            filePath = testsConfigPath + testsExcludedFile2;
            res = TestsBase.class.getClassLoader().getResource(filePath);
        }
        if (res != null) {
            try {
                testsExcludedFileLines = readCSVFromFile(filePath, TestsBase.class.getClassLoader());
                System.out.println("Use file "+filePath+" with list of excluded tests");

            } catch (Exception e) {
                throw new RuntimeException(err + "Can't read: " + filePath);
            }
        } else {
            testsExcludedFileLines = null;
        }


        // check if we want to run one time with data from sys prop
        String browser = System.getProperty(sysPropBrowser, "");
        if (!browser.isBlank()) {
            // one browser to test for all tests
            System.out.println("One browser "+browser);
            testsBrowsersFileLines = null;
            // read from command line
            Configuration.browser = browser;
            Configuration.browserVersion = System.getProperty(sysPropBrowserVersion, "");
            Configuration.browserSize = System.getProperty(sysPropBrowserSize, "1280x1024");


        } else {
            // list of browsers to test from testsBrowsersFile
            filePath = testsConfigPath + testsBrowsersFile1;
            res = TestsBase.class.getClassLoader().getResource(filePath);

            if (res == null) {
                filePath = testsConfigPath + testsBrowsersFile2;
                res = TestsBase.class.getClassLoader().getResource(filePath);

                if (res == null) {
                    throw new RuntimeException(err + filePath + " is not found");
                }
            }
            System.out.println("Use file "+filePath+" with list of browsers");
            System.out.println("Path in Jenkins: src/test/resources/config/testsBrowsers.....");
            try {
                testsBrowsersFileLines = readCSVFromFile(filePath, TestsBase.class.getClassLoader());
            } catch (Exception e) {
                throw new RuntimeException(err + "Can't read: " + filePath);
            }


        }
    }


//    @BeforeEach
//    void beforeEachTest() {
//        System.out.println("*** BEFOREeach");
//        //step(configString);
//    }


    @AfterEach
    void afterEachTest(){
        closeWebDriver();
    }


    public void stepLast(StringBuffer config) {
        //System.out.println("*** AFTEREach");
        //step(" Тест завершен успешно", () -> {
            screenshotAs("Screenshot");
            pageSource("");
            browserConsoleLogs("");
            //
            addVideo();
            //
            config.setLength(0);
            //
            closeWebDriver();

        //});
    }

    @Attachment(value = "{attachName}", type = "text/plain")
    public static String attachAsText(String attachName, String message) {
        return message;
    }

    @Attachment(value = "{attachName} Page source", type = "text/plain")
    public static byte[] pageSource(String attachName) {
        return getWebDriver().getPageSource().getBytes(StandardCharsets.UTF_8);
    }

    @Attachment(value = "{attachName}", type = "image/png")
    public static byte[] screenshotAs(String attachName) {
        byte[] screenshot;
        try {
            screenshot = ((TakesScreenshot) getWebDriver()).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            screenshot = null;
        }
        return screenshot;
    }

    public static void browserConsoleLogs(String text) {
        // to avoid error @org.openqa.selenium.UnsupportedCommandException: HTTP method not allowed
        if (    Configuration.browser.contentEquals("FIREFOX") ||
                Configuration.browser.contentEquals("firefox") ||
                Configuration.browser.contentEquals("safari") ||
                Configuration.browser.contentEquals("SAFARI")) {
            return;
        }
        attachAsText(
                text + " Browser console logs",
                String.join("\n", Selenide.getWebDriverLogs(BROWSER))
        );
    }

    @Attachment(value = "Video", type = "text/html", fileExtension = ".html")
    public static String addVideo() {
        return "<html><body><video width='100%' height='100%' controls autoplay><source src='"
                + getVideoUrl(getSessionId())
                + "' type='video/mp4'></video></body></html>";
    }

    public static URL getVideoUrl(String sessionId) {

        //TODO https

        String videoUrl = selenoidProtocol + selenoidIp + ":4444/video/" + sessionId + ".mp4";
        System.out.println("videoURL: " + videoUrl);
        try {
            return new URL(videoUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSessionId() {
        return ((RemoteWebDriver) getWebDriver()).getSessionId().toString();
    }


    // check if test in excluded list
    // return true if test is in excluded list
    public static boolean testExcluded(String testName) {

        // if we run many tests
        if (testsBrowsersFileLines != null) {
            // is this test in excluded list
            if (testsExcludedFileLines != null) {
                for (String[] line : testsExcludedFileLines) {
                    int i = line.length;
                    while (i > 0) {
                        if (line[--i].equalsIgnoreCase(testName)) {
                            // do not run this test
                            step("SKIPPED by testsExcluded.csv file");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    public static String testContinueConfig(String methodName) {
        String config =
                ">>>> TEST: "
                        + methodName
                        + ',' + Configuration.browser
                        + ',' + Configuration.browserVersion
                        + ',' + Configuration.browserSize;
        System.out.println(config);
        return config;
    }

    // return config string if we have to run test
    // return "" if we do not need to run test
    public static String testContinue(int i, String methodName) {

        // if test run only once
        if (i == 0) {
            if (testsBrowsersFileLines == null) {
                return testContinueConfig(methodName);
            }
        } else if (i == 1) {
            if (testsBrowsersFileLines == null) return "";
        }

        int size = testsBrowsersFileLines.size();
        if (i == size) return "";

        String[] line = testsBrowsersFileLines.get(i);


        Configuration.browser = line[testsBrowsersFileCol.colBrowser.ordinal()];
        Configuration.browserVersion = line[testsBrowsersFileCol.colBrowserVersion.ordinal()];
        Configuration.browserSize = line[testsBrowsersFileCol.colBrowserResolution.ordinal()];


        return testContinueConfig(methodName);
    }


    public static String getURL() {
        return WebDriverRunner.getWebDriver().getCurrentUrl();
    }

    public static void stop() {
        sleepMs(1000000);
    }

    public static int getRandomUnsignedInt(int max) {
        return randomGen.nextInt(max);

        // second var
        //        if (max <= 0) return 0;
        //        int li = ThreadLocalRandom.current().nextInt();
        //        if (li < 0) li = -li;
        //return li % max;
    }

    public static void sleepMs(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // this part is executed when an exception (in this example InterruptedException) occurs
            // System.exit();
        }
    }

    public static List<String[]> readCSVFromFile(String file, ClassLoader cl) {
        List<String[]> lines = new ArrayList<String[]>();
        try {
            // input stream getClass()
            InputStream in = cl.getResourceAsStream(file);
            if (in==null){
                return lines;
            }
            CSVReader csv = new CSVReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            lines = csv.readAll();
        } catch (Exception e) {
            System.out.println(err+"read from "+file);
        }
        return lines;
    }


    public static List<String> readFile(String file, ClassLoader cl) {
        List<String> lines = new ArrayList<String>();

        try {
            // input stream
            InputStream in = cl.getResourceAsStream(file);
            if (in==null){
                return lines;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in));

            // reads each line
            String l;
            while ((l = r.readLine()) != null) {
                lines.add(l);
            }
            in.close();
        } catch (Exception e) {
            System.out.println(err+"read from "+file);
        }
        return lines;
    }

}
