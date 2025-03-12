package com.lyxtera.axiom.engine;

import static com.lyxtera.axiom.api.exception.AxiomEngineException.MSG_CTXGET_REQUIRES_STRING;

import com.lyxtera.axiom.api.exception.RuleFunctionException;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.config.Arg;
import com.lyxtera.axiom.config.RuleMetadata;

/**
 * BusinessCheck implementation that allows accessing context values using
 * string keys.
 * This is used by the ctxGet(CONTEXT_KEY) function in rule expressions.
 */
@RuleMetadata(name = "ctxGet", description = "Built-in function to access a value from the context using a string key")
public class CtxGet<K extends Enum<K>> implements BusinessCheck<K> {

    public Value execute(RuleContext<K> ctx, @Arg("ctxKey") Value ctxKey) {
        if (ctxKey.getType() != Value.Type.STRING) {
            throw RuleFunctionException.invalidArgument(MSG_CTXGET_REQUIRES_STRING);
        }

        return ctx.get(ctx.keyByValue(ctxKey), Object.class)
            .map(Value::fromObject)
            .orElse(Value.EMPTY);
    }
}
