package com.lyxtera.axiom.api.model;

import java.util.Optional;

import com.lyxtera.axiom.api.exception.RuleFunctionException;
import static com.lyxtera.axiom.api.exception.AxiomEngineException.MSG_FUNCTION_NOT_IMPLEMENTED;
import static com.lyxtera.axiom.api.exception.AxiomEngineException.MSG_MISSING_METADATA;
import com.lyxtera.axiom.config.RuleMetadata;
import com.lyxtera.axiom.engine.RuleContext;

/**
 * Marker interface for rule functions.
 * 
 * @param <T> the type of the enum that represents the rule function
 */
public interface RuleFunction<T extends Enum<T>> {
    /**
     * Executes the action with the given context.
     * 
     * @param context the context in which to execute the action
     */ 
    default Value execute(RuleContext<T> context) {
        throw RuleFunctionException.of(MSG_FUNCTION_NOT_IMPLEMENTED);
    }

    default String getName() {
        return Optional.ofNullable(getClass().getAnnotation(RuleMetadata.class))
            .map(RuleMetadata::name)
            .orElseThrow(() -> RuleFunctionException.of(MSG_MISSING_METADATA, getClass().getName()));
    }
}
