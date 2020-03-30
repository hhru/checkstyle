package ru.hh.checkstyle.maven.build;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public final class Utils {
  private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
  private static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
  private static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

  private static final EntityResolver EMPTY_ENTITY_RESOLVER = (publicId, systemId) ->
      new InputSource(new ByteArrayInputStream(new byte[0]));

  private Utils() {
  }

  public static DocumentBuilder createConfigDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    // to be able to work offline w/o access to checkstyle.org
    factory.setValidating(false);
    factory.setFeature(LOAD_EXTERNAL_DTD, false);
    factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
    factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);

    DocumentBuilder documentBuilder = factory.newDocumentBuilder();
    // just in case
    documentBuilder.setEntityResolver(EMPTY_ENTITY_RESOLVER);

    return documentBuilder;
  }
}
