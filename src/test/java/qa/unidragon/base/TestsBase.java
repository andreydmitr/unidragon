package qa.unidragon.base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Attachment;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static org.openqa.selenium.logging.LogType.BROWSER;

public class TestsBase {

    //static String pathToSelenoid = "selenoid.autotests.cloud";
    static String selenoidIp;
    public static SecureRandom randomGen;


    static public void initTests() {
        byte byteArray[] = {
                (byte) System.currentTimeMillis(), (byte) System.currentTimeMillis()
        };
        randomGen = new SecureRandom(byteArray);
        //randomGen.setSeed(System.currentTimeMillis() + (System.currentTimeMillis() >> 1));

        Configuration.pageLoadTimeout = 60000;

        String BASE_URL = System.getProperty("BASE_URL", "https://unidragon.ru");
        Configuration.baseUrl = BASE_URL;

        String BROWSER = System.getProperty("BROWSER", "chrome");
        Configuration.browser = BROWSER;

        String BROWSER_VERSION = System.getProperty("BROWSER_VERSION", "100");
        Configuration.browserVersion = BROWSER_VERSION;

        String BROWSER_SIZE = System.getProperty("BROWSER_SIZE", "1280x1024");
        Configuration.browserSize = BROWSER_SIZE;


        //
        System.out.println("Browser: " + BROWSER + ", version: " + BROWSER_VERSION + ", size: " + BROWSER_SIZE);
        System.out.println("Base URL: " + BASE_URL);


        selenoidIp = System.getProperty("IP", "");
        if (selenoidIp.contentEquals("")) {
            //local
            System.out.println("Configuration.remote: local");
        } else {
            //Configuration.remote = "https://user1:1234@"+pathToSelenoid+"/wd/hub";
            Configuration.remote = "http://" + selenoidIp + ":4444/wd/hub";
            System.out.println("Configuration.remote: " + Configuration.remote);
        }


        Configuration.fastSetValue = true;

        SelenideLogger.addListener("allure", new AllureSelenide());

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("enableVideo", true);
        Configuration.browserCapabilities = capabilities;


    }


    public void addAttachments() {
        screenshotAs("Last screenshot");
        pageSource();
        browserConsoleLogs();
        //
        addVideo();
        closeWebDriver();
    }


    @Attachment(value = "{attachName}", type = "text/plain")
    public static String attachAsText(String attachName, String message) {
        return message;
    }

    @Attachment(value = "Page source", type = "text/plain")
    public static byte[] pageSource() {
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

    public static void browserConsoleLogs() {
        if (Configuration.browser.contentEquals("firefox") ||
                Configuration.browser.contentEquals("safari")) {
            return;
        }
        attachAsText(
                "Browser console logs",
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

        String videoUrl = "http://" + selenoidIp + ":4444/video/" + sessionId + ".mp4";
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

    public static String getURL() {
        return WebDriverRunner.getWebDriver().getCurrentUrl();
    }

    public static void stop() {
        sleepMs(1000000);
    }
}
