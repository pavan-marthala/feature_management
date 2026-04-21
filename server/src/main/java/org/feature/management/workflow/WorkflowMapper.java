package org.feature.management.workflow;

import org.feature.management.models.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkflowMapper {

    @Mapping(target = "version", source = "version")
    WorkflowBase toBaseModel(WorkflowEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    WorkflowEntity toEntity(WorkflowBase model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    WorkflowEntity toEntity(WorkflowRequest request);

    @Mapping(target = "version", source = "version")
    Stage toModel(StageEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    StageEntity toEntity(Stage model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "workflowId", ignore = true)
    @Mapping(target = "nextStageId", ignore = true)
    @Mapping(target = "approvalNeeded", ignore = true)
    StageEntity toEntity(StageRequest request);
}
