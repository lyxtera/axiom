package com.lyxtera.axiom.api.model;

import static com.lyxtera.axiom.api.exception.AxiomEngineException.MSG_UNSUPPORTED_VALUE_TYPE;

import java.math.BigDecimal;
import java.util.Objects;

import com.lyxtera.axiom.api.exception.AxiomEngineException;

/**
 * Represents a value in a business rule expression.
 * Values can be literals (string, number, boolean)
 */
public class Value {

    public enum Type {
        STRING,
        NUMBER,
        BOOLEAN
    }

    public static final Value EMPTY = new Value(null, Type.STRING);
    
    private final String value;
    private final Type type;
    
    public Value(String value, Type type) {
        this.value = value;
        this.type = type;
    }
    
    public Object getValue() {
        switch (type) {
            case STRING:
                return asString();
            case NUMBER:
                return asNumber();
            case BOOLEAN:
                return asBoolean(); 
            default:
                return value;
        }
    }

    public BigDecimal asNumber() {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(value);
    }

    public Boolean asBoolean() {
        if (value == null) {
            return false;
        }

        return Boolean.valueOf(value);
    }

    public String asString() {
        return Objects.toString(value);
    }
    
    public Type getType() {
        return type;
    }
    
    /**
     * Creates a Value from any object by determining the appropriate type based on the object's class.
     * 
     * @param source The object to convert to a Value
     * @return A new Value instance with the appropriate type for the given object
     */
    public static Value of(Object source) {
        if (source == null) {
            return EMPTY;
        }

        if (source instanceof String) {
            return new Value(source.toString(), Type.STRING);
        } else if (source instanceof Boolean) {
            return new Value(source.toString(), Type.BOOLEAN);
        } else if (source instanceof Number) {
            return new Value(source.toString(), Type.NUMBER);
        } else {
            throw AxiomEngineException.of(MSG_UNSUPPORTED_VALUE_TYPE, source.getClass().getName(), source);
        }
    }
    
    @Override
    public String toString() {
        if (type == Type.STRING) {
            return "\"" + value + "\"";
        }
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (that == null || !(that instanceof Value)) {
            return false;
        }

        Value other = (Value) that;
        return Objects.equals(getValue(), other.getValue()) && getType() == other.getType();
    }
    
} 