package qa.unidragon.tests;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import qa.unidragon.base.TestsBase;

import static com.codeborne.selenide.Selenide.$$;
import static io.qameta.allure.Allure.step;
import static qa.unidragon.base.TestsSteps.*;

public class TestAddToCart extends TestsBase {


    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Добавление товара в корзину")
    void testAddToCart(@NotNull TestInfo testInfo) {

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

                    String product = stepAddOneProductToCart();
                    System.out.println("Product: "+product);

                    stepPressBtnGoToCart();

                    step("Проверка, что товар \"" + product + "\" в корзине", () -> {
                        int size=$$(".order__item").filterBy(Condition.visible).size();
                        while (size-->0){
                            System.out.println(
                                            $$(".order__item")
                                            .filterBy(Condition.visible)
                                            .get(size)
                                            .$(".order__item-title")
                                            .text()
                                    );
                        }
                        Assertions.assertTrue(product
                                .equalsIgnoreCase($$(".order__item").get(0)
                                        .$(".order__item-title").text()));
                    });

                    step("Калькуляция корзины", () -> {
                        stepCalcCart();
                    });


                    // { LAST STEP --------
                    stepLast(config);
                });
            }
        });
        // LAST STEP } --------------------
    }


}
