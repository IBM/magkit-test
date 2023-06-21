package de.ibmix.magkit.test.cms.context.examples;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

import static de.ibmix.magkit.test.cms.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static de.ibmix.magkit.test.cms.ServerConfigurationStubbingOperation.stubDefaultExtension;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 *
 * @author wolf.bubenik
 * @since 17.02.16.
 */
//CHECKSTYLE:OFF
public class MockProperties {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @After
    public void cleanUp() {
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        MgnlContext.setInstance(null);
    }

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
        assertThat(testNode.hasProperties(), is(true));
        assertThat(testNode.getProperties().getSize(), is(7L));

        assertThat(testNode.hasProperty("string"), is(true));
        assertThat(testNode.getProperty("string").getType(), is(PropertyType.STRING));
        assertThat(testNode.getProperty("string").getString(), is("testString"));
        assertThat(testNode.getProperty("string").getValue().getString(), is("testString"));
        // We can read String properties as boolean:
        assertThat(testNode.getProperty("string").getBoolean(), is(false));
        // Accessing String property values as date, double or long results in a ValueFormatException:
        assertValueFormatExceptionForDate(testNode, "string");
        assertValueFormatExceptionForDouble(testNode, "string");
        assertValueFormatExceptionForLong(testNode, "string");

        assertThat(testNode.hasProperty("boolean"), is(true));
        assertThat(testNode.getProperty("boolean").getType(), is(PropertyType.BOOLEAN));
        assertThat(testNode.getProperty("boolean").getString(), is("true"));
        assertThat(testNode.getProperty("boolean").getBoolean(), is(true));
        // Accessing Boolean property values as date, double or long results in a ValueFormatException:
        assertValueFormatExceptionForDate(testNode, "boolean");
        assertValueFormatExceptionForDouble(testNode, "boolean");
        assertValueFormatExceptionForLong(testNode, "boolean");

        assertThat(testNode.hasProperty("double"), is(true));
        assertThat(testNode.getProperty("double").getType(), is(PropertyType.DOUBLE));
        assertThat(testNode.getProperty("double").getString(), is("1.25"));
        assertThat(testNode.getProperty("double").getDouble(), is(1.25D));
        // We can read Double properties as long, boolean and date:
        assertThat(testNode.getProperty("double").getLong(), is(1L));
        assertThat(testNode.getProperty("double").getBoolean(), is(false));
        assertThat(testNode.getProperty("double").getDate().getTimeInMillis(), is(1L));

        assertThat(testNode.hasProperty("long"), is(true));
        assertThat(testNode.getProperty("long").getType(), is(PropertyType.LONG));
        assertThat(testNode.getProperty("long").getString(), is("123456"));
        assertThat(testNode.getProperty("long").getLong(), is(123456L));
        // We can read Long properties as double, boolean and date:
        assertThat(testNode.getProperty("long").getDouble(), is(123456D));
        assertThat(testNode.getProperty("long").getBoolean(), is(false));
        assertThat(testNode.getProperty("long").getDate().getTimeInMillis(), is(123456L));

        assertThat(testNode.hasProperty("binary"), is(true));
        assertThat(testNode.getProperty("binary").getType(), is(PropertyType.BINARY));
        assertThat(testNode.getProperty("binary").getString(), is("test bytes"));
        // Accessing Binary property values as date, double or long SHOULD result in a ValueFormatException, but DOES NOT:
//        assertValueFormatExceptionForDate(testNode, "binary");
//        assertValueFormatExceptionForDouble(testNode, "binary");
//        assertValueFormatExceptionForLong(testNode, "binary");

