package org.feature.management.workspace;

import org.feature.management.models.Workspace;
import org.feature.management.models.WorkspaceRequest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceMapperTest {

    private final WorkspaceMapper mapper = Mappers.getMapper(WorkspaceMapper.class);

    @Test
    void shouldMapRequestToEntity_ignoringManagedFields() {
        WorkspaceRequest request = new WorkspaceRequest();
        request.setName("ws");
        request.setDescription("desc");

        WorkspaceEntity entity = mapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getModifiedAt()).isNull();
        assertThat(entity.getName()).isEqualTo("ws");
        assertThat(entity.getDescription()).isEqualTo("desc");
    }

    @Test
    void shouldMapEntityToModel() {
        UUID id = UUID.randomUUID();
        WorkspaceEntity entity = WorkspaceEntity.builder()
                .id(id)
                .name("ws")
                .description("desc")
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .version(10L)
                .build();

        Workspace model = mapper.toModel(entity);

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo(id);
        assertThat(model.getName()).isEqualTo("ws");
        assertThat(model.getDescription()).isEqualTo("desc");
    }

    @Test
    void shouldMapNullEntityToNullModel() {
        assertThat(mapper.toModel(null)).isNull();
    }
}

