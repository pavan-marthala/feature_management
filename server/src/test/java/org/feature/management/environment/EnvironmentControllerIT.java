package org.feature.management.environment;

import io.restassured.http.ContentType;
import org.feature.management.AbstractIntegrationTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;

import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnvironmentControllerIT extends AbstractIntegrationTest {

    private static UUID sharedEnvId;

    @Test
    @Order(1)
    void shouldCreateEnvironment() {

        String requestBody = """
                {
                  "name": "dev",
                  "description": "Development Environment"
                }
                """;
         sharedEnvId  =   given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/environments")
                .then()
                .statusCode(201)
                .body(notNullValue())
                .extract().as(UUID.class);
    }

    @Test
    @Order(2)
    void shouldGetEnvironmentById() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/environments/{id}", sharedEnvId.toString())
                .then()
                .body("id", equalTo(sharedEnvId.toString()));
    }


    @Test
    @Order(3)
    void shouldGetAllEnvironments(){
        given()
                .when()
                .get("/environments")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .log().all();
    }

    @Test
    @Order(4)
    void shouldAssignOwnerToEnvironmentById() {
        String owner = "pavan@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/environments/{envId}/owners/{owner}", sharedEnvId.toString(), owner)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    void shouldAssignSecondOwnerToEnvironmentById() {
        String owner = "sam@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/environments/{envId}/owners/{owner}", sharedEnvId.toString(), owner)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(6)
    void shouldGetAllEnvironmentsAfterAssigningOwner(){
        given()
                .when()
                .get("/environments")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .log().all();
    }

    @Test
    @Order(7)
    void shouldRemoveOwnerFromEnvironmentByIdWithOutETAG() {
        String owner = "pavan@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/environments/{envId}/owners/{owner}", sharedEnvId.toString(), owner)
                .then()
                .statusCode(428);
    }

    @Test
    @Order(8)
    void shouldRemoveOwnerFromEnvironmentByIdWithUnmatchedETAG() {
        String owner = "pavan@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .when()
                .delete("/environments/{envId}/owners/{owner}", sharedEnvId.toString(), owner)
                .then()
                .statusCode(412);
    }

    @Test
    @Order(9)
    void shouldRemoveOwnerFromEnvironmentByIdIfAccessIsDenied() {
        String owner = "pavan1@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "2")
                .when()
                .delete("/environments/{envId}/owners/{owner}", sharedEnvId.toString(), owner)
                .then()
                .statusCode(401);
    }
    @Test
    @Order(10)
    void shouldRemoveOwnerFromEnvironmentByIdWhenEnvironmentIsNotExist() {
        String owner = "pavan1@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "2")
                .when()
                .delete("/environments/{envId}/owners/{owner}", UUID.randomUUID().toString(), owner)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(11)
    void shouldRemoveOwnerFromEnvironmentById() {
        String owner = "pavan@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "2")
                .when()
                .delete("/environments/{envId}/owners/{owner}", sharedEnvId.toString(), owner)
                .then()
                .statusCode(204);
    }
    @Test
    @Order(12)
    void shouldRemoveLastOwnerFromEnvironmentById() {
        String owner = "sam@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "3")
                .when()
                .delete("/environments/{envId}/owners/{owner}", sharedEnvId.toString(), owner)
                .then()
                .statusCode(404);
    }


    @Test
    @Order(13)
    void shouldUpdateEnvironmentByIdWithOutETAG() {

        String requestBody = """
                {
                  "name": "dev",
                  "description": "Environment for development"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .delete("/environments/{id}", sharedEnvId.toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(14)
    void shouldUpdateEnvironmentByIdWithUnmatchedETAG() {

        String requestBody = """
                {
                  "name": "dev",
                  "description": "Environment for development"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .body(requestBody)
                .when()
                .delete("/environments/{id}", sharedEnvId.toString())
                .then()
                .statusCode(412);
    }
    @Test
    @Order(15)
    void shouldUpdateEnvironmentByIdWhenEnvironmentIsNotExist() {

        String requestBody = """
                {
                  "name": "dev",
                  "description": "Environment for development"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "3")
                .body(requestBody)
                .when()
                .patch("/environments/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(16)
    void shouldUpdateEnvironmentById() {

        String requestBody = """
                {
                  "name": "dev",
                  "description": "Environment for development"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "3")
                .body(requestBody)
                .when()
                .patch("/environments/{id}", sharedEnvId.toString())
                .then()
                .statusCode(204);
    }

    @Test
    @Order(17)
    void shouldGetEnvironmentByIdWhenEnvironmentIsNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/environments/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(18)
    void shouldDeleteEnvironmentByIdWithOutETAG() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/environments/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(19)
    void shouldDeleteEnvironmentByIdWithUnmatchedETAG() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .when()
                .delete("/environments/{id}", sharedEnvId.toString())
                .then()
                .statusCode(412);
    }

    @Test
    @Order(20)
    void shouldDeleteEnvironmentByIdWhenEnvironmentIsNotExist() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "4")
                .when()
                .delete("/environments/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(21)
    void shouldDeleteEnvironmentById() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "4")
                .when()
                .delete("/environments/{id}",sharedEnvId.toString())
                .then()
                .statusCode(204);
    }
}
