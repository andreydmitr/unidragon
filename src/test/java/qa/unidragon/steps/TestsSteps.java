package qa.unidragon.steps;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.$$;
import static io.qameta.allure.Allure.step;
import static org.openqa.selenium.By.linkText;
import static qa.unidragon.base.TestsBase.*;

public class TestsSteps {


    static public void stepCloseNotifyPage() {
        step("Закрываем окно с предложением подписаться на оповещения", () -> {
            try {
                if ($$("div.backdrop-close").filterBy(visible).size() != 0) {
                    $$("div.backdrop-close").findBy(visible).click();
                }
            } catch (Exception e) {

            }
        });
    }

    static public void stepOpenSite() {

        // repeat count
        int n = 3;

        while (n-- > 0) {
            try {
                step("Открыть сайт", () -> {
                    long lstart = System.currentTimeMillis();
                    open(Configuration.baseUrl);
                    randomGen.setSeed(System.currentTimeMillis() - lstart);
                });
                break;
            } catch (Exception e) {
                closeWebDriver();
            }
        }

        // site has opened without exception
        if (n > 0) {
            stepCloseNotifyPage();

        } else {
            Assertions.fail("Ошибка открытия сайта " + Configuration.baseUrl);
        }
    }


    static public void stepMainPageIsOk() {

        String textMainPage = "Unidragon";
        String textMainPageTitle = "Интернет-магазин Пазлов";

        step("Ожидаемый результат: текст заголовка включает текст \"" + textMainPage + "\"", () -> {
            stepCloseNotifyPage();

            $("span.intro__title-unidragon")
                    .shouldBe(visible, Duration.ofSeconds(120))
                    .shouldHave(text(textMainPage));
        });


        step("Ожидаемый результат: текст заголовка включает текст \"" + textMainPageTitle + "\"", () -> {
            String title = title();
            if (title != null) {
                // System.out.println(title);
                Assertions.assertTrue(title.contains(textMainPageTitle));
            }
        });
    }


    static public boolean selectProductSize(String loc) {
        stepCloseNotifyPage();
        int sizeCount = $$(loc).filter(visible).size();
        System.out.println("Sizes count: " + sizeCount);
        if (sizeCount > 0) {
            sizeCount = getRandomUnsignedInt(sizeCount);
            System.out.println("Size selected -> " + sizeCount);
            stepCloseNotifyPage();
            $$(loc).filter(visible).get(sizeCount).click();
            return true;
        }
        return false;
    }


    static public void stepSelectRndProductSize() {

        step("Выбираем размер товара случайным образом", () -> {
            while (true) {
                if (selectProductSize(".queezle-sets__item")) break;
                if (selectProductSize(".product__size-radio")) break;
                if (selectProductSize(".radio-size__label")) break;
                if (selectProductSize(".radio-size__picture")) break;
                break;
            }
        });
    }


    static public void stepPressBtnGoToCart() {
        stepCloseNotifyPage();

        step("Перейти к корзине", () -> {
            $(linkText("Перейти в корзину")).click();
        });

        String textOrdering = "Оформление заказа";
        step("Проверка названия страницы \"" + textOrdering + "\"", () -> {
            $("h2.order__title")
                    .shouldBe(visible, Duration.ofSeconds(40))
                    .shouldHave(text(textOrdering));
        });
    }


    // remove all except digits and dot from string
    static long parseLongFromString(@NotNull String str) {
        return Long.parseLong(str.replaceAll("[^\\d.]", ""));
    }


    // assert if it is not because of rounding
    static void assertIfNotRound(Long first, Long second) {
        if (first > second) {
            first = first - second;
        } else {
            first = second - first;
        }
        if (first > 1) Assertions.fail();
    }

