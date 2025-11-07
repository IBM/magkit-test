package de.ibmix.magkit.test.cms.examples;

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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils;
import de.ibmix.magkit.test.jcr.ValueMockUtils;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.link.LinkUtil;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.NodeTestUtil;
import org.apache.jackrabbit.util.ISO8601;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultExtension;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Compare Magnolia JCR Mock-Objects with magkit builder API for mockito mocks.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-02-17
 */
//CHECKSTYLE:OFF
public class MockProperties {

    @BeforeEach
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @AfterEach
    public void cleanUp() {
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        MgnlContext.setInstance(null);
    }

    /**
     * This test demonstrates, how to mock node properties using the MagnoliaNodeMockUtils from Magkit.
     */
    @Test
    public void mockMockitoNodeWithProperties() throws RepositoryException {
        // What you do...
        // Stub properties when mocking a Node...
        Node testNode = MagnoliaNodeMockUtils.mockMgnlNode("testNode", "testWorkspace", NodeTypes.ContentNode.NAME,
            stubProperty("string", "testString"),
            stubProperty("boolean", true),
            stubProperty("double", 1.25D),
            stubProperty("long", 123456L)
        );
        // or stub Properties for existing Node:
        Binary binary = ValueMockUtils.mockBinary("test bytes");
        stubProperty("binary", binary).of(testNode);

        Calendar now = Calendar.getInstance();
        stubProperty("calendar", now).of(testNode);

        // ... and what you get:
        assertTrue(testNode.hasProperties());
        // Each magnolia node is mocked with a type property. Therefor we have one Property more than expected!
        assertEquals(7L, testNode.getProperties().getSize());

        assertTrue(testNode.hasProperty("string"));
        assertEquals(PropertyType.STRING, testNode.getProperty("string").getType());
        assertEquals("testString", testNode.getProperty("string").getString());
        assertEquals("testString", testNode.getProperty("string").getValue().getString());
        // We can read String properties as boolean:
        assertFalse(testNode.getProperty("string").getBoolean());
        // Accessing String property values as date, double or long results in a ValueFormatException:
        assertValueFormatExceptionForDate(testNode, "string");
        assertValueFormatExceptionForDouble(testNode, "string");
        assertValueFormatExceptionForLong(testNode, "string");

        assertTrue(testNode.hasProperty("boolean"));
        assertEquals(PropertyType.BOOLEAN, testNode.getProperty("boolean").getType());
        assertEquals("true", testNode.getProperty("boolean").getString());
        assertTrue(testNode.getProperty("boolean").getBoolean());
        // Accessing Boolean property values as date, double or long results in a ValueFormatException:
        assertValueFormatExceptionForDate(testNode, "boolean");
        assertValueFormatExceptionForDouble(testNode, "boolean");
        assertValueFormatExceptionForLong(testNode, "boolean");

        assertTrue(testNode.hasProperty("double"));
        assertEquals(PropertyType.DOUBLE, testNode.getProperty("double").getType());
        assertEquals("1.25", testNode.getProperty("double").getString());
        assertEquals(1.25D, testNode.getProperty("double").getDouble());
        // We can read Double properties as long, boolean and date:
        assertEquals(1L, testNode.getProperty("double").getLong());
        assertFalse(testNode.getProperty("double").getBoolean());
        assertEquals(1L, testNode.getProperty("double").getDate().getTimeInMillis());

        assertTrue(testNode.hasProperty("long"));
        assertEquals(PropertyType.LONG, testNode.getProperty("long").getType());
        assertEquals("123456", testNode.getProperty("long").getString());
        assertEquals(123456L, testNode.getProperty("long").getLong());
        // We can read Long properties as double, boolean and date:
        assertEquals(123456D, testNode.getProperty("long").getDouble());
        assertFalse(testNode.getProperty("long").getBoolean());
        assertEquals(123456L, testNode.getProperty("long").getDate().getTimeInMillis());

        assertTrue(testNode.hasProperty("binary"));
        assertEquals(PropertyType.BINARY, testNode.getProperty("binary").getType());
        assertEquals("test bytes", testNode.getProperty("binary").getString());
        // Accessing Binary property values as date, double or long result in a ValueFormatException.
        assertValueFormatExceptionForDate(testNode, "binary");
        assertValueFormatExceptionForDouble(testNode, "binary");
        assertValueFormatExceptionForLong(testNode, "binary");

        assertTrue(testNode.hasProperty("calendar"));
        assertEquals(PropertyType.DATE, testNode.getProperty("calendar").getType());
        assertEquals(ISO8601.format(now), testNode.getProperty("calendar").getString());
        assertEquals(now, testNode.getProperty("calendar").getDate());
        // We can read Date properties as double and long:
        assertEquals(now.getTimeInMillis(), testNode.getProperty("calendar").getLong());
        assertEquals(((Long) now.getTimeInMillis()).doubleValue(), testNode.getProperty("calendar").getDouble());
        // Accessing Date property values as boolean results in a ValueFormatException:
        assertValueFormatExceptionForBoolean(testNode, "calendar");


    }

