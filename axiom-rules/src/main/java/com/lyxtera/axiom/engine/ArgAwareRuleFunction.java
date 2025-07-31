package com.lyxtera.axiom.engine;

import static com.lyxtera.axiom.api.exception.AxiomEngineException.MSG_METHOD_HANDLE_ERROR;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.lyxtera.axiom.api.exception.RuleFunctionException;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.RuleFunction;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessActionDescriptor;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessCheckDescriptor;
import com.lyxtera.axiom.api.model.Value;

/**
 * A decorator for RuleFunction that handles passing arguments to the delegate's
 * execute method.
 * This class decorates a RuleFunction implementation and provides a way to call
 * its execute method
 * with additional arguments.
 *
 * @param <K> The type parameter for the RuleContext
 */
public final class ArgAwareRuleFunction<K extends Enum<K>> implements RuleFunction<K> {

    private static final ConcurrentHashMap<Class<?>, MethodHandle> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final RuleFunction<K> delegate;
    private final List<Value> args;
    private final RuleSet.Metadata metadata;
    private final MethodHandle methodHandle;

    /**
     * Creates a new ArgAwareRuleFunction
     * 
     * @param delegate The delegate function
     * @param args The arguments for the execute method
     * @param metadata The metadata for creating detailed error messages
     */
    private ArgAwareRuleFunction(RuleFunction<K> delegate, List<Value> args, RuleSet.Metadata metadata) {
        this.delegate = requireNonNull(delegate, "Delegate cannot be null");
        this.args = requireNonNull(args, "Args cannot be null");
        this.metadata = requireNonNull(metadata, "Metadata cannot be null");
        requireNonNull(metadata.getRuleSetName(), "Ruleset name cannot be null");

        // Get or create method handle from cache
        this.methodHandle = METHOD_CACHE.computeIfAbsent(delegate.getClass(), target -> {
            try {
                Method method = findExecuteMethod(target);
                return LOOKUP.unreflect(method).bindTo(delegate);
            } catch (IllegalAccessException e) {
                throw RuleFunctionException.of(
                        MSG_METHOD_HANDLE_ERROR, e, getClass().getSimpleName(), target.getName());
            }
        });
    }

    /**
     * Creates a new ArgAwareRuleFunction
     * 
     * @param delegate The delegate function
     * @param args The arguments
     * @param metadata The ruleset metadata
     * @param <K> The context key type
     * @return A new ArgAwareRuleFunction
     */
    public static <K extends Enum<K>> ArgAwareRuleFunction<K> of(RuleFunction<K> delegate, List<Value> args,
            RuleSet.Metadata metadata) {
        return new ArgAwareRuleFunction<>(delegate, args, metadata);
    }

    /**
     * Calls the execute method with the given context and arguments.
     *
     * @param ctx The rule context
     * @return The result of the method invocation
     */
    @Override
    public final Value execute(RuleContext<K> ctx) {
        try {
            // If the number of args doesn't match the expected args count then delegate to the original function
            if (args.isEmpty()) {
                return delegate.execute(ctx);
            }

            Object[] invokeArgs = new Object[args.size() + 1];
            invokeArgs[0] = ctx;

            // Add the arguments in order
            for (int i = 0; i < args.size(); i++) {
                invokeArgs[i + 1] = args.get(i);
            }

            return (Value) methodHandle.invokeWithArguments(invokeArgs);
        } catch (Throwable e) {
            // Create a more detailed error message by trying to use metadata
            String detailedMessage = createDetailedErrorMessage(e);
            throw RuleFunctionException.executionError(detailedMessage, e);
        }
    }

