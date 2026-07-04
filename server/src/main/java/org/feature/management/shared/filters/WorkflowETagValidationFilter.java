package org.feature.management.shared.filters;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.feature.management.shared.record.ETagRoute;
import org.feature.management.workflow.StageRepository;
import org.feature.management.workflow.WorkflowRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(4)
@RequiredArgsConstructor
public class WorkflowETagValidationFilter extends AbstractETagValidationFilter {

    private final WorkflowRepository workflowRepository;
    private final StageRepository stageRepository;

    @PostConstruct
    public void init() {
        routes.add(new ETagRoute(
                Pattern.compile("^/workflows/([0-9a-fA-F\\-]+)$"),
                matcher -> UUID.fromString(matcher.group(1)),
                workflowRepository::findById));
        routes.add(new ETagRoute(
                Pattern.compile("^/workflows/([0-9a-fA-F\\-]+)/status$"),
                matcher -> UUID.fromString(matcher.group(1)),
                workflowRepository::findById));
        routes.add(new ETagRoute(
                Pattern.compile("^/workflows/([0-9a-fA-F\\-]+)/stages/([0-9a-fA-F\\-]+)$"),
                matcher -> UUID.fromString(matcher.group(2)),
                stageRepository::findById));
    }
}
