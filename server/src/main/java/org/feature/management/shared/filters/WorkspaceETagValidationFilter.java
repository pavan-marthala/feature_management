package org.feature.management.shared.filters;

import org.feature.management.shared.record.ETagRoute;
import org.feature.management.workspace.WorkspaceRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(3)
@RequiredArgsConstructor
public class WorkspaceETagValidationFilter extends AbstractETagValidationFilter {

    private final WorkspaceRepository workspaceRepository;

    @PostConstruct
    public void init() {
        routes.add(new ETagRoute(
                Pattern.compile("^/workspaces/([0-9a-fA-F\\-]+)$"),
                matcher -> UUID.fromString(matcher.group(1)),
                workspaceRepository::findById));
    }
}
