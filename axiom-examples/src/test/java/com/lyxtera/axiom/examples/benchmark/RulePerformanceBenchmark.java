package com.lyxtera.axiom.examples.benchmark;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.lyxtera.axiom.engine.RuleContext;
import com.lyxtera.axiom.engine.RuleExecutionResult;
import com.lyxtera.axiom.engine.RuleOrchestrator;
import com.lyxtera.axiom.examples.config.ApplicationMainModule;
import com.lyxtera.axiom.examples.rules.CustomerContextKey;

/**
 * JMH performance benchmarks for Axiom rule execution.
 * 
 * This benchmark measures the performance of different rule execution scenarios
 * to ensure the rule engine meets performance requirements.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class RulePerformanceBenchmark {

    private RuleOrchestrator<CustomerContextKey> ruleOrchestrator;
    
    // Pre-created contexts to avoid object creation overhead during benchmarks
    private RuleContext<CustomerContextKey> simpleCustomerContext;
    private RuleContext<CustomerContextKey> highValueCustomerContext;
    private RuleContext<CustomerContextKey> vipCustomerContext;
    private RuleContext<CustomerContextKey> newCustomerContext;

    @Setup(Level.Trial)
    public void setUp() {
        // Initialize Guice injector and rule orchestrator
        Injector injector = Guice.createInjector(new ApplicationMainModule());
        injector.injectMembers(this);
        
        // Pre-create contexts for different customer scenarios
        setupContexts();
    }

    @Inject
    public void setRuleOrchestrator(@Named("customer_discount") RuleOrchestrator<CustomerContextKey> ruleOrchestrator) {
        this.ruleOrchestrator = ruleOrchestrator;
    }

    private void setupContexts() {
        // Simple customer - triggers seasonal discount only
        simpleCustomerContext = createContext(
            new BigDecimal("100.00"), 
            1, 
            LocalDateTime.now().minusDays(5)
        );

        // High value customer - triggers multiple rules
        highValueCustomerContext = createContext(
            new BigDecimal("2500.00"), 
            5, 
            LocalDateTime.now().minusDays(35)
        );

        // VIP customer scenario - triggers VIP upgrade rules
        vipCustomerContext = createContext(
            new BigDecimal("5000.00"), 
            5, 
            LocalDateTime.now().minusDays(60)
        );

        // New customer - minimal rules triggered
        newCustomerContext = createContext(
            new BigDecimal("50.00"), 
            1, 
            LocalDateTime.now()
        );
    }

    @Benchmark
    public RuleExecutionResult<CustomerContextKey> benchmarkSimpleCustomerRules() {
        return ruleOrchestrator.executeAllMatchingRules(simpleCustomerContext);
    }

    @Benchmark
    public RuleExecutionResult<CustomerContextKey> benchmarkHighValueCustomerRules() {
        return ruleOrchestrator.executeAllMatchingRules(highValueCustomerContext);
    }

    @Benchmark
    public RuleExecutionResult<CustomerContextKey> benchmarkVipCustomerRules() {
        return ruleOrchestrator.executeAllMatchingRules(vipCustomerContext);
    }

    @Benchmark
    public RuleExecutionResult<CustomerContextKey> benchmarkNewCustomerRules() {
        return ruleOrchestrator.executeAllMatchingRules(newCustomerContext);
    }

    @Benchmark
    public RuleExecutionResult<CustomerContextKey> benchmarkFirstMatchingRule() {
        return ruleOrchestrator.executeFirstMatchingRule(highValueCustomerContext);
    }

    @Benchmark
    public RuleContext<CustomerContextKey> benchmarkContextCreation() {
        return createContext(
            new BigDecimal("1000.00"), 
            3, 
            LocalDateTime.now().minusDays(30)
        );
    }

    /**
     * Benchmark rule evaluation without execution (condition checking only)
     */
    @Benchmark
    public boolean benchmarkRuleConditionEvaluation() {
        // This would require accessing internal rule evaluation methods
        // For now, we approximate by checking if rules match
        RuleExecutionResult<CustomerContextKey> result = ruleOrchestrator.executeAllMatchingRules(highValueCustomerContext);
        return result.hasMatches();
    }

    private RuleContext<CustomerContextKey> createContext(BigDecimal spendingAmount, 
                                                         int loyaltyLevel, 
                                                         LocalDateTime registrationDate) {
        RuleContext<CustomerContextKey> context = new RuleContext<>(CustomerContextKey.class);
        context.add(CustomerContextKey.SPENDING_AMOUNT, spendingAmount);
        context.add(CustomerContextKey.LOYALTY_LEVEL, loyaltyLevel);
        context.add(CustomerContextKey.REGISTRATION_DATE, registrationDate);
        context.add(CustomerContextKey.DISCOUNT_PERCENTAGE, BigDecimal.ZERO);
        context.add(CustomerContextKey.IS_VIP, false);
        context.add(CustomerContextKey.SEND_WELCOME_GIFT, false);
        return context;
    }

    /**
     * Main method to run the benchmarks
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RulePerformanceBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}