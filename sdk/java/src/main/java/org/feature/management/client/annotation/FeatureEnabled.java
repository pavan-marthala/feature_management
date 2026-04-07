package org.feature.management.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate that a method requires a specific feature to be
 * enabled.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureEnabled {
    /**
     * The name of the feature to check.
     */
    String value();

    /**
     * Optional fallback method name to execute if the feature is disabled.
     * The fallback method must be in the same class and have the same signature as
     * the annotated method.
     */
    String fallbackMethod() default "";
}
