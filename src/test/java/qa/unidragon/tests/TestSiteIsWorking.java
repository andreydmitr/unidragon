package qa.unidragon.tests;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.*;
import qa.unidragon.base.TestsBase;

import static io.qameta.allure.Allure.step;
import static qa.unidragon.base.TestsSteps.*;

public class TestSiteIsWorking extends TestsBase {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Сайт работает")
    void testSiteIsWorking(TestInfo testInfo) {
        // { FIRST STEP ---------------------------------------------------
        if (testExcluded(testInfo.getTestMethod().get().getName())) return;
        step(testInfo.getDisplayName(), () -> {
            int i = 0;
            final StringBuffer config = new StringBuffer();
            while (!config.append(
                            testContinue(i++, testInfo.getTestMethod().get().getName()))
                    .toString().isBlank()) {
                step(config.toString(), () -> {
                    // FIRST STEP } ---------------------------------------

                    stepOpenSite();

                    stepMainPageIsOk();

                    // { LAST STEP --------
                    stepLast(config);
                });
            }
        });
        // LAST STEP } --------------------
    }


}
