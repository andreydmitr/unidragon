package qa.unidragon.tests;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Keys;
import qa.unidragon.base.TestsBase;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.$$;
import static io.qameta.allure.Allure.step;
import static org.openqa.selenium.By.linkText;
import static qa.unidragon.steps.TestsSteps.*;

public class TestsOk extends TestsBase {

    @BeforeAll
    static void testOkInitTests() {
        initTests();

    }

    @AfterEach
    void testOkAddAttachments() {
        addAttachments();
    }


    @Tag("testSiteIsWorking")
    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Сайт работает")
    void testSiteIsWorking() {

        stepOpenSite();

        stepMainPageIsOk();
    }


    @Tag("testAddToCart")
    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Добавление товара в корзину")
    void testAddToCart() {

        stepOpenSite();

        String product = stepAddOneProductToCart();

        stepPressBtnGoToCart();

        step("Проверка, что товар \"" + product + "\" в корзине", () -> {
            Assertions.assertTrue(product
                    .equalsIgnoreCase($$(".order__item").get(0)
                            .$(".order__item-title").text()));
        });

        step("Калькуляция корзины", () -> {
            stepCalcCart();
        });
    }

    @Tag("debug")
    //@Tag("testDeliveryByCourierSDEK")
    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Заказ курьером СДЭК")
    void testDeliveryByCourierSDEK() {

        stepOpenSite();

        step("Положим в корзину несколько товаров", () -> {

            int maxRowsInOrder = 5;
            int rows = getRandomUnsignedInt(maxRowsInOrder) + 1;
            //int rows = 1;
            System.out.println("Товаров, чтобы положить в корзину: " + rows);

            while (true) {
                stepAddOneProductToCart();
                if (--rows == 0) break;
                step("Продолжить покупки", () -> {
                    stepCloseNotifyPage();

                    $(linkText("Продолжить покупки")).click();
                });

                stepMainPageIsOk();
            }
            stepPressBtnGoToCart();
        });

        step("Калькуляция корзины", () -> {
            stepCalcCart();
        });

        step("Выбрать населенный пункт", () -> {
            $("[id='form-order-city'").shouldBe(visible, Duration.ofSeconds(40)).click();
            $("[id='form-order-city'").setValue("г Самара");
            $(".suggestions-suggestions").
                    shouldBe(visible, Duration.ofSeconds(40));
            //sleepMs(3000);
            $("[id='form-order-city'").sendKeys(Keys.DOWN, Keys.RETURN);
        });

        String deliveryType = "Курьером СДЭК";
        step("Выбрать способ доставки " + deliveryType, () -> {

            $$(".field--type-radio")
                    //.filter(visible)
                    .findBy(text(deliveryType))
                    .scrollIntoView(false)
                    .shouldBe(visible, Duration.ofSeconds(60))
                    .click();
        });

        String address = "Адрес для тестов д.31 кв.234";
        step("Вводим адрес", () -> {
            Assertions.assertTrue(
                    fieldSet("input[name='adres']", address)
                            .equalsIgnoreCase(address)
            );
        });

        String name = "Иван Иванович";
        step("Вводим имя", () -> {
            Assertions.assertTrue(
                    fieldSet("[id='form-order-name'", name)
                            .equalsIgnoreCase(name)
            );
        });

        String phone = "+7 999 999 99 99";
        step("Вводим телефон", () -> {
            String phoneGet = fieldSet("[id='customer_phone'", phone)
                    .replaceAll("-", " ");

            System.out.println(phoneGet);
            Assertions.assertTrue(phoneGet
                    .equalsIgnoreCase(phone)
            );
        });

        String email = "test@test.com";
        step("Вводим email", () -> {
            Assertions.assertTrue(
                    fieldSet("[id='form-order-email'", email)
                            .equalsIgnoreCase(email)
            );
        });

        step("Выбор способа оплаты картой", () -> {
            $("[for='form-order-payment-cloudpayments']")
                    .shouldBe(visible, Duration.ofSeconds(40))
                    .click();
        });

        final StringBuffer orderSumToPay = new StringBuffer();
        step("Калькуляция корзины", () -> {
            orderSumToPay.append(stepCalcCart());
            System.out.println("Сумма заказа: " + orderSumToPay);
        });

        step("Ожидаемый результат - видима и доступна кнопка \"Офомить заказ\"", () -> {
            $(".order__button-submit")
                    .shouldBe(visible, Duration.ofSeconds(40))
                    .shouldBe(enabled);
        });
    }


}
