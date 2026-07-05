package org.feature.management.integrationTest;

import io.restassured.http.ContentType;
import org.feature.management.AbstractIntegrationTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Should create environment successfully with a valid request")
    void shouldCreateEnvironmentSuccessfully() {

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
    @DisplayName("Should retrieve environment by its ID")
    void shouldGetEnvironmentByIdSuccessfully() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/environments/{id}", sharedEnvId.toString())
                .then()
                .body("id", equalTo(sharedEnvId.toString()));
    }


    @Test
    @Order(3)
    @DisplayName("Should retrieve all environments")
    void shouldGetAllEnvironmentsSuccessfully() {
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
    @DisplayName("Should assign owner to environment by ID")
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
    @DisplayName("Should assign a second owner to environment by ID")
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
    @DisplayName("Should retrieve all environments after assigning an owner")
    void shouldGetAllEnvironmentsAfterAssigningOwner() {
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
    @DisplayName("Should fail to remove owner from environment if If-Match header is missing")
    void shouldRemoveOwnerFromEnvironmentByIdWithoutETag() {
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
    @DisplayName("Should fail to remove owner from environment if ETag does not match")
    void shouldRemoveOwnerFromEnvironmentByIdWithUnmatchedETag() {
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
    @DisplayName("Should fail to remove owner from environment if access is denied")
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
    @DisplayName("Should return 404 when environment does not exist during owner removal")
    void shouldRemoveOwnerFromEnvironmentByIdWhenEnvironmentDoesNotExist() {
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
    @DisplayName("Should remove owner from environment successfully")
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
    @DisplayName("Should fail to remove the last owner from an environment")
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
    @DisplayName("Should fail to update environment if If-Match header is missing")
    void shouldUpdateEnvironmentByIdWithoutETag() {

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
    @DisplayName("Should fail to update environment if ETag does not match")
    void shouldUpdateEnvironmentByIdWithUnmatchedETag() {

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
                .patch("/environments/{id}", sharedEnvId.toString())
                .then()
                .statusCode(412);
    }
    @Test
    @Order(15)
    @DisplayName("Should return 404 when environment does not exist during update")
    void shouldUpdateEnvironmentByIdWhenEnvironmentDoesNotExist() {

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
    @DisplayName("Should update environment successfully with a valid request and matching ETag")
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
    @DisplayName("Should return 404 when environment does not exist")
    void shouldGetEnvironmentByIdWhenEnvironmentDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/environments/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(18)
    @DisplayName("Should fail to delete environment if If-Match header is missing")
    void shouldDeleteEnvironmentByIdWithoutETag() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/environments/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(19)
    @DisplayName("Should fail to delete environment if ETag does not match")
    void shouldDeleteEnvironmentByIdWithUnmatchedETag() {
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
    @DisplayName("Should return 404 when deleting a non-existent environment")
    void shouldDeleteEnvironmentByIdWhenEnvironmentDoesNotExist() {
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
    @DisplayName("Should delete environment successfully with matching ETag")
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
