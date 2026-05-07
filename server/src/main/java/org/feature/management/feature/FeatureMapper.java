package org.feature.management.feature;

import org.feature.management.models.Feature;
import org.feature.management.models.FeatureCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeatureMapper {

    @Mapping(target = "configuration", source = "configuration")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    FeatureEntity toEntity(Feature feature);

    @Mapping(target = "configuration", source = "configuration")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "etag", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "environmentId", ignore = true)
    FeatureEntity toEntity(FeatureCreateRequest feature);

    @Mapping(target = "_configuration", source = "configuration")
    Feature toModel(FeatureEntity entity);

}
