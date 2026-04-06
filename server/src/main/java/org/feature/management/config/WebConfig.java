package org.feature.management.config;

import org.feature.management.models.Environment;
import org.feature.management.models.Feature;
import org.feature.management.models.JWTClaimFeatureStrategy;
import org.feature.management.models.JWTClaimFeatureStrategyClaimsInnerOneOf;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;
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
    MANUAL,        // Human triggers promotion
    AUTOMATIC,     // Auto promote when conditions met
    SCHEDULED      // Time-based promotion
}

class FeatureEnvMapping {
    String envId;
    String featureId;
}

class Example {
    public void exampleWorkflow() {

        var config = JWTClaimFeatureStrategy.builder().strategy("JWTClaimFeatureStrategy").claims(List.of(JWTClaimFeatureStrategyClaimsInnerOneOf.builder().scopes(List.of("scope1"
                , "scope2")).build())).build();
        var f1 = Feature.builder()._configuration(config).name("fbname").build();
        var f2 = Feature.builder()._configuration(config).name("fbname").build();
        var e1 = Environment.builder().name("dev").build();
        var e2 = Environment.builder().name("test").build();

        FeatureEnvMapping featureEnvMapping = new FeatureEnvMapping();
        featureEnvMapping.envId = "e1";
        featureEnvMapping.featureId = "f1";
        FeatureEnvMapping featureEnvMapping2 = new FeatureEnvMapping();
        featureEnvMapping.envId = "e2";
        featureEnvMapping.featureId = "f1";

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
