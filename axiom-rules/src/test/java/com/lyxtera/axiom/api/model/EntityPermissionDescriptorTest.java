package com.lyxtera.axiom.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for EntityPermissionDescriptor.
 */
public class EntityPermissionDescriptorTest {
    
    @Test
    public void testDefaultConstructor() {
        EntityPermissionDescriptor descriptor = new EntityPermissionDescriptor();
        
        assertThat(descriptor.getName()).isNull();
        assertThat(descriptor.getAllowedFunctions()).isEmpty();
        assertThat(descriptor.getDeniedFunctions()).isEmpty();
    }
    
    @Test
    public void testConstructorWithName() {
        EntityPermissionDescriptor descriptor = new EntityPermissionDescriptor("testService");
        
        assertThat(descriptor.getName()).isEqualTo("testService");
        assertThat(descriptor.getAllowedFunctions()).isEmpty();
        assertThat(descriptor.getDeniedFunctions()).isEmpty();
    }
    
    @Test
    public void testConstructorWithAllParameters() {
        List<String> allowed = Arrays.asList("function1", "function2");
        List<String> denied = Arrays.asList("function3");
        
        EntityPermissionDescriptor descriptor = new EntityPermissionDescriptor("testService", allowed, denied);
        
        assertThat(descriptor.getName()).isEqualTo("testService");
        assertThat(descriptor.getAllowedFunctions()).containsExactlyInAnyOrder("function1", "function2");
        assertThat(descriptor.getDeniedFunctions()).containsExactly("function3");
    }
    
    @Test
    public void testIsFunctionAllowed() {
        EntityPermissionDescriptor descriptor = new EntityPermissionDescriptor();
        descriptor.setName("testService");
        descriptor.setAllowedFunctions(Arrays.asList("allowedFunction", "anotherAllowed"));
        descriptor.setDeniedFunctions(Arrays.asList("deniedFunction"));
        
        // Function in allowed list
        assertThat(descriptor.isFunctionAllowed("allowedFunction")).isTrue();
        assertThat(descriptor.isFunctionAllowed("anotherAllowed")).isTrue();
        
        // Function not in allowed list
        assertThat(descriptor.isFunctionAllowed("notListed")).isFalse();
        
        // Function in denied list takes precedence
        descriptor.addAllowedFunction("deniedFunction"); // Add to both lists
        assertThat(descriptor.isFunctionAllowed("deniedFunction")).isFalse();
        
        // Null function name
        assertThat(descriptor.isFunctionAllowed(null)).isFalse();
    }
    
    @Test
    public void testAddAndRemoveFunctions() {
        EntityPermissionDescriptor descriptor = new EntityPermissionDescriptor("testService");
        
        // Test adding allowed functions
        descriptor.addAllowedFunction("function1");
        descriptor.addAllowedFunction("function2");
        descriptor.addAllowedFunction("function1"); // Duplicate should not be added
        descriptor.addAllowedFunction(null); // Null should be ignored
        
        assertThat(descriptor.getAllowedFunctions()).containsExactlyInAnyOrder("function1", "function2");
        
        // Test adding denied functions
        descriptor.addDeniedFunction("function3");
        descriptor.addDeniedFunction("function4");
        descriptor.addDeniedFunction("function3"); // Duplicate should not be added
        descriptor.addDeniedFunction(null); // Null should be ignored
        
        assertThat(descriptor.getDeniedFunctions()).containsExactlyInAnyOrder("function3", "function4");
        
        // Test removing functions
        descriptor.removeAllowedFunction("function1");
        assertThat(descriptor.getAllowedFunctions()).containsExactly("function2");
        
        descriptor.removeDeniedFunction("function3");
        assertThat(descriptor.getDeniedFunctions()).containsExactly("function4");
        
        // Removing non-existent function should not cause error
        descriptor.removeAllowedFunction("nonExistent");
        descriptor.removeDeniedFunction("nonExistent");
    }
    
    @Test
    public void testSettersWithNullLists() {
        EntityPermissionDescriptor descriptor = new EntityPermissionDescriptor("testService");
        
        // Setting null lists should create empty lists
        descriptor.setAllowedFunctions(null);
        descriptor.setDeniedFunctions(null);
        
        assertThat(descriptor.getAllowedFunctions()).isEmpty();
        assertThat(descriptor.getDeniedFunctions()).isEmpty();
    }
    
    @Test
    public void testEqualsAndHashCode() {
        EntityPermissionDescriptor descriptor1 = new EntityPermissionDescriptor("testService",
            Arrays.asList("func1", "func2"), Arrays.asList("func3"));
        
        EntityPermissionDescriptor descriptor2 = new EntityPermissionDescriptor("testService",
            Arrays.asList("func1", "func2"), Arrays.asList("func3"));
        
        EntityPermissionDescriptor descriptor3 = new EntityPermissionDescriptor("otherService",
            Arrays.asList("func1", "func2"), Arrays.asList("func3"));
        
        // Test equals
        assertThat(descriptor1).isEqualTo(descriptor2);
        assertThat(descriptor1).isNotEqualTo(descriptor3);
        assertThat(descriptor1).isNotEqualTo(null);
        assertThat(descriptor1).isNotEqualTo("not a descriptor");
        
        // Test hashCode
        assertThat(descriptor1.hashCode()).isEqualTo(descriptor2.hashCode());
        // Note: Different objects may have same hash code, but equal objects must have same hash code
    }
    
    @Test
    public void testToString() {
        EntityPermissionDescriptor descriptor = new EntityPermissionDescriptor("testService",
            Arrays.asList("func1", "func2"), Arrays.asList("func3"));
        
        String toString = descriptor.toString();
        
        assertThat(toString).contains("testService");
        assertThat(toString).contains("func1");
        assertThat(toString).contains("func2");
        assertThat(toString).contains("func3");
        assertThat(toString).contains("EntityPermissionDescriptor");
    }
}

