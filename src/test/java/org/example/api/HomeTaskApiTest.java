package org.example.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.example.model.Order;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class HomeTaskApiTest {
    @BeforeClass
    public void prepare() throws IOException {
        System.getProperties().load(ClassLoader.getSystemResourceAsStream("application.properties"));
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://petstore.swagger.io/v2/")
                .addHeader("api_key", System.getProperty("api.key"))
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
        RestAssured.filters(new ResponseLoggingFilter());
    }

    @Test
    public void checkObjectSave() throws IOException {
        // в application.properties - лучше использывать сложные orderId и petId, т.к. они могут уже быть и будут мешать успешному завершению тестов
        System.getProperties().load(ClassLoader.getSystemResourceAsStream("application.properties"));
        Order order = new Order();
        order.setId(Integer.parseInt(System.getProperty("orderId")));
        order.setPetId(Integer.parseInt(System.getProperty("petId")));
        order.setQuantity(2);
        order.setComplete(true);

        given()
                .body(order)
                .when()
                .post("store/order")
                .then()
                .statusCode(200);

        Order actual =
                given()
                        .pathParam("orderId", Integer.parseInt(System.getProperty("orderId")))
                        .when()
                        .get("store/order/{orderId}")
                        .then()
                        .statusCode(200)
                        .extract().body()
                        .as(Order.class);

        Assert.assertEquals(actual.getId(), order.getId());
    }

    @Test
    public void deleteOrder() throws IOException {
        System.getProperties().load(ClassLoader.getSystemResourceAsStream("application.properties"));
        given()
                .pathParam("orderId", Integer.parseInt(System.getProperty("orderId")))
                .when()
                .delete("store/order/{orderId}")
                .then()
                .statusCode(200);
        given()
                .pathParam("orderId", Integer.parseInt(System.getProperty("orderId")))
                .when()
                .get("store/order/{orderId}")
                .then()
                .statusCode(404);
    }

    @Test
    public void checkInventoryStatus() {
        Map<String, Object> inventory =
        given()
                .when()
                .get("store/inventory")
                .then()
                .statusCode(200)
                .extract().body()
                .as(new TypeRef<Map<String, Object>>() {});

        System.out.println(inventory);

        Assert.assertTrue(inventory.containsKey("sold"), "Inventory не содержит статус sold");
    }
}

