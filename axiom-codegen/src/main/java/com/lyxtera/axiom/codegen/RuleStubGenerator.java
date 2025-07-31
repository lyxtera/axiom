package com.lyxtera.axiom.codegen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lyxtera.axiom.api.model.RuleSetDescriptor;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessActionDescriptor;
import com.lyxtera.axiom.api.model.RuleSetDescriptor.BusinessCheckDescriptor;
import com.lyxtera.axiom.engine.RuleSetLoader.YamlRuleSetLoader;

/**
 * Generator for creating Java stub classes for business checks and actions 
 * from Axiom rule set YAML files.
 */
public class RuleStubGenerator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleStubGenerator.class);
    
    private final String basePackage;
    private final String outputDirectory;
    private final List<String> ruleSetPaths;
    private final boolean overwriteExisting;
    private final String contextKeyEnum;
    
    // Template file paths
    private static final String CHECK_TEMPLATE_PATH = "codegen/check_template.tpl";
    private static final String ACTION_TEMPLATE_PATH = "codegen/action_template.tpl";
    
    /**
     * Creates a new RuleStubGenerator with the specified configuration.
     *
     * @param basePackage      The base package for generated classes (e.g., "com.mycompany.rules")
     * @param outputDirectory  The output directory for generated Java files (e.g., "src/main/java")
     * @param ruleSetPaths     List of paths to rule set YAML files
     * @param overwriteExisting Whether to overwrite existing files (default: false)
     * @param contextKeyEnum   The fully qualified name of the context enum class (e.g., "com.company.ContextKey")
     */
    public RuleStubGenerator(String basePackage, String outputDirectory, List<String> ruleSetPaths, 
            boolean overwriteExisting, String contextKeyEnum) {
        this.basePackage = basePackage;
        this.outputDirectory = outputDirectory;
        this.ruleSetPaths = ruleSetPaths;
        this.overwriteExisting = overwriteExisting;
        this.contextKeyEnum = contextKeyEnum;
    }
    
    /**
     * Generates Java stubs for all business checks and actions found in the configured rule sets.
     *
     * @return The number of files generated
     * @throws IOException If an error occurs while writing files
     */
    public int generateStubs() throws IOException {
        int totalFilesGenerated = 0;
        
        for (String ruleSetPath : ruleSetPaths) {
            RuleSetDescriptor descriptor = new YamlRuleSetLoader<>(ruleSetPath).loadDescriptor();
            totalFilesGenerated += generateStubsForRuleSet(descriptor);
        }
        
        return totalFilesGenerated;
    }

    private int generateStubsForRuleSet(RuleSetDescriptor descriptor) throws IOException {
        int filesGenerated = 0;
        
        // Create directory structure
        Path checksDir = Paths.get(outputDirectory, basePackage.replace('.', File.separatorChar), "checks");
        Path actionsDir = Paths.get(outputDirectory, basePackage.replace('.', File.separatorChar), "actions");
        
        Files.createDirectories(checksDir);
        Files.createDirectories(actionsDir);
        
        // Generate checks
        for (BusinessCheckDescriptor check : descriptor.getBusinessChecks()) {
            String className = toClassName(check.getName()) + "Check";
            Path filePath = checksDir.resolve(className + ".java");
            
            if (shouldGenerateFile(filePath)) {
                generateCheckStub(filePath, check);
                filesGenerated++;
            }
        }
        
        // Generate actions
        for (BusinessActionDescriptor action : descriptor.getBusinessActions()) {
            String className = toClassName(action.getName()) + "Action";
            Path filePath = actionsDir.resolve(className + ".java");
            
            if (shouldGenerateFile(filePath)) {
                generateActionStub(filePath, action);
                filesGenerated++;
            }
        }
        
        return filesGenerated;
    }
    
    /**
     * Determines if a file should be generated based on whether it exists and overwrite setting.
     */
    private boolean shouldGenerateFile(Path filePath) {
        return overwriteExisting || !Files.exists(filePath);
    }

    private void generateCheckStub(Path filePath, BusinessCheckDescriptor check) throws IOException {
        generateStub(filePath, CHECK_TEMPLATE_PATH, "Check", check.getName(), check.getDescription(), check.getParams());
    }
    
    private void generateActionStub(Path filePath, BusinessActionDescriptor action) throws IOException {
        generateStub(filePath, ACTION_TEMPLATE_PATH, "Action", action.getName(), action.getDescription(), action.getParams());
    }
    
    private void generateStub(Path filePath, String templatePath, String suffix, String name, 
            String description, List<String> params) throws IOException {
        var template = loadTemplateResource(templatePath);
        var className = toClassName(name) + suffix;
        
        // Extract the simple class name from the fully qualified contextKeyEnum
        var contextKeySimpleName = contextKeyEnum.substring(contextKeyEnum.lastIndexOf('.') + 1);
        
        var content = template
            .replace("{0}", basePackage)
            .replace("{1}", escapeJavadoc(description))
            .replace("{2}", name)
            .replace("{3}", escapeJavadoc(description))
            .replace("{4}", className)
            .replace("{5}", contextKeyEnum)
            .replace("{6}", generateParams(params))
            .replace("CTX-KEY-ENUM", contextKeySimpleName);

        // Insert parameter javadoc after generation
        String javadocParams = generateJavadocParams(params);
        if (!javadocParams.isEmpty()) {
            content = content.replace("     * @return", javadocParams + "\n     * @return");
        }
        
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, content);
    }
    
    /**
     * Loads a template from a resource file.
     * @return The template content or null if the file cannot be found or read
     */
    private String loadTemplateResource(String templatePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new IOException("Template resource not found: " + templatePath);
            }
            
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Converts a camelCase or snake_case name to a proper Java class name.
     */
    private String toClassName(String name) {
        if (name.isEmpty()) {
            return "Unknown";
        }
        
        // Handle snake_case
        if (name.contains("_")) {
            String[] parts = name.split("_");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    sb.append(Character.toUpperCase(part.charAt(0)));
                    if (part.length() > 1) {
                        sb.append(part.substring(1));
                    }
                }
            }
            return sb.toString();
        }
        
        // Handle camelCase
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    
    /**
     * Generates method parameters for a business component.
     * 
     * @param params The parameter names
     * @return A string containing the method parameters for the execute method
     */
    private String generateParams(List<String> params) {
        StringBuilder sb = new StringBuilder();
        
        // Always add RuleContext as the first parameter regardless of what's in the descriptor
        sb.append("RuleContext<CTX-KEY-ENUM> ctx");
        
        // Then add any additional parameters from the descriptor
        if (params != null && !params.isEmpty()) {
            for (String param : params) {
                sb.append(", ");
                sb.append("@Arg(\"").append(param).append("\") Value ");
                sb.append(toCamelCase(param));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Generates javadoc parameter descriptions for a business component.
     * 
     * @param params The parameter names
     * @return A string containing the javadoc parameter descriptions
     */
    private String generateJavadocParams(List<String> params) {
        StringBuilder sb = new StringBuilder();
        
        // Always add RuleContext javadoc
        sb.append("     * @param ctx The rule execution context");
        
        // Then add any additional parameter javadocs
        if (params != null && !params.isEmpty()) {
            for (String param : params) {
                sb.append("\n     * @param ");
                sb.append(toCamelCase(param));
                sb.append(" The ");
                sb.append(param.replace('_', ' ').toLowerCase());
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Converts a string to camelCase format.
     */
    private String toCamelCase(String str) {
        if (str.isEmpty()) {
            return "param";
        }
        
        // Handle snake_case
        if (str.contains("_")) {
            String[] parts = str.split("_");
            StringBuilder sb = new StringBuilder(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    sb.append(Character.toUpperCase(parts[i].charAt(0)));
                    if (parts[i].length() > 1) {
                        sb.append(parts[i].substring(1));
                    }
                }
            }
            return sb.toString();
        }
        
        // Already camelCase or single word
        return str;
    }
    
    /**
     * Escapes special characters in Javadoc strings.
     */
    private String escapeJavadoc(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replace("*/", "* /").replace("\"", "\\\"");
    }
    
    /**
     * Builder for creating RuleStubGenerator instances.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for RuleStubGenerator.
     */
    public static class Builder {
        private String basePackage;
        private String outputDirectory;
        private final List<String> ruleSetPaths = new ArrayList<>();
        private boolean overwriteExisting = false;
        private String contextKeyEnum;
        
        private Builder() {
        }
        
        /**
         * Sets the base package for generated classes.
         */
        public Builder withBasePackage(String basePackage) {
            this.basePackage = basePackage;
            return this;
        }
        
        /**
         * Sets the output directory for generated files.
         */
        public Builder withOutputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
            return this;
        }
        
        /**
         * Adds a rule set YAML file to process.
         */
        public Builder addRuleSet(String ruleSetPath) {
            this.ruleSetPaths.add(ruleSetPath);
            return this;
        }
        
        /**
         * Sets whether to overwrite existing files.
         */
        public Builder overwriteExisting(boolean overwriteExisting) {
            this.overwriteExisting = overwriteExisting;
            return this;
        }
        
        /**
         * Sets the context key enum.
         */
        public Builder withContextKeyEnum(String contextKeyEnum) {
            this.contextKeyEnum = contextKeyEnum;
            return this;
        }
        
        /**
         * Builds the RuleStubGenerator.
         */
        public RuleStubGenerator build() {
            validateState();
            
            String outputDir = outputDirectory;
            if (outputDir == null) {
                outputDir = "target/generated-sources/axiom";
            }
            
            return new RuleStubGenerator(basePackage, outputDir, ruleSetPaths, overwriteExisting, contextKeyEnum);
        }
        
        /**
         * Validates the builder state before building.
         */
        private void validateState() {
            if (basePackage == null || basePackage.trim().isEmpty()) {
                throw new IllegalStateException("Base package must be specified");
            }
            if (ruleSetPaths.isEmpty()) {
                throw new IllegalStateException("At least one rule set path must be specified");
            }
            if (contextKeyEnum == null || contextKeyEnum.trim().isEmpty()) {
                throw new IllegalStateException("Context key enum must be specified");
            }
            if (outputDirectory == null || outputDirectory.trim().isEmpty()) {
                throw new IllegalStateException("Output directory must be specified");
            }
        }
    }
    
    public static void main(String[] args) {
        String basePackage = null;
        String outputDirectory = "src/main/java";
        List<String> ruleSets = new ArrayList<>();
        boolean overwriteExisting = false;
        String contextKeyEnum = null;
        
        // Parse command line arguments
        for (String arg : args) {
            if (arg.startsWith("--basePackage=")) {
                basePackage = arg.substring("--basePackage=".length());
            } else if (arg.startsWith("--outputDirectory=")) {
                outputDirectory = arg.substring("--outputDirectory=".length());
            } else if (arg.startsWith("--ruleSet=")) {
                ruleSets.add(arg.substring("--ruleSet=".length()));
            } else if (arg.startsWith("--overwriteExisting=")) {
                overwriteExisting = Boolean.parseBoolean(arg.substring("--overwriteExisting=".length()));
            } else if (arg.startsWith("--contextKeyEnum=")) {
                contextKeyEnum = arg.substring("--contextKeyEnum=".length());
            }
        }
        
        // Validate required parameters
        if (basePackage == null || basePackage.trim().isEmpty()) {
            LOGGER.error("Error: basePackage is required");
            printUsage();
            System.exit(1);
        }
        
        if (ruleSets.isEmpty()) {
            LOGGER.error("Error: at least one ruleSet is required");
            printUsage();
            System.exit(1);
        }
        
        try {
            Builder builder = builder()
                .withBasePackage(basePackage)
                .withOutputDirectory(outputDirectory)
                .overwriteExisting(overwriteExisting);
            
            if (contextKeyEnum != null) {
                builder.withContextKeyEnum(contextKeyEnum);
            }
            
            for (String ruleSet : ruleSets) {
                builder.addRuleSet(ruleSet);
            }
            
            RuleStubGenerator generator = builder.build();
            int filesGenerated = generator.generateStubs();
            LOGGER.info("Generated {} stub files", filesGenerated);
        } catch (Exception e) {
            LOGGER.error("Error generating stubs: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    /**
     * Prints usage information to stderr.
     */
    private static void printUsage() {
        System.err.println("Usage: RuleStubGenerator [options]");
        System.err.println("Options:");
        System.err.println("  --basePackage=<package>         Base package for generated classes (required)");
        System.err.println("  --outputDirectory=<dir>         Output directory (default: src/main/java)");
        System.err.println("  --ruleSet=<path>                Path to rule set YAML file (can be specified multiple times)");
        System.err.println("  --overwriteExisting=<true|false> Whether to overwrite existing files (default: false)");
        System.err.println("  --contextKeyEnum=<enum>          Fully qualified name of the context enum class (e.g., com.company.ContextKey)");
    }
} 