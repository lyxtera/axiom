package com.lyxtera.axiom.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DynamicRuleRequestTest {

    private enum TestKey {
        TEST_KEY
    }

    @Test
    void constructor_setsEntityRulesetAndExpressions() {
        List<String> expressions = List.of("x > 5 then doAction()");
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>("entity1", "ruleset1", expressions);

        assertThat(request.getEntityName()).isEqualTo("entity1");
        assertThat(request.getRulesetName()).isEqualTo("ruleset1");
        assertThat(request.getRuleExpressions()).containsExactly("x > 5 then doAction()");
    }

    @Test
    void constructor_withNullExpressionsCreatesEmptyList() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>("entity1", "ruleset1", null);

        assertThat(request.getRuleExpressions()).isNotNull().isEmpty();
    }

    @Test
    void isValid_returnsTrueForFullyPopulatedRequest() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "entity1", "ruleset1", List.of("x > 5 then doAction()"));

        assertThat(request.isValid()).isTrue();
    }

    @Test
    void isValid_returnsFalseForNullEntity() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            null, "ruleset1", List.of("expr"));

        assertThat(request.isValid()).isFalse();
    }

    @Test
    void isValid_returnsFalseForEmptyEntity() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "  ", "ruleset1", List.of("expr"));

        assertThat(request.isValid()).isFalse();
    }

    @Test
    void isValid_returnsFalseForNullRuleset() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "entity1", null, List.of("expr"));

        assertThat(request.isValid()).isFalse();
    }

    @Test
    void isValid_returnsFalseForEmptyRuleset() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "entity1", "  ", List.of("expr"));

        assertThat(request.isValid()).isFalse();
    }

    @Test
    void isValid_returnsFalseForEmptyExpressions() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "entity1", "ruleset1", List.of());

        assertThat(request.isValid()).isFalse();
    }

    @Test
    void isValid_returnsFalseWhenExpressionsContainBlankEntry() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "entity1", "ruleset1", Arrays.asList("valid", "  "));

        assertThat(request.isValid()).isFalse();
    }

    @Test
    void addRuleExpression_ignoresNull() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>();
        request.addRuleExpression(null);

        assertThat(request.getRuleExpressions()).isEmpty();
    }

    @Test
    void addRuleExpression_ignoresBlank() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>();
        request.addRuleExpression("   ");

        assertThat(request.getRuleExpressions()).isEmpty();
    }

    @Test
    void addRuleExpression_addsValidExpression() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>();
        request.addRuleExpression("x > 5 then doAction()");

        assertThat(request.getRuleExpressions()).containsExactly("x > 5 then doAction()");
    }

    @Test
    void addContextEntry_ignoresNullKey() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>();
        request.addContextEntry(null, "value");

        assertThat(request.getAdditionalContext()).isEmpty();
    }

    @Test
    void addContextEntry_storesValidEntry() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>();
        request.addContextEntry("key", "value");

        assertThat(request.getAdditionalContext()).containsEntry("key", "value");
    }

    @Test
    void setAdditionalContext_withNullCreatesEmptyMap() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>();
        request.setAdditionalContext(null);

        assertThat(request.getAdditionalContext()).isNotNull().isEmpty();
    }

    @Test
    void getRuleCount_returnsCorrectCount() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>(
            "entity1", "ruleset1", List.of("expr1", "expr2", "expr3"));

        assertThat(request.getRuleCount()).isEqualTo(3);
    }

    @Test
    void equals_andHashCode() {
        DynamicRuleRequest<TestKey> r1 = new DynamicRuleRequest<>("e", "rs", List.of("x"));
        DynamicRuleRequest<TestKey> r2 = new DynamicRuleRequest<>("e", "rs", List.of("x"));

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void equals_returnsFalseForDifferentRequest() {
        DynamicRuleRequest<TestKey> r1 = new DynamicRuleRequest<>("e1", "rs", List.of("x"));
        DynamicRuleRequest<TestKey> r2 = new DynamicRuleRequest<>("e2", "rs", List.of("x"));

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void equals_returnsFalseForNull() {
        DynamicRuleRequest<TestKey> r1 = new DynamicRuleRequest<>("e", "rs", List.of("x"));
        assertThat(r1).isNotEqualTo(null);
    }

    @Test
    void toString_containsFields() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>("entity1", "ruleset1", List.of("expr1"));

        String str = request.toString();

        assertThat(str).contains("entity1");
        assertThat(str).contains("ruleset1");
        assertThat(str).contains("expr1");
    }

    @Test
    void settersAndGetters_roundTrip() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>();
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("ctxKey", "ctxVal");

        request.setEntityName("ent");
        request.setRulesetName("rs");
        request.setRuleExpressions(List.of("e1", "e2"));
        request.setAdditionalContext(ctx);
        request.setDefaultPriority(100);
        request.setExecuteBeforeStaticRules(true);

        assertThat(request.getEntityName()).isEqualTo("ent");
        assertThat(request.getRulesetName()).isEqualTo("rs");
        assertThat(request.getRuleExpressions()).containsExactly("e1", "e2");
        assertThat(request.getAdditionalContext()).containsEntry("ctxKey", "ctxVal");
        assertThat(request.getDefaultPriority()).isEqualTo(100);
        assertThat(request.isExecuteBeforeStaticRules()).isTrue();
    }

    @Test
    void setRuleExpressions_withNullCreatesEmptyList() {
        DynamicRuleRequest<TestKey> request = new DynamicRuleRequest<>();
        request.setRuleExpressions(null);

        assertThat(request.getRuleExpressions()).isNotNull().isEmpty();
    }
}
