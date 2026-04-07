package org.feature.management.client.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.feature.management.client.annotation.FeatureEnabled;
import org.feature.management.client.exception.FeatureManagementException;
import org.feature.management.client.service.FeatureService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
                            return (Mono<?>) executeDisabledLogic(joinPoint, featureEnabled, returnType);
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
                            return (Flux<?>) executeDisabledLogic(joinPoint, featureEnabled, returnType);
                        }
                    });
        }

        // Only reactive types are supported.
        throw new FeatureManagementException(
                "FeatureEnabled annotation is only supported on methods returning Mono or Flux. Method '" +
                signature.getName() + "' returns " + returnType.getName(), null
        );
    }

    private Object executeDisabledLogic(ProceedingJoinPoint joinPoint, FeatureEnabled featureEnabled, Class<?> returnType) {
        String fallbackMethodName = featureEnabled.fallbackMethod();

        if (!fallbackMethodName.isEmpty()) {
            return invokeFallback(joinPoint, fallbackMethodName);
        }

        // Default non-throwing behavior
        if (Mono.class.isAssignableFrom(returnType)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            if (isBooleanMono(signature.getMethod())) {
                return Mono.just(false);
            }
            return Mono.empty();
        } else if (Flux.class.isAssignableFrom(returnType)) {
            return Flux.empty();
        }

        return null;
    }

    private boolean isBooleanMono(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                Type typeArg = actualTypeArguments[0];
                return typeArg.equals(Boolean.class) || typeArg.equals(boolean.class);
            }
        }
        return false;
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethodName) {
        Object target = joinPoint.getTarget();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        try {
            Method fallbackMethod = target.getClass().getDeclaredMethod(fallbackMethodName, signature.getParameterTypes());
            fallbackMethod.setAccessible(true);
            return fallbackMethod.invoke(target, args);
        } catch (NoSuchMethodException e) {
            throw new FeatureManagementException("Fallback method '" + fallbackMethodName + "' not found in class " + target.getClass().getName(), e);
        } catch (Exception e) {
            throw new FeatureManagementException("Error invoking fallback method '" + fallbackMethodName + "'", e);
        }
    }
}
