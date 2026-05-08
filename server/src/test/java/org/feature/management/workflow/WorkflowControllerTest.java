package org.feature.management.workflow;

import org.feature.management.environment.EnvironmentRepository;
import org.feature.management.feature.FeatureRepository;
import org.feature.management.models.*;
import org.feature.management.workspace.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(WorkflowController.class)
class WorkflowControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private WorkflowServiceInterface workflowService;

    // Needed for ETag filters that are @Component beans
    @MockitoBean
    private FeatureRepository featureRepository;
    @MockitoBean
    private EnvironmentRepository environmentRepository;
    @MockitoBean
    private WorkspaceRepository workspaceRepository;
    @MockitoBean
    private WorkflowRepository workflowRepository;
    @MockitoBean
    private StageRepository stageRepository;

    @Test
    void shouldGetAllWorkflows() {
        WorkflowBase wf = new WorkflowBase();
        wf.setId(UUID.randomUUID());
        wf.setName("wf");
        wf.setStatus(WorkflowStatus.ACTIVE);

        when(workflowService.getAllWorkflows(0, 25, null))
                .thenReturn(Mono.just(new PageImpl<>(List.of(wf))));

        webTestClient.get()
                .uri("/workflows")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].name").isEqualTo("wf")
                .jsonPath("$.totalItems").isEqualTo(1);
    }

    @Test
    void shouldCreateWorkflow() {
        WorkflowBase request = new WorkflowBase();
        request.setName("wf");
        request.setStatus(WorkflowStatus.ACTIVE);

        UUID id = UUID.randomUUID();
        when(workflowService.createWorkflow(any())).thenReturn(Mono.just(id));

        webTestClient.post()
                .uri("/workflows")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UUID.class).isEqualTo(id);
    }

    @Test
    void shouldRejectInvalidWorkflowOnCreate() {
        // missing required fields should trigger @Valid -> 400
        WorkflowBase request = new WorkflowBase();

        webTestClient.post()
                .uri("/workflows")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldGetWorkflowById() {
        UUID id = UUID.randomUUID();
        WorkflowBase wf = new WorkflowBase();
        wf.setId(id);
        wf.setName("wf");
        wf.setStatus(WorkflowStatus.ACTIVE);

        when(workflowService.getWorkflowById(id)).thenReturn(Mono.just(wf));

        webTestClient.get()
                .uri("/workflows/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString())
                .jsonPath("$.name").isEqualTo("wf");
    }

    @Test
    void shouldUpdateWorkflow() {
        UUID id = UUID.randomUUID();
        WorkflowBase request = new WorkflowBase();
        request.setName("wf2");
        request.setStatus(WorkflowStatus.ACTIVE);

        when(workflowService.updateWorkflow(eq(id), any())).thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/workflows/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldDeleteWorkflow_withMatchingIfMatchHeader() {
        UUID id = UUID.randomUUID();
        WorkflowEntity entity = new WorkflowEntity();
        entity.setId(id);
        entity.setVersion(3L);

        when(workflowRepository.findById(id)).thenReturn(Mono.just(entity));
        when(workflowService.deleteWorkflow(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/workflows/{id}", id)
                .header("If-Match", "3")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldUpdateWorkflowStatus() {
        UUID id = UUID.randomUUID();
        when(workflowService.updateWorkflowStatus(id, WorkflowStatus.ARCHIVED)).thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/workflows/{id}/status").queryParam("status", "ARCHIVED").build(id))
                .header("If-Match", "1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldGetStagesForWorkflow() {
        UUID id = UUID.randomUUID();
        Workflow wf = Workflow.builder()
                .id(id)
                .name("wf")
                .status(WorkflowStatus.ACTIVE)
                .stages(List.of())
                .build();

        when(workflowService.getStagesForWorkflow(id)).thenReturn(Mono.just(wf));

        webTestClient.get()
                .uri("/workflows/{id}/stages", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString())
                .jsonPath("$.stages").isArray();
    }

    @Test
    void shouldAddStageToWorkflow() {
        UUID workflowId = UUID.randomUUID();
        Stage request = new Stage();
        request.setEnvironmentId(UUID.randomUUID());
        request.setOrderIndex(0);
        request.setType(StageType.MANUAL);

        UUID newStageId = UUID.randomUUID();
        when(workflowService.addStageToWorkflow(eq(workflowId), any())).thenReturn(Mono.just(newStageId));

        webTestClient.post()
                .uri("/workflows/{id}/stages", workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UUID.class).isEqualTo(newStageId);
    }

    @Test
    void shouldUpdateStage_withIfMatchHeader() {
        UUID workflowId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();

        StageEntity entity = new StageEntity();
        entity.setId(stageId);
        entity.setVersion(7L);
        when(stageRepository.findById(stageId)).thenReturn(Mono.just(entity));

        when(workflowService.updateStage(eq(stageId), any())).thenReturn(Mono.empty());

        StageRequest request = new StageRequest();
        request.setOrderIndex(1);
        request.setEnvironmentId(UUID.randomUUID());
        request.setType(StageType.MANUAL);

        webTestClient.patch()
                .uri("/workflows/{id}/stages/{stageId}", workflowId, stageId)
                .header("If-Match", "7")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldDeleteStage_withIfMatchHeader() {
        UUID workflowId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();

        StageEntity entity = new StageEntity();
        entity.setId(stageId);
        entity.setVersion(2L);
        when(stageRepository.findById(stageId)).thenReturn(Mono.just(entity));

        when(workflowService.deleteStage(stageId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/workflows/{id}/stages/{stageId}", workflowId, stageId)
                .header("If-Match", "2")
                .exchange()
                .expectStatus().isNoContent();
    }
}