    /**
     * Creates a detailed error message using metadata if available
     * 
     * @param e The original exception
     * @return A detailed error message
     */
    private String createDetailedErrorMessage(Throwable e) {
        StringBuilder message = new StringBuilder();

        message.append(getClass().getSimpleName()).append(": Error invoking execute method for [")
            .append(delegate.getName())
            .append("]");

            message.append("\n\nDetailed information:");

            // Check if this is a BusinessCheck
            if (delegate instanceof BusinessCheck) {
                BusinessCheckDescriptor descriptor = metadata.getBusinessCheckDescriptor(delegate.getName());
                if (descriptor != null) {
                    message.append("\nBusiness Check: ").append(descriptor.getName());
                    message.append("\nDescription: ").append(descriptor.getDescription());
                    message.append("\nExpected Parameters: ").append(descriptor.getParams());
                    message.append("\nProvided Arguments: ")
                            .append(args.stream().map(Value::toString).collect(Collectors.joining(", ")));

                    // Check if we have a parameter count mismatch
                    if (descriptor.getParams().size() != args.size()) {
                        message.append("\n\nPARAMETER COUNT MISMATCH: ");
                        message.append("Expected ").append(descriptor.getParams().size());
                        message.append(" but got ").append(args.size());

                        if (!descriptor.getParams().isEmpty()) {
                            message.append("\nExpected parameter names: ")
                                    .append(join(", ", descriptor.getParams()));
                        }
                    }
                }
            }
            // Check if this is a BusinessAction
            else if (delegate instanceof BusinessAction) {
                BusinessActionDescriptor descriptor = metadata.getBusinessActionDescriptor(delegate.getName());
                if (descriptor != null) {
                    message.append("\nBusiness Action: ").append(descriptor.getName());
                    message.append("\nDescription: ").append(descriptor.getDescription());
                    message.append("\nExpected Parameters: ").append(descriptor.getParams());
                    message.append("\nProvided Arguments: ")
                            .append(args.stream().map(Value::toString).collect(Collectors.joining(", ")));

                    // Check if we have a parameter count mismatch
                    if (descriptor.getParams().size() != args.size()) {
                        message.append("\n\nPARAMETER COUNT MISMATCH: ");
                        message.append("Expected ").append(descriptor.getParams().size());
                        message.append(" but got ").append(args.size());

                        if (!descriptor.getParams().isEmpty()) {
                            message.append("\nExpected parameter names: ")
                                .append(join(", ", descriptor.getParams()));
                    }
                }
            }
        }     

        // Add the original error message
        message.append("\n\nCause: ").append(e.getMessage());
        return message.toString();
    }

    /**
     * Delegates the getName call to the decorated RuleFunction.
     * 
     * @return The name of the decorated RuleFunction
     */
    @Override
    public String getName() {
        return delegate.getName();
    }

    /**
     * Finds the execute method on the given class with the most parameters.
     */
    private Method findExecuteMethod(Class<?> target) {
        Method bestMatch = null;
        StringBuilder availableMethods = new StringBuilder();

        for (Method method : target.getMethods()) {
            if (method.getName().equals("execute")) {
                availableMethods.append("\n  - ").append(method.toString());

                if (method.getParameterCount() == args.size() + 1 &&
                        RuleContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    bestMatch = method;
                }
            }
        }

        if (bestMatch == null) {
            String expectedSignature = format("Value execute(RuleContext<K>%s)",
                args.isEmpty() ? "" : ", " + join(", ", nCopies(args.size(), " @Arg(\"#paramName\") Value arg")));

            // Try to get parameter names from metadata
            List<String> expectedParamNames = getExpectedParameterNames();
            String paramNamesInfo = "";
            if (!expectedParamNames.isEmpty()) {
                paramNamesInfo = format("\nExpected parameter names: %s", join(", ", expectedParamNames));
            }

            throw RuleFunctionException.invalidArgument(format(
                "%s: Method not found: %s\n" +
                "Expected signature: %s%s\n" +
                "Available execute methods on %s:%s\n" +
                "To fix this:\n" +
                "1. Make sure your business check/action class implements the correct execute method\n" +
                "2. Check if the number of arguments in your rule expression matches the method signature\n" +
                "3. Check if all arguments has @Arg() annotation\n" +
                "4. Check if all arguments are of type Value\n" +
                "5. Check if the class is annotated with @RuleMetadata annotation",
                getClass().getSimpleName(),
                target.getName(),
                expectedSignature,
                paramNamesInfo,
                target.getSimpleName(),
                availableMethods));
        }

        return bestMatch;
    }

    /**
     * Gets the expected parameter names for the delegate function
     * @return The expected parameter names
     */
    private List<String> getExpectedParameterNames() {
        if (delegate instanceof BusinessCheck) {
            return metadata.getBusinessCheckParamNames(delegate.getName());
        } else if (delegate instanceof BusinessAction) {
            return metadata.getBusinessActionParamNames(delegate.getName());
        }

        return List.of();
    }
}
