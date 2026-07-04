package org.feature.management.integrationTest;

import io.restassured.http.ContentType;
import org.feature.management.AbstractIntegrationTest;
import org.feature.management.models.Stage;
import org.feature.management.models.StageType;
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
    void shouldGetAllWorkflows(){
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
    void shouldGetWorkflowByIdWhenWorkflowIsNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/workflows/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }
    @Test
    @Order(5)
    void shouldUpdateWorkflowByIdWhenWorkflowIsNotExist() {

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
    void shouldUpdateWorkflowStatusByIdWithOutETAG() {

        given()
                .contentType(ContentType.JSON)
                .when()
                .patch("/workflows/{id}/status?status=DRAFT", workflowId.toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(8)
    void shouldUpdateWorkflowStatusByIdWithUnmatchedETAG() {

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
    void shouldUpdateWorkflowStatusByIdWhenWorkflowIsNotExist() {

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
    void shouldCreateStage(){

        String env1Body = """
                {
                  "name": "dev",
                  "description": "Development Environment"
                }
                """;
        envId = given().contentType(ContentType.JSON).body(env1Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage1 = Stage.builder().environmentId(envId).environmentName("dev").orderIndex(0).approvalNeeded(false).type(StageType.MANUAL).build();
        stageId = given().contentType(ContentType.JSON).body(stage1).when().post("/workflows/{id}/stages", workflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);

    }
    @Test
    @Order(12)
    void shouldCreate2ndStage(){

        String env2Body = """
                {
                  "name": "staging",
                  "description": "Staging Environment"
                }
                """;
        UUID sharedEnv2Id = given().contentType(ContentType.JSON).body(env2Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage2 = Stage.builder().environmentId(sharedEnv2Id).environmentName("staging").orderIndex(1).approvalNeeded(false).type(StageType.MANUAL).build();
        given().contentType(ContentType.JSON).body(stage2).when().post("/workflows/{id}/stages", workflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);

    }

    @Test
    @Order(13)
    void shouldGetStageByIdWhenStageIsNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/workflows/{id}/stages/{id}",workflowId.toString(),UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(14)
    void shouldGetStageByIdWhenWorkFlowIsNotExist() {
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
    void shouldUpdateStageByIdWhenStageIsNotExist() {
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
    void shouldUpdateEnvironmentByIdWithOutETAG() {
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
    void shouldUpdateEnvironmentByIdWithUnmatchedETAG() {
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
}
