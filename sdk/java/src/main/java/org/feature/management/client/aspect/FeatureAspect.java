package org.feature.management.client.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.feature.management.client.annotation.FeatureEnabled;
import org.feature.management.client.exception.FeatureDisabledException;
import org.feature.management.client.exception.FeatureManagementException;
import org.feature.management.client.service.FeatureService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
public class FeatureAspect {

    private final FeatureService featureService;

    public FeatureAspect(FeatureService featureService) {
        this.featureService = featureService;
    }

    @Around("@annotation(featureEnabled)")
    public Object checkFeature(ProceedingJoinPoint joinPoint, FeatureEnabled featureEnabled) {
        if (!(joinPoint.getSignature() instanceof MethodSignature)) {
            throw new FeatureManagementException("FeatureEnabled annotation can only be used on methods", null);
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        String featureName = featureEnabled.value();

        if (Mono.class.isAssignableFrom(returnType)) {
            return featureService.isEnabled(featureName)
                    .flatMap(enabled -> {
                        if (enabled) {
                            try {
                                return (Mono<?>) joinPoint.proceed();
                            } catch (Throwable e) {
                                return Mono.error(e);
                            }
                        } else {
                            return Mono.error(new FeatureDisabledException("Feature " + featureName + " is disabled"));
                        }
                    });
        } else if (Flux.class.isAssignableFrom(returnType)) {
            return featureService.isEnabled(featureName)
                    .flatMapMany(enabled -> {
                        if (enabled) {
                            try {
                                return (Flux<?>) joinPoint.proceed();
                            } catch (Throwable e) {
                                return Flux.error(e);
                            }
                        } else {
                            return Flux.error(new FeatureDisabledException("Feature " + featureName + " is disabled"));
                        }
                    });
        }

        // Only reactive types are supported.
        throw new FeatureManagementException(
                "FeatureEnabled annotation is only supported on methods returning Mono or Flux. Method '" +
                signature.getName() + "' returns " + returnType.getName(), null
        );
    }
}
