package ru.hh.checkstyle.maven.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.resource.ResourceManager;
import org.codehaus.plexus.resource.loader.FileResourceCreationException;
import org.codehaus.plexus.resource.loader.FileResourceLoader;
import org.codehaus.plexus.resource.loader.ResourceNotFoundException;
import org.codehaus.plexus.util.ReflectionUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class CheckstyleViolationCheckMojo extends org.apache.maven.plugins.checkstyle.CheckstyleViolationCheckMojo {

  @Parameter(property = "hh.checkstyle.component.config.location", required = true)
  private List<String> hhCheckStyleConfig;

  @Parameter(property = "hh.checkstyle.result.config.location", defaultValue = "hh-checkstyle-checker.xml", required = true)
  private String hhCheckStyleCompilationResult;

  @Parameter(defaultValue = "${project.build.directory}", required = true)
  private File outputDirectory;

  @Component
  private ResourceManager locator;

  private final DocumentBuilder builder;

  public CheckstyleViolationCheckMojo() throws ParserConfigurationException {
    builder = Utils.createConfigDocumentBuilder();
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      if (getParameter("skip", Boolean.class)) {
        return;
      }
      configureResourceLocator(locator);
      List<Path> hhConfigs = hhCheckStyleConfig.stream().map(this::resolveResource).collect(Collectors.toList());
      String configLocation = getParameter("configLocation", String.class);
      if (!"sun_checks.xml".equals(configLocation)) {
        hhConfigs.add(resolveResource(configLocation));
      }
      if (hhConfigs.isEmpty()) {
        throw new MojoFailureException("Please, provide configuration file/-s");
      }
      List<Node> elements = resolveToElements(hhConfigs);
      Node result = mergeElements(elements);
      File resultFile = outputDirectory.toPath().resolve(hhCheckStyleCompilationResult).toFile();
      ConfigMergeProcessor.writeNodeToFile(builder, result, resultFile);
      ReflectionUtils.setVariableValueInObject(this, "configLocation", hhCheckStyleCompilationResult);
      super.execute();
    } catch (ExpectedExceptionRuntimeWrapper | IllegalAccessException | ParserConfigurationException | TransformerException e) {
      throw new MojoExecutionException("Exception on checkstyle check", e);
    }

  }

  private static Node mergeElements(List<Node> elements) {
    return elements.stream().skip(1).reduce(elements.get(0), ConfigMergeProcessor::merge);
  }

  private List<Node> resolveToElements(List<Path> hhConfigs) {
    return hhConfigs.stream().map(hhConfig -> {
      try {
        return ConfigMergeProcessor.handleFile(this::resolveResource, builder, hhConfig, getLog());
      } catch (IOException | SAXException | ParserConfigurationException e) {
        throw new ExpectedExceptionRuntimeWrapper(e);
      }
    }).collect(Collectors.toList());
  }

  private void configureResourceLocator(ResourceManager resourceManager) throws IllegalAccessException {
    resourceManager.setOutputDirectory(new File(project.getBuild().getDirectory()));
    resourceManager.addSearchPath(FileResourceLoader.ID, project.getBuild().getDirectory());

    MavenProject parent = project;
    while (parent != null && parent.getFile() != null) {
      File dir = parent.getFile().getParentFile();
      resourceManager.addSearchPath(FileResourceLoader.ID, dir.getAbsolutePath());
      parent = parent.getParent();
    }
    Object checkstyleExecutor = ReflectionUtils.getValueIncludingSuperclasses("checkstyleExecutor", this);
    ReflectionUtils.setVariableValueInObject(checkstyleExecutor, "locator", locator);
  }

  private Path resolveResource(String cfgLocation) {
    try {
      return locator.getResourceAsFile(cfgLocation).toPath();
    } catch (ResourceNotFoundException | FileResourceCreationException e) {
      throw new ExpectedExceptionRuntimeWrapper(e);
    }
  }

  private <T> T getParameter(String name, Class<T> clazz) throws MojoFailureException {
    try {
      return clazz.cast(ReflectionUtils.getValueIncludingSuperclasses(name, this));
    } catch (IllegalAccessException e) {
      throw new MojoFailureException("failed to access parameter", e);
    }
  }


}

