package org.feature.management.client.config;

import org.feature.management.client.aspect.FeatureAspect;
import org.feature.management.client.context.DefaultEvaluationContextExtractor;
import org.feature.management.client.context.EvaluationContextExtractor;
import org.feature.management.client.evaluator.FeatureEvaluationManager;
import org.feature.management.client.service.FeatureClient;
import org.feature.management.client.service.FeatureService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
@EnableConfigurationProperties(FeatureManagementProperties.class)
public class FeatureManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebClient featureManagementWebClient() {
        return WebClient.builder().build();
    }

    @Bean
    @ConditionalOnMissingBean
    public FeatureClient featureFetcher(WebClient featureManagementWebClient, FeatureManagementProperties properties) {
        return new FeatureClient(featureManagementWebClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FeatureEvaluationManager featureEvaluationManager() {
        return new FeatureEvaluationManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public EvaluationContextExtractor evaluationContextExtractor() {
        return new DefaultEvaluationContextExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public FeatureService featureService(FeatureClient featureFetcher,
                                         FeatureEvaluationManager featureEvaluationManager,
                                         EvaluationContextExtractor evaluationContextExtractor) {
        return new FeatureService(featureFetcher, featureEvaluationManager, evaluationContextExtractor);
    }

    @Bean
    @ConditionalOnMissingBean
    public FeatureAspect featureAspect(FeatureService featureService) {
        return new FeatureAspect(featureService);
    }
}
