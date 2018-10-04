package ru.hh.checkstyle.maven.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.resource.ResourceManager;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@Mojo(name = "compile-config",
  defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ProcessConfigMojo extends AbstractMojo {
  @Parameter( defaultValue = "${project.build.outputDirectory}", required = true )
  private File outputDirectory;

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Requirement( hint = "default" )
  private ResourceManager locator;

  private final DocumentBuilder builder;

  public ProcessConfigMojo() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    builder = factory.newDocumentBuilder();
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    for (Resource resource : project.getResources()) {
      processResource(resource);
    }
  }

  private void processResource(Resource resource) throws MojoExecutionException {
    try {
      Path resourcePath = Paths.get(resource.getDirectory());
      Files.walk(resourcePath)
        .filter(Files::isRegularFile)
        .forEach(file -> {
          try {
            Node result = ConfigMergeProcessor.handleFile(builder, file, getLog());
            Path relativeFilePath = resourcePath.relativize(file);
            getLog().info("relative file path: " + relativeFilePath);
            ConfigMergeProcessor.writeNodeToFile(builder, result, outputDirectory.toPath().resolve(relativeFilePath).toFile());
          } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            throw new IORuntimeException(e);
          }
        });
    } catch (IOException | IORuntimeException e) {
      throw new MojoExecutionException("failed to process resources", e);
    }
  }
}
