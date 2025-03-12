package com.lyxtera.axiom.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lyxtera.axiom.api.model.Value.Type;

/**
 * Annotation to specify the name of a parameter for rule evaluation.
 * Used in conjunction with ArgAwareFunctionCaller to map named arguments
 * to their corresponding parameter positions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Arg {
    /**
     * The name of the argument, which will be used to map the argument
     * value when invoking the method through ArgAwareFunctionCaller.
     */
    String value();

    /**
     * The description of the argument.
     */
    String description() default "";

    /**
     * The type of the argument.
     */
    Type type() default Type.STRING;
} 