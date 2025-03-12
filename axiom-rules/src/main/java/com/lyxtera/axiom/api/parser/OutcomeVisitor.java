package com.lyxtera.axiom.api.parser;

import static com.lyxtera.axiom.api.exception.AxiomEngineException.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.lyxtera.axiom.antlr.BusinessRuleBaseVisitor;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessActionCallContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessActionContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessActionListContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.LiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.NumberLiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.StringLiteralContext;
import com.lyxtera.axiom.api.exception.RuleParserException;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.RuleFunction;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.ArgAwareRuleFunction;
import com.lyxtera.axiom.engine.RuleSet;

/**
 * Specialized visitor for parsing actions in business rules.
 * <p>
 * This visitor traverses the parse tree for action expressions and constructs
 * a list of {@link BusinessAction} objects that can be executed at runtime.
 * It handles business action function calls with their arguments.
 *
 * @param <K> The enum type to be used as context keys
 */
public class OutcomeVisitor<K extends Enum<K>> extends BusinessRuleBaseVisitor<List<RuleFunction<K>>> {
    
    private final ArgumentVisitor<K> argumentVisitor;
    private final Map<String, BusinessAction<K>> businessActions;
    private final RuleSet.Metadata metadata;
    
    /**
     * Creates a new OutcomeVisitor with the specified business actions.
     *
     * @param businessActions Map of business action implementations keyed by name
     */
    public OutcomeVisitor(Map<String, BusinessAction<K>> businessActions, RuleSet.Metadata metadata) {
        this.argumentVisitor = new ArgumentVisitor<>();
        this.businessActions = businessActions;
        this.metadata = metadata;
    }
    
    /**
     * Visits a business action list node in the parse tree and constructs a list of {@link BusinessAction} objects.
     * <p>
     * This method iterates through all business action calls in the list and constructs
     * a BusinessAction object for each one.
     *
     * @param ctx The business action list context from the parse tree
     * @return A list of business actions to be executed
     */
    @Override
    public List<RuleFunction<K>> visitBusinessActionList(BusinessActionListContext ctx) {
        List<RuleFunction<K>> actions = new ArrayList<>();

        BusinessActionVisitor businessActionVisitor = new BusinessActionVisitor();
        
        for (BusinessActionContext actionCtx : ctx.businessAction()) {
            actions.add(businessActionVisitor.visitBusinessActionCall((BusinessActionCallContext) actionCtx));
        }
        
        return actions;
    }

    /**
     * Inner visitor class for business action function calls.
     */
    private class BusinessActionVisitor extends BusinessRuleBaseVisitor<RuleFunction<K>> {
            
        /**
         * Visits a business action call node in the parse tree and constructs a business action function.
         * <p>
         * A business action call is a function call with a name and optional arguments.
         *
         * @param ctx The business action call context from the parse tree
         * @return A business action function with the specified name and arguments
         * @throws RuleParserException if the business action function is unknown
         */
        @Override
        public RuleFunction<K> visitBusinessActionCall(BusinessActionCallContext ctx) {
            String actionName = ctx.IDENTIFIER().getText();
            List<Value> parameters = new ArrayList<>();
            
            if (ctx.arguments() != null) {
                for (LiteralContext literalCtx : ctx.arguments().literal()) {
                    if (literalCtx instanceof NumberLiteralContext) {
                        parameters.add(argumentVisitor.visitNumberLiteral((NumberLiteralContext) literalCtx));
                    } else {
                        parameters.add(argumentVisitor.visitStringLiteral((StringLiteralContext) literalCtx));
                    }
                }
            }
            
            return Optional.ofNullable(businessActions.get(actionName))
                .map(action -> ArgAwareRuleFunction.of(action, parameters, metadata))
                .orElseThrow(() -> RuleParserException.of(MSG_UNKNOWN_BUSINESS_ACTION, actionName));
        }
    }
} 