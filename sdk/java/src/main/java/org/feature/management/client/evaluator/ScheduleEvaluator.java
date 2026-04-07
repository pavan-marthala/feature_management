package org.feature.management.client.evaluator;

import org.feature.management.client.context.EvaluationContext;
import org.feature.management.models.ScheduleFeatureStrategy;
import org.springframework.scheduling.support.CronExpression;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class ScheduleEvaluator implements FeatureEvaluator<ScheduleFeatureStrategy> {

    @Override
    public boolean evaluate(ScheduleFeatureStrategy strategy, EvaluationContext context) {
        String cron = strategy.getCron();
        if (cron == null || cron.isBlank()) {
            return false;
        }

        try {
            CronExpression expression = CronExpression.parse(cron);
            ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            // In Spring's CronExpression, .next(temporal) returns the next occurrence strictly after the given temporal.
            // To check if 'now' matches, we check the next execution after (now - 1 second).
            // If that next execution is exactly 'now', then the feature is active.
            ZonedDateTime nextOccurrence = expression.next(now.minusSeconds(1));

            return nextOccurrence != null && nextOccurrence.toEpochSecond() == now.toEpochSecond();
        } catch (IllegalArgumentException e) {
            // Invalid cron expression - fail closed
            return false;
        }
    }

    @Override
    public String getStrategyName() {
        return "ScheduleFeatureStrategy";
    }
}
