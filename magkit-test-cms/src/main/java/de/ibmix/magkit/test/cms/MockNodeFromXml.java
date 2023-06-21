package de.ibmix.magkit.test.cms;

/*-
 * #%L
 * magkit-test-cms Magnolia Module
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

import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockProperty;
import info.magnolia.test.mock.jcr.MockValue;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.Node;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Utility class to create mock {@link Node} hierarchy from JCR export xml.
 *
 * @author jfrantzius
 * @deprecated use de.ibmix.magkit.test.jcr.NodeMockUtils.mockNodeFromXml
 */

@Deprecated
public final class MockNodeFromXml {

    private MockNodeFromXml() {
    }

    public static Node mockNodeFromXml(InputStream xmlUtf8) {
        try {
            JcrXmlHandler jcrXmlHandler = new JcrXmlHandler();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(xmlUtf8, jcrXmlHandler);
            return jcrXmlHandler.getResult();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class JcrXmlHandler extends DefaultHandler {
        private MockNode _result;
        private Stack<MockNode> _currentPath = new Stack<MockNode>();
        private String _currentPropertyName = null;
        private List<MockValue> _currentValues = null;
        private String _currentValue = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("sv:node".equalsIgnoreCase(qName)) {
                MockNode node = new MockNode(attributes.getValue("sv:name"));
                if (!_currentPath.isEmpty()) {
                    _currentPath.peek().addNode(node);
                }
                _currentPath.push(node);
            } else if ("sv:property".equalsIgnoreCase(qName)) {
                _currentValues = new ArrayList<MockValue>();
                _currentPropertyName = attributes.getValue("sv:name");
            }
            // obtain actual value in characters() method
        }

        @Override
        public void characters(char[] ac, int offset, int count) throws SAXException {
            _currentValue = String.valueOf(ac, offset, count);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("sv:value".equalsIgnoreCase(qName)) {
                _currentValues.add(new MockValue(_currentValue));
                if ("jcr:primaryType".equalsIgnoreCase(_currentPropertyName)) {
                    _currentPath.peek().setPrimaryType(_currentValue);
                } else if ("jcr:uuid".equalsIgnoreCase(_currentPropertyName)) {
                    _currentPath.peek().setIdentifier(_currentValue);
                }
            } else if ("sv:property".equalsIgnoreCase(qName)) {
                MockProperty prop = null;
                if (_currentValues.size() > 1) {
                    prop = new MockProperty(_currentPropertyName, _currentValues.toArray(new MockValue[_currentValues.size()]), _currentPath.peek());
                } else {
                    prop = new MockProperty(_currentPropertyName, _currentValues.isEmpty() ? null : _currentValues.get(0), _currentPath.peek());
                }
                _currentPath.peek().addProperty(prop);
            } else if ("sv:node".equalsIgnoreCase(qName)) {
                _result = _currentPath.pop();
            }
        }

        public MockNode getResult() {
            return _result;
        }
    }
}