    static public Long stepCalcCart() {

        String locItem = ".order__item";
        int countItems;
        countItems = $$(locItem).size();
        System.out.println("Cart: " + countItems + " rows.");
        Assertions.assertNotEquals(countItems, 0);


        long cost, number, sum, rowCost, rowCostFromPage;
        long orderSumToPay;
        long totalSum = 0;

        int i = 0;
        while (i < countItems) {
            step($$(locItem).get(i)
                    .shouldBe(visible, Duration.ofSeconds(40))
                    .$(".order__item-title").text());
            //price
            if ($$(locItem).get(i).$$(".order__item-price-actual").size() != 0) {
                cost = parseLongFromString($$(locItem).get(i)
                        .$(".order__item-price-actual").text());
            } else {
                cost = parseLongFromString($$(locItem).get(i)
                        .$$(".order__item-price").get(0).text());
            }

            number = 0;
            String num = $$(locItem).get(i).$("input[type=number]").attr("value");
            if (num != null) {
                number = parseLongFromString(num);
            }

            rowCost = cost * number;

            rowCostFromPage = parseLongFromString($$(locItem).get(i)
                    .$$(".order__item-price").get(1).text());

            System.out.println(
                    $$(locItem).get(i).$(".order__item-title").text() + " : " +
                            cost + "*" + number + "=" + rowCost + " ? " + rowCostFromPage);


            assertIfNotRound(rowCost, rowCostFromPage);

            totalSum = totalSum + rowCost;

            i++;
        }
        System.out.println("РАСЧЕТНАЯ стоимость всех товаров с учетом только ценовой скидки " +
                totalSum);


        // get cost of all products in order
        String textCostAllProducts = "Общая стоимость товаров";
        long orderCostAllProducts;
        if ($(withText(textCostAllProducts))
                .is(visible)) {
            orderCostAllProducts = parseLongFromString(
                    $(withText(textCostAllProducts))
                            .parent().text());
            System.out.println(textCostAllProducts + " : " + orderCostAllProducts);
        }


        // get discount for amount
        String textDiscountByAmount = "Скидка за количество";
        long orderDiscountForAmount = 0L;
        if ($(withText(textDiscountByAmount))
                .is(visible)) {
            orderDiscountForAmount = parseLongFromString(
                    $(withText(textDiscountByAmount))
                            .parent().text());
            System.out.println(textDiscountByAmount + " : " + orderDiscountForAmount);
        }


        // get discount
        String textDiscount = "Скидка";
        long orderDiscount;
        if ($(withText(textDiscount))
                .is(visible)) {
            orderDiscount = parseLongFromString(
                    $(withText(textDiscount))
                            .parent().text());
            System.out.println(textDiscount + " : " + orderDiscount);
        }


        // get delivery cost
        String textDeliveryCourierSDEK = "Доставка курьером СДЭК";
        long orderCostDeliveryCourierSDEK = 0L;
        if ($(withText(textDeliveryCourierSDEK))
                .is(visible)) {
            orderCostDeliveryCourierSDEK = parseLongFromString(
                    $(withText(textDeliveryCourierSDEK))
                            .parent().text());
            System.out.println(textDeliveryCourierSDEK + " : " + orderCostDeliveryCourierSDEK);

        }


        // to pay
        String textSumToPay = "Сумма к оплате";
        orderSumToPay = parseLongFromString($$(".order__total-sum").get(1)
                .shouldBe(visible, Duration.ofSeconds(40)).text());
        System.out.println(textSumToPay + " : " + orderSumToPay);


        // compare products cost
        assertIfNotRound(
                orderSumToPay
                        + orderDiscountForAmount
                        - orderCostDeliveryCourierSDEK,
                totalSum);


        return orderSumToPay;
    }


    static public String stepAddOneProductToCart() {
        final StringBuffer prod = new StringBuffer();
        while (true) {

            step("Щелкаем на произвольном товаре", () -> {
                stepCloseNotifyPage();

                int productCount = $$("article.c-product")
                        .filterBy(visible)
                        .size();
                //System.out.println("productCount :" + productCount);

                int productSelected = getRandomUnsignedInt(productCount);
                System.out.println($$("article.c-product").get(productSelected).attr("id"));

                stepCloseNotifyPage();

                $$("article.c-product").get(productSelected).click();
            });

            step("Загружается страница товара", () -> {
                stepCloseNotifyPage();
                $(".button.product__button-cart")
                        .shouldBe(visible, Duration.ofSeconds(40));

                step(getURL());
                System.out.println(getURL());
            });

            stepSelectRndProductSize();


            step("Сохраним название товара", () -> {
                prod.append($(".product__title").text());
            });

            int numberRndMax = 5;
            int numberRnd = getRandomUnsignedInt(numberRndMax) + 1;
            System.out.println("order pcs: " + numberRnd);
            step("Выберем случайное количество товара \"" + prod + "\": " + numberRnd, () -> {
                int i = 0;
                while (i < numberRnd) {

                    //$("button.field__plus").scrollTo();
                    // test if not in stock
                    //class="product__stock-notification-title"

                    stepCloseNotifyPage();
                    boolean clicked = false;
                    if ($("button.field__plus").is(visible)) {
                        i++;
                        stepCloseNotifyPage();
                        if ($("button.field__plus").is(visible)) {
                            clicked = true;
                            if (i > 1) {
                                $("button.field__plus").click();
                            }
                        }
                    }
                    if (!clicked) {
                        step("Товара \"" + prod + "\" нет в наличии", () -> {
                            $(".product__stock-notification-title")
                                    .shouldBe(visible, Duration.ofSeconds(40))
                                    .shouldHave(text("Нет в наличии"));
                            System.out.println("Товара \"" + prod + "\" нет в наличии");
                            prod.setLength(0);


                            // back
                            back();
                            // wait
                            stepMainPageIsOk();
                        });

                        break;
                    }
                }

            });

            // if product is not in stock
            if (prod.length() == 0) continue;

            step("Щелкаем на кнопку \"Добавить в корзину\" товар \"" + prod + "\"", () -> {
                stepCloseNotifyPage();
                $(".button.product__button-cart")
                        .shouldBe(visible, Duration.ofSeconds(40)).click();

            });

            String textProductAddedToCart = "Товар добавлен в корзину!";
            step("Проверка названия страницы \"" + textProductAddedToCart + "\"", () -> {
                $("h2.success__title")
                        .shouldBe(visible, Duration.ofSeconds(40))
                        .shouldHave(text(textProductAddedToCart));
            });

            break;
        }
        return prod.toString();
    }


    static public String fieldSet(String loc, String valueSet) {
        $(loc).shouldBe(visible, Duration.ofSeconds(40)).click();
        $(loc).setValue(valueSet);
        String valueGet = $(loc).getValue();
        System.out.println("loc: " + loc + " set: " + valueSet + " get: " + valueGet);
        return valueGet;
    }


}
