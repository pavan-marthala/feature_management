package org.feature.management.feature;

import org.feature.management.models.Feature;
import org.feature.management.models.FeatureCreateRequest;
import org.feature.management.shared.utils.DateMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = DateMapper.class)
public interface FeatureMapper {

    FeatureMapper INSTANCE = Mappers.getMapper(FeatureMapper.class);

    @Mapping(target = "configuration", source = "configuration")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    FeatureEntity toEntity(Feature feature);

    @Mapping(target = "configuration", source = "configuration")
    @Mapping(target = "environmentId", source = "envId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "etag", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    FeatureEntity toEntity(FeatureCreateRequest feature);

    @Mapping(target = "_configuration", source = "configuration")
    Feature toModel(FeatureEntity entity);

}
