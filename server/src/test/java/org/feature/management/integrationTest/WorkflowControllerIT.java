package org.feature.management.integrationTest;

import io.restassured.http.ContentType;
import org.feature.management.AbstractIntegrationTest;
import org.feature.management.models.Stage;
import org.feature.management.models.StageType;
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
public class WorkflowControllerIT extends AbstractIntegrationTest {

    private static UUID workflowId;
    private static UUID stageId;
    private static UUID envId;

    @Test
    @Order(1)
    @DisplayName("Should create workflow successfully")
    void shouldCreateWorkflow() {
 
        String requestBody = """
                {
                  "name": "Production Pipeline",
                  "status": "DRAFT"
                }
                """;
        workflowId =   given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/workflows")
                .then()
                .statusCode(201)
                .body(notNullValue())
                .extract().as(UUID.class);
    }

    @Test
    @Order(2)
    @DisplayName("Should retrieve workflow by ID")
    void shouldGetWorkflowById() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/workflows/{id}", workflowId.toString())
                .then()
                .body("id", equalTo(workflowId.toString()));
    }


    @Test
    @Order(3)
    @DisplayName("Should retrieve all workflows")
    void shouldGetAllWorkflows() {
        given()
                .when()
                .get("/workflows")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .log().all();
    }


    @Test
    @Order(4)
    @DisplayName("Should return 404 when retrieving a non-existent workflow by ID")
    void shouldGetWorkflowByIdWhenWorkflowDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/workflows/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }
    @Test
    @Order(5)
    @DisplayName("Should return 404 when updating a non-existent workflow")
    void shouldUpdateWorkflowByIdWhenWorkflowDoesNotExist() {

        String requestBody = """
                {
                  "name": "Production Pipeline",
                  "status": "DRAFT"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/workflows/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    @DisplayName("Should update workflow successfully")
    void shouldUpdateWorkflowById() {

        String requestBody = """
                {
                  "name": "Production Pipeline",
                  "status": "DRAFT"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/workflows/{id}", workflowId.toString())
                .then()
                .statusCode(200);
    }

    @Test
    @Order(7)
    @DisplayName("Should fail to update workflow status if If-Match header is missing")
    void shouldUpdateWorkflowStatusByIdWithoutETag() {

        given()
                .contentType(ContentType.JSON)
                .when()
                .patch("/workflows/{id}/status?status=DRAFT", workflowId.toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(8)
    @DisplayName("Should fail to update workflow status if ETag does not match")
    void shouldUpdateWorkflowStatusByIdWithUnmatchedETag() {

        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .when()
                .patch("/workflows/{id}/status?status=DRAFT", workflowId.toString())
                .then()
                .log().all()
                .statusCode(412);
    }

    @Test
    @Order(9)
    @DisplayName("Should return 404 when workflow does not exist during status update")
    void shouldUpdateWorkflowStatusByIdWhenWorkflowDoesNotExist() {

        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "1")
                .when()
                .patch("/workflows/{id}/status?status=DRAFT", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(10)
    @DisplayName("Should update workflow status successfully")
    void shouldUpdateWorkflowStatusById() {

        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "1")
                .when()
                .patch("/workflows/{id}/status?status=DRAFT", workflowId.toString())
                .then()
                .statusCode(204);
    }

    @Test
    @Order(11)
    @DisplayName("Should create a stage for a workflow successfully")
    void shouldCreateStage() {

        String env1Body = """
                {
                  "name": "dev",
                  "description": "Development Workflow"
                }
                """;
        envId = given().contentType(ContentType.JSON).body(env1Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage1 = Stage.builder().environmentId(envId).environmentName("dev").orderIndex(0).approvalNeeded(false).type(StageType.MANUAL).build();
        stageId = given().contentType(ContentType.JSON).body(stage1).when().post("/workflows/{id}/stages", workflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);

    }
    @Test
    @Order(12)
    @DisplayName("Should create a second stage for a workflow successfully")
    void shouldCreateSecondStage() {

        String env2Body = """
                {
                  "name": "staging",
                  "description": "Staging Workflow"
                }
                """;
        UUID sharedEnv2Id = given().contentType(ContentType.JSON).body(env2Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage2 = Stage.builder().environmentId(sharedEnv2Id).environmentName("staging").orderIndex(1).approvalNeeded(false).type(StageType.MANUAL).build();
        given().contentType(ContentType.JSON).body(stage2).when().post("/workflows/{id}/stages", workflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);

    }

    @Test
    @Order(13)
    @DisplayName("Should return 404 when retrieving a non-existent stage by ID")
    void shouldGetStageByIdWhenStageDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/workflows/{id}/stages/{id}",workflowId.toString(),UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(14)
    @DisplayName("Should return 200 when retrieving a stage even with a non-existent workflow ID")
    void shouldGetStageByIdWhenWorkflowDoesNotExist() {
        // TODO:workflowId id not using
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/workflows/{id}/stages/{id}",UUID.randomUUID().toString(),stageId.toString())
                .then()
                .statusCode(200);
    }
    @Test
    @Order(15)
    @DisplayName("Should retrieve stage by ID")
    void shouldGetStageById() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/workflows/{id}/stages/{id}",workflowId.toString(),stageId.toString())
                .then()
                .statusCode(200);
    }
    @Test
    @Order(16)
    @DisplayName("Should retrieve all stages for a workflow")
    void shouldGetAllStages() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/workflows/{id}/stages",workflowId.toString())
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    @Order(17)
    @DisplayName("Should return 404 when updating a non-existent stage")
    void shouldUpdateStageByIdWhenStageDoesNotExist() {
        Stage stage = Stage.builder().environmentId(envId).orderIndex(0).approvalNeeded(false).type(StageType.MANUAL).build();

        given()
                .contentType(ContentType.JSON)
                .body(stage)
                .header("If-Match", "0")
                .when()
                .patch("/workflows/{id}/stages/{id}",workflowId.toString(), UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(18)
    @DisplayName("Should update stage successfully")
    void shouldUpdateStageById() {
        Stage stage = Stage.builder().environmentId(envId).orderIndex(0).approvalNeeded(false).type(StageType.MANUAL).build();

        given()
                .contentType(ContentType.JSON)
                .body(stage)
                .header("If-Match", "1")
                .when()
                .patch("/workflows/{id}/stages/{id}",workflowId.toString(),stageId.toString())
                .then()
                .statusCode(204);
    }

    @Test
    @Order(19)
    @DisplayName("Should fail to update stage if If-Match header is missing")
    void shouldUpdateStageByIdWithoutETag() {
        Stage stage = Stage.builder().environmentId(envId).orderIndex(0).approvalNeeded(false).type(StageType.MANUAL).build();

        given()
                .contentType(ContentType.JSON)
                .body(stage)
                .when()
                .delete("/workflows/{id}/stages/{id}",workflowId.toString(),stageId.toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(20)
    @DisplayName("Should fail to update stage if ETag does not match")
    void shouldUpdateStageByIdWithUnmatchedTag() {
        Stage stage = Stage.builder().environmentId(envId).orderIndex(0).approvalNeeded(false).type(StageType.MANUAL).build();

        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .body(stage)
                .when()
                .patch("/workflows/{id}/stages/{id}",workflowId.toString(),stageId.toString())
                .then()
                .statusCode(412);
    }

    @Test
    @Order(21)
    @DisplayName("Should fail to delete stage if If-Match header is missing")
    void shouldDeleteStageByIdWithoutETag() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/workflows/{id}/stages/{id}",workflowId.toString(),stageId.toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(22)
    @DisplayName("Should fail to delete stage if ETag does not match")
    void shouldDeleteStageByIdWithUnmatchedTag() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .when()
                .delete("/workflows/{id}/stages/{id}", workflowId.toString(),stageId.toString())
                .then()
                .statusCode(412);
    }

    @Test
    @Order(23)
    @DisplayName("Should return 404 when deleting a non-existent stage")
    void shouldDeleteStageByIdWhenStageDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "2")
                .when()
                .delete("/workflows/{id}/stages/{id}", UUID.randomUUID().toString(),UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(24)
    @DisplayName("Should delete stage successfully")
    void shouldDeleteStageById() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "2")
                .when()
                .delete("/workflows/{id}/stages/{id}",workflowId.toString(),stageId.toString())
                .then()
                .statusCode(204);
    }

    @Test
    @Order(25)
    @DisplayName("Should fail to delete workflow if If-Match header is missing")
    void shouldDeleteWorkflowByIdWithoutETag() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/workflows/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(26)
    @DisplayName("Should fail to delete workflow if ETag does not match")
    void shouldDeleteWorkflowByIdWithUnmatchedETag() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .when()
                .delete("/workflows/{id}", workflowId.toString())
                .then()
                .statusCode(412);
    }

    @Test
    @Order(27)
    @DisplayName("Should return 404 when deleting a non-existent workflow")
    void shouldDeleteWorkflowByIdWhenWorkflowDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "2")
                .when()
                .delete("/workflows/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(28)
    @DisplayName("Should delete workflow successfully")
    void shouldDeleteWorkflowById() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "2")
                .when()
                .delete("/workflows/{id}",workflowId.toString())
                .then()
                .statusCode(204);
    }
}
