package org.feature.management.environment;

import io.restassured.http.ContentType;
import org.feature.management.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static org.hamcrest.Matchers.notNullValue;

public class EnvironmentControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldCreateEnvironment() {

        String requestBody = """
                {
                  "name": "dev",
                  "description": "Development Environment"
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/environments")
                .then()
                .statusCode(201)
                .body(notNullValue());
    }
}
