package org.feature.management.dashboard;

import org.feature.management.environment.EnvironmentRepository;
import org.feature.management.feature.FeatureRepository;
import org.feature.management.models.DashboardStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void shouldReturnDashboardStats() {
        when(featureRepository.count()).thenReturn(Mono.just(10L));
        when(featureRepository.countByEnabledTrue()).thenReturn(Mono.just(7L));
        when(featureRepository.countByEnabledFalse()).thenReturn(Mono.just(3L));
        when(environmentRepository.count()).thenReturn(Mono.just(2L));

        StepVerifier.create(dashboardService.getDashboardStats())
                .consumeNextWith(stats -> {
                    assertThat(stats.getTotalFeatures()).isEqualTo(10L);
                    assertThat(stats.getActiveFeatures()).isEqualTo(7L);
                    assertThat(stats.getDisabledFeatures()).isEqualTo(3L);
                    assertThat(stats.getTotalEnvironments()).isEqualTo(2L);
                })
                .verifyComplete();
    }
}
