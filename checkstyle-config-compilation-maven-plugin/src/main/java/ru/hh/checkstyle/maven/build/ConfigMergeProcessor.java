package ru.hh.checkstyle.maven.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.function.Function;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class ConfigMergeProcessor {

  private static final String PROPERTY_TAG = "property";
  private static final String IMPORT_PROPERTY_KEY = "hh-parent-config";
  private static final String IMPORT_PROPERTY_VALUE_ATTRIBUTE_NAME = "value";
  private static final String IDENTIFIER_ATTRIBUTE_NAME = "name";

  private ConfigMergeProcessor() {
  }

  public static Node handleFile(DocumentBuilder builder, Path filePath, Log log)
      throws IOException, SAXException, ParserConfigurationException {
    return handleFile(resourcePath -> filePath.getParent().resolve(resourcePath).normalize(), builder, filePath, log);
  }

  public static Node handleFile(Function<String, Path> resourceResolver, DocumentBuilder builder, Path filePath, Log log)
      throws IOException, SAXException, ParserConfigurationException {
    log.info("Processing file: " + filePath);
    List<Node> mergeList = new ArrayList<>();
    Document doc = builder.parse(Files.newInputStream(filePath, StandardOpenOption.READ));
    Element root = doc.getDocumentElement();
    NodeList properties = root.getElementsByTagName(PROPERTY_TAG);
    List<Node> importProperties = IntStream.range(0, properties.getLength()).mapToObj(properties::item)
      .filter(property -> IMPORT_PROPERTY_KEY.equals(property.getAttributes().getNamedItem(IDENTIFIER_ATTRIBUTE_NAME).getNodeValue()))
      .collect(toList());
    if (importProperties.isEmpty()) {
      log.info("File " + filePath + " contains no imports, return as is");
      return root;
    }

    for (Node importProperty : importProperties) {
      String importResourceRelativePath = importProperty.getAttributes().getNamedItem(IMPORT_PROPERTY_VALUE_ATTRIBUTE_NAME).getNodeValue();
      Path importResourcePath = resourceResolver.apply(importResourceRelativePath);
      mergeList.add(handleFile(resourceResolver, builder, importResourcePath, log));
      Node prevElem = importProperty.getPreviousSibling();
      if (prevElem != null && prevElem.getNodeType() == Node.TEXT_NODE && prevElem.getNodeValue().trim().length() == 0) {
        root.removeChild(prevElem);
      }
      root.removeChild(importProperty);
    }
    mergeList.add(root);
    Node base = mergeList.get(0);
    for (int i = 1; i < mergeList.size(); i++) {
      base = merge(base, mergeList.get(i));
    }
    return base;
  }

  public static void writeNodeToFile(DocumentBuilder builder, Node node, File file) throws ParserConfigurationException, TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Document document = builder.newDocument();
    document.appendChild(document.adoptNode(node.cloneNode(true)));
    document.setXmlVersion("1.0");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN");
    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "https://checkstyle.org/dtds/configuration_1_3.dtd");
    transformer.transform(new DOMSource(document), new StreamResult(file));
  }

  /**
   * merges two nodes(elements) recursively and removes indentation
   * for repeating elements(tagName + identifier atribute) order preserves:
   * if you want to merge second element you have to declare two of em, leving first unchanged
   * @param base base node
   * @param up node to update base with
   * @return base node with replaced and extended content and with no indentation
   */
  public static Node merge(Node base, Node up) {
    if (up == null) {
      return base;
    }

    if (base == null) {
      return up;
    }
    if (up.hasAttributes()) {
      NamedNodeMap upAttributes = up.getAttributes();
      NamedNodeMap baseAttributes = base.getAttributes();
      for (int i = 0; i < upAttributes.getLength(); i++) {
        Node upItem = upAttributes.item(i);
        Node baseNamedItem = baseAttributes.getNamedItem(upItem.getNodeName());
        if (baseNamedItem == null) {
          Node namedItem = upItem.cloneNode(true);
          baseAttributes.setNamedItem(base.getOwnerDocument().adoptNode(namedItem));
        }
        if (Optional.ofNullable(upItem.getNodeValue()).map(value -> !value.isEmpty()).orElse(false)) {
          baseNamedItem.setNodeValue(upItem.getNodeValue());
        }
        if (Optional.ofNullable(upItem.getTextContent()).map(content -> !content.isEmpty()).orElse(false)) {
          baseNamedItem.setTextContent(upItem.getTextContent());
        }
      }
    }
    clearIndentation(up);
    if (up.hasChildNodes()) {
      NodeList upChildNodes = up.getChildNodes();
      NodeList baseChildNodes = base.getChildNodes();
      Map<NodeNameKey, List<NodeWrapper>> baseNodeMap = IntStream.range(0, baseChildNodes.getLength()).mapToObj(baseChildNodes::item)
        .filter(node -> node.getNodeType() != Node.TEXT_NODE)
        .collect(groupingBy(NodeNameKey::new, mapping(NodeWrapper::new, toList())));
      for (int i = 0; i < upChildNodes.getLength(); i++) {
        Node upChildNode = upChildNodes.item(i);
        if (upChildNode.getNodeType() != Node.TEXT_NODE) {
          NodeNameKey upNodeKey = new NodeNameKey(upChildNode);
          Node baseChildNode = ofNullable(baseNodeMap.get(upNodeKey))
            .flatMap(wrappers -> wrappers.stream().filter(wrapper -> !wrapper.isMerged()).findFirst()).map(wrapper -> {
              wrapper.setMerged(true);
              return wrapper.getNode();
            }).orElse(null);
          if (baseChildNode == null) {
            Node newChild = cloneAndAdopt(upChildNode, base);
            base.appendChild(newChild);
            continue;
          }
          Node result = merge(baseChildNode, upChildNode);
          Node newChild = cloneAndAdopt(result, base);
          base.replaceChild(newChild, baseChildNode);
        }
      }
    }
    clearIndentation(base);
    return base;
  }

  private static Node cloneAndAdopt(Node child, Node parent) {
    Node newChild = child.cloneNode(true);
    newChild = parent.getOwnerDocument().adoptNode(newChild);
    return newChild;
  }

  private static void clearIndentation(Node node) {
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node item = childNodes.item(i);
      if (item.hasChildNodes()) {
        NodeList itemChildren = item.getChildNodes();
        IntStream.range(0, itemChildren.getLength()).mapToObj(itemChildren::item).forEach(ConfigMergeProcessor::clearIndentation);
      } else if (item.getNodeType() == Node.TEXT_NODE && Optional.ofNullable(item.getTextContent()).map(String::isBlank).orElse(true)) {
        node.removeChild(item);
      }
    }
  }

  private static final class NodeWrapper {
    private final Node node;
    private boolean merged;

    private NodeWrapper(Node node) {
      this.node = node;
    }

    public Node getNode() {
      return node;
    }

    public boolean isMerged() {
      return merged;
    }

    public void setMerged(boolean merged) {
      this.merged = merged;
    }
  }

  private static final class NodeNameKey {
    private final String nodeName;
    private final String nameAttributeValue;

    private NodeNameKey(Node node) {
      nodeName = node.getNodeName();
      nameAttributeValue = node.hasAttributes() ? ofNullable(node.getAttributes().getNamedItem(IDENTIFIER_ATTRIBUTE_NAME))
        .map(Node::getTextContent).orElse(null) : null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      NodeNameKey thatKey = (NodeNameKey) o;
      return Objects.equals(nodeName, thatKey.nodeName) &&
        Objects.equals(nameAttributeValue, thatKey.nameAttributeValue);
    }

    @Override
    public int hashCode() {
      return Objects.hash(nodeName, nameAttributeValue);
    }

    @Override
    public String toString() {
      return "NodeNameKey{" +
        "nodeName='" + nodeName + '\'' +
        ", nameAttributeValue='" + nameAttributeValue + '\'' +
        '}';
    }
  }
}
