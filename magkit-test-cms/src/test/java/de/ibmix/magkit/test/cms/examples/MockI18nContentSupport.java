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
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.MgnlInstantiationException;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.NodeTestUtil;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 * Demonstrate how to mock a Magnolia I18nContentSupport.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-03-09
 */
public class MockI18nContentSupport {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    /**
     * This test demonstrates, how to create a mock af I18nContentSupport using the ComponentsMockUtils of the Magkit.
     */
    @Test
    public void mockI18nContentSupportWithMagkit() throws RepositoryException {
        // 1) Create a I18nContentSupport mock:
        I18nContentSupport i18n = mockI18nContentSupport();
        // This mock is registered as component:
        assertThat(Components.getComponent(I18nContentSupport.class), is(i18n));
        // It provides a simple "echo behaviour" ...
        assertThat(i18n.isEnabled(), is(false));
        assertThat(i18n.toI18NURI("test"), is("test"));
        // ... for properties
        Node node = mockNode("Paul", stubProperty("property", "test"), stubProperty("property_zh", "test-zh"));
        assertThat(i18n.hasProperty(node, "property"), is(true));
        assertThat(i18n.getProperty(node, "property"), is(node.getProperty("property")));
        assertThat(i18n.getProperty(node, "property", Locale.CHINESE), is(node.getProperty("property_zh")));
    }

    @Test
    public void mockI18nContentSupportWithMagkitImplicit() throws RepositoryException {
        // When mocking a WebContext the I18nContentSupport is automatically mocked:
        ContextMockUtils.mockWebContext();
        assertThat(Components.getComponent(I18nContentSupport.class), notNullValue());
    }

    /**
     * This test demonstrates, how to create a mock af I18nContentSupport using the Magnolia-Test-Utils.
     */
    @Test
    public void mockI18nContentSupportWithMagnolia() throws RepositoryException, IOException {
        // The Magnolia-Test-Utils provide no mocking support for I18nContentSupport.
        // Within the Magnolia mock framework you use the default implementation directly:
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        I18nContentSupport i18n = Components.getComponent(I18nContentSupport.class);
        // You get the standard behaviour out of the box:
        assertThat(i18n.isEnabled(), is(false));
        assertThat(i18n.toI18NURI("test"), is("test"));
        // Same for properties:
        Node node = NodeTestUtil.createNode("/node", "webapp", "/node.property=test", "/node.property_zh=test_zh");
        assertThat(i18n.hasProperty(node, "property"), is(true));
        assertThat(i18n.getProperty(node, "property"), is(node.getProperty("property")));
        assertThat(i18n.getProperty(node, "property", Locale.CHINESE), is(node.getProperty("property_zh")));
    }

    @Test(expected = MgnlInstantiationException.class)
    public void mockI18nContentSupportWithMagnoliaImplicit() {
        // When mocking a WebContext magnolia does not create a I18nContentSupport:
        MockUtil.getMockContext(true);
        assertThat(Components.getComponent(I18nContentSupport.class), notNullValue());
    }
}
