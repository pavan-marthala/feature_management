package org.feature.management.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FeatureManagementClientApplicationTests.TestApplication.class)
class FeatureManagementClientApplicationTests {

    @SpringBootApplication
    static class TestApplication {}

    @Test
    void contextLoads() {
    }

}
