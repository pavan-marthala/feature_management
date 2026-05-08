package org.feature.management.feature;

import org.feature.management.config.FeatureStrategyConfig;
import org.feature.management.models.BooleanFeatureStrategy;
import org.feature.management.models.Feature;
import org.feature.management.models.FeatureConfiguration;
import org.feature.management.models.FeatureCreateRequest;
import org.feature.management.models.FeaturePromotionResponse;
import org.feature.management.models.FeatureStrategyResponseInner;
import org.feature.management.models.IdType;
import org.feature.management.propagation.PropagationHistoryRepository;
import org.feature.management.shared.exception.FeatureException;
import org.feature.management.shared.exception.ResourceNotFoundException;
import org.feature.management.workflow.StageEntity;
import org.feature.management.workflow.StageRepository;
import org.feature.management.workflow.WorkflowEntity;
import org.feature.management.workflow.WorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureServiceTest {

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private FeatureStrategyConfig strategyConfig;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private PropagationHistoryRepository propagationHistoryRepo;
    @Mock
    private TransactionalOperator transactionalOperator;

    private FeatureService featureService;

    @Mock
    private FeatureMapper featureMapper;

    @BeforeEach
    void setUp() {
        featureService = new FeatureService(featureRepository, strategyConfig,
                workflowRepository, stageRepository, propagationHistoryRepo, featureMapper, transactionalOperator);
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldCreateFeature() {
        UUID workflowId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID envId = UUID.randomUUID();
        UUID generatedId = UUID.randomUUID();

        FeatureCreateRequest request = new FeatureCreateRequest();
        request.setName("feature-1");
        request.setWorkflowId(workflowId);
        request.setWorkspaceId(workspaceId);

        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setId(workflowId);

        StageEntity firstStage = new StageEntity();
        firstStage.setWorkflowId(workflowId);
        firstStage.setEnvironmentId(envId);

        FeatureEntity mapped = new FeatureEntity();
        mapped.setId(generatedId);
        mapped.setName("feature-1");
        mapped.setWorkspaceId(workspaceId);
        mapped.setWorkflowId(workflowId);

        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(stageRepository.findFirstByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Mono.just(firstStage));
        when(featureRepository.existsByNameAndEnvironmentIdAndWorkspaceId("feature-1", envId, workspaceId))
                .thenReturn(Mono.just(false));
        when(featureMapper.toEntity(request)).thenReturn(mapped);
        when(featureRepository.save(any(FeatureEntity.class))).thenReturn(Mono.just(mapped));

        StepVerifier.create(featureService.createFeature(request))
                .expectNext(generatedId)
                .verifyComplete();

        verify(featureRepository).save(any(FeatureEntity.class));
    }

    @Test
    void shouldGetFeatureById() {
        UUID id = UUID.randomUUID();
        FeatureEntity entity = new FeatureEntity();
        entity.setId(id);
        entity.setName("feature-1");

        when(featureRepository.findById(id)).thenReturn(Mono.just(entity));
        Feature expected = new Feature();
        expected.setId(id);
        expected.setName("feature-1");
        when(featureMapper.toModel(entity)).thenReturn(expected);

        StepVerifier.create(featureService.getById(id.toString(), IdType.ID, null))
                .consumeNextWith(model -> {
                    assertThat(model.getName()).isEqualTo("feature-1");
                })
                .verifyComplete();
    }

    @Test
    void shouldGetFeatureByName() {
        String name = "feature-name";
        FeatureEntity entity = new FeatureEntity();
        entity.setName(name);

        UUID envId = UUID.randomUUID();
        when(featureRepository.getByNameAndEnvironmentId(name, envId)).thenReturn(Mono.just(entity));
        Feature expected = new Feature();
        expected.setName(name);
        when(featureMapper.toModel(entity)).thenReturn(expected);

        StepVerifier.create(featureService.getById(name, IdType.NAME, envId))
                .consumeNextWith(model -> {
                    assertThat(model.getName()).isEqualTo(name);
                })
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenFeatureNotFound() {
        UUID id = UUID.randomUUID();
        when(featureRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.getById(id.toString(), IdType.ID, null))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldGetAllFeatures() {
        FeatureEntity entity = new FeatureEntity();
        entity.setName("feature-1");

        when(featureRepository.findBy(any(Pageable.class))).thenReturn(Flux.just(entity));
        when(featureRepository.count()).thenReturn(Mono.just(1L));
        Feature model = new Feature();
        model.setName("feature-1");
        when(featureMapper.toModel(entity)).thenReturn(model);

        StepVerifier.create(featureService.getAllFeatures(0, 10, null))
                .consumeNextWith(page -> {
                    assertThat(page.getTotalElements()).isEqualTo(1);
                    assertThat(page.getContent().get(0).getName()).isEqualTo("feature-1");
                })
                .verifyComplete();
    }

    @Test
    void shouldGetAllStrategies() {
        FeatureStrategyResponseInner strategy = new FeatureStrategyResponseInner();
        when(strategyConfig.getStrategies()).thenReturn(List.of(strategy));

        StepVerifier.create(featureService.getAllFeatureStrategies())
                .expectNext(strategy)
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenFeatureAlreadyExistsOnCreate() {
        UUID workflowId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID envId = UUID.randomUUID();

        FeatureCreateRequest request = new FeatureCreateRequest();
        request.setName("existing-feature");
        request.setWorkflowId(workflowId);
        request.setWorkspaceId(workspaceId);

        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setId(workflowId);

        StageEntity firstStage = new StageEntity();
        firstStage.setWorkflowId(workflowId);
        firstStage.setEnvironmentId(envId);

        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(stageRepository.findFirstByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Mono.just(firstStage));
        when(featureRepository.existsByNameAndEnvironmentIdAndWorkspaceId("existing-feature", envId, workspaceId))
                .thenReturn(Mono.just(true));

        StepVerifier.create(featureService.createFeature(request))
                .expectError(FeatureException.class)
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenFeatureByNameNotFound() {
        String name = "non-existent";
        UUID envId = UUID.randomUUID();
        when(featureRepository.getByNameAndEnvironmentId(name, envId)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.getById(name, IdType.NAME, envId))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldAssignOwnerToFeature() {
        UUID id = UUID.randomUUID();
        FeatureEntity entity = new FeatureEntity();
        Set<String> owners = new HashSet<>();
        entity.setOwners(owners);

        when(featureRepository.findById(id)).thenReturn(Mono.just(entity));
        when(featureRepository.save(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(featureService.assignOwnerToFeature(id, "new-owner"))
                .verifyComplete();

        assertThat(entity.getOwners()).contains("new-owner");
    }

    @Test
    void shouldRemoveOwnerFromFeature() {
        UUID id = UUID.randomUUID();
        Set<String> owners = new HashSet<>();
        owners.add("owner1");
        owners.add("owner2");
        FeatureEntity entity = new FeatureEntity();
        entity.setOwners(owners);

        when(featureRepository.findById(id)).thenReturn(Mono.just(entity));
        when(featureRepository.save(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(featureService.removeOwnerFromFeature(id, "owner1"))
                .verifyComplete();

        assertThat(entity.getOwners()).doesNotContain("owner1");
        assertThat(entity.getOwners()).contains("owner2");
    }

    @Test
    void shouldThrowAccessDeniedWhenRemovingNonOwnerFromFeature() {
        UUID id = UUID.randomUUID();
        Set<String> owners = new HashSet<>();
        owners.add("owner1");
        FeatureEntity entity = new FeatureEntity();
        entity.setOwners(owners);

        when(featureRepository.findById(id)).thenReturn(Mono.just(entity));

        StepVerifier.create(featureService.removeOwnerFromFeature(id, "non-owner"))
                .expectError(org.feature.management.shared.exception.AccessDeniedException.class)
                .verify();
    }

    @Test
    void shouldThrowEnvironmentExceptionWhenRemovingLastOwnerFromFeature() {
        UUID id = UUID.randomUUID();
        Set<String> owners = new HashSet<>();
        owners.add("owner1");
        FeatureEntity entity = new FeatureEntity();
        entity.setOwners(owners);

        when(featureRepository.findById(id)).thenReturn(Mono.just(entity));

        StepVerifier.create(featureService.removeOwnerFromFeature(id, "owner1"))
                .expectError(org.feature.management.shared.exception.EnvironmentException.class)
                .verify();
    }

    @Test
    void shouldThrowAccessDeniedWhenOwnersIsNullOnRemove() {
        UUID id = UUID.randomUUID();
        FeatureEntity entity = new FeatureEntity();
        entity.setOwners(null);

        when(featureRepository.findById(id)).thenReturn(Mono.just(entity));

        StepVerifier.create(featureService.removeOwnerFromFeature(id, "owner1"))
                .expectError(org.feature.management.shared.exception.AccessDeniedException.class)
                .verify();
    }

    @Test
    void shouldThrowResourceNotFoundWhenAssigningOwnerToNonExistentFeature() {
        UUID id = UUID.randomUUID();
        when(featureRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.assignOwnerToFeature(id, "some-owner"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldGetFeatureByNameDirectly() {
        String name = "feature-1";
        FeatureEntity entity = new FeatureEntity();
        entity.setName(name);

        UUID envId = UUID.randomUUID();
        when(featureRepository.getByNameAndEnvironmentId(name, envId)).thenReturn(Mono.just(entity));
        Feature expected = new Feature();
        expected.setName(name);
        when(featureMapper.toModel(entity)).thenReturn(expected);

        StepVerifier.create(featureService.getFeatureByNameAndEnvironmentId(name, envId))
                .consumeNextWith(model -> {
                    assertThat(model.getName()).isEqualTo(name);
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateFeatureConfig() {
        UUID id = UUID.randomUUID();
        FeatureConfiguration config = new BooleanFeatureStrategy();
        FeatureEntity entity = new FeatureEntity();

        when(featureRepository.findById(id)).thenReturn(Mono.just(entity));
        when(featureRepository.save(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(featureService.updateFeature(id, config))
                .verifyComplete();

        assertThat(entity.getConfiguration()).isEqualTo(config);
    }

    @Test
    void shouldThrowResourceNotFoundOnUpdateWhenFeatureMissing() {
        UUID id = UUID.randomUUID();
        when(featureRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.updateFeature(id, new BooleanFeatureStrategy()))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldUpdateFeatureStatus() {
        UUID id = UUID.randomUUID();
        FeatureEntity entity = new FeatureEntity();
        entity.setEnabled(false);

        when(featureRepository.findById(id)).thenReturn(Mono.just(entity));
        when(featureRepository.save(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(featureService.updateFeatureStatus(id, true))
                .verifyComplete();

        assertThat(entity.isEnabled()).isTrue();
    }

    @Test
    void shouldThrowResourceNotFoundOnStatusUpdateWhenFeatureMissing() {
        UUID id = UUID.randomUUID();
        when(featureRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.updateFeatureStatus(id, true))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldDeleteFeature() {
        UUID id = UUID.randomUUID();
        FeatureEntity entity = new FeatureEntity();

        when(featureRepository.findById(id)).thenReturn(Mono.just(entity));
        when(featureRepository.delete(entity)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.deleteById(id))
                .verifyComplete();

        verify(featureRepository).delete(entity);
    }

    @Test
    void shouldFailCreateFeatureWhenWorkflowNotFound() {
        FeatureCreateRequest request = new FeatureCreateRequest();
        request.setWorkflowId(UUID.randomUUID());

        when(workflowRepository.findById(request.getWorkflowId())).thenReturn(Mono.empty());

        StepVerifier.create(featureService.createFeature(request))
                .expectError(FeatureException.class)
                .verify();

        verifyNoInteractions(stageRepository);
    }

    @Test
    void shouldFailCreateFeatureWhenWorkflowHasNoStages() {
        UUID workflowId = UUID.randomUUID();

        FeatureCreateRequest request = new FeatureCreateRequest();
        request.setWorkflowId(workflowId);

        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setId(workflowId);

        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(stageRepository.findFirstByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.createFeature(request))
                .expectError(FeatureException.class)
                .verify();
    }

    @Test
    void shouldErrorWhenGettingByNameWithoutEnvironmentId() {
        StepVerifier.create(featureService.getById("feature-name", IdType.NAME, null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldPropagateFeatureToNextStageCreatingNewFeature() {
        UUID workflowId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID sourceEnvId = UUID.randomUUID();
        UUID targetEnvId = UUID.randomUUID();
        UUID sourceFeatureId = UUID.randomUUID();

        FeatureEntity source = FeatureEntity.builder()
                .id(sourceFeatureId)
                .name("feature-x")
                .workspaceId(workspaceId)
                .workflowId(workflowId)
                .environmentId(sourceEnvId)
                .configuration(new BooleanFeatureStrategy())
                .enabled(true)
                .owners(Set.of("owner"))
                .description("desc")
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .build();

        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setId(workflowId);

        StageEntity stage1 = StageEntity.builder().id(UUID.randomUUID()).workflowId(workflowId).environmentId(sourceEnvId).orderIndex(0).build();
        StageEntity stage2 = StageEntity.builder().id(UUID.randomUUID()).workflowId(workflowId).environmentId(targetEnvId).orderIndex(1).build();

        when(featureRepository.findById(sourceFeatureId)).thenReturn(Mono.just(source));
        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.just(stage1, stage2));
        when(featureRepository.getByNameAndWorkspaceIdAndEnvironmentId("feature-x", workspaceId, targetEnvId))
                .thenReturn(Mono.empty());

        when(featureRepository.save(any(FeatureEntity.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        when(propagationHistoryRepo.save(any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(featureService.propagateFeature(sourceFeatureId))
                .consumeNextWith(resp -> {
                    assertThat(resp).isNotNull();
                    assertThat(resp.getStatus()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void shouldFailPropagationWhenCurrentEnvironmentIsLastStage() {
        UUID workflowId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID envId = UUID.randomUUID();
        UUID featureId = UUID.randomUUID();

        FeatureEntity source = FeatureEntity.builder()
                .id(featureId)
                .name("feature-x")
                .workspaceId(workspaceId)
                .workflowId(workflowId)
                .environmentId(envId)
                .configuration(new BooleanFeatureStrategy())
                .enabled(true)
                .owners(Set.of("owner"))
                .build();

        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setId(workflowId);

        StageEntity onlyStage = StageEntity.builder().id(UUID.randomUUID()).workflowId(workflowId).environmentId(envId).orderIndex(0).build();

        when(featureRepository.findById(featureId)).thenReturn(Mono.just(source));
        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.just(onlyStage));

        StepVerifier.create(featureService.propagateFeature(featureId))
                .expectError(FeatureException.class)
                .verify();
    }

    @Test
    void shouldFailPropagationWhenWorkflowNotFound() {
        UUID workflowId = UUID.randomUUID();
        UUID featureId = UUID.randomUUID();

        FeatureEntity source = FeatureEntity.builder()
                .id(featureId)
                .name("feature-x")
                .workspaceId(UUID.randomUUID())
                .workflowId(workflowId)
                .environmentId(UUID.randomUUID())
                .owners(Set.of("owner"))
                .configuration(new BooleanFeatureStrategy())
                .enabled(true)
                .build();

        when(featureRepository.findById(featureId)).thenReturn(Mono.just(source));
        when(workflowRepository.findById(workflowId)).thenReturn(Mono.empty());

        StepVerifier.create(featureService.propagateFeature(featureId))
                .expectError(FeatureException.class)
                .verify();
    }

    @Test
    void shouldFailPropagationWhenWorkflowHasNoStages() {
        UUID workflowId = UUID.randomUUID();
        UUID featureId = UUID.randomUUID();

        FeatureEntity source = FeatureEntity.builder()
                .id(featureId)
                .name("feature-x")
                .workspaceId(UUID.randomUUID())
                .workflowId(workflowId)
                .environmentId(UUID.randomUUID())
                .owners(Set.of("owner"))
                .configuration(new BooleanFeatureStrategy())
                .enabled(true)
                .build();

        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setId(workflowId);

        when(featureRepository.findById(featureId)).thenReturn(Mono.just(source));
        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.empty());

        StepVerifier.create(featureService.propagateFeature(featureId))
                .expectError(FeatureException.class)
                .verify();
    }

    @Test
    void shouldFailPropagationWhenCurrentEnvironmentNotInWorkflow() {
        UUID workflowId = UUID.randomUUID();
        UUID featureId = UUID.randomUUID();
        UUID currentEnvId = UUID.randomUUID();

        FeatureEntity source = FeatureEntity.builder()
                .id(featureId)
                .name("feature-x")
                .workspaceId(UUID.randomUUID())
                .workflowId(workflowId)
                .environmentId(currentEnvId)
                .owners(Set.of("owner"))
                .configuration(new BooleanFeatureStrategy())
                .enabled(true)
                .build();

        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setId(workflowId);

        StageEntity otherStage = StageEntity.builder()
                .id(UUID.randomUUID())
                .workflowId(workflowId)
                .environmentId(UUID.randomUUID())
                .orderIndex(0)
                .build();

        when(featureRepository.findById(featureId)).thenReturn(Mono.just(source));
        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.just(otherStage));

        StepVerifier.create(featureService.propagateFeature(featureId))
                .expectError(FeatureException.class)
                .verify();
    }

    @Test
    void shouldPropagateFeatureToNextStageUpdatingExistingTargetFeature() {
        UUID workflowId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID sourceEnvId = UUID.randomUUID();
        UUID targetEnvId = UUID.randomUUID();
        UUID featureId = UUID.randomUUID();

        FeatureEntity source = FeatureEntity.builder()
                .id(featureId)
                .name("feature-x")
                .workspaceId(workspaceId)
                .workflowId(workflowId)
                .environmentId(sourceEnvId)
                .configuration(new BooleanFeatureStrategy())
                .enabled(true)
                .owners(Set.of("owner"))
                .build();

        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setId(workflowId);

        StageEntity stage1 = StageEntity.builder().id(UUID.randomUUID()).workflowId(workflowId).environmentId(sourceEnvId).orderIndex(0).build();
        StageEntity stage2 = StageEntity.builder().id(UUID.randomUUID()).workflowId(workflowId).environmentId(targetEnvId).orderIndex(1).build();

        FeatureEntity existingTarget = FeatureEntity.builder()
                .id(UUID.randomUUID())
                .name("feature-x")
                .workspaceId(workspaceId)
                .workflowId(workflowId)
                .environmentId(targetEnvId)
                .enabled(false)
                .configuration(new BooleanFeatureStrategy())
                .owners(Set.of("owner"))
                .build();

        when(featureRepository.findById(featureId)).thenReturn(Mono.just(source));
        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.just(stage1, stage2));
        when(featureRepository.getByNameAndWorkspaceIdAndEnvironmentId("feature-x", workspaceId, targetEnvId))
                .thenReturn(Mono.just(existingTarget));

        when(featureRepository.save(any(FeatureEntity.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(propagationHistoryRepo.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(featureService.propagateFeature(featureId))
                .consumeNextWith(resp -> {
                    assertThat(resp.getStatus()).isNotNull();
                    assertThat(existingTarget.isEnabled()).isTrue();
                })
                .verifyComplete();

        verify(featureRepository).save(existingTarget);
    }

    @Test
    void shouldErrorWhenPropagationHistorySaveFails() {
        UUID workflowId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID sourceEnvId = UUID.randomUUID();
        UUID targetEnvId = UUID.randomUUID();
        UUID featureId = UUID.randomUUID();

        FeatureEntity source = FeatureEntity.builder()
                .id(featureId)
                .name("feature-x")
                .workspaceId(workspaceId)
                .workflowId(workflowId)
                .environmentId(sourceEnvId)
                .configuration(new BooleanFeatureStrategy())
                .enabled(true)
                .owners(Set.of("owner"))
                .build();

        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setId(workflowId);

        StageEntity stage1 = StageEntity.builder().id(UUID.randomUUID()).workflowId(workflowId).environmentId(sourceEnvId).orderIndex(0).build();
        StageEntity stage2 = StageEntity.builder().id(UUID.randomUUID()).workflowId(workflowId).environmentId(targetEnvId).orderIndex(1).build();

        when(featureRepository.findById(featureId)).thenReturn(Mono.just(source));
        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.just(stage1, stage2));
        when(featureRepository.getByNameAndWorkspaceIdAndEnvironmentId("feature-x", workspaceId, targetEnvId))
                .thenReturn(Mono.empty());

        when(featureRepository.save(any(FeatureEntity.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(propagationHistoryRepo.save(any())).thenReturn(Mono.error(new RuntimeException("db-down")));

        StepVerifier.create(featureService.propagateFeature(featureId))
                .expectErrorMatches(err -> err instanceof RuntimeException && err.getMessage().contains("db-down"))
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenTransactionalOperatorFails() {
        UUID featureId = UUID.randomUUID();
        when(featureRepository.findById(featureId)).thenReturn(Mono.just(new FeatureEntity()));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenReturn(Mono.error(new RuntimeException("transaction-failed")));

        StepVerifier.create(featureService.propagateFeature(featureId))
                .expectErrorMatches(err -> err instanceof RuntimeException && err.getMessage().contains("transaction-failed"))
                .verify();
    }
}
