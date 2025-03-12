package com.lyxtera.axiom.api.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a rule set in a YAML file.
 * <p>
 * This class represents a rule set in a YAML file, including its name, description,
 * business checks, business actions, and rules.
 */
public class RuleSetDescriptor {
    
    /**
     * The name of the rule set.
     */
    @JsonProperty("rulesetName")
    private String rulesetName;
    
    /**
     * The description of the rule set.
     */
    @JsonProperty("rulesetDescription")
    private String rulesetDescription;
    
    /**
     * The business checks defined in the rule set.
     */
    @JsonProperty("businessChecks")
    private List<BusinessCheckDescriptor> businessChecks = new ArrayList<>();
    
    /**
     * The business actions defined in the rule set.
     */
    @JsonProperty("businessActions")
    private List<BusinessActionDescriptor> businessActions = new ArrayList<>();
    
    /**
     * The rules defined in the rule set.
     */
    @JsonProperty("rules")
    private List<RuleDescriptor> rules = new ArrayList<>();
    
    /**
     * Getters and setters
     */
    public String getRulesetName() {
        return rulesetName;
    }
    
    public void setRulesetName(String rulesetName) {
        this.rulesetName = rulesetName;
    }
    
    public String getRulesetDescription() {
        return rulesetDescription;
    }
    
    public void setRulesetDescription(String rulesetDescription) {
        this.rulesetDescription = rulesetDescription;
    }
    
    public List<BusinessCheckDescriptor> getBusinessChecks() {
        return businessChecks;
    }
    
    public void setBusinessChecks(List<BusinessCheckDescriptor> businessChecks) {
        this.businessChecks = businessChecks;
    }
    
    public List<BusinessActionDescriptor> getBusinessActions() {
        return businessActions;
    }
    
    public void setBusinessActions(List<BusinessActionDescriptor> businessActions) {
        this.businessActions = businessActions;
    }
    
    public List<RuleDescriptor> getRules() {
        return rules;
    }
    
    public void setRules(List<RuleDescriptor> rules) {
        this.rules = rules;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleSetDescriptor that = (RuleSetDescriptor) o;
        return Objects.equals(rulesetName, that.rulesetName) &&
               Objects.equals(rulesetDescription, that.rulesetDescription) &&
               Objects.equals(businessChecks, that.businessChecks) &&
               Objects.equals(businessActions, that.businessActions) &&
               Objects.equals(rules, that.rules);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rulesetName, rulesetDescription, businessChecks, businessActions, rules);
    }
    
    @Override
    public String toString() {
        return "RuleSetDescriptor{" +
               "rulesetName='" + rulesetName + '\'' +
               ", rulesetDescription='" + rulesetDescription + '\'' +
               ", businessChecks=" + businessChecks +
               ", businessActions=" + businessActions +
               ", rules=" + rules +
               '}';
    }
    
    /**
     * Represents a business check in a YAML rule set.
     */
    public static class BusinessCheckDescriptor {
        
        /**
         * The name of the business check.
         */
        @JsonProperty("name")
        private String name;
        
        /**
         * The description of the business check.
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * The parameters of the business check.
         */
        @JsonProperty("params")
        private List<String> params = new ArrayList<>();
        
        /**
         * Getters and setters
         */
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public List<String> getParams() {
            return params;
        }
        
        public void setParams(List<String> params) {
            this.params = params;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BusinessCheckDescriptor that = (BusinessCheckDescriptor) o;
            return Objects.equals(name, that.name) &&
                   Objects.equals(description, that.description) &&
                   Objects.equals(params, that.params);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, description, params);
        }
        
        @Override
        public String toString() {
            return "BusinessCheckDescriptor{" +
                   "name='" + name + '\'' +
                   ", description='" + description + '\'' +
                   ", params=" + params +
                   '}';
        }
    }
    
    /**
     * Represents a business action in a YAML rule set.
     */
    public static class BusinessActionDescriptor {
        
        /**
         * The name of the business action.
         */
        @JsonProperty("name")
        private String name;
        
        /**
         * The description of the business action.
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * The parameters of the business action.
         */
        @JsonProperty("params")
        private List<String> params = new ArrayList<>();
        
        /**
         * Getters and setters
         */
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public List<String> getParams() {
            return params;
        }
        
        public void setParams(List<String> params) {
            this.params = params;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BusinessActionDescriptor that = (BusinessActionDescriptor) o;
            return Objects.equals(name, that.name) &&
                   Objects.equals(description, that.description) &&
                   Objects.equals(params, that.params);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, description, params);
        }
        
        @Override
        public String toString() {
            return "BusinessActionDescriptor{" +
                   "name='" + name + '\'' +
                   ", description='" + description + '\'' +
                   ", params=" + params +
                   '}';
        }
    }
    
    /**
     * Represents a rule in a YAML rule set.
     */
    public static class RuleDescriptor {
        
        /**
         * The name of the rule.
         */
        @JsonProperty("name")
        private String name;
        
        /**
         * The description of the rule.
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * The expression of the rule.
         */
        @JsonProperty("expression")
        private String expression;
        
        /**
         * The priority of the rule.
         */
        @JsonProperty("priority")
        private int priority;
        
        /**
         * The date and time from which the rule is effective.
         */
        @JsonProperty("effectiveFrom")
        private ZonedDateTime effectiveFrom;
        
        /**
         * The date and time until which the rule is effective.
         * If not set, the rule is effective indefinitely (after effectiveFrom).
         */
        @JsonProperty("effectiveTo")
        private ZonedDateTime effectiveTo;
        
        /**
         * Additional metadata for the rule.
         */
        @JsonProperty("metadata")
        private Map<String, Object> metadata = new HashMap<>();
        
        /**
         * Getters and setters
         */
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getExpression() {
            return expression;
        }
        
        public void setExpression(String expression) {
            this.expression = expression;
        }
        
        public int getPriority() {
            return priority;
        }
        
        public void setPriority(int priority) {
            this.priority = priority;
        }
        
        public ZonedDateTime getEffectiveFrom() {
            return effectiveFrom;
        }
        
        public void setEffectiveFrom(ZonedDateTime effectiveFrom) {
            this.effectiveFrom = effectiveFrom;
        }
        
        public ZonedDateTime getEffectiveTo() {
            return effectiveTo;
        }
        
        public void setEffectiveTo(ZonedDateTime effectiveTo) {
            this.effectiveTo = effectiveTo;
        }
        
        public Map<String, Object> getMetadata() {
            return metadata;
        }
        
        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RuleDescriptor that = (RuleDescriptor) o;
            return priority == that.priority &&
                   Objects.equals(name, that.name) &&
                   Objects.equals(description, that.description) &&
                   Objects.equals(expression, that.expression) &&
                   Objects.equals(effectiveFrom, that.effectiveFrom) &&
                   Objects.equals(effectiveTo, that.effectiveTo) &&
                   Objects.equals(metadata, that.metadata);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, description, expression, priority, effectiveFrom, effectiveTo, metadata);
        }
        
        @Override
        public String toString() {
            return "RuleDescriptor{" +
                   "name='" + name + '\'' +
                   ", description='" + description + '\'' +
                   ", expression='" + expression + '\'' +
                   ", priority=" + priority +
                   ", effectiveFrom=" + effectiveFrom +
                   ", effectiveTo=" + effectiveTo +
                   ", metadata=" + metadata +
                   '}';
        }
    }
} 