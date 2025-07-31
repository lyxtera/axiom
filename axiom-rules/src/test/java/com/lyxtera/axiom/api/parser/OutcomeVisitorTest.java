package com.lyxtera.axiom.api.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.antlr.BusinessRuleParser;
import com.lyxtera.axiom.antlr.BusinessRuleParser.ArgumentsContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.BusinessActionCallContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.NumberLiteralContext;
import com.lyxtera.axiom.antlr.BusinessRuleParser.StringLiteralContext;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.RuleFunction;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.RuleSet;
import com.lyxtera.axiom.api.exception.RuleParserException;

/**
 * Test class for {@link OutcomeVisitor}.
 */
public class OutcomeVisitorTest {

    public enum TestKey {
        AGE,
        IS_PREMIUM,
        NAME
    }

    private Map<String, BusinessAction<TestKey>> businessActions;
    private OutcomeVisitor<TestKey> visitor;
    private BusinessAction<TestKey> notifyAction;
    private BusinessAction<TestKey> logAction;
    private RuleSet.Metadata metadata;
    
    @BeforeEach
    public void setUp() {
        businessActions = new HashMap<>();
        
        // Set up a mock business action for notify
        notifyAction = mock(BusinessAction.class);
        when(notifyAction.getName()).thenReturn("notify");
        businessActions.put("notify", notifyAction);
        
        // Set up a mock business action for log
        logAction = mock(BusinessAction.class);
        when(logAction.getName()).thenReturn("log");
        businessActions.put("log", logAction);
        
        // Create mock metadata
        metadata = createMockMetadata();
        
        visitor = new OutcomeVisitor<>(businessActions, metadata);
    }
    
    // Helper method to create properly mocked metadata
    private RuleSet.Metadata createMockMetadata() {
        RuleSet.Metadata metadata = new RuleSet.Metadata();
        try {
            // Use reflection to set the ruleset name since it has a package-private setter
            java.lang.reflect.Method setRuleSetNameMethod = RuleSet.Metadata.class.getDeclaredMethod("setRuleSetName", String.class);
            setRuleSetNameMethod.setAccessible(true);
            setRuleSetNameMethod.invoke(metadata, "TestRuleSet");
        } catch (Exception e) {
            // In case reflection fails, use mocking as a fallback
            metadata = mock(RuleSet.Metadata.class);
            when(metadata.getRuleSetName()).thenReturn("TestRuleSet");
        }
        return metadata;
    }
    
