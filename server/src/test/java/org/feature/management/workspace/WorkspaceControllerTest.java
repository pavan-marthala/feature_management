package org.feature.management.workspace;

import org.feature.management.environment.EnvironmentRepository;
import org.feature.management.feature.FeatureRepository;
import org.feature.management.models.*;
import org.feature.management.workflow.StageRepository;
import org.feature.management.workflow.WorkflowRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(WorkspaceController.class)
class WorkspaceControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private WorkspaceServiceInterface workspaceService;

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
    void shouldCreateWorkspace() {
        WorkspaceRequest request = new WorkspaceRequest();
        request.setName("ws");

        UUID id = UUID.randomUUID();
        when(workspaceService.createWorkspace(any())).thenReturn(Mono.just(id));

        webTestClient.post()
                .uri("/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UUID.class).isEqualTo(id);
    }

    @Test
    void shouldGetWorkspaces() {
        Workspace ws = new Workspace();
        ws.setId(UUID.randomUUID());
        ws.setName("ws");

        WorkspaceResponse response = WorkspaceResponse.builder()
                .items(List.of(ws))
                .page(0)
                .size(25)
                .totalItems(1)
                .totalPages(1)
                .build();

        when(workspaceService.getWorkspaces(0, 25)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/workspaces")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].name").isEqualTo("ws")
                .jsonPath("$.totalItems").isEqualTo(1);
    }

    @Test
    void shouldGetWorkspaceById() {
        UUID id = UUID.randomUUID();
        Workspace ws = new Workspace();
        ws.setId(id);
        ws.setName("ws");

        when(workspaceService.getWorkspaceById(id)).thenReturn(Mono.just(ws));

        webTestClient.get()
                .uri("/workspaces/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString())
                .jsonPath("$.name").isEqualTo("ws");
    }

    @Test
    void shouldUpdateWorkspace() {
        UUID id = UUID.randomUUID();
        WorkspaceRequest request = new WorkspaceRequest();
        request.setName("ws2");

        when(workspaceService.updateWorkspace(eq(id), any())).thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/workspaces/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldDeleteWorkspace_withIfMatchHeader() {
        UUID id = UUID.randomUUID();
        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setId(id);
        entity.setVersion(5L);

        when(workspaceRepository.findById(id)).thenReturn(Mono.just(entity));
        when(workspaceService.deleteWorkspace(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/workspaces/{id}", id)
                .header("If-Match", "5")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldGetWorkspaceSummary() {
        UUID id = UUID.randomUUID();
        GetWorkspaceSummary200Response summary = new GetWorkspaceSummary200Response();
        summary.setFeatureCount(3);
        summary.setWorkflowStages(0);
        summary.setEnvironments(0);

        when(workspaceService.getWorkspaceSummary(id)).thenReturn(Mono.just(summary));

        webTestClient.get()
                .uri("/workspaces/{id}/summary", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.featureCount").isEqualTo(3);
    }

    @Test
    void shouldGetWorkspaceFeatures_withEnvironmentId() {
        UUID id = UUID.randomUUID();
        UUID envId = UUID.randomUUID();
        Feature f = new Feature();
        f.setId(UUID.randomUUID());
        f.setName("f1");

        FeatureResponse response = FeatureResponse.builder()
                .items(List.of(f))
                .page(0)
                .size(25)
                .totalItems(1)
                .totalPages(1)
                .build();

        when(workspaceService.getWorkspaceFeatures(id, envId, 0, 25)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/workspaces/{id}/features")
                        .queryParam("environmentId", envId)
                        .build(id))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].name").isEqualTo("f1");
    }
}

