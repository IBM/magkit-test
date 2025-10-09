package de.ibmix.magkit.test.jcr;

/*-
 * #%L
 * Aperto Mockito Test-Utils - JCR
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubType;

/**
 * SAX {@link org.xml.sax.ContentHandler} implementation that parses JCR system view XML ("sv:" namespace)
 * and builds a hierarchy of mocked {@link Node} instances for use in unit tests.
 * <p>
 * Supported tags:
 * <ul>
 *     <li><code>sv:node</code> – creates a new mocked node and pushes it on an internal path stack.</li>
 *     <li><code>sv:property</code> – collects property meta-data (name & type) and its values.</li>
 *     <li><code>sv:value</code> – accumulates character data for a single property value.</li>
 * </ul>
 * Properties <code>jcr:primaryType</code> and <code>jcr:uuid</code> are treated specially to stub node type and identifier.
 * <p>
 * After the parse has finished (i.e. the root <code>sv:node</code> element is closed) {@link #getResult()} returns
 * the root node of the parsed subtree.
 * <p>
 * Example usage:
 * <pre>
 *     XMLReader reader = XMLReaderFactory.createXMLReader();
 *     JcrXmlHandler handler = new JcrXmlHandler("website");
 *     reader.setContentHandler(handler);
 *     reader.parse(inputSource);
 *     Node root = handler.getResult();
 * </pre>
 * This handler is stateful and not thread-safe – create a new instance per parse invocation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2015-01-13
 */
public class JcrXmlHandler extends DefaultHandler {

    private final String _repository;
    private Node _result;
    private final Stack<Node> _currentPath = new Stack<>();
    private String _currentPropertyName = null;
    private String _currentPropertyType = null;
    private List<Value> _currentValues = null;
    private String _currentValue = null;

    /**
     * Creates a new handler for parsing JCR system view XML.
     * If the provided repository name is blank, the default value "website" is used.
     *
     * @param repository the logical repository/workspace name used to build mock node paths; may be {@code null} or blank
     */
    public JcrXmlHandler(String repository) {
        _repository = StringUtils.isEmpty(repository) ? "website" : repository;
    }

    /**
     * Handles the start of an XML element. Creates and pushes nodes for <code>sv:node</code>,
     * initializes property collection for <code>sv:property</code>, and prepares for value accumulation on <code>sv:value</code>.
     *
     * @param uri       the Namespace URI, or the empty string if none
     * @param localName the local name (without prefix), or the empty string if none
     * @param qName     the qualified name (with prefix), or the empty string if none
     * @param attributes the attributes attached to the element
     * @throws SAXException if a SAX error occurs
     */
    @Override
    @SuppressWarnings("RedundantThrows")
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("sv:node".equalsIgnoreCase(qName)) {
            String nodeName = attributes.getValue("sv:name");
            Node node = null;
            try {
                if (!_currentPath.isEmpty()) {
                    Node current = _currentPath.peek();
                    node = mockNode(_repository, current.getPath() + "/" + nodeName);
                } else {
                    node = mockNode(_repository, nodeName);
                }
            } catch (RepositoryException e) {
                // ignore, no repository involved
            }
            _currentPath.push(node);
        } else if ("sv:property".equalsIgnoreCase(qName)) {
            _currentValues = new ArrayList<>();
            _currentPropertyName = attributes.getValue("sv:name");
            _currentPropertyType = attributes.getValue("sv:type");
        } else if ("sv:value".equalsIgnoreCase(qName)) {
            _currentValue = null;
        }
    }

    /**
     * Accumulates character data for the current <code>sv:value</code>. Multiple invocations will concatenate text
     * to form the complete property value content.
     *
     * @param ac     the array of characters
     * @param offset the start position in the array
     * @param count  the number of characters to read from the array
     * @throws SAXException if a SAX error occurs
     */
    @Override
    @SuppressWarnings("RedundantThrows")
    public void characters(char[] ac, int offset, int count) throws SAXException {
        String str = String.valueOf(ac, offset, count);
        if (_currentValue == null) {
            _currentValue = str;
        } else {
            _currentValue = _currentValue + str;
        }
    }

    /**
     * Handles the end of an XML element. On closing <code>sv:value</code> a mocked {@link Value} is constructed.
     * On closing <code>sv:property</code> the collected values are stubbed onto the current node. On closing
     * <code>sv:node</code> the node is popped from the stack and may become the result root.
     *
     * @param uri       the Namespace URI, or the empty string if none
     * @param localName the local name (without prefix), or the empty string if none
     * @param qName     the qualified name (with prefix), or the empty string if none
     * @throws SAXException if a SAX error occurs
     */
    @Override
    @SuppressWarnings("RedundantThrows")
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if ("sv:value".equalsIgnoreCase(qName)) {
                _currentValues.add(ValueMockUtils.mockValue(_currentValue, PropertyType.valueFromName(_currentPropertyType)));
                if ("jcr:primaryType".equalsIgnoreCase(_currentPropertyName)) {
                    stubType(_currentValue).of(_currentPath.peek());
                } else if ("jcr:uuid".equalsIgnoreCase(_currentPropertyName)) {
                    stubIdentifier(_currentValue).of(_currentPath.peek());
                }
            } else if ("sv:property".equalsIgnoreCase(qName)) {
                stubProperty(_currentPropertyName, _currentValues.toArray(new Value[0])).of(_currentPath.peek());
            } else if ("sv:node".equalsIgnoreCase(qName)) {
                _result = _currentPath.pop();
            }
        } catch (RepositoryException e) {
            // ignore, no repository involved
        }
    }

    /**
     * Returns the root {@link Node} of the parsed XML subtree. This will be set after the closing tag of the top-level
     * <code>sv:node</code> element has been processed. If parsing has not completed or no node was parsed, this may be {@code null}.
     *
     * @return the root mocked node or {@code null} if unavailable
     */
    public Node getResult() {
        return _result;
    }
}