    @Test
    public void testVisitBusinessActionList_SingleAction() {
        // Create mock context for a single action
        BusinessRuleParser.BusinessActionListContext ctx = mock(BusinessRuleParser.BusinessActionListContext.class);
        BusinessActionCallContext actionCtx = mock(BusinessActionCallContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        
        // Set up the mock to return a single action
        List<BusinessRuleParser.BusinessActionContext> actionList = new ArrayList<>();
        actionList.add(actionCtx);
        when(ctx.businessAction()).thenReturn(actionList);
        
        // Set up the action context
        when(actionCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("notify");
        
        // Visit the mock context
        List<RuleFunction<TestKey>> result = visitor.visitBusinessActionList(ctx);
        
        // Verify the result
        assertThat(result)
            .isNotNull()
            .hasSize(1)
            .extracting(RuleFunction::getName)
            .containsExactly("notify");
    }
    
    @Test
    public void testVisitBusinessActionList_MultipleActions() {
        // Create mock context for multiple actions
        BusinessRuleParser.BusinessActionListContext ctx = mock(BusinessRuleParser.BusinessActionListContext.class);
        BusinessActionCallContext actionCtx1 = mock(BusinessActionCallContext.class);
        BusinessActionCallContext actionCtx2 = mock(BusinessActionCallContext.class);
        TerminalNode identifierNode1 = mock(TerminalNode.class);
        TerminalNode identifierNode2 = mock(TerminalNode.class);
        
        // Set up the mock to return multiple actions
        List<BusinessRuleParser.BusinessActionContext> actionList = new ArrayList<>();
        actionList.add(actionCtx1);
        actionList.add(actionCtx2);
        when(ctx.businessAction()).thenReturn(actionList);
        
        // Set up the first action context
        when(actionCtx1.IDENTIFIER()).thenReturn(identifierNode1);
        when(identifierNode1.getText()).thenReturn("notify");
        
        // Set up the second action context
        when(actionCtx2.IDENTIFIER()).thenReturn(identifierNode2);
        when(identifierNode2.getText()).thenReturn("log");
        
        // Visit the mock context
        List<RuleFunction<TestKey>> result = visitor.visitBusinessActionList(ctx);
        
        // Verify the result
        assertThat(result)
            .isNotNull()
            .hasSize(2)
            .extracting(RuleFunction::getName)
            .containsExactly("notify", "log");
    }
    
    @Test
    public void testVisitBusinessActionList_WithNumberArguments() {
        // Create mock context for an action with number arguments
        BusinessRuleParser.BusinessActionListContext ctx = mock(BusinessRuleParser.BusinessActionListContext.class);
        BusinessActionCallContext actionCtx = mock(BusinessActionCallContext.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);
        NumberLiteralContext numberLiteralCtx = mock(NumberLiteralContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        TerminalNode numberNode = mock(TerminalNode.class);
        
        // Set up the mock to return a single action
        List<BusinessRuleParser.BusinessActionContext> actionList = new ArrayList<>();
        actionList.add(actionCtx);
        when(ctx.businessAction()).thenReturn(actionList);
        
        // Set up the action context with arguments
        when(actionCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("notify");
        when(actionCtx.arguments()).thenReturn(argumentsCtx);
        
        // Set up the arguments context with a number literal
        List<BusinessRuleParser.LiteralContext> literals = new ArrayList<>();
        literals.add(numberLiteralCtx);
        when(argumentsCtx.literal()).thenReturn(literals);
        when(numberLiteralCtx.NUMBER()).thenReturn(numberNode);
        when(numberNode.getText()).thenReturn("42");
        
        // Visit the mock context
        List<RuleFunction<TestKey>> result = visitor.visitBusinessActionList(ctx);
        
        // Verify the result
        assertThat(result)
            .isNotNull()
            .hasSize(1)
            .extracting(RuleFunction::getName)
            .containsExactly("notify");
    }
    
    @Test
    public void testVisitBusinessActionList_WithStringArguments() {
        // Create mock context for an action with string arguments
        BusinessRuleParser.BusinessActionListContext ctx = mock(BusinessRuleParser.BusinessActionListContext.class);
        BusinessActionCallContext actionCtx = mock(BusinessActionCallContext.class);
        ArgumentsContext argumentsCtx = mock(ArgumentsContext.class);
        StringLiteralContext stringLiteralCtx = mock(StringLiteralContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        TerminalNode stringNode = mock(TerminalNode.class);
        
        // Set up the mock to return a single action
        List<BusinessRuleParser.BusinessActionContext> actionList = new ArrayList<>();
        actionList.add(actionCtx);
        when(ctx.businessAction()).thenReturn(actionList);
        
        // Set up the action context with arguments
        when(actionCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("log");
        when(actionCtx.arguments()).thenReturn(argumentsCtx);
        
        // Set up the arguments context with a string literal
        List<BusinessRuleParser.LiteralContext> literals = new ArrayList<>();
        literals.add(stringLiteralCtx);
        when(argumentsCtx.literal()).thenReturn(literals);
        when(stringLiteralCtx.STRING()).thenReturn(stringNode);
        when(stringNode.getText()).thenReturn("\"test message\"");
        
        // Visit the mock context
        List<RuleFunction<TestKey>> result = visitor.visitBusinessActionList(ctx);
        
        // Verify the result
        assertThat(result)
            .isNotNull()
            .hasSize(1)
            .extracting(RuleFunction::getName)
            .containsExactly("log");
    }
    
    @Test
    public void testVisitBusinessActionList_WithUnknownAction() {
        // Create mock context for an unknown action
        BusinessRuleParser.BusinessActionListContext ctx = mock(BusinessRuleParser.BusinessActionListContext.class);
        BusinessActionCallContext actionCtx = mock(BusinessActionCallContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        
        // Set up the mock to return a single action
        List<BusinessRuleParser.BusinessActionContext> actionList = new ArrayList<>();
        actionList.add(actionCtx);
        when(ctx.businessAction()).thenReturn(actionList);
        
        // Set up the action context with an unknown action name
        when(actionCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("unknown");
        
        // Verify that an RuleParserException is thrown
        assertThatThrownBy(() -> visitor.visitBusinessActionList(ctx))
            .isInstanceOf(RuleParserException.class);
    }

    @Test
    public void testUnknownBusinessAction() {
        // Create mock context for a single action
        BusinessRuleParser.BusinessActionListContext ctx = mock(BusinessRuleParser.BusinessActionListContext.class);
        BusinessActionCallContext actionCtx = mock(BusinessActionCallContext.class);
        TerminalNode identifierNode = mock(TerminalNode.class);
        
        // Set up the mock to return a single action
        List<BusinessRuleParser.BusinessActionContext> actionList = new ArrayList<>();
        actionList.add(actionCtx);
        when(ctx.businessAction()).thenReturn(actionList);
        
        // Set up the action context with an unknown action name
        when(actionCtx.IDENTIFIER()).thenReturn(identifierNode);
        when(identifierNode.getText()).thenReturn("unknown");
        
        // Verify that a RuleParserException is thrown
        assertThatThrownBy(() -> visitor.visitBusinessActionList(ctx))
            .isInstanceOf(RuleParserException.class);
    }
} 