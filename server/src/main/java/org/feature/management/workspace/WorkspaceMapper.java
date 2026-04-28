package org.feature.management.workspace;

import org.feature.management.models.*;
import org.feature.management.shared.utils.DateMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = DateMapper.class)
public interface WorkspaceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    WorkspaceEntity toEntity(WorkspaceRequest request);

    Workspace toModel(WorkspaceEntity entity);
}
