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
 * Sax-Parser that reads jcr-xml files and builds Node mocks.
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

    public JcrXmlHandler(String repository) {
        _repository = StringUtils.isEmpty(repository) ? "website" : repository;
    }

    @Override
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
            _currentValues = new ArrayList<Value>();
            _currentPropertyName = attributes.getValue("sv:name");
            _currentPropertyType = attributes.getValue("sv:type");
        } else if ("sv:value".equalsIgnoreCase(qName)) {
            _currentValue = null;
        }
    }

    @Override
    public void characters(char[] ac, int offset, int count) throws SAXException {
        String str = String.valueOf(ac, offset, count);
        if (_currentValue == null) {
            _currentValue = str;
        } else {
            _currentValue = _currentValue + str;
        }
    }

    @Override
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

    public Node getResult() {
        return _result;
    }
}
