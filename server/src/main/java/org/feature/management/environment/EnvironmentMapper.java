package org.feature.management.environment;

import org.feature.management.models.Environment;
import org.feature.management.models.EnvironmentRequest;
import org.feature.management.shared.utils.DateMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnvironmentMapper {

    Environment toModel(EnvironmentEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "etag", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    EnvironmentEntity toEntity(EnvironmentRequest model);
}
