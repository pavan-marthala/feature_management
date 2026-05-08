package org.feature.management.workflow;

import org.feature.management.models.Stage;
import org.feature.management.models.StageRequest;
import org.feature.management.models.StageType;
import org.feature.management.models.WorkflowBase;
import org.feature.management.models.WorkflowStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowMapperTest {

    private final WorkflowMapper mapper = Mappers.getMapper(WorkflowMapper.class);

    @Test
    void shouldMapWorkflowEntityToBaseModel_includingVersion() {
        UUID id = UUID.randomUUID();
        WorkflowEntity entity = new WorkflowEntity();
        entity.setId(id);
        entity.setName("wf");
        entity.setStatus(WorkflowStatus.ACTIVE);
        entity.setVersion(4L);

        WorkflowBase model = mapper.toBaseModel(entity);

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo(id);
        assertThat(model.getName()).isEqualTo("wf");
        assertThat(model.getStatus()).isEqualTo(WorkflowStatus.ACTIVE);
        assertThat(model.getVersion()).isEqualTo(4L);
    }

    @Test
    void shouldMapWorkflowBaseToEntity_ignoringIdAndVersion() {
        WorkflowBase model = new WorkflowBase();
        model.setId(UUID.randomUUID());
        model.setName("wf");
        model.setStatus(WorkflowStatus.ACTIVE);
        model.setVersion(99L);

        WorkflowEntity entity = mapper.toEntity(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.getName()).isEqualTo("wf");
        assertThat(entity.getStatus()).isEqualTo(WorkflowStatus.ACTIVE);
    }

    @Test
    void shouldMapStageEntityToModel_includingVersion() {
        UUID id = UUID.randomUUID();
        UUID envId = UUID.randomUUID();

        StageEntity entity = new StageEntity();
        entity.setId(id);
        entity.setEnvironmentId(envId);
        entity.setOrderIndex(2);
        entity.setType(StageType.MANUAL);
        entity.setVersion(7L);

        Stage model = mapper.toModel(entity);

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo(id);
        assertThat(model.getEnvironmentId()).isEqualTo(envId);
        assertThat(model.getOrderIndex()).isEqualTo(2);
        assertThat(model.getType()).isEqualTo(StageType.MANUAL);
        assertThat(model.getVersion()).isEqualTo(7L);
    }

    @Test
    void shouldMapStageToEntity_ignoringIdAndVersion() {
        Stage model = new Stage();
        model.setId(UUID.randomUUID());
        model.setEnvironmentId(UUID.randomUUID());
        model.setOrderIndex(1);
        model.setType(StageType.MANUAL);
        model.setVersion(123L);

        StageEntity entity = mapper.toEntity(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.getEnvironmentId()).isEqualTo(model.getEnvironmentId());
        assertThat(entity.getOrderIndex()).isEqualTo(1);
        assertThat(entity.getType()).isEqualTo(StageType.MANUAL);
    }

    @Test
    void shouldMapStageRequestToEntity_ignoringWorkflowFieldsAndPointers() {
        StageRequest request = new StageRequest();
        request.setEnvironmentId(UUID.randomUUID());
        request.setOrderIndex(3);
        request.setType(StageType.SCHEDULED);
        request.setScheduleExpression("0 0 * * *");

        StageEntity entity = mapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.getWorkflowId()).isNull();
        assertThat(entity.getNextStageId()).isNull();
        assertThat(entity.getApprovalNeeded()).isNull();
        assertThat(entity.getEnvironmentId()).isEqualTo(request.getEnvironmentId());
        assertThat(entity.getOrderIndex()).isEqualTo(3);
        assertThat(entity.getType()).isEqualTo(StageType.SCHEDULED);
        assertThat(entity.getScheduleExpression()).isEqualTo("0 0 * * *");
    }

    @Test
    void shouldMapNullEntityToNullModel() {
        assertThat(mapper.toBaseModel(null)).isNull();
        assertThat(mapper.toModel((StageEntity) null)).isNull();
    }
}

