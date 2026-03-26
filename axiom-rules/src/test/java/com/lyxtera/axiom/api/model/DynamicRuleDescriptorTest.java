package com.lyxtera.axiom.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DynamicRuleDescriptorTest {

    @Test
    void constructor_setsNameAndExpression() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("rule1", "x > 5 then doAction()");

        assertThat(descriptor.getName()).isEqualTo("rule1");
        assertThat(descriptor.getExpression()).isEqualTo("x > 5 then doAction()");
        assertThat(descriptor.getPriority()).isNull();
    }

    @Test
    void constructor_setsNameExpressionAndPriority() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("rule1", "x > 5 then doAction()", 10);

        assertThat(descriptor.getName()).isEqualTo("rule1");
        assertThat(descriptor.getExpression()).isEqualTo("x > 5 then doAction()");
        assertThat(descriptor.getPriority()).isEqualTo(10);
    }

    @Test
    void isValid_returnsTrueWhenExpressionIsSet() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("rule1", "x > 5 then doAction()");
        assertThat(descriptor.isValid()).isTrue();
    }

    @Test
    void isValid_returnsFalseForNullExpression() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("rule1", null);
        assertThat(descriptor.isValid()).isFalse();
    }

    @Test
    void isValid_returnsFalseForBlankExpression() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("rule1", "   ");
        assertThat(descriptor.isValid()).isFalse();
    }

    @Test
    void generateName_returnsExistingNameIfSet() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("myRule", "expr");
        assertThat(descriptor.generateName(5)).isEqualTo("myRule");
    }

    @Test
    void generateName_generatesFallbackWhenNameIsNull() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor();
        assertThat(descriptor.generateName(3)).isEqualTo("DynamicRule_3");
    }

    @Test
    void generateName_generatesFallbackWhenNameIsBlank() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("  ", "expr");
        assertThat(descriptor.generateName(1)).isEqualTo("DynamicRule_1");
    }

    @Test
    void isEffectiveAt_returnsTrueForNullTime() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("r", "e");
        assertThat(descriptor.isEffectiveAt(null)).isTrue();
    }

    @Test
    void isEffectiveAt_returnsFalseWhenBeforeEffectiveFrom() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("r", "e");
        ZonedDateTime futureStart = ZonedDateTime.now().plusDays(10);
        descriptor.setEffectiveFrom(futureStart);

        assertThat(descriptor.isEffectiveAt(ZonedDateTime.now())).isFalse();
    }

    @Test
    void isEffectiveAt_returnsFalseWhenAfterEffectiveTo() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("r", "e");
        ZonedDateTime pastEnd = ZonedDateTime.now().minusDays(10);
        descriptor.setEffectiveTo(pastEnd);

        assertThat(descriptor.isEffectiveAt(ZonedDateTime.now())).isFalse();
    }

    @Test
    void isEffectiveAt_returnsTrueWhenWithinWindow() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("r", "e");
        descriptor.setEffectiveFrom(ZonedDateTime.now().minusDays(5));
        descriptor.setEffectiveTo(ZonedDateTime.now().plusDays(5));

        assertThat(descriptor.isEffectiveAt(ZonedDateTime.now())).isTrue();
    }

    @Test
    void toRuleDescriptor_convertsWithPriority() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("rule1", "x > 5 then doAction()", 42);
        descriptor.setDescription("A test rule");

        RuleSetDescriptor.RuleDescriptor result = descriptor.toRuleDescriptor();

        assertThat(result.getName()).isEqualTo("rule1");
        assertThat(result.getExpression()).isEqualTo("x > 5 then doAction()");
        assertThat(result.getPriority()).isEqualTo(42);
        assertThat(result.getDescription()).isEqualTo("A test rule");
    }

    @Test
    void toRuleDescriptor_usesDefaultPriorityWhenNull() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("rule1", "expr");

        RuleSetDescriptor.RuleDescriptor result = descriptor.toRuleDescriptor();

        assertThat(result.getPriority()).isEqualTo(1000);
    }

    @Test
    void setMetadata_withNullCreatesEmptyMap() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor();
        descriptor.setMetadata(null);

        assertThat(descriptor.getMetadata()).isNotNull().isEmpty();
    }

    @Test
    void addMetadata_ignoresNullKey() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor();
        descriptor.addMetadata(null, "value");

        assertThat(descriptor.getMetadata()).isEmpty();
    }

    @Test
    void addMetadata_storesValidEntry() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor();
        descriptor.addMetadata("key", "value");

        assertThat(descriptor.getMetadata()).containsEntry("key", "value");
    }

    @Test
    void equals_andHashCode() {
        DynamicRuleDescriptor d1 = new DynamicRuleDescriptor("rule1", "expr", 10);
        DynamicRuleDescriptor d2 = new DynamicRuleDescriptor("rule1", "expr", 10);

        assertThat(d1).isEqualTo(d2);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }

    @Test
    void equals_returnsFalseForDifferentDescriptor() {
        DynamicRuleDescriptor d1 = new DynamicRuleDescriptor("rule1", "expr", 10);
        DynamicRuleDescriptor d2 = new DynamicRuleDescriptor("rule2", "expr", 10);

        assertThat(d1).isNotEqualTo(d2);
    }

    @Test
    void equals_returnsFalseForNull() {
        DynamicRuleDescriptor d1 = new DynamicRuleDescriptor("rule1", "expr");
        assertThat(d1).isNotEqualTo(null);
    }

    @Test
    void toString_containsFields() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor("myRule", "x > 5 then doAction()", 10);

        String str = descriptor.toString();

        assertThat(str).contains("myRule");
        assertThat(str).contains("x > 5 then doAction()");
        assertThat(str).contains("10");
    }

    @Test
    void settersAndGetters_roundTrip() {
        DynamicRuleDescriptor descriptor = new DynamicRuleDescriptor();
        ZonedDateTime now = ZonedDateTime.now();
        Map<String, Object> meta = new HashMap<>();
        meta.put("k", "v");

        descriptor.setName("n");
        descriptor.setExpression("e");
        descriptor.setPriority(5);
        descriptor.setDescription("d");
        descriptor.setEffectiveFrom(now);
        descriptor.setEffectiveTo(now);
        descriptor.setMetadata(meta);

        assertThat(descriptor.getName()).isEqualTo("n");
        assertThat(descriptor.getExpression()).isEqualTo("e");
        assertThat(descriptor.getPriority()).isEqualTo(5);
        assertThat(descriptor.getDescription()).isEqualTo("d");
        assertThat(descriptor.getEffectiveFrom()).isEqualTo(now);
        assertThat(descriptor.getEffectiveTo()).isEqualTo(now);
        assertThat(descriptor.getMetadata()).containsEntry("k", "v");
    }
}
