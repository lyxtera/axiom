package com.lyxtera.axiom.config;

import static com.google.inject.TypeLiteral.get;
import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.util.Types.newParameterizedType;
import static java.lang.String.format;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.lyxtera.axiom.api.exception.RuleLoadException;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.api.model.RuleFunction;
import com.lyxtera.axiom.api.parser.DefaultParser;
import com.lyxtera.axiom.api.parser.Parser;
import com.lyxtera.axiom.engine.CtxGet;
import com.lyxtera.axiom.engine.RuleOrchestrator;
import com.lyxtera.axiom.engine.RuleSetLoader;

/**
 * Base Guice module for Axiom configuration. This abstract module defines
 * core components of the rule engine and requires concrete implementations
 * to provide the necessary business checks and actions.
 * 
 * <p>This module also handles loading rule sets and provides named RuleOrchestrator
 * instances for each rule set.</p>
 */
public abstract class AxiomModule<K extends Enum<K>> extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(AxiomModule.class);

    private final TypeLiteral<K> keyType;
    
        /**
         * @param ruleSetPaths 
         * @param ruleSetPaths A map of rule set names to their resource paths
         */
        protected AxiomModule(Class<K> ctxClass) {
            this.keyType = get(ctxClass);
    }

    /**
     * Creates a new builder for the specified context key class.
     *
     * @param contextKeyClass The enum class used for context keys
     * @return A new builder instance
     */
    public static <K extends Enum<K>> AxiomModuleBuilder<K> buildForKey(Class<K> contextKeyClass) {
        return new AxiomModuleBuilder<>(contextKeyClass);
    }

    /**
     * Implement this method to register custom business checks and actions.
     *
     * @param checks The MapBinder for registering BusinessCheck implementations
     * @param actions The MapBinder for registering BusinessAction implementations
     */
    protected abstract void configureBusinessRules(MapBinder<String, BusinessCheck<K>> checks,
            MapBinder<String, BusinessAction<K>> actions);

    protected abstract Map<String, RuleSetLoader<K>> getRegisteredLoaders();
    
    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        //Ensure that all dependencies are not discovered via JIT (Just-In-Time) bindings to trigger validation
        binder().requireExplicitBindings();

        // Create MapBinder instances for checks and actions
        var checks = newMapBinder(binder(), get(String.class), this.<BusinessCheck<K>>asGeneric(BusinessCheck.class));
        var actions = newMapBinder(binder(), get(String.class), this.<BusinessAction<K>>asGeneric(BusinessAction.class));
       
        // Configure custom checks and actions
        configureBusinessRules(checks, actions);

        bind(asGeneric(Parser.class)).to(asGeneric(DefaultParser.class));

        // Register built-in context access function
        checks.addBinding("ctxGet").toProvider(() -> new CtxGet<K>());

        // Bind the rule orchestrator with a named annotation
        var parser = (Provider<Parser<K>>) getProvider(Key.get(asGeneric(Parser.class).getType()));

        for (Entry<String, RuleSetLoader<K>> entry : getRegisteredLoaders().entrySet()) {
            RuleSetLoader<K> loader = entry.getValue();
          
            // Bind the rule orchestrator with a named annotation
            bind(asGeneric(RuleOrchestrator.class))
                .annotatedWith(Names.named(entry.getKey()))
                .toProvider(() -> new RuleOrchestrator<>(loader.loadRuleSet(parser.get())));
        }
    }

    @Provides
    @Singleton
    @Named("axiom-module-validation-status")
    boolean provideRuleSets() {
       // validateRuleMetadata(checks, "BusinessCheck");
       // validateRuleMetadata(actions, "BusinessAction");

        return true;
    }

    @SuppressWarnings("unchecked")
    private <R> TypeLiteral<R> asGeneric(Class<?> base) {
        return (TypeLiteral<R>) get(newParameterizedType(base, keyType.getType()));
    }
    
    /**
     * Validates that all rule functions have the required valid @RuleMetadata annotation.
     * 
     * @param <T> The type of rule function being validated
     * @param ruleFunctions Map of rule functions to validate
     * @param functionType The type name for error messages
     */
    private <T extends RuleFunction<K>> void validateRuleMetadata(Map<String, T> ruleFunctions, String functionType) {
        StringBuilder errors = new StringBuilder();
        
        for (Map.Entry<String, T> entry : ruleFunctions.entrySet()) {
            String name = entry.getKey();
            T function = entry.getValue();
            Class<?> functionClass = function.getClass();
            
            RuleMetadata metadata = functionClass.getAnnotation(RuleMetadata.class);
            if (metadata == null) {
                errors.append(format("- %s '%s' (%s) is missing @RuleMetadata annotation\n", 
                    functionType, name, functionClass.getName()));
                continue;
            }
            
            // Validate that the name in the annotation matches the registered name
            if (!name.equals(metadata.name())) {
                errors.append(format("- %s '%s' has mismatched name in @RuleMetadata: '%s'\n", 
                    functionType, name, metadata.name()));
            }
            
            // Validate that the description is not empty
            if (metadata.description().isEmpty()) {
                errors.append(format("- %s '%s' has empty description in @RuleMetadata\n", 
                    functionType, name));
            }
        }
        
        if (errors.length() > 0) {
            throw RuleLoadException.validationErrors(errors.toString());
        }
    }
}