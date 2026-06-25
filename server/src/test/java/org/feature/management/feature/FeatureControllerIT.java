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
        UUID env1Id = given().contentType(ContentType.JSON).body(env1Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage1 = Stage.builder().environmentId(env1Id).environmentName("dev").orderIndex(0).approvalNeeded(false).type(StageType.MANUAL).build();
        sharedStage1Id = given().contentType(ContentType.JSON).body(stage1).when().post("/workflows/{id}/stages",sharedWorkflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);

        String env2Body = """
                {
                  "name": "staging",
                  "description": "Staging Environment"
                }
                """;
        UUID env2Id = given().contentType(ContentType.JSON).body(env2Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage2 = Stage.builder().environmentId(env2Id).environmentName("staging").orderIndex(1).approvalNeeded(false).type(StageType.MANUAL).build();
        sharedStage2Id = given().contentType(ContentType.JSON).body(stage2).when().post("/workflows/{id}/stages",sharedWorkflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);


        String env3Body = """
                {
                  "name": "qa",
                  "description": "QA Environment"
                }
                """;
        UUID env3Id = given().contentType(ContentType.JSON).body(env3Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage3 = Stage.builder().environmentId(env3Id).environmentName("qa").orderIndex(2).approvalNeeded(false).type(StageType.MANUAL).build();
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

}
