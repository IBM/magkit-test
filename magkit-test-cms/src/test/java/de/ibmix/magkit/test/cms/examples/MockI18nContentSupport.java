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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 * Demonstrate how to mock a Magnolia I18nContentSupport.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-03-09
 */
public class MockI18nContentSupport {

    @BeforeEach
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
        assertSame(i18n, Components.getComponent(I18nContentSupport.class));
        // It provides a simple "echo behaviour" ...
        assertFalse(i18n.isEnabled());
        assertEquals("test", i18n.toI18NURI("test"));
        // ... for properties
        Node node = mockNode("Paul", stubProperty("property", "test"), stubProperty("property_zh", "test-zh"));
        assertTrue(i18n.hasProperty(node, "property"));
        assertSame(node.getProperty("property"), i18n.getProperty(node, "property"));
        assertSame(node.getProperty("property_zh"), i18n.getProperty(node, "property", Locale.CHINESE));
    }

    @Test
    public void mockI18nContentSupportWithMagkitImplicit() throws RepositoryException {
        // When mocking a WebContext the I18nContentSupport is automatically mocked:
        ContextMockUtils.mockWebContext();
        assertNotNull(Components.getComponent(I18nContentSupport.class));
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
        assertFalse(i18n.isEnabled());
        assertEquals("test", i18n.toI18NURI("test"));
        // Same for properties:
        Node node = NodeTestUtil.createNode("/node", "webapp", "/node.property=test", "/node.property_zh=test_zh");
        assertTrue(i18n.hasProperty(node, "property"));
        assertSame(node.getProperty("property"), i18n.getProperty(node, "property"));
        assertSame(node.getProperty("property_zh"), i18n.getProperty(node, "property", Locale.CHINESE));
    }

    @Test
    public void mockI18nContentSupportWithMagnoliaImplicit() {
        // When mocking a WebContext magnolia does not create a I18nContentSupport:
        assertThrows(MgnlInstantiationException.class, () -> {
            MockUtil.getMockContext(true);
            assertNotNull(Components.getComponent(I18nContentSupport.class));
        });
    }
}
