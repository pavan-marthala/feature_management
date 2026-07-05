package org.feature.management.integrationTest;

import io.restassured.http.ContentType;
import org.feature.management.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;

import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkspaceControllerIT extends AbstractIntegrationTest {
    private static UUID workspaceId;

    @Test
    @Order(1)
    @DisplayName("Should create workspace successfully")
    void shouldCreateWorkspace() {

        String requestBody = """
                {
                  "name": "Checkout Service",
                  "description": "Workspace for checkout features"
                }
                """;
        workspaceId  =   given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/workspaces")
                .then()
                .statusCode(201)
                .body(notNullValue())
                .extract().as(UUID.class);
    }

    @Test
    @Order(2)
    @DisplayName("Should retrieve workspace by ID")
    void shouldGetWorkspaceById() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/workspaces/{id}", workspaceId.toString())
                .then()
                .body("id", equalTo(workspaceId.toString()));
    }


    @Test
    @Order(3)
    @DisplayName("Should retrieve all workspaces")
    void shouldGetAllWorkspaces() {
        given()
                .when()
                .get("/workspaces")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .log().all();
    }

    @Test
    @Order(4)
    @DisplayName("Should retrieve all features within a workspace")
    void shouldGetAllWorkspaceFeatures() {
        given()
                .when()
                .get("/workspaces/{id}/features",workspaceId.toString())
                .then()
                .statusCode(200)
                .body(notNullValue())
                .log().all();
    }
    @Test
    @Order(5)
    @DisplayName("Should retrieve summary for a workspace")
    void shouldGetAllWorkspaceSummaries() {
        given()
                .when()
                .get("/workspaces/{id}/summary",workspaceId.toString())
                .then()
                .statusCode(200)
                .body(notNullValue())
                .log().all();
    }
    @Test
    @Order(6)
    @DisplayName("Should return 404 when updating a non-existent workspace")
    void shouldUpdateWorkspaceByIdWhenWorkspaceDoesNotExist() {

        String requestBody = """
                {
                  "name": "dev",
                  "description": "Workspace for development"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/workspaces/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    @DisplayName("Should update workspace successfully")
    void shouldUpdateWorkspaceById() {

        String requestBody = """
                {
                  "name": "dev",
                  "description": "Workspace for development"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/workspaces/{id}", workspaceId.toString())
                .then()
                .statusCode(204);
    }

    @Test
    @Order(8)
    @DisplayName("Should fail to delete workspace if If-Match header is missing")
    void shouldDeleteWorkspaceByIdWithoutETag() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/workspaces/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(9)
    @DisplayName("Should fail to delete workspace if ETag does not match")
    void shouldDeleteWorkspaceByIdWithUnmatchedETag() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .when()
                .delete("/workspaces/{id}", workspaceId.toString())
                .then()
                .statusCode(412);
    }

    @Test
    @Order(10)
    @DisplayName("Should return 404 when deleting a non-existent workspace")
    void shouldDeleteWorkspaceByIdWhenWorkspaceDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "4")
                .when()
                .delete("/workspaces/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(11)
    @DisplayName("Should delete workspace successfully")
    void shouldDeleteWorkspaceById() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "1")
                .when()
                .delete("/workspaces/{id}",workspaceId.toString())
                .then()
                .statusCode(204);
    }
}