    @Test
    public void mockMagnoliaNodeWithProperties() throws RepositoryException, IOException {
        // What you do...
        // Stub properties when mocking a Node...
        Node testNode = NodeTestUtil.createNode("/testNode", "testWorkspace",
            "/testNode.string=testString"
            // As I see it, we can only mock single value String Properties this way
        );
        // or stub Properties for existing Node:
        testNode.setProperty("boolean", true);
        testNode.setProperty("double", 1.25D);
        testNode.setProperty("long", 123456L);
        // The magnolia test utils do not provide a MockBinary.
        Binary binary = ValueMockUtils.mockBinary("test bytes");
        testNode.setProperty("binary", binary);

        Calendar now = Calendar.getInstance();
        testNode.setProperty("calendar", now);

        // ... and what you get:
        assertTrue(testNode.hasProperties());
        // Here we have one Property less than expected!
        assertEquals(6L, testNode.getProperties().getSize());

        assertTrue(testNode.hasProperty("string"));
        assertEquals(PropertyType.STRING, testNode.getProperty("string").getType());
        assertEquals("testString", testNode.getProperty("string").getString());
        assertEquals("testString", testNode.getProperty("string").getValue().getString());
        // Accessing String property values as boolean results in a ValueFormatException:
        assertValueFormatExceptionForBoolean(testNode, "string");
        // Accessing String property values as date, double or long results in a ValueFormatException:
        assertValueFormatExceptionForDate(testNode, "string");
        assertValueFormatExceptionForDouble(testNode, "string");
        assertValueFormatExceptionForLong(testNode, "string");

        assertTrue(testNode.hasProperty("boolean"));
        assertEquals(PropertyType.BOOLEAN, testNode.getProperty("boolean").getType());
        assertEquals("true", testNode.getProperty("boolean").getString());
        assertTrue(testNode.getProperty("boolean").getBoolean());
        // Accessing Boolean property values as date, double or long results in a ValueFormatException:
        assertValueFormatExceptionForDate(testNode, "boolean");
        assertValueFormatExceptionForDouble(testNode, "boolean");
        assertValueFormatExceptionForLong(testNode, "boolean");

        assertTrue(testNode.hasProperty("double"));
        assertEquals(PropertyType.DOUBLE, testNode.getProperty("double").getType());
        assertEquals("1.25", testNode.getProperty("double").getString());
        assertEquals(1.25D, testNode.getProperty("double").getDouble());
        // Accessing Double property values as boolean, long or date results in a ValueFormatException:
        assertValueFormatExceptionForDate(testNode, "double");
        assertValueFormatExceptionForBoolean(testNode, "double");
        assertValueFormatExceptionForLong(testNode, "double");

        assertTrue(testNode.hasProperty("long"));
        assertEquals(PropertyType.LONG, testNode.getProperty("long").getType());
        assertEquals("123456", testNode.getProperty("long").getString());
        assertEquals(123456L, testNode.getProperty("long").getLong());
        // Accessing Long property values as boolean, double or date results in a ValueFormatException:
        assertValueFormatExceptionForDate(testNode, "long");
        assertValueFormatExceptionForBoolean(testNode, "long");
        assertValueFormatExceptionForDouble(testNode, "long");

        assertTrue(testNode.hasProperty("binary"));
        assertEquals(PropertyType.BINARY, testNode.getProperty("binary").getType());
        assertEquals("test bytes", testNode.getProperty("binary").getString());
        // Accessing Binary property values as date, double or long result in a ValueFormatException.
        assertValueFormatExceptionForDate(testNode, "binary");
        assertValueFormatExceptionForDouble(testNode, "binary");
        assertValueFormatExceptionForLong(testNode, "binary");

        assertTrue(testNode.hasProperty("calendar"));
        assertEquals(PropertyType.DATE, testNode.getProperty("calendar").getType());
        assertEquals(ISO8601.format(now), testNode.getProperty("calendar").getString());
        assertEquals(now, testNode.getProperty("calendar").getDate());
        // Accessing Date property values as boolean, double or long results in a ValueFormatException:
        assertValueFormatExceptionForBoolean(testNode, "calendar");
        assertValueFormatExceptionForDouble(testNode, "calendar");
        assertValueFormatExceptionForLong(testNode, "calendar");
    }

    private void assertValueFormatExceptionForDate(Node node, String name) throws RepositoryException {
        try {
            node.getProperty(name).getDate();
            fail();
        } catch (ValueFormatException e) {
            // ignore expected
        }
    }

    private void assertValueFormatExceptionForDouble(Node node, String name) throws RepositoryException {
        try {
            node.getProperty(name).getDouble();
            fail();
        } catch (ValueFormatException e) {
            // ignore expected
        }
    }

