package org.feature.management.integrationTest;

import io.restassured.http.ContentType;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;

import org.feature.management.AbstractIntegrationTest;
import org.feature.management.models.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeatureControllerIT extends AbstractIntegrationTest {
    private static UUID sharedFeatureId;
    private static UUID sharedWorkflowId;
    private static UUID sharedWorkspaceId;
    private static UUID sharedEnv1Id;

    @Test
    @Order(1)
    @DisplayName("Should create workflow with stages successfully")
    void shouldCreateWorkflow() {

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
        UUID sharedStage1Id = given().contentType(ContentType.JSON).body(stage1).when().post("/workflows/{id}/stages", sharedWorkflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);

        String env2Body = """
                {
                  "name": "staging",
                  "description": "Staging Environment"
                }
                """;
        UUID sharedEnv2Id = given().contentType(ContentType.JSON).body(env2Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage2 = Stage.builder().environmentId(sharedEnv2Id).environmentName("staging").orderIndex(1).approvalNeeded(false).type(StageType.MANUAL).build();
        UUID sharedStage2Id = given().contentType(ContentType.JSON).body(stage2).when().post("/workflows/{id}/stages", sharedWorkflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);


        String env3Body = """
                {
                  "name": "qa",
                  "description": "QA Environment"
                }
                """;
        UUID sharedEnv3Id = given().contentType(ContentType.JSON).body(env3Body).when().post("/environments").then().statusCode(201).body(notNullValue()).extract().as(UUID.class);
        Stage stage3 = Stage.builder().environmentId(sharedEnv3Id).environmentName("qa").orderIndex(2).approvalNeeded(false).type(StageType.MANUAL).build();
        UUID sharedStage3Id = given().contentType(ContentType.JSON).body(stage3).when().post("/workflows/{id}/stages", sharedWorkflowId.toString()).then().statusCode(201).body(notNullValue()).extract().as(UUID.class);


    }

    @Test
    @Order(2)
    @DisplayName("Should create workspace successfully")
    void shouldCreateWorkspace() {

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
    @DisplayName("Should return 404 when creating feature with non-existent workflow ID")
    void shouldCreateFeatureWhenWorkflowNotFound() {

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
    @DisplayName("Should return 404 when creating feature with workflow that has no stages")
    void shouldCreateFeatureWhenWorkflowHasNoStages() {

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
    @DisplayName("Should create boolean feature strategy successfully")
    void shouldCreateBooleanFeatureStrategy() {

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
    @DisplayName("Should return 404 when creating feature with a name that already exists")
    void shouldCreateFeatureWithSameName() {

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
    @DisplayName("Should return 404 when retrieving a non-existent feature by ID")
    void shouldGetFeatureByIdWhenFeatureDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/features/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }
    @Test
    @Order(8)
    @DisplayName("Should retrieve feature by ID")
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
    @DisplayName("Should return 404 when retrieving a non-existent feature by name")
    void shouldGetFeatureByNameWhenFeatureDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/features/{id}?idType=NAME&envId={envId}", "NewFeature", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }
    @Test
    @Order(10)
    @DisplayName("Should return 404 when retrieving feature by name without envId parameter")
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
    @DisplayName("Should retrieve feature by name and environment ID")
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
    @DisplayName("Should retrieve all features successfully")
    void shouldGetAllFeatures() {
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
    @DisplayName("Should assign a second owner to feature by ID")
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
    @DisplayName("Should fail to remove owner from feature if If-Match header is missing")
    void shouldRemoveOwnerFromFeatureByIdWithoutETag() {
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
    @DisplayName("Should fail to remove owner from feature if ETag does not match")
    void shouldRemoveOwnerFromFeatureByIdWithUnmatchedETag() {
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
    @DisplayName("Should fail to remove owner from feature if access is denied")
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
    @DisplayName("Should return 404 when feature does not exist during owner removal")
    void shouldRemoveOwnerFromFeatureByIdWhenFeatureDoesNotExist() {
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
    @DisplayName("Should remove owner from feature successfully")
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
    @DisplayName("Should fail to remove the last owner from a feature")
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
    @DisplayName("Should retrieve all feature strategies")
    void shouldGetAllFeatureStrategies() {
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
    @DisplayName("Should fail to update feature if If-Match header is missing")
    void shouldUpdateFeatureByIdWithoutETag() {

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
    @DisplayName("Should fail to update feature if ETag does not match")
    void shouldUpdateFeatureByIdWithUnmatchedETag() {

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
    @DisplayName("Should return 404 when feature does not exist during update")
    void shouldUpdateFeatureByIdWhenFeatureDoesNotExist() {
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
    @DisplayName("Should update feature successfully with a valid request and matching ETag")
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
    @DisplayName("Should fail to update feature status if If-Match header is missing")
    void shouldUpdateFeatureStatusByIdWithoutETag() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .patch("/features/{id}/status?status=true", sharedFeatureId.toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(26)
    @DisplayName("Should update feature status successfully if ETag matches")
    void shouldUpdateFeatureStatusByIdWithUnmatchedETag() {

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
    @DisplayName("Should return 404 when feature does not exist during status update")
    void shouldUpdateFeatureStatusByIdWhenFeatureDoesNotExist() {
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
    @DisplayName("Should update feature status successfully")
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
    @DisplayName("Should return 404 when propagating a non-existent feature")
    void shouldPropagateFeatureByIdWhenFeatureDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/features/{id}/propagate", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(30)
    @DisplayName("Should propagate feature by ID successfully")
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
    @DisplayName("Should fail to delete feature if If-Match header is missing")
    void shouldDeleteFeatureByIdWithoutETag() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/features/{id}", UUID.randomUUID().toString())
                .then()
                .statusCode(428);
    }

    @Test
    @Order(32)
    @DisplayName("Should fail to delete feature if ETag does not match")
    void shouldDeleteFeatureByIdWithUnmatchedETag() {
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
    @DisplayName("Should return 404 when deleting a non-existent feature")
    void shouldDeleteFeatureByIdWhenFeatureDoesNotExist() {
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
    @DisplayName("Should delete feature successfully with matching ETag")
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
    @DisplayName("Should retrieve all feature propagation history")
    void shouldGetAllFeaturePropagationHistory() {
        given()
                .when()
                .get("/features/{id}/propagations",UUID.randomUUID().toString())
                .then()
                .statusCode(200)
                .body(notNullValue())
                .log().all();
    }


//    @Test
//    @Order(5)
//    @DisplayName("Should create JWT claim feature strategy successfully")
//    void shouldCreateJWTClaimFeatureStrategy() {
//
//        FeatureConfiguration configuration = JWTClaimFeatureStrategy.builder().strategy("JWTClaimFeatureStrategy").build();
//        FeatureCreateRequest request =  FeatureCreateRequest.builder().workflowId(sharedWorkflowId).workspaceId(sharedWorkspaceId).enabled(true).owners(List.of("pavan@gmail.com")).name("NewFeature").description("Creating new feature")._configuration(configuration).build();
//
//        sharedFeatureId = given()
//                .contentType(ContentType.JSON)
//                .body(request)
//                .when()
//                .post("/features")
//                .then()
//                .statusCode(201)
//                .body(notNullValue())
//                .extract().as(UUID.class);
//    }
    @Test
    @Order(36)
    @DisplayName("Should create HTTP request feature strategy successfully")
    void shouldCreateHTTPRequestFeatureStrategy() {

        FeatureConfiguration configuration = HTTPRequestFeatureStrategy.builder().strategy("HTTPRequestFeatureStrategy").query(Map.of("name","q","value","value")).build();
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
    @Order(37)
    @DisplayName("Should create schedule feature strategy successfully")
    void shouldCreateScheduleFeatureStrategy() {

        FeatureConfiguration configuration = ScheduleFeatureStrategy.builder().strategy("ScheduleFeatureStrategy").cron("* * * * *").build();
        FeatureCreateRequest request =  FeatureCreateRequest.builder().workflowId(sharedWorkflowId).workspaceId(sharedWorkspaceId).enabled(true).owners(List.of("pavan@gmail.com")).name("NewScheduleFeature").description("Creating new feature")._configuration(configuration).build();

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
}
