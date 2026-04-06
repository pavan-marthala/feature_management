package org.feature.management.client.context;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class EvaluationContext {
    private String userId;
    private List<String> roles;
    private Map<String, String> attributes;
    private Map<String, String> headers;
}
