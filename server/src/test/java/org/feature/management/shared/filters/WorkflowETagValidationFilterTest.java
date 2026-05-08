package org.feature.management.shared.filters;

import org.feature.management.workflow.StageEntity;
import org.feature.management.workflow.StageRepository;
import org.feature.management.workflow.WorkflowEntity;
import org.feature.management.workflow.WorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowETagValidationFilterTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private WebFilterChain filterChain;

    private WorkflowETagValidationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new WorkflowETagValidationFilter(workflowRepository, stageRepository);
        filter.init();
    }

    @Test
    void shouldPassWhenNotETagMethod() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/workflows/" + UUID.randomUUID()).build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldReturnPreconditionRequiredWhenHeaderMissing_evenIfNoRouteMatches() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.patch("/workflows/" + UUID.randomUUID() + "/status").build());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.PRECONDITION_REQUIRED);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldReturnBadRequestWhenIfMatchHeaderIsInvalid() {
        UUID id = UUID.randomUUID();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.delete("/workflows/" + id)
                        .header("If-Match", "not-a-number")
                        .build());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldReturnPreconditionFailedWhenWorkflowEtagMismatch() {
        UUID id = UUID.randomUUID();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.delete("/workflows/" + id)
                        .header("If-Match", "1")
                        .build());

        WorkflowEntity entity = new WorkflowEntity();
        entity.setId(id);
        entity.setVersion(2L);

        when(workflowRepository.findById(id)).thenReturn(Mono.just(entity));

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.PRECONDITION_FAILED);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldPassWhenWorkflowEtagMatches() {
        UUID id = UUID.randomUUID();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.delete("/workflows/" + id)
                        .header("If-Match", "2")
                        .build());

        WorkflowEntity entity = new WorkflowEntity();
        entity.setId(id);
        entity.setVersion(2L);

        when(workflowRepository.findById(id)).thenReturn(Mono.just(entity));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldPassWhenWorkflowEntityNotFound() {
        UUID id = UUID.randomUUID();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.delete("/workflows/" + id)
                        .header("If-Match", "1")
                        .build());

        when(workflowRepository.findById(id)).thenReturn(Mono.empty());
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldValidateStageRouteUsesStageIdGroup() {
        UUID workflowId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.patch("/workflows/" + workflowId + "/stages/" + stageId)
                        .header("If-Match", "5")
                        .build());

        StageEntity entity = new StageEntity();
        entity.setId(stageId);
        entity.setVersion(5L);

        when(stageRepository.findById(stageId)).thenReturn(Mono.just(entity));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(stageRepository).findById(stageId);
        verifyNoMoreInteractions(stageRepository);
        verify(filterChain).filter(exchange);
    }

    @Test
    void writeError_shouldFailWhenResponseAlreadyCommitted() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.delete("/workflows/" + UUID.randomUUID())
                        .header("If-Match", "1")
                        .build());

        // commit response
        StepVerifier.create(exchange.getResponse().setComplete())
                .verifyComplete();

        StepVerifier.create(filter.writeError(exchange, HttpStatus.BAD_REQUEST, "{\"error\":\"x\"}"))
                .expectError(IllegalStateException.class)
                .verify();
    }
}

