package org.feature.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.UUID;

@Configuration
public class WebConfig {
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}

class WorkFlow {
    String id;
    String name;
    Stage stage;
}

class Stage {
    String id;
    int order;
    PromotionType type;
    String envId;
    Stage next;
    String scheduleExpression;
}

enum PromotionType {
    MANUAL,
    AUTOMATIC,
    SCHEDULED
}

class FeatureEnvMapping {
    String envId;
    String featureId;
}

class Example {
    public void exampleWorkflow() {

        Stage prodStage = new Stage();
        prodStage.id = UUID.randomUUID().toString();
        prodStage.order = 2;
        prodStage.type = PromotionType.MANUAL;
        prodStage.envId = "e1";

        Stage qaStage = new Stage();
        qaStage.id = UUID.randomUUID().toString();
        qaStage.order = 1;
        qaStage.type = PromotionType.AUTOMATIC;
        qaStage.envId = "e2";
        qaStage.next = prodStage;

        WorkFlow featureRelease = new WorkFlow();
        featureRelease.id = UUID.randomUUID().toString();
        featureRelease.name = "Standard Release Pipeline";
        featureRelease.stage = qaStage;

    }

}
