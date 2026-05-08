package org.feature.management.workspace;

import org.feature.management.feature.FeatureEntity;
import org.feature.management.feature.FeatureMapper;
import org.feature.management.feature.FeatureRepository;
import org.feature.management.models.Feature;
import org.feature.management.models.FeatureResponse;
import org.feature.management.models.Workspace;
import org.feature.management.models.WorkspaceRequest;
import org.feature.management.shared.exception.AccessDeniedException;
import org.feature.management.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMapper workspaceMapper;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private FeatureMapper featureMapper;

    private WorkspaceService service;

    @BeforeEach
    void setUp() {
        service = new WorkspaceService(workspaceRepository, workspaceMapper, featureRepository, featureMapper);
    }

    @Test
    void shouldCreateWorkspace() {
        WorkspaceRequest request = new WorkspaceRequest();
        request.setName("ws");

        when(workspaceRepository.existsByName("ws")).thenReturn(Mono.just(false));

        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setName("ws");
        entity.setId(UUID.randomUUID());
        when(workspaceMapper.toEntity(request)).thenReturn(entity);
        when(workspaceRepository.save(any(WorkspaceEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.createWorkspace(request))
                .assertNext(id -> assertThat(id).isNotNull())
                .verifyComplete();
    }

    @Test
    void shouldRejectDuplicateWorkspaceNameOnCreate() {
        WorkspaceRequest request = new WorkspaceRequest();
        request.setName("dup");

        when(workspaceRepository.existsByName("dup")).thenReturn(Mono.just(true));

        StepVerifier.create(service.createWorkspace(request))
                .expectError(DataIntegrityViolationException.class)
                .verify();

        verify(workspaceRepository, never()).save(any(WorkspaceEntity.class));
    }

    @Test
    void shouldFailGetWorkspaceByIdWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(workspaceRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service.getWorkspaceById(id))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldUpdateWorkspace_onlyNonNullFields() {
        UUID id = UUID.randomUUID();
        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setId(id);
        entity.setName("old");
        entity.setDescription("old-desc");
        when(workspaceRepository.findById(id)).thenReturn(Mono.just(entity));
        when(workspaceRepository.save(entity)).thenReturn(Mono.just(entity));

        WorkspaceRequest request = new WorkspaceRequest();
        request.setName("new-name");
        // description left null

        StepVerifier.create(service.updateWorkspace(id, request))
                .verifyComplete();

        assertThat(entity.getName()).isEqualTo("new-name");
        assertThat(entity.getDescription()).isEqualTo("old-desc");
    }

    @Test
    void shouldDeleteWorkspaceWhenNoFeatures() {
        UUID id = UUID.randomUUID();
        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setId(id);

        when(workspaceRepository.findById(id)).thenReturn(Mono.just(entity));
        when(featureRepository.countByWorkspaceId(id)).thenReturn(Mono.just(0L));
        when(workspaceRepository.delete(entity)).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteWorkspace(id))
                .verifyComplete();

        verify(workspaceRepository).delete(entity);
    }

    @Test
    void shouldRejectDeleteWorkspaceWhenFeaturesExist() {
        UUID id = UUID.randomUUID();
        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setId(id);

        when(workspaceRepository.findById(id)).thenReturn(Mono.just(entity));
        when(featureRepository.countByWorkspaceId(id)).thenReturn(Mono.just(3L));

        StepVerifier.create(service.deleteWorkspace(id))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(workspaceRepository, never()).delete(any());
    }

    @Test
    void shouldGetWorkspaceFeatures_withoutEnvironmentId_usesWorkspaceQueries() {
        UUID workspaceId = UUID.randomUUID();
        UUID featureId = UUID.randomUUID();

        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setId(featureId);
        featureEntity.setName("f1");

        when(featureRepository.findByWorkspaceId(eq(workspaceId), any(Pageable.class)))
                .thenReturn(Flux.just(featureEntity));
        when(featureRepository.countByWorkspaceId(workspaceId)).thenReturn(Mono.just(1L));

        Feature feature = new Feature();
        feature.setId(featureId);
        feature.setName("f1");
        when(featureMapper.toModel(featureEntity)).thenReturn(feature);

        StepVerifier.create(service.getWorkspaceFeatures(workspaceId, null, 0, 10))
                .consumeNextWith(resp -> {
                    assertThat(resp.getItems()).hasSize(1);
                    assertThat(resp.getItems().get(0).getName()).isEqualTo("f1");
                    assertThat(resp.getTotalItems()).isEqualTo(1);
                    assertThat(resp.getTotalPages()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void shouldGetWorkspaceFeatures_withEnvironmentId_usesWorkspaceAndEnvironmentQueries() {
        UUID workspaceId = UUID.randomUUID();
        UUID envId = UUID.randomUUID();
        UUID featureId = UUID.randomUUID();

        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setId(featureId);
        featureEntity.setName("f-env");

        when(featureRepository.findByWorkspaceIdAndEnvironmentId(eq(workspaceId), eq(envId), any(Pageable.class)))
                .thenReturn(Flux.just(featureEntity));
        when(featureRepository.countByWorkspaceIdAndEnvironmentId(workspaceId, envId)).thenReturn(Mono.just(1L));

        Feature feature = new Feature();
        feature.setId(featureId);
        feature.setName("f-env");
        when(featureMapper.toModel(featureEntity)).thenReturn(feature);

        StepVerifier.create(service.getWorkspaceFeatures(workspaceId, envId, 0, 10))
                .consumeNextWith(resp -> {
                    assertThat(resp.getItems()).hasSize(1);
                    assertThat(resp.getItems().get(0).getName()).isEqualTo("f-env");
                    assertThat(resp.getTotalItems()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void shouldGetWorkspaceById_mapsEntityToModel() {
        UUID id = UUID.randomUUID();
        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setId(id);
        entity.setName("ws");
        when(workspaceRepository.findById(id)).thenReturn(Mono.just(entity));

        Workspace model = new Workspace();
        model.setId(id);
        model.setName("ws");
        when(workspaceMapper.toModel(entity)).thenReturn(model);

        StepVerifier.create(service.getWorkspaceById(id))
                .consumeNextWith(ws -> assertThat(ws.getName()).isEqualTo("ws"))
                .verifyComplete();
    }

    @Test
    void shouldGetWorkspacesWithPagination() {
        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setName("ws1");
        when(workspaceRepository.findBy(any(Pageable.class))).thenReturn(Flux.just(entity));
        when(workspaceRepository.count()).thenReturn(Mono.just(1L));

        Workspace model = new Workspace();
        model.setName("ws1");
        when(workspaceMapper.toModel(entity)).thenReturn(model);

        StepVerifier.create(service.getWorkspaces(0, 10))
                .consumeNextWith(resp -> {
                    assertThat(resp.getItems()).hasSize(1);
                    assertThat(resp.getTotalItems()).isEqualTo(1);
                    assertThat(resp.getTotalPages()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void shouldThrowResourceNotFoundOnUpdateWhenWorkspaceMissing() {
        UUID id = UUID.randomUUID();
        when(workspaceRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service.updateWorkspace(id, new WorkspaceRequest()))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldThrowResourceNotFoundOnDeleteWhenWorkspaceMissing() {
        UUID id = UUID.randomUUID();
        when(workspaceRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteWorkspace(id))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldGetWorkspaceSummary() {
        UUID id = UUID.randomUUID();
        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setId(id);
        when(workspaceRepository.findById(id)).thenReturn(Mono.just(entity));
        when(featureRepository.countByWorkspaceId(id)).thenReturn(Mono.just(5L));

        StepVerifier.create(service.getWorkspaceSummary(id))
                .consumeNextWith(summary -> {
                    assertThat(summary.getFeatureCount()).isEqualTo(5);
                    assertThat(summary.getEnvironments()).isEqualTo(0);
                    assertThat(summary.getWorkflowStages()).isEqualTo(0);
                })
                .verifyComplete();
    }
}

