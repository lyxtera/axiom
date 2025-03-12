package com.lyxtera.axiom.engine;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT;
import static com.lyxtera.axiom.api.exception.AxiomEngineException.MSG_INVALID_ENUM_CONVERSION;
import static com.lyxtera.axiom.api.exception.AxiomEngineException.MSG_NO_VALUE_FOR_KEY;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.annotation.concurrent.ThreadSafe;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lyxtera.axiom.api.exception.ContextException;
import com.lyxtera.axiom.api.model.Value;

/**
 * A thread-safe generic context that uses enum keys to store and retrieve typed values.
 * This context is designed to be used with any enum type as keys, allowing consumers
 * to define their own enum for storing context values.
 *
 * Example usage:
 * {@code
 *     enum MyContextKey { USERNAME, AGE, LAST_LOGIN }
 *     Context<MyContextKey> context = new Context<>();
 *     context.add(MyContextKey.USERNAME, "john_doe");
 *     context.add(MyContextKey.AGE, 25);
 *     
 *     String username = context.getRequired(MyContextKey.USERNAME, String.class);
 *     Optional<Integer> age = context.get(MyContextKey.AGE, Integer.class);
 * }
 *
 * @param <K> The enum type to be used as keys in this context
 */
@ThreadSafe
public class RuleContext<K extends Enum<K>> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setSerializationInclusion(Include.NON_DEFAULT)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule())
        .activateDefaultTyping(BasicPolymorphicTypeValidator.builder().build(), JAVA_LANG_OBJECT);
    
    private final Function<Key, String> jsonKeyExtractor = key -> ofNullable(key.variableName)
        .map(Object::toString)
        .orElse("NULL::" + key.valueType.getSimpleName());

    private final Map<Key, Optional<?>> data;
    private final Class<K> keyType;

    /**
     * Creates a new empty Context instance.
     */
    public RuleContext(Class<K> keyType) {
        data = new ConcurrentHashMap<>();
        this.keyType = keyType; 
    }

    /** 
     * Retrieves the key type of the context.
     *
     * @return The key type of the context
     */
    public Class<K> getKeyType() {
        return keyType;
    }

    /**
     * Retrieves a value from the context with type safety.
     *
     * @param key The enum key to lookup
     * @param type The expected type of the value
     * @param <V> The type parameter for the value
     * @return An Optional containing the value if present and of the correct type
     */
    @SuppressWarnings("unchecked")
    public <V> Optional<V> get(K key, Class<V> type) {
        return (Optional<V>) data.getOrDefault(keyFor(key, type), empty());
    }

    /**
     * Retrieves a required value from the context. Throws an exception if the value is not present.
     *
     * @param key The enum key to lookup
     * @param type The expected type of the value
     * @param <V> The type parameter for the value
     * @return The value associated with the key
     * @throws ContextException if no value is present for the given key
     */
    public <V> V getRequired(K key, Class<V> type) {
        return get(key, type).orElseThrow(() -> ContextException.of(MSG_NO_VALUE_FOR_KEY, keyFor(key, type)));
    }

    /**
     * Adds a value to the context with the specified enum key.
     *
     * @param key The enum key to associate the value with
     * @param value The value to store
     * @param <V> The type parameter for the value
     * @return An Optional containing the stored value
     */
    @SuppressWarnings("unchecked")
    public <V> Optional<V> add(K key, V value) {
        Optional<V> result = ofNullable(value);
        Class<V> valueType = (Class<V>) ofNullable(value).map(Object::getClass).orElse(null);

        result.ifPresent(d -> data.compute(keyFor(requireNonNull(key, "Key may not be null"), valueType), (k, v) -> result));        
        return result;
    }

    /**
     * Removes a value from the context.
     *
     * @param key The enum key of the value to remove
     * @param type The type of the value to remove
     * @param <V> The type parameter for the value
     */
    public <V> void remove(K key, Class<V> type) {
        data.remove(keyFor(key, type));     
    }

    /**
     * Checks if the context is empty.
     *
     * @return true if the context contains no key-value mappings
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Deserializes a context from a JSON string.
     *
     * @param keyType The enum class representing context keys
     * @param json The JSON string to deserialize
     * @param <K> The enum type for context keys
     * @return A new RuleContext initialized with the deserialized data
     * @throws ContextException if deserialization fails
     */
    @SuppressWarnings("unchecked")
    public static <K extends Enum<K>> RuleContext<K> fromJson(Class<K> keyType, String json) {        
        try {
            RuleContext<K> result = new RuleContext<>(keyType);
            
            MAPPER.readValue(json, Map.class).forEach((jsonKey, value) -> {
                K key = toEnumKey(jsonKey.toString(), keyType);
                
                if (key == null) {
                    result.addRaw(value);
                } else {
                    result.add(key, value);
                }
            });
            
            return result;
        } catch (IOException e) {
            throw ContextException.serializationError(e);
        }
    }

    /**
     * Serializes the context to a JSON string.
     *
     * @return A JSON string representation of the context
     * @throws ContextException if serialization fails
     */
    public String toJson() {        
        try {
            Map<Object, Object> rawData = data.entrySet().stream()
                .collect(toMap(e -> jsonKeyExtractor.apply(e.getKey()), e -> e.getValue().get()));
            
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rawData);
        } catch (JsonProcessingException e) {
            throw ContextException.serializationError(e);
        }
    }

    /**
     * Converts a Value object to an enum key of type K.
     * This is useful for extracting enum keys from string values in rule expressions.
     *
     * @param value The Value object containing a string representation of the enum key
     * @return The enum key of type K
     * @throws ContextException if the value cannot be converted to a valid enum key
     */
    public K keyByValue(Value value) {
        if (value == null || value.getValue() == null || value.getType() != Value.Type.STRING) {
            throw ContextException.valueNotString();
        }
        
        return ofNullable(value.getValue())
            .map(Object::toString)
            .flatMap(str -> stream(getKeyType().getEnumConstants())
                .filter(enumConstant -> enumConstant.name().equals(str))
                .findFirst())
            .orElseThrow(() -> ContextException.of(
                MSG_INVALID_ENUM_CONVERSION,
                value.getValue(), getKeyType().getSimpleName()));
    }

    /**
     * Copies all data from another context into this context.
     *
     * @param source The source context to copy from
     */
    void copyDataFrom(RuleContext<K> source) {     
        data.putAll(source.data);    
    }

    private <V> void addRaw(V value) {     
        ofNullable(value).ifPresent(src -> data.compute(keyFor(null, src.getClass()), (k, v) -> of(src)));
    }

    private Key keyFor(K variableName, Class<?> valueType) {
        return new Key(variableName, valueType);
    }

    private static <K extends Enum<K>> K toEnumKey(String source, Class<K> keyType) {
        K[] enumConstants = keyType.getEnumConstants();
        
        for (K key : enumConstants) {
            if (key.name().equals(source)) {
                return key;
            }
        }

        return null;
    }


    @Override
    public String toString() {
        return format("Context [data=%s]", data);
    }

    /**
     * Internal key class that combines an enum value with its associated value type.
     */
    private class Key {          
        private final K variableName;
        private final Class<?> valueType;

        private Key(K variableName, Class<?> valueType) {
            this.variableName = variableName;
            this.valueType = valueType;
        }

        @Override
        public boolean equals(Object that) {
            if (that == this)
                return true;

            if (!(that instanceof RuleContext<?>.Key)) {
                return false;
            }

            RuleContext<?>.Key otherKey = (RuleContext<?>.Key) that;                
            return Objects.equals(variableName, otherKey.variableName) 
                && (valueType == otherKey.valueType || valueType.isAssignableFrom(otherKey.valueType));
        }

        @Override
        public int hashCode() {
            return Objects.hash(variableName);
        }

        @Override
        public String toString() {
            return String.format("Key [variableName=%s, valueType=%s]", variableName, valueType);
        }
    }
}