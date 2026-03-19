package com.lyxtera.axiom.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents entity permission configuration for dynamic rule execution.
 * <p>
 * This class defines which business functions (checks and actions) a specific
 * entity is allowed or denied to use in dynamic rules. This provides security
 * and access control for the dynamic rule execution feature.
 */
public class EntityPermissionDescriptor {
    
    /**
     * The name of the entity (e.g., "checkoutService", "orderService").
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * List of function names that this entity is explicitly allowed to use.
     * If empty, entity has no explicit permissions (will be denied by default).
     */
    @JsonProperty("allowedFunctions")
    private List<String> allowedFunctions = new ArrayList<>();
    
    /**
     * List of function names that this entity is explicitly denied to use.
     * Denied functions take precedence over allowed functions.
     */
    @JsonProperty("deniedFunctions")
    private List<String> deniedFunctions = new ArrayList<>();
    
    /**
     * Default constructor for Jackson deserialization.
     */
    public EntityPermissionDescriptor() {
    }
    
    /**
     * Creates a new EntityPermissionDescriptor with the specified name.
     *
     * @param name The name of the entity
     */
    public EntityPermissionDescriptor(String name) {
        this.name = name;
    }
    
    /**
     * Creates a new EntityPermissionDescriptor with all parameters.
     *
     * @param name The name of the entity
     * @param allowedFunctions List of allowed function names
     * @param deniedFunctions List of denied function names
     */
    public EntityPermissionDescriptor(String name, List<String> allowedFunctions, List<String> deniedFunctions) {
        this.name = name;
        this.allowedFunctions = allowedFunctions != null ? new ArrayList<>(allowedFunctions) : new ArrayList<>();
        this.deniedFunctions = deniedFunctions != null ? new ArrayList<>(deniedFunctions) : new ArrayList<>();
    }
    
    /**
     * Gets the name of the entity.
     *
     * @return The entity name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the entity.
     *
     * @param name The entity name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the list of allowed function names.
     *
     * @return The list of allowed functions
     */
    public List<String> getAllowedFunctions() {
        return allowedFunctions;
    }
    
    /**
     * Sets the list of allowed function names.
     *
     * @param allowedFunctions The list of allowed functions
     */
    public void setAllowedFunctions(List<String> allowedFunctions) {
        this.allowedFunctions = allowedFunctions != null ? new ArrayList<>(allowedFunctions) : new ArrayList<>();
    }
    
    /**
     * Gets the list of denied function names.
     *
     * @return The list of denied functions
     */
    public List<String> getDeniedFunctions() {
        return deniedFunctions;
    }
    
    /**
     * Sets the list of denied function names.
     *
     * @param deniedFunctions The list of denied functions
     */
    public void setDeniedFunctions(List<String> deniedFunctions) {
        this.deniedFunctions = deniedFunctions != null ? new ArrayList<>(deniedFunctions) : new ArrayList<>();
    }
    
    /**
     * Checks if the entity is allowed to use the specified function.
     * <p>
     * A function is allowed if:
     * - It is in the allowedFunctions list AND
     * - It is NOT in the deniedFunctions list
     * <p>
     * Denied functions take precedence over allowed functions.
     *
     * @param functionName The name of the function to check
     * @return true if the function is allowed, false otherwise
     */
    public boolean isFunctionAllowed(String functionName) {
        if (functionName == null) {
            return false;
        }
        
        // Denied functions take precedence
        if (deniedFunctions.contains(functionName)) {
            return false;
        }
        
        // Must be explicitly allowed
        return allowedFunctions.contains(functionName);
    }
    
    /**
     * Adds a function to the allowed list.
     *
     * @param functionName The function name to allow
     */
    public void addAllowedFunction(String functionName) {
        if (functionName != null && !allowedFunctions.contains(functionName)) {
            allowedFunctions.add(functionName);
        }
    }
    
    /**
     * Adds a function to the denied list.
     *
     * @param functionName The function name to deny
     */
    public void addDeniedFunction(String functionName) {
        if (functionName != null && !deniedFunctions.contains(functionName)) {
            deniedFunctions.add(functionName);
        }
    }
    
    /**
     * Removes a function from the allowed list.
     *
     * @param functionName The function name to remove
     */
    public void removeAllowedFunction(String functionName) {
        allowedFunctions.remove(functionName);
    }
    
    /**
     * Removes a function from the denied list.
     *
     * @param functionName The function name to remove
     */
    public void removeDeniedFunction(String functionName) {
        deniedFunctions.remove(functionName);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityPermissionDescriptor that = (EntityPermissionDescriptor) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(allowedFunctions, that.allowedFunctions) &&
               Objects.equals(deniedFunctions, that.deniedFunctions);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, allowedFunctions, deniedFunctions);
    }
    
    @Override
    public String toString() {
        return "EntityPermissionDescriptor{" +
               "name='" + name + '\'' +
               ", allowedFunctions=" + allowedFunctions +
               ", deniedFunctions=" + deniedFunctions +
               '}';
    }
}

