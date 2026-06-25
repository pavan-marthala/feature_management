package org.feature.management.feature;

import org.feature.management.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class FeatureControllerIT extends AbstractIntegrationTest {
    private static UUID sharedFeatureId;

    @Test
    void shouldCreateFeature(){

        String requestBody = """
                {
                   "name": "NewFeature",
                   "description": "string",
                   "configuration": {
                     "value": false,
                     "strategy": "string"
                   },
                   "owners": [
                     "owner1"
                   ],
                   "enabled": true,
                   "workspaceId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                   "workflowId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                 }
                """;
    }

}
