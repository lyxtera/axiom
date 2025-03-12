package com.lyxtera.axiom.api.model;

import static com.lyxtera.axiom.api.exception.AxiomEngineException.MSG_UNKNOWN_OPERATOR_TYPE;
import static com.lyxtera.axiom.api.exception.AxiomEngineException.TYPE_NUMBERS;
import static com.lyxtera.axiom.api.exception.AxiomEngineException.TYPE_BOOLEANS;

import com.lyxtera.axiom.api.exception.OperatorException;

/**
 * Represents operators used in business rule conditions.
 * <p>
 * This enum defines the operators that can be used in business rule conditions,
 * such as comparison operators (=, &lt;, &gt;) and logical operators (AND, OR).
 * Each operator has a symbol and a method to apply the operation to operands.
 */
public enum Operator {

    /**
     * Equality operator (=).
     * Checks if two values are equal.
     */
    EQUALS("=") {
        /**
         * Applies the equality operator to the given operands.
         * 
         * @param left The left operand
         * @param right The right operand
         * @param <T> The type of the operands
         * @return true if the operands are equal, false otherwise
         */
        @Override
        public <T> boolean apply(T left, T right) {
            if (left == null && right == null) return true;
            if (left == null || right == null) return false;
            return left.equals(right);
        }
    },
    
    /**
     * Greater than operator (&gt;).
     * Checks if the left value is greater than the right value.
     * Only applicable to numeric values.
     */
    GREATER_THAN(">") {
        /**
         * Applies the greater than operator to the given operands.
         * 
         * @param left The left operand
         * @param right The right operand
         * @param <T> The type of the operands
         * @return true if the left operand is greater than the right operand
         * @throws OperatorException if the operands are not numbers
         */
        @Override
        public <T> boolean apply(T left, T right) {
            if (left instanceof Number && right instanceof Number) {
                return ((Number) left).doubleValue() > ((Number) right).doubleValue();
            }
            throw OperatorException.invalidType(getSymbol(), TYPE_NUMBERS);
        }
    },

    /**
     * Less than operator (&lt;).
     * Checks if the left value is less than the right value.
     * Only applicable to numeric values.
     */
    LESS_THAN("<") {
        /**
         * Applies the less than operator to the given operands.
         * 
         * @param left The left operand
         * @param right The right operand
         * @param <T> The type of the operands
         * @return true if the left operand is less than the right operand
         * @throws OperatorException if the operands are not numbers
         */
        @Override
        public <T> boolean apply(T left, T right) {
            if (left instanceof Number && right instanceof Number) {
                return ((Number) left).doubleValue() < ((Number) right).doubleValue();
            }
            throw OperatorException.invalidType(getSymbol(), TYPE_NUMBERS);
        }
    }, 

    /**
     * Logical AND operator.
     * Returns true if both operands are true.
     * Only applicable to boolean values.
     */
    AND("and") {
        /**
         * Applies the logical AND operator to the given operands.
         * 
         * @param left The left operand
         * @param right The right operand
         * @param <T> The type of the operands
         * @return true if both operands are true
         * @throws OperatorException if the operands are not booleans
         */
        @Override
        public <T> boolean apply(T left, T right) {
            if (left instanceof Boolean && right instanceof Boolean) {
                return ((Boolean) left) && ((Boolean) right);
            }
            throw OperatorException.invalidType(getSymbol(), TYPE_BOOLEANS);
        }
    },

    /**
     * Logical OR operator.
     * Returns true if either operand is true.
     * Only applicable to boolean values.
     */
    OR("or") {
        /**
         * Applies the logical OR operator to the given operands.
         * 
         * @param left The left operand
         * @param right The right operand
         * @param <T> The type of the operands
         * @return true if either operand is true
         * @throws OperatorException if the operands are not booleans
         */
        @Override
        public <T> boolean apply(T left, T right) {
            if (left instanceof Boolean && right instanceof Boolean) {
                return ((Boolean) left) || ((Boolean) right);
            }
            throw OperatorException.invalidType(getSymbol(), TYPE_BOOLEANS);
        }
    },
    
    /**
     * Unknown operator.
     * Used as a placeholder for operators that are not recognized.
     */
    UNKNOWN("-U-") {
        /**
         * Throws an exception as this operator cannot be applied.
         * 
         * @param left The left operand
         * @param right The right operand
         * @param <T> The type of the operands
         * @return Never returns
         * @throws OperatorException always
         */
        @Override
        public <T> boolean apply(T left, T right) {
            throw OperatorException.unsupportedOperation(MSG_UNKNOWN_OPERATOR_TYPE);
        }
    };

    private final String symbol;
    
    /**
     * Constructor for Operator.
     *
     * @param symbol The symbol representing this operator
     */
    Operator(String symbol) {
        this.symbol = symbol;
    }
    
    /**
     * Returns the symbol for this operator.
     *
     * @return The symbol
     */
    public String getSymbol() {
        return symbol;
    }
    
    /**
     * Applies this operator to the given operands.
     *
     * @param left The left operand
     * @param right The right operand
     * @param <T> The type of the operands
     * @return The result of applying this operator to the operands
     */
    public abstract <T> boolean apply(T left, T right);
    
    /**
     * Returns the operator corresponding to the given symbol.
     *
     * @param symbol The operator symbol
     * @return The corresponding operator
     * @throws OperatorException if the symbol is not a valid operator
     */
    public static Operator fromSymbol(String symbol) {
        for (Operator operator : values()) {
            if (operator.getSymbol().equals(symbol)) {
                return operator;
            }
        }
        throw OperatorException.unknownOperator(symbol);
    }
} 