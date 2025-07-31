package com.lyxtera.axiom.codegen.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.lyxtera.axiom.codegen.RuleStubGenerator;

/**
 * Goal to generate rule stubs based on ruleset YAML files.
 */
@Mojo(name = "generate-stubs", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class RuleStubGeneratorMojo extends AbstractMojo {

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Skip the rule stub generation. This is useful when you don't want to generate rule stubs.
     */
    @Parameter(property = "axiom.stubs.skip", defaultValue = "false")
    private boolean skip;

    /**
     * The package for the generated rule stubs.
     */
    @Parameter(property = "axiom.stubs.package", required = true)
    private String packageName;

    /**
     * The comma-separated ruleset YAML files to process.
     */
    @Parameter(property = "axiom.stubs.ruleSets", required = true)
    private String ruleSets;

    /**
     * Directory to output generated sources to.
     */
    @Parameter(property = "axiom.stubs.outputDirectory", defaultValue = "${project.build.directory}/generated-sources/axiom")
    private File outputDirectory;

    /**
     * Whether to overwrite existing files.
     */
    @Parameter(property = "axiom.stubs.overwriteExisting", defaultValue = "false")
    private boolean overwriteExisting;

    /**
     * The fully qualified name of the context enum class (e.g. "com.lyxtera.axiom.examples.rules.CustomerContextKey").
     */
    @Parameter(property = "axiom.stubs.contextKeyEnum", required = true)
    private String contextKeyEnum;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping rule stub generation");
            return;
        }

        getLog().info("Generating axiom rule stubs");
        getLog().info("Package: " + packageName);
        getLog().info("Output directory: " + outputDirectory);
        getLog().info("Overwrite existing: " + overwriteExisting);
        getLog().info("Context key enum: " + contextKeyEnum);
        
        // Parse the ruleSets parameter into a list of paths
        List<String> ruleSetPaths = new ArrayList<>();
        for (String ruleSetPath : ruleSets.split(",")) {
            String trimmedPath = ruleSetPath.trim();
            File ruleSetFile = new File(trimmedPath);
            if (!ruleSetFile.exists()) {
                getLog().warn("Rule set file does not exist: " + ruleSetFile);
                continue;
            }
            ruleSetPaths.add(trimmedPath);
        }
        
        if (ruleSetPaths.isEmpty()) {
            getLog().warn("No rule set files found");
            return;
        }
        
        try {
            // Create output directory if it doesn't exist
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            
            // Create the generator
            RuleStubGenerator generator = new RuleStubGenerator(
                    packageName, 
                    outputDirectory.getAbsolutePath(), 
                    ruleSetPaths, 
                    overwriteExisting,
                    contextKeyEnum);
            
            // Generate stubs
            int filesGenerated = generator.generateStubs();
            
            // Add the generated sources directory to the project
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
            
            getLog().info(filesGenerated + " rule stub files generated successfully to " + outputDirectory);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate rule stubs", e);
        }
    }
} 