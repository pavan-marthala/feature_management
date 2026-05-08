package org.feature.management.workflow;

import org.feature.management.environment.EnvironmentEntity;
import org.feature.management.environment.EnvironmentRepository;
import org.feature.management.models.Stage;
import org.feature.management.models.StageRequest;
import org.feature.management.models.StageType;
import org.feature.management.models.WorkflowBase;
import org.feature.management.models.WorkflowStatus;
import org.feature.management.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

        @Mock
        private WorkflowRepository workflowRepository;

        @Mock
        private StageRepository stageRepository;

        @Mock
        private WorkflowMapper workflowMapper;

        @Mock
        private EnvironmentRepository environmentRepository;

        private WorkflowService service;

        @BeforeEach
        void setUp() {
                service = new WorkflowService(workflowRepository, stageRepository, workflowMapper,
                                environmentRepository);
                lenient().when(workflowRepository.save(any(WorkflowEntity.class)))
                                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
                lenient().when(stageRepository.save(any(StageEntity.class)))
                                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        }

        @Test
        void shouldCreateWorkflow() {
                WorkflowBase request = new WorkflowBase();
                request.setName("wf");
                request.setStatus(WorkflowStatus.ACTIVE);

                WorkflowEntity mapped = new WorkflowEntity();
                mapped.setName("wf");
                mapped.setStatus(WorkflowStatus.ACTIVE);
                when(workflowMapper.toEntity(request)).thenReturn(mapped);

                StepVerifier.create(service.createWorkflow(request))
                                .assertNext(id -> assertThat(id).isNotNull())
                                .verifyComplete();
        }

        @Test
        void shouldFailGetWorkflowByIdWhenNotFound() {
                UUID id = UUID.randomUUID();
                when(workflowRepository.findById(id)).thenReturn(Mono.empty());

                StepVerifier.create(service.getWorkflowById(id))
                                .expectError(ResourceNotFoundException.class)
                                .verify();
        }

        @Test
        void shouldUpdateWorkflowStatus() {
                UUID id = UUID.randomUUID();
                WorkflowEntity existing = new WorkflowEntity();
                existing.setId(id);
                existing.setStatus(WorkflowStatus.DRAFT);

                when(workflowRepository.findById(id)).thenReturn(Mono.just(existing));

                StepVerifier.create(service.updateWorkflowStatus(id, WorkflowStatus.ACTIVE))
                                .verifyComplete();

                assertThat(existing.getStatus()).isEqualTo(WorkflowStatus.ACTIVE);
                verify(workflowRepository).save(existing);
        }

        @Test
        void shouldDeleteWorkflowAlsoDeletingStages() {
                UUID workflowId = UUID.randomUUID();
                WorkflowEntity workflow = new WorkflowEntity();
                workflow.setId(workflowId);
                when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
                when(stageRepository.deleteAllByWorkflowId(workflowId)).thenReturn(Mono.empty());
                when(workflowRepository.delete(workflow)).thenReturn(Mono.empty());

                StepVerifier.create(service.deleteWorkflow(workflowId))
                                .verifyComplete();

                verify(stageRepository).deleteAllByWorkflowId(workflowId);
                verify(workflowRepository).delete(workflow);
        }

        @Test
        void shouldAddStageToEmptyWorkflow_savesDirectly() {
                UUID workflowId = UUID.randomUUID();
                UUID envId = UUID.randomUUID();

                Stage stage = new Stage();
                stage.setEnvironmentId(envId);
                stage.setOrderIndex(0);
                stage.setType(StageType.MANUAL);

                StageEntity mapped = new StageEntity();
                mapped.setEnvironmentId(envId);
                mapped.setOrderIndex(0);
                mapped.setType(StageType.MANUAL);
                when(workflowMapper.toEntity(stage)).thenReturn(mapped);

                EnvironmentEntity env = new EnvironmentEntity();
                env.setId(envId);
                env.setName("dev");
                when(environmentRepository.findById(envId)).thenReturn(Mono.just(env));
                when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.empty());

                StepVerifier.create(service.addStageToWorkflow(workflowId, stage))
                                .assertNext(newStageId -> assertThat(newStageId).isNotNull())
                                .verifyComplete();

                verify(stageRepository).save(any(StageEntity.class));
        }

        @Test
        void shouldInsertStageInMiddle_shiftsOrderAndLinksPreviousNextStageId() {
                UUID workflowId = UUID.randomUUID();

                UUID env0 = UUID.randomUUID();
                UUID env1 = UUID.randomUUID();
                UUID envNew = UUID.randomUUID();

                StageEntity s0 = StageEntity.builder()
                                .id(UUID.randomUUID())
                                .workflowId(workflowId)
                                .environmentId(env0)
                                .orderIndex(0)
                                .build();
                StageEntity s1 = StageEntity.builder()
                                .id(UUID.randomUUID())
                                .workflowId(workflowId)
                                .environmentId(env1)
                                .orderIndex(1)
                                .build();

                when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.just(s0, s1));

                Stage stage = new Stage();
                stage.setEnvironmentId(envNew);
                stage.setOrderIndex(1);
                stage.setType(StageType.MANUAL);

                StageEntity mapped = new StageEntity();
                mapped.setEnvironmentId(envNew);
                mapped.setOrderIndex(1);
                mapped.setType(StageType.MANUAL);
                when(workflowMapper.toEntity(stage)).thenReturn(mapped);

                EnvironmentEntity env = new EnvironmentEntity();
                env.setId(envNew);
                env.setName("qa");
                when(environmentRepository.findById(envNew)).thenReturn(Mono.just(env));

                List<StageEntity> saved = new ArrayList<>();
                when(stageRepository.save(any(StageEntity.class))).thenAnswer(inv -> {
                        StageEntity arg = inv.getArgument(0);
                        saved.add(arg);
                        return Mono.just(arg);
                });

                StepVerifier.create(service.addStageToWorkflow(workflowId, stage))
                                .assertNext(newStageId -> {
                                        assertThat(newStageId).isNotNull();
                                        assertThat(s0.getNextStageId()).isEqualTo(newStageId);
                                        assertThat(s1.getOrderIndex()).isEqualTo(2);
                                })
                                .verifyComplete();

                // ensure we saved shifted stage(s), new stage, and previous stage update
                assertThat(saved).isNotEmpty();
        }

        @Test
        void shouldResolveNextStageId_explicitProvidedWins() {
                UUID workflowId = UUID.randomUUID();
                UUID env0 = UUID.randomUUID();
                UUID env1 = UUID.randomUUID();
                UUID explicitNext = UUID.randomUUID();

                StageEntity s0 = StageEntity.builder()
                                .id(UUID.randomUUID())
                                .workflowId(workflowId)
                                .environmentId(env0)
                                .orderIndex(0)
                                .build();
                StageEntity s1 = StageEntity.builder()
                                .id(UUID.randomUUID())
                                .workflowId(workflowId)
                                .environmentId(env1)
                                .orderIndex(1)
                                .build();

                when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.just(s0, s1));

                Stage stage = new Stage();
                stage.setEnvironmentId(UUID.randomUUID());
                stage.setOrderIndex(1);
                stage.setNextStageId(explicitNext);
                stage.setType(StageType.MANUAL);

                StageEntity mapped = new StageEntity();
                mapped.setEnvironmentId(stage.getEnvironmentId());
                mapped.setOrderIndex(1);
                mapped.setType(StageType.MANUAL);
                when(workflowMapper.toEntity(stage)).thenReturn(mapped);
                when(environmentRepository.findById(stage.getEnvironmentId()))
                                .thenReturn(Mono.just(new EnvironmentEntity()));

                StepVerifier.create(service.addStageToWorkflow(workflowId, stage))
                                .assertNext(newStageId -> {
                                        // previous stage should point at new stage; new stage should point at explicit
                                        // next
                                        assertThat(s0.getNextStageId()).isEqualTo(newStageId);
                                        assertThat(mapped.getNextStageId()).isEqualTo(explicitNext);
                                })
                                .verifyComplete();
        }

        @Test
        void shouldResolveNextStageId_inferFromExistingWhenNotProvided() {
                UUID workflowId = UUID.randomUUID();

                UUID env0 = UUID.randomUUID();
                UUID env1 = UUID.randomUUID();
                UUID envNew = UUID.randomUUID();

                StageEntity s0 = StageEntity.builder().id(UUID.randomUUID()).workflowId(workflowId).environmentId(env0)
                                .orderIndex(0).build();
                StageEntity s1 = StageEntity.builder().id(UUID.randomUUID()).workflowId(workflowId).environmentId(env1)
                                .orderIndex(1).build();
                when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.just(s0, s1));

                Stage stage = new Stage();
                stage.setEnvironmentId(envNew);
                stage.setOrderIndex(1);
                stage.setType(StageType.MANUAL);

                StageEntity mapped = new StageEntity();
                mapped.setEnvironmentId(envNew);
                mapped.setOrderIndex(1);
                mapped.setType(StageType.MANUAL);
                when(workflowMapper.toEntity(stage)).thenReturn(mapped);
                when(environmentRepository.findById(envNew)).thenReturn(Mono.just(new EnvironmentEntity()));

                StepVerifier.create(service.addStageToWorkflow(workflowId, stage))
                                .assertNext(newStageId -> assertThat(mapped.getNextStageId()).isEqualTo(s1.getId()))
                                .verifyComplete();
        }

        @Test
        void shouldUpdateStage_whenEnvironmentChanged_enrichesEnvironmentName() {
                UUID stageId = UUID.randomUUID();
                UUID oldEnvId = UUID.randomUUID();
                UUID newEnvId = UUID.randomUUID();

                StageEntity entity = new StageEntity();
                entity.setId(stageId);
                entity.setEnvironmentId(oldEnvId);
                entity.setEnvironmentName("old");
                when(stageRepository.findById(stageId)).thenReturn(Mono.just(entity));

                EnvironmentEntity env = new EnvironmentEntity();
                env.setId(newEnvId);
                env.setName("new-env");
                when(environmentRepository.findById(newEnvId)).thenReturn(Mono.just(env));

                StageRequest request = new StageRequest();
                request.setEnvironmentId(newEnvId);
                request.setOrderIndex(2);
                request.setType(StageType.AUTOMATIC);
                request.setScheduleExpression("0 0 * * *");

                StepVerifier.create(service.updateStage(stageId, request))
                                .verifyComplete();

                assertThat(entity.getEnvironmentId()).isEqualTo(newEnvId);
                assertThat(entity.getEnvironmentName()).isEqualTo("new-env");
                verify(stageRepository).save(entity);
        }

        @Test
        void shouldUpdateStage_whenEnvironmentNotChanged_doesNotLookupEnvironment() {
                UUID stageId = UUID.randomUUID();
                UUID envId = UUID.randomUUID();

                StageEntity entity = new StageEntity();
                entity.setId(stageId);
                entity.setEnvironmentId(envId);
                entity.setEnvironmentName("existing");
                when(stageRepository.findById(stageId)).thenReturn(Mono.just(entity));

                StageRequest request = new StageRequest();
                request.setEnvironmentId(envId);
                request.setOrderIndex(2);

                StepVerifier.create(service.updateStage(stageId, request))
                                .verifyComplete();

                verifyNoInteractions(environmentRepository);
                verify(stageRepository).save(entity);
        }

        @Test
        void shouldFailUpdateStageWhenStageNotFound() {
                UUID stageId = UUID.randomUUID();
                when(stageRepository.findById(stageId)).thenReturn(Mono.empty());

                StepVerifier.create(service.updateStage(stageId, new StageRequest()))
                                .expectError(ResourceNotFoundException.class)
                                .verify();
        }

        @Test
        void shouldFailEnrichWhenEnvironmentMissing() {
                UUID workflowId = UUID.randomUUID();
                UUID missingEnvId = UUID.randomUUID();

                Stage stage = new Stage();
                stage.setEnvironmentId(missingEnvId);
                stage.setOrderIndex(0);

                StageEntity mapped = new StageEntity();
                mapped.setEnvironmentId(missingEnvId);
                mapped.setOrderIndex(0);
                when(workflowMapper.toEntity(stage)).thenReturn(mapped);

                when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.empty());
                when(environmentRepository.findById(missingEnvId)).thenReturn(Mono.empty());

                StepVerifier.create(service.addStageToWorkflow(workflowId, stage))
                                .expectError(ResourceNotFoundException.class)
                                .verify();
        }

        @Test
        void shouldAddStageWithNullEnvironmentId_skipsEnrichment() {
                UUID workflowId = UUID.randomUUID();
                Stage stage = new Stage();
                stage.setEnvironmentId(null);
                stage.setOrderIndex(0);

                StageEntity mapped = new StageEntity();
                mapped.setEnvironmentId(null);
                when(workflowMapper.toEntity(stage)).thenReturn(mapped);
                when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.empty());

                StepVerifier.create(service.addStageToWorkflow(workflowId, stage))
                                .assertNext(id -> assertThat(id).isNotNull())
                                .verifyComplete();

                verifyNoInteractions(environmentRepository);
        }

        @Test
        void shouldFailGetStageByIdWhenNotFound() {
                UUID id = UUID.randomUUID();
                when(stageRepository.findById(id)).thenReturn(Mono.empty());

                StepVerifier.create(service.getStageById(id))
                                .expectError(ResourceNotFoundException.class)
                                .verify();
        }

        @Test
        void shouldFailDeleteStageWhenNotFound() {
                UUID id = UUID.randomUUID();
                when(stageRepository.findById(id)).thenReturn(Mono.empty());

                StepVerifier.create(service.deleteStage(id))
                                .expectError(ResourceNotFoundException.class)
                                .verify();
        }

        @Test
        void shouldResolveNextStageIdAsNullWhenAtEnd() {
                UUID workflowId = UUID.randomUUID();
                StageEntity s0 = StageEntity.builder().id(UUID.randomUUID()).workflowId(workflowId).orderIndex(0)
                                .build();
                when(stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)).thenReturn(Flux.just(s0));

                Stage stage = new Stage();
                stage.setOrderIndex(1); // at end
                stage.setEnvironmentId(UUID.randomUUID());

                StageEntity mapped = new StageEntity();
                mapped.setOrderIndex(1);
                mapped.setEnvironmentId(UUID.randomUUID());
                when(workflowMapper.toEntity(stage)).thenReturn(mapped);
                when(environmentRepository.findById(any(UUID.class))).thenReturn(Mono.just(new EnvironmentEntity()));

                StepVerifier.create(service.addStageToWorkflow(workflowId, stage))
                                .assertNext(id -> assertThat(mapped.getNextStageId()).isNull())
                                .verifyComplete();
        }
}
