package org.feature.management.feature;

import io.restassured.http.ContentType;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;

import org.feature.management.AbstractIntegrationTest;
import org.feature.management.models.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeatureControllerIT extends AbstractIntegrationTest {
    private static UUID sharedFeatureId;
    private static UUID sharedWorkflowId;
    private static UUID sharedWorkspaceId;
    private static UUID sharedStage1Id;
    private static UUID sharedStage2Id;
    private static UUID sharedStage3Id;
    private static UUID sharedEnv1Id;
    private static UUID sharedEnv2Id;
    private static UUID sharedEnv3Id;

    @Test
    @Order(1)
    void createWorkflow(){

        String workflowRequestBody = """
                {
                  "name": "Production Pipeline",
                  "status": "DRAFT"
                }
                """;
        sharedWorkflowId = given().contentType(ContentType.JSON).body(workflowRequestBody).when().post("/workflows").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);

        String env1Body = """
                {
                  "name": "dev",
                  "description": "Development Environment"
                }
                """;
        sharedEnv1Id = given().contentType(ContentType.JSON).body(env1Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage1 = Stage.builder().environmentId(sharedEnv1Id).environmentName("dev").orderIndex(0).approvalNeeded(false).type(StageType.MANUAL).build();
        sharedStage1Id = given().contentType(ContentType.JSON).body(stage1).when().post("/workflows/{id}/stages",sharedWorkflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);

        String env2Body = """
                {
                  "name": "staging",
                  "description": "Staging Environment"
                }
                """;
        sharedEnv2Id = given().contentType(ContentType.JSON).body(env2Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage2 = Stage.builder().environmentId(sharedEnv2Id).environmentName("staging").orderIndex(1).approvalNeeded(false).type(StageType.MANUAL).build();
        sharedStage2Id = given().contentType(ContentType.JSON).body(stage2).when().post("/workflows/{id}/stages",sharedWorkflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);


        String env3Body = """
                {
                  "name": "qa",
                  "description": "QA Environment"
                }
                """;
        sharedEnv3Id = given().contentType(ContentType.JSON).body(env3Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage3 = Stage.builder().environmentId(sharedEnv3Id).environmentName("qa").orderIndex(2).approvalNeeded(false).type(StageType.MANUAL).build();
        sharedStage3Id = given().contentType(ContentType.JSON).body(stage3).when().post("/workflows/{id}/stages",sharedWorkflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);


    }

    @Test
    @Order(2)
    void createWorkspace(){

        String workspaceRequestBody = """
                {
                    "name": "Checkout Service",
                    "description": "Workspace for checkout features"
                }
                """;
        sharedWorkspaceId = given().contentType(ContentType.JSON).body(workspaceRequestBody).when().post("/workspaces").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);

    }

    @Test
    @Order(3)
    void shouldCreateFeatureWhenWorkflowNotFound(){

        FeatureConfiguration configuration = BooleanFeatureStrategy.builder().strategy("BooleanFeatureStrategy").value(true).build();
        FeatureCreateRequest request =  FeatureCreateRequest.builder().workflowId(UUID.randomUUID()).workspaceId(sharedWorkspaceId).enabled(true).owners(List.of("pavan@gmail.com")).name("NewFeature").description("Creating new feature")._configuration(configuration).build();

         given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/features")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(4)
    void shouldCreateFeatureWhenWorkflowHasNoStages(){

        String workflowRequestBody = """
                {
                  "name": "Production",
                  "status": "DRAFT"
                }
                """;
       UUID workflowId = given().contentType(ContentType.JSON).body(workflowRequestBody).when().post("/workflows").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);

        FeatureConfiguration configuration = BooleanFeatureStrategy.builder().strategy("BooleanFeatureStrategy").value(true).build();
        FeatureCreateRequest request =  FeatureCreateRequest.builder().workflowId(workflowId).workspaceId(sharedWorkspaceId).enabled(true).owners(List.of("pavan@gmail.com")).name("NewFeature").description("Creating new feature")._configuration(configuration).build();

         given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/features")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    void shouldCreateBooleanFeatureStrategy(){

        FeatureConfiguration configuration = BooleanFeatureStrategy.builder().strategy("BooleanFeatureStrategy").value(true).build();
        FeatureCreateRequest request =  FeatureCreateRequest.builder().workflowId(sharedWorkflowId).workspaceId(sharedWorkspaceId).enabled(true).owners(List.of("pavan@gmail.com")).name("NewFeature").description("Creating new feature")._configuration(configuration).build();

        sharedFeatureId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/features")
                .then()
                .statusCode(201)
                .body(notNullValue())
                .extract().as(UUID.class);
    }

    @Test
    @Order(6)
    void shouldCreateFeatureWithSameName(){

        FeatureConfiguration configuration = BooleanFeatureStrategy.builder().strategy("BooleanFeatureStrategy").value(true).build();
        FeatureCreateRequest request =  FeatureCreateRequest.builder().workflowId(sharedWorkflowId).workspaceId(sharedWorkspaceId).enabled(true).owners(List.of("pavan@gmail.com")).name("NewFeature").description("Creating new feature")._configuration(configuration).build();

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/features")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    void shouldGetFeatureByIdWhenFeatureIsNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/features/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }
    @Test
    @Order(8)
    void shouldGetFeatureById() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/features/{id}?idType=ID", sharedFeatureId.toString())
                .then()
                .body("id", equalTo(sharedFeatureId.toString()));
    }


    @Test
    @Order(9)
    void shouldGetFeatureByNameWhenFeatureIsNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/features/{id}?idType=NAME&envId={envId}", "NewFeature", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }
    @Test
    @Order(10)
    void shouldGetFeatureByNameWithoutEnvId() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/features/{id}?idType=NAME", "NewFeature")
                .then()
                .statusCode(404);

    }
    @Test
    @Order(11)
    void shouldGetFeatureByName() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/features/{id}?idType=NAME&envId={envId}", "NewFeature", sharedEnv1Id.toString())
                .then()
                .body("name", equalTo("NewFeature"));

    }
    @Test
    @Order(12)
    void shouldGetAllFeatures(){
        given()
                .when()
                .get("/features")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .log().all();
    }

    @Test
    @Order(13)
    void shouldAssignSecondOwnerToFeatureById() {
        String owner = "sam@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/features/{id}/owners/{owner}", sharedFeatureId.toString(), owner)
                .then()
                .statusCode(204);
    }


    @Test
    @Order(14)
    void shouldRemoveOwnerFromFeatureByIdWithOutETAG() {
        String owner = "pavan@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/features/{id}/owners/{owner}", sharedFeatureId.toString(), owner)
                .then()
                .statusCode(428);
    }

    @Test
    @Order(15)
    void shouldRemoveOwnerFromFeatureByIdWithUnmatchedETAG() {
        String owner = "pavan@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .when()
                .delete("/features/{envId}/owners/{owner}", sharedFeatureId.toString(), owner)
                .then()
                .statusCode(412);
    }

    @Test
    @Order(16)
    void shouldRemoveOwnerFromFeatureByIdIfAccessIsDenied() {
        String owner = "pavan1@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "1")
                .when()
                .delete("/features/{envId}/owners/{owner}", sharedFeatureId.toString(), owner)
                .then()
                .statusCode(401);
    }
    @Test
    @Order(17)
    void shouldRemoveOwnerFromFeatureByIdWhenFeatureIsNotExist() {
        String owner = "pavan1@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "1")
                .when()
                .delete("/features/{envId}/owners/{owner}", UUID.randomUUID().toString(), owner)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(18)
    void shouldRemoveOwnerFromFeatureById() {
        String owner = "pavan@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "1")
                .when()
                .delete("/features/{envId}/owners/{owner}", sharedFeatureId.toString(), owner)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(19)
    void shouldRemoveLastOwnerFromFeatureById() {
        String owner = "sam@gmail.com";
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "2")
                .when()
                .delete("/features/{envId}/owners/{owner}", sharedFeatureId.toString(), owner)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(20)
    void shouldGetAllFeatureStrategies(){
        given()
                .when()
                .get("/features/strategies")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .log().all();
    }



    @Test
    @Order(21)
    void shouldUpdateFeatureByIdWithOutETAG() {

        String requestBody = """
                {
                  "name": "dev",
                  "description": "Feature for development"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .delete("/features/{id}", sharedFeatureId.toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(22)
    void shouldUpdateFeatureByIdWithUnmatchedETAG() {

        String requestBody = """
                {
                  "name": "dev",
                  "description": "Feature for development"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .body(requestBody)
                .when()
                .delete("/features/{id}", sharedFeatureId.toString())
                .then()
                .statusCode(412);
    }
    @Test
    @Order(23)
    void shouldUpdateFeatureByIdWhenFeatureIsNotExist() {
        FeatureConfiguration configuration = BooleanFeatureStrategy.builder().strategy("BooleanFeatureStrategy").value(false).build();

        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "2")
                .body(configuration)
                .when()
                .patch("/features/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(24)
    void shouldUpdateFeatureById() {
        FeatureConfiguration configuration = BooleanFeatureStrategy.builder().strategy("BooleanFeatureStrategy").value(false).build();
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "2")
                .body(configuration)
                .when()
                .patch("/features/{id}", sharedFeatureId.toString())
                .then()
                .statusCode(204);
    }



    @Test
    @Order(25)
    void shouldUpdateFeatureStatusByIdWithOutETAG() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .patch("/features/{id}/status?status=true", sharedFeatureId.toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(26)
    void shouldUpdateFeatureStatusByIdWithUnmatchedETAG() {

        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .when()
                .patch("/features/{id}/status?status=true", sharedFeatureId.toString())
                .then()
                .statusCode(204);
    }
    @Test
    @Order(27)
    void shouldUpdateFeatureStatusByIdWhenFeatureIsNotExist() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "3")
                .when()
                .patch("/features/{id}/status?status=true", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(28)
    void shouldUpdateFeatureStatusById() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "3")
                .when()
                .patch("/features/{id}/status?status=true", sharedFeatureId.toString())
                .then()
                .statusCode(204);
    }

    @Test
    @Order(29)
    void shouldPropagateFeatureByIdWhenFeatureIsNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/features/{id}/propagate", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(30)
    void shouldPropagateFeatureById() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/features/{id}/propagate", sharedFeatureId.toString())
                .then()
                .statusCode(200);
    }

    @Test
    @Order(31)
    void shouldDeleteFeatureByIdWithOutETAG() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/features/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(32)
    void shouldDeleteFeatureByIdWithUnmatchedETAG() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "0")
                .when()
                .delete("/features/{id}", sharedFeatureId.toString())
                .then()
                .statusCode(412);
    }

    @Test
    @Order(33)
    void shouldDeleteFeatureByIdWhenFeatureIsNotExist() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "5")
                .when()
                .delete("/features/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(34)
    void shouldDeleteFeatureById() {
        given()
                .contentType(ContentType.JSON)
                .header("If-Match", "5")
                .when()
                .delete("/features/{id}",sharedFeatureId.toString())
                .then()
                .statusCode(204);
    }


    @Test
    @Order(35)
    void shouldGetAllFeaturePropagationHistory(){
        given()
                .when()
                .get("/features/{id}/propagations",UUID.randomUUID().toString())
                .then()
                .statusCode(200)
                .body(notNullValue())
                .log().all();
    }
}