        assertThat(testNode.hasProperty("calendar"), is(true));
        assertThat(testNode.getProperty("calendar").getType(), is(PropertyType.DATE));
        assertThat(testNode.getProperty("calendar").getString(), is(ISO8601.format(now)));
        assertThat(testNode.getProperty("calendar").getDate(), is(now));
        // We can read Date properties as double and long:
        assertThat(testNode.getProperty("calendar").getLong(), is(now.getTimeInMillis()));
        assertThat(testNode.getProperty("calendar").getDouble(), is(((Long) now.getTimeInMillis()).doubleValue()));
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
//        assertThat(testNode.hasProperties(), is(true));
//        // Here we have one Property less than expected!
//        assertThat(testNode.getProperties().getSize(), is(6L));
//
//        assertThat(testNode.hasProperty("string"), is(true));
//        assertThat(testNode.getProperty("string").getType(), is(PropertyType.STRING));
//        assertThat(testNode.getProperty("string").getString(), is("testString"));
//        assertThat(testNode.getProperty("string").getValue().getString(), is("testString"));
//        // Accessing String property values as boolean results in a ValueFormatException:
//        assertValueFormatExceptionForBoolean(testNode, "string");
//        // Accessing String property values as date, double or long results in a ValueFormatException:
//        assertValueFormatExceptionForDate(testNode, "string");
//        assertValueFormatExceptionForDouble(testNode, "string");
//        assertValueFormatExceptionForLong(testNode, "string");
//
//        assertThat(testNode.hasProperty("boolean"), is(true));
//        assertThat(testNode.getProperty("boolean").getType(), is(PropertyType.BOOLEAN));
//        assertThat(testNode.getProperty("boolean").getString(), is("true"));
//        assertThat(testNode.getProperty("boolean").getBoolean(), is(true));
//        // Accessing Boolean property values as date, double or long results in a ValueFormatException:
//        assertValueFormatExceptionForDate(testNode, "boolean");
//        assertValueFormatExceptionForDouble(testNode, "boolean");
//        assertValueFormatExceptionForLong(testNode, "boolean");
//
//        assertThat(testNode.hasProperty("double"), is(true));
//        assertThat(testNode.getProperty("double").getType(), is(PropertyType.DOUBLE));
//        assertThat(testNode.getProperty("double").getString(), is("1.25"));
//        assertThat(testNode.getProperty("double").getDouble(), is(1.25D));
//        // Accessing Double property values as boolean, long or date results in a ValueFormatException:
//        assertValueFormatExceptionForDate(testNode, "double");
//        assertValueFormatExceptionForBoolean(testNode, "double");
//        assertValueFormatExceptionForLong(testNode, "double");
//
//        assertThat(testNode.hasProperty("long"), is(true));
//        assertThat(testNode.getProperty("long").getType(), is(PropertyType.LONG));
//        assertThat(testNode.getProperty("long").getString(), is("123456"));
//        assertThat(testNode.getProperty("long").getLong(), is(123456L));
//        // Accessing Long property values as boolean, double or date results in a ValueFormatException:
//        assertValueFormatExceptionForDate(testNode, "long");
//        assertValueFormatExceptionForBoolean(testNode, "long");
//        assertValueFormatExceptionForDouble(testNode, "long");
//
//        assertThat(testNode.hasProperty("binary"), is(true));
//        assertThat(testNode.getProperty("binary").getType(), is(PropertyType.BINARY));
//        assertThat(testNode.getProperty("binary").getString(), is("test bytes"));
//        assertValueFormatExceptionForDate(testNode, "binary");
//        assertValueFormatExceptionForDouble(testNode, "binary");
//        assertValueFormatExceptionForLong(testNode, "binary");
//
//        assertThat(testNode.hasProperty("calendar"), is(true));
//        assertThat(testNode.getProperty("calendar").getType(), is(PropertyType.DATE));
//        assertThat(testNode.getProperty("calendar").getString(), is(ISO8601.format(now)));
//        assertThat(testNode.getProperty("calendar").getDate(), is(now));
//        // Accessing Date property values as boolean, double or long results in a ValueFormatException:
//        assertValueFormatExceptionForBoolean(testNode, "calendar");
//        assertValueFormatExceptionForDouble(testNode, "calendar");
//        assertValueFormatExceptionForLong(testNode, "calendar");
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
        assertThat(testNode.hasProperties(), is(true));
        assertThat(testNode.getProperties().getSize(), is(2L));

