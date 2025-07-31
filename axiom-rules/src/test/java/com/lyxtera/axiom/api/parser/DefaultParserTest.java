package com.lyxtera.axiom.api.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.lyxtera.axiom.api.exception.RuleParserException;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.BusinessRule;
import com.lyxtera.axiom.api.model.Value;
import com.lyxtera.axiom.engine.RuleSet;

@DisplayName("DefaultParser")
class DefaultParserTest {

    public enum TestKey {
        AGE,
        IS_PREMIUM,
        NAME
    }

    private DefaultParser<TestKey> parser;
    private Map<String, BusinessCheck<TestKey>> businessChecks;
    private Map<String, BusinessAction<TestKey>> businessActions;
    private RuleSet.Metadata metadata;

    @BeforeEach
    void setUp() {
        // Reset everything between tests to avoid state leakage
        businessChecks = new HashMap<>();
        businessActions = new HashMap<>();
        metadata = mock(RuleSet.Metadata.class);
        when(metadata.getRuleSetName()).thenReturn("test-ruleset");
        
        // Register default action for tests
        BusinessAction<TestKey> defaultAction = mock(BusinessAction.class);
        businessActions.put("doAction", defaultAction);
        
        // Create a fresh parser for each test
        parser = new DefaultParser<>(businessChecks, businessActions);
    }

    @Nested
    @DisplayName("when parsing simple conditions")
    class SimpleConditions {

