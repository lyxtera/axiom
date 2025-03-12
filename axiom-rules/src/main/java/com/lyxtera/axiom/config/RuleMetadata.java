package com.lyxtera.axiom.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RuleMetadata {
    /**
     * The name of the rule function.
     */
    String name();

    /**
     * The description of the rule function.
     */
    String description();
}