        assertThat(testNode.hasProperty("string"), is(true));
        assertThat(testNode.getProperty("string").getType(), is(PropertyType.STRING));
        // We get the first value when calling property.getString() - jcr behaviour now would be an ValueFormatException
        assertThat(testNode.getProperty("string").getString(), is("value1"));
        assertThat(testNode.getProperty("string").getValue().getString(), is("value1"));
        // We have correct multi value settings
        assertThat(testNode.getProperty("string").isMultiple(), is(true));
        assertThat(testNode.getProperty("string").getValues().length, is(3));
        assertThat(testNode.getProperty("string").getValues()[0].getString(), is("value1"));
        assertThat(testNode.getProperty("string").getValues()[1].getString(), is("value2"));
        assertThat(testNode.getProperty("string").getValues()[2].getString(), is("value3"));
    }

    @Test
    public void mockMagnoliaMultiValueProperties() throws RepositoryException, IOException {
        Node testNode = NodeTestUtil.createNode("/testNode", "testWorkspace", "/testNode");
        PropertyUtil.setProperty(testNode, "string", Arrays.asList("value1", "value2", "value3"));

        // ... and what you get:
        assertThat(testNode.hasProperties(), is(true));
        assertThat(testNode.getProperties().getSize(), is(1L));

        assertThat(testNode.hasProperty("string"), is(true));
        assertThat(testNode.getProperty("string").getType(), is(PropertyType.STRING));
        // We get an ValueFormatException when calling property.getString() or property.getValue()
        try {
            assertThat(testNode.getProperty("string").getString(), is("value1"));
            fail();
        } catch (ValueFormatException e) {
            // ignore
        }
        try {
            assertThat(testNode.getProperty("string").getValue().getString(), is("value1"));
            fail();
        } catch (ValueFormatException e) {
            // ignore
        }
        // We have correct multi value settings
        assertThat(testNode.getProperty("string").isMultiple(), is(true));
        assertThat(testNode.getProperty("string").getValues().length, is(3));
        assertThat(testNode.getProperty("string").getValues()[0].getString(), is("value1"));
        assertThat(testNode.getProperty("string").getValues()[1].getString(), is("value2"));
        assertThat(testNode.getProperty("string").getValues()[2].getString(), is("value3"));
    }

    // Scenario: Es soll Code getestet werden, der LinkUtil verwendet, um die URL für einen Node zu erzeugen.
    @Test
    public void mockMockitoNodeWithInternalLink() throws RepositoryException {
        // 1. Einen Mock für das Link-Ziel erzeugen:
        Node linkTarget = MagnoliaNodeMockUtils.mockPageNode("linkTarget");
        // 2. Sollen externe Links erzeugt werden, benötigen wir noch den Host-Namen und eine Default-Dateiendung:
        mockServerConfiguration(stubDefaultBaseUrl("http://test.aperto.de"), stubDefaultExtension("html"));
        // ... und ggf einen Context-Pfat für absolute Links:
        ContextMockUtils.mockWebContext(stubContextPath("/author"));

        assertThat(LinkUtil.createLink(linkTarget), is("/linkTarget.html"));
        assertThat(LinkUtil.createAbsoluteLink(linkTarget), is("/author/linkTarget.html"));
        assertThat(LinkUtil.createExternalLink(linkTarget), is("http://test.aperto.de/linkTarget.html"));
    }

    @Test
    public void mockMagnoliaNodeWithInternalLink() throws RepositoryException, IOException {
        // 1. Einen Mock für das Link-Ziel erzeugen:
        Node linkTarget = NodeTestUtil.createNode("/linkTarget", "webapp", "/linkTarget");
        // 2. Sollen externe Links erzeugt werden, benötigen wir noch den Host-Namen und eine Default-Dateiendung:
        ServerConfiguration config = new ServerConfiguration();
        config.setDefaultBaseUrl("http://test.aperto.de");
        config.setDefaultExtension("html");
        ComponentsTestUtil.setInstance(ServerConfiguration.class, config);
        // ... und ggf einen Context-Pfat für absolute Links:
        ((MockWebContext) MockUtil.getMockContext(true)).setContextPath("/author");
        // Und wir benötigen noch einen I18nContentSupport:
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());

        assertThat(LinkUtil.createLink(linkTarget), is("/linkTarget.html"));
        assertThat(LinkUtil.createAbsoluteLink(linkTarget), is("/author/linkTarget.html"));
        assertThat(LinkUtil.createExternalLink(linkTarget), is("http://test.aperto.de/linkTarget.html"));
    }
}
