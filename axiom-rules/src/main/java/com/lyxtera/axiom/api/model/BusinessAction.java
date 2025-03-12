package com.lyxtera.axiom.api.model;

/**
 * Marker interface for business actions.
 * Represents an action to be executed when a rule condition is met.
 */
public interface BusinessAction<K extends Enum<K>> extends RuleFunction<K> {

} 