    private void assertValueFormatExceptionForLong(Node node, String name) throws RepositoryException {
        try {
            node.getProperty(name).getLong();
            fail();
        } catch (ValueFormatException e) {
            // ignore expected
        }
    }

    private void assertValueFormatExceptionForBoolean(Node node, String name) throws RepositoryException {
        try {
            node.getProperty(name).getBoolean();
            fail();
        } catch (ValueFormatException e) {
            // ignore expected
        }
    }

    @Test
    public void mockMockitoMultiValueProperties() throws RepositoryException {
        Node testNode = MagnoliaNodeMockUtils.mockMgnlNode("testNode", "testWorkspace", NodeTypes.ContentNode.NAME,
            stubProperty("string", "value1", "value2", "value3")
        );

        // ... and what you get:
        assertTrue(testNode.hasProperties());
        assertEquals(2L, testNode.getProperties().getSize());

        assertTrue(testNode.hasProperty("string"));
        assertEquals(PropertyType.STRING, testNode.getProperty("string").getType());
        // We get the first value when calling property.getString() - jcr behaviour now would be an ValueFormatException
        assertEquals("value1", testNode.getProperty("string").getString());
        assertEquals("value1", testNode.getProperty("string").getValue().getString());
        // We have correct multi value settings
        assertTrue(testNode.getProperty("string").isMultiple());
        assertEquals(3, testNode.getProperty("string").getValues().length);
        assertEquals("value1", testNode.getProperty("string").getValues()[0].getString());
        assertEquals("value2", testNode.getProperty("string").getValues()[1].getString());
        assertEquals("value3", testNode.getProperty("string").getValues()[2].getString());
    }

    @Test
    public void mockMagnoliaMultiValueProperties() throws RepositoryException, IOException {
        Node testNode = NodeTestUtil.createNode("/testNode", "testWorkspace", "/testNode");
        PropertyUtil.setProperty(testNode, "string", Arrays.asList("value1", "value2", "value3"));

        // ... and what you get:
        assertTrue(testNode.hasProperties());
        assertEquals(1L, testNode.getProperties().getSize());

        assertTrue(testNode.hasProperty("string"));
        assertEquals(PropertyType.STRING, testNode.getProperty("string").getType());
        // We get an ValueFormatException when calling property.getString() or property.getValue()
        try {
            assertEquals("value1", testNode.getProperty("string").getString());
            fail();
        } catch (ValueFormatException e) {
            // ignore
        }
        try {
            assertEquals("value1", testNode.getProperty("string").getValue().getString());
            fail();
        } catch (ValueFormatException e) {
            // ignore
        }
        // We have correct multi value settings
        assertTrue(testNode.getProperty("string").isMultiple());
        assertEquals(3, testNode.getProperty("string").getValues().length);
        assertEquals("value1", testNode.getProperty("string").getValues()[0].getString());
        assertEquals("value2", testNode.getProperty("string").getValues()[1].getString());
        assertEquals("value3", testNode.getProperty("string").getValues()[2].getString());
    }

    // Here we demonstrate how to test code that creates a URL for a node using info.magnolia.link.LinkUtil.
    @Test
    public void mockMockitoNodeWithInternalLink() throws RepositoryException {
        // 1. Create a mock for the link target node:
        Node linkTarget = MagnoliaNodeMockUtils.mockPageNode("linkTarget");
        // 2. If we want to create external links we need the base URL and file extension at the ServerConfiguration:
        mockServerConfiguration(stubDefaultBaseUrl("http://test.aperto.de"), stubDefaultExtension("html"));
        // ... and a context pat for absolute links:
        ContextMockUtils.mockWebContext(stubContextPath("/author"));

        assertEquals("/linkTarget.html", LinkUtil.createLink(linkTarget));
        assertEquals("/author/linkTarget.html", LinkUtil.createAbsoluteLink(linkTarget));
        assertEquals("http://test.aperto.de/linkTarget.html", LinkUtil.createExternalLink(linkTarget));
    }

    @Test
    public void mockMagnoliaNodeWithInternalLink() throws RepositoryException, IOException {
        // 1. Create a mock for the link target node:
        Node linkTarget = NodeTestUtil.createNode("/linkTarget", "webapp", "/linkTarget");
        // 2. If we want to create external links we need the base URL and file extension at the ServerConfiguration:
        ServerConfiguration config = new ServerConfiguration();
        config.setDefaultBaseUrl("http://test.aperto.de");
        config.setDefaultExtension("html");
        ComponentsTestUtil.setInstance(ServerConfiguration.class, config);
        // ... and a context pat for absolute links:
        ((MockWebContext) MockUtil.getMockContext(true)).setContextPath("/author");
        // And we need an I18nContentSupport instance:
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());

        assertEquals("/linkTarget.html", LinkUtil.createLink(linkTarget));
        assertEquals("/author/linkTarget.html", LinkUtil.createAbsoluteLink(linkTarget));
        assertEquals("http://test.aperto.de/linkTarget.html", LinkUtil.createExternalLink(linkTarget));
    }
}
