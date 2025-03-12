package com.lyxtera.axiom.api.model;

/**
 * Represents a value in a business rule expression.
 * Values can be literals (string, number, boolean)
 */
public class Value {

    public enum Type {
        STRING,
        INTEGER,
        DECIMAL,
        BOOLEAN
    }

    public static final Value EMPTY = new Value(null, Type.STRING);
    
    private final Object value;
    private final Type type;
    
    public Value(Object value, Type type) {
        this.value = value;
        this.type = type;
    }
    
    public Object getValue() {
        return value;
    }

    public Integer asInteger() {
        return (Integer) value  ;
    }

    public Double asDouble() {
        return (Double) value;
    }

    public Boolean asBoolean() {
        return (Boolean) value;
    }

    public String asString() {
        return (String) value;
    }

    public Long asLong() {
        return (Long) value;
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
    public static Value fromObject(Object source) {
        if (source == null) {
            return EMPTY;
        }

        if (source instanceof String) {
            return new Value(source, Type.STRING);
        } else if (source instanceof Integer || source instanceof Long) {
            return new Value(source, Type.INTEGER);
        } else if (source instanceof Float || source instanceof Double) {
            return new Value(source, Type.DECIMAL);
        } else if (source instanceof Boolean) {
            return new Value(source, Type.BOOLEAN);
        } else {
            return new Value(source.toString(), Type.STRING);
        }
    }
    
    @Override
    public String toString() {
        if (type == Type.STRING) {
            return "\"" + value + "\"";
        }
        return String.valueOf(value);
    }
} 