        @BeforeEach
        void setUp() {
            BusinessCheck<TestKey> isPremiumCheck = mock(BusinessCheck.class);
            BusinessCheck<TestKey> getAgeCheck = mock(BusinessCheck.class);
            BusinessCheck<TestKey> getNameCheck = mock(BusinessCheck.class);
            when(isPremiumCheck.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            when(getAgeCheck.execute(any())).thenReturn(new Value("25", Value.Type.NUMBER));
            when(getNameCheck.execute(any())).thenReturn(new Value("John", Value.Type.STRING));
            businessChecks.put("isPremium", isPremiumCheck);
            businessChecks.put("getAge", getAgeCheck);
            businessChecks.put("getName", getNameCheck);
        }

        @Test
        @DisplayName("should parse boolean check")
        void shouldParseBooleanCheck() {
            // given
            BusinessCheck<TestKey> isPremiumCheck = mock(BusinessCheck.class);
            when(isPremiumCheck.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("isPremium", isPremiumCheck);

            // when
            BusinessRule<TestKey> rule = parser.parseRule(metadata, "test-rule", "isPremium() then doAction()");

            // then
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isEqualTo("test-rule");
        }

        @Test
        @DisplayName("should parse comparison with number")
        void shouldParseComparisonWithNumber() {
            // given
            BusinessCheck<TestKey> getAgeCheck = mock(BusinessCheck.class);
            when(getAgeCheck.execute(any())).thenReturn(new Value("25", Value.Type.NUMBER));
            businessChecks.put("getAge", getAgeCheck);

            // when
            BusinessRule<TestKey> rule = parser.parseRule(metadata, "test-rule", "getAge() > 18 then doAction()");

            // then
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isEqualTo("test-rule");
        }

        @Test
        @DisplayName("should parse comparison with string")
        void shouldParseComparisonWithString() {
            // given
            BusinessCheck<TestKey> getNameCheck = mock(BusinessCheck.class);
            when(getNameCheck.execute(any())).thenReturn(new Value("John", Value.Type.STRING));
            businessChecks.put("getName", getNameCheck);

            // when
            BusinessRule<TestKey> rule = parser.parseRule(metadata, "test-rule", "getName() = \"John\" then doAction()");

            // then
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isEqualTo("test-rule");
        }
    }

    @Nested
    @DisplayName("when parsing complex conditions")
    class ComplexConditions {

        @BeforeEach
        void setUp() {
            BusinessCheck<TestKey> check1 = mock(BusinessCheck.class);
            BusinessCheck<TestKey> check2 = mock(BusinessCheck.class);
            BusinessCheck<TestKey> check3 = mock(BusinessCheck.class);
            when(check1.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            when(check2.execute(any())).thenReturn(new Value("false", Value.Type.BOOLEAN));
            when(check3.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("check1", check1);
            businessChecks.put("check2", check2);
            businessChecks.put("check3", check3);
            businessChecks.put("check", check1); // Also add 'check' for other tests
        }

        @Test
        @DisplayName("should parse AND condition")
        void shouldParseAndCondition() {
            // given
            BusinessCheck<TestKey> check1 = mock(BusinessCheck.class);
            BusinessCheck<TestKey> check2 = mock(BusinessCheck.class);
            when(check1.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            when(check2.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("check1", check1);
            businessChecks.put("check2", check2);

            // when
            BusinessRule<TestKey> rule = parser.parseRule(metadata, "test-rule", "check1() and check2() then doAction()");

            // then
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isEqualTo("test-rule");
        }

        @Test
        @DisplayName("should parse OR condition")
        void shouldParseOrCondition() {
            // given
            BusinessCheck<TestKey> check1 = mock(BusinessCheck.class);
            BusinessCheck<TestKey> check2 = mock(BusinessCheck.class);
            when(check1.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            when(check2.execute(any())).thenReturn(new Value("false", Value.Type.BOOLEAN));
            businessChecks.put("check1", check1);
            businessChecks.put("check2", check2);

            // when
            BusinessRule<TestKey> rule = parser.parseRule(metadata, "test-rule", "check1() or check2() then doAction()");

            // then
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isEqualTo("test-rule");
        }

        @Test
        @DisplayName("should parse NOT condition")
        void shouldParseNotCondition() {
            // given
            BusinessCheck<TestKey> check = mock(BusinessCheck.class);
            when(check.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("check", check);

            // when
            BusinessRule<TestKey> rule = parser.parseRule(metadata, "test-rule", "not check() then doAction()");

            // then
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isEqualTo("test-rule");
        }

        @Test
        @DisplayName("should parse nested conditions")
        void shouldParseNestedConditions() {
            // given
            BusinessCheck<TestKey> check1 = mock(BusinessCheck.class);
            BusinessCheck<TestKey> check2 = mock(BusinessCheck.class);
            BusinessCheck<TestKey> check3 = mock(BusinessCheck.class);
            when(check1.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            when(check2.execute(any())).thenReturn(new Value("false", Value.Type.BOOLEAN));
            when(check3.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("check1", check1);
            businessChecks.put("check2", check2);
            businessChecks.put("check3", check3);

            // when
            BusinessRule<TestKey> rule = parser.parseRule(metadata, "test-rule", 
                "check1() and (check2() or check3()) then doAction()");

            // then
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isEqualTo("test-rule");
        }
    }

    @Nested
    @DisplayName("when parsing actions")
    class Actions {

        @BeforeEach
        void setUp() {
            BusinessCheck<TestKey> check = mock(BusinessCheck.class);
            BusinessAction<TestKey> action1 = mock(BusinessAction.class);
            BusinessAction<TestKey> action2 = mock(BusinessAction.class);
            when(check.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("check", check);
            businessActions.put("action1", action1);
            businessActions.put("action2", action2);
        }

        @Test
        @DisplayName("should parse action without arguments")
        void shouldParseActionWithoutArguments() {
            // given
            BusinessCheck<TestKey> check = mock(BusinessCheck.class);
            BusinessAction<TestKey> action = mock(BusinessAction.class);
            when(check.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("check", check);
            businessActions.put("doAction", action);

            // when
            BusinessRule<TestKey> rule = parser.parseRule(metadata, "test-rule", "check() then doAction()");

            // then
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isEqualTo("test-rule");
        }

        @Test
        @DisplayName("should parse action with arguments")
        void shouldParseActionWithArguments() {
            // given
            BusinessCheck<TestKey> check = mock(BusinessCheck.class);
            BusinessAction<TestKey> action = mock(BusinessAction.class);
            when(check.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("check", check);
            businessActions.put("doAction", action);

            // when
            BusinessRule<TestKey> rule = parser.parseRule(metadata, "test-rule", 
                "check() then doAction(\"param1\", 42)");

            // then
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isEqualTo("test-rule");
        }

        @Test
        @DisplayName("should parse multiple actions")
        void shouldParseMultipleActions() {
            // given
            BusinessCheck<TestKey> check = mock(BusinessCheck.class);
            BusinessAction<TestKey> action1 = mock(BusinessAction.class);
            BusinessAction<TestKey> action2 = mock(BusinessAction.class);
            when(check.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("check", check);
            businessActions.put("action1", action1);
            businessActions.put("action2", action2);

            // when
            BusinessRule<TestKey> rule = parser.parseRule(metadata, "test-rule", 
                "check() then action1(); action2()");

            // then
            assertThat(rule).isNotNull();
            assertThat(rule.getName()).isEqualTo("test-rule");
        }
    }

    @Nested
    @DisplayName("when handling errors")
    class ErrorHandling {

        @Test
        void shouldThrowExceptionForUnknownCheck() {
            // when & then
            assertThatThrownBy(() -> parser.parseRule(metadata, "test-rule", "unknownCheck() then doAction()"))
                    .isInstanceOf(RuleParserException.class)
                    .hasMessageContaining("Unknown business check: unknownCheck");
        }

        @Test
        void shouldThrowExceptionForUnknownAction() {
            // given
            BusinessCheck<TestKey> check = mock(BusinessCheck.class);
            when(check.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("check", check);

            // when & then
            assertThatThrownBy(() -> parser.parseRule(metadata, "test-rule", "check() then unknownAction()"))
                    .isInstanceOf(RuleParserException.class)
                    .hasMessageContaining("Unknown business action: unknownAction");
        }

        @Test
        void shouldThrowExceptionForInvalidSyntax() {
            // given
            BusinessCheck<TestKey> check = mock(BusinessCheck.class);
            when(check.execute(any())).thenReturn(new Value("true", Value.Type.BOOLEAN));
            businessChecks.put("check", check);

            // when & then
            // Use a syntax with open/close parenthesis mismatch
            String invalidSyntax = "check() and ((check() then doAction()";
            assertThatThrownBy(() -> parser.parseRule(metadata, "test-rule", invalidSyntax))
                    .isInstanceOf(RuleParserException.class)
                    .hasMessageContaining("Syntax error in rule");
        }

        @Test
        void shouldThrowExceptionForInvalidOperator() {
            // given
            BusinessCheck<TestKey> check = mock(BusinessCheck.class);
            when(check.execute(any())).thenReturn(new Value("test", Value.Type.STRING));
            businessChecks.put("check", check);

            // when & then
            // Use a comparison operator that doesn't exist in grammar but doesn't break the lexer
            assertThatThrownBy(() -> parser.parseRule(metadata, "test-rule", "check() % \"value\" then doAction()"))
                    .isInstanceOf(RuleParserException.class)
                    .hasMessageContaining("mismatched input '%'");
        }
    }
} 