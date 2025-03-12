package com.lyxtera.axiom.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.inject.multibindings.MapBinder;
import com.lyxtera.axiom.api.model.BusinessAction;
import com.lyxtera.axiom.api.model.BusinessCheck;
import com.lyxtera.axiom.engine.RuleSetLoader;

/**
 * A fluent builder for creating AxiomModule instances.
 * This builder allows for easy configuration of rule sets, business checks, and business actions
 * using a chainable API.
 *
 * @param <K> The enum type used for context keys
 */
public class AxiomModuleBuilder<K extends Enum<K>> {

    private final Class<K> contextKeyClass;

    private final Map<String, Class<? extends BusinessCheck<K>>> checks = new HashMap<>();
    private final Map<String, Class<? extends BusinessAction<K>>> actions = new HashMap<>();
    private final Map<String, RuleSetLoader<K>> ruleSetLoaders = new HashMap<>();

    AxiomModuleBuilder(Class<K> contextKeyClass) {
        this.contextKeyClass = contextKeyClass;
    }
    
    /**
     * Adds multiple rule set loaders to the module using a consumer.
     * Rule set loaders provide the mechanism to load rule definitions from various sources.
     *
     * @param configurator A consumer that configures rule loaders
     * @return This builder instance for method chaining
     */
    public AxiomModuleBuilder<K> withRuleLoaders(Consumer<LoaderConfigurator> configurator) {
        configurator.accept(new LoaderConfigurator());
        return this;
    }

    /**
     * Adds multiple business checks to the module using a consumer.
     *
     * @param checksConfigurator A consumer that configures checks
     * @return This builder instance for method chaining
     */
    public AxiomModuleBuilder<K> withChecks(Consumer<ChecksConfigurator> configurator) {
        configurator.accept(new ChecksConfigurator());
        return this;
    }

    /**
     * Adds multiple business actions to the module using a consumer.
     *
     * @param actionsConfigurator A consumer that configures actions
     * @return This builder instance for method chaining
     */
    public AxiomModuleBuilder<K> withActions(Consumer<ActionsConfigurator> configurator) {
        configurator.accept(new ActionsConfigurator());
        return this;
    }

    /**
     * Builds and returns a new AxiomModule instance.
     *
     * @return A new AxiomModule instance
     */
    public AxiomModule<K> build() {
        return new AxiomModule<K>(contextKeyClass) {

            @Override
            protected void configureBusinessRules(MapBinder<String, BusinessCheck<K>> checksBinder,
                     MapBinder<String, BusinessAction<K>> actionsBinder) {

                // Register all configured checks
                for (Map.Entry<String, Class<? extends BusinessCheck<K>>> entry : checks.entrySet()) {
                    checksBinder.addBinding(entry.getKey()).to(entry.getValue());
                }

                // Register all configured actions
                for (Map.Entry<String, Class<? extends BusinessAction<K>>> entry : actions.entrySet()) {
                    actionsBinder.addBinding(entry.getKey()).to(entry.getValue());
                }
            }

            @Override
            protected Map<String, RuleSetLoader<K>> getRegisteredLoaders() {
                return Collections.unmodifiableMap(ruleSetLoaders);
            }
        };
    }

    /**
     * Helper class for configuring multiple checks.
     */
    public class ChecksConfigurator {
        /**
         * Adds a business check.
         *
         * @param name The name of the check
         * @param checkClass The class implementing the check
         * @return This configurator instance for method chaining
         */
        public ChecksConfigurator check(String name, Class<? extends BusinessCheck<K>> checkClass) {
            checks.put(name, checkClass);
            return this;
        }
    }

    /**
     * Helper class for configuring multiple actions.
     */
    public class ActionsConfigurator {

        /**
         * Adds a business action.
         *
         * @param name The name of the action
         * @param actionClass The class implementing the action
         * @return This configurator instance for method chaining
         */
        public ActionsConfigurator action(String name, Class<? extends BusinessAction<K>> actionClass) {
            actions.put(name, actionClass);
            return this;
        }
    }

    /**
     * Helper class for configuring multiple rule loaders.
     */
    public class LoaderConfigurator {
        /**
         * Adds a ruleset loader                                                                            q`
         *
         * @param name The name of the rule loader
         * @param loader The class implementing the rule loader
         * @return This configurator instance for method chaining
         */
        public LoaderConfigurator loader(String name, RuleSetLoader<K> loader) {
            ruleSetLoaders.put(name, loader);            
            return this;
        }
    }
}
