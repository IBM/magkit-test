package com.aperto.magkit.mockito.examples;

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

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.mockito.I18nContentSupportMockUtils;
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

import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 * Demonstrate how to mock a Magnolia I18nContentSupport.
 *
 * @author wolf.bubenik
 * @since 09.03.16.
 */
public class MockI18nContentSupport {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockI18nContentSupportWithMagkit() throws RepositoryException {
        // 1) Ein I18nContentSupport-Mock erzeugen:
        I18nContentSupport i18n = I18nContentSupportMockUtils.mockI18nContentSupport();
        // Der Mock wird als Component registriert:
        assertThat(Components.getComponent(I18nContentSupport.class), is(i18n));
        // Der Mock bringt ein einfaches Echo-Verhalten als Basisimplementierung mit:
        assertThat(i18n.isEnabled(), is(false));
        assertThat(i18n.toI18NURI("test"), is("test"));
        // auch für properties
        Node node = mockNode("Paul", stubProperty("property", "test"));
        assertThat(i18n.hasProperty(node, "property"), is(true));
        assertThat(i18n.getProperty(node, "property"), is(node.getProperty("property")));
        // Dies sollte verbessert werden und die Property "name_locale" zurückgeben
        assertThat(i18n.getProperty(node, "property", Locale.CHINESE), is(node.getProperty("property")));
    }

    @Test
    public void mockI18nContentSupportWithMagkitImplicit() throws RepositoryException {
        // Beim Mocken eines WebContext wird immer auch ein I18nContentSuport mock angelegt:
        ContextMockUtils.mockWebContext();
        assertThat(Components.getComponent(I18nContentSupport.class), notNullValue());
    }

    @Test
    public void mockI18nContentSupportWithMagnolia() throws RepositoryException, IOException {
        // Die Magnolia-Test-Utils bringen keine Test-Utils für I18nContentSupport mit.
        // Statt dessen kann mit der Magnolia-Implementierung direkt gearbeitet werden.
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        I18nContentSupport i18n = Components.getComponent(I18nContentSupport.class);
        // Es steht das Standardverhalten zur Verfügung:
        assertThat(i18n.isEnabled(), is(false));
        assertThat(i18n.toI18NURI("test"), is("test"));
        // auch für properties
        Node node = NodeTestUtil.createNode("/node", "webapp", "/node.property=test", "/node.property_zh=test_zh");
        assertThat(i18n.hasProperty(node, "property"), is(true));
        assertThat(i18n.getProperty(node, "property"), is(node.getProperty("property")));
        assertThat(i18n.getProperty(node, "property", Locale.CHINESE), is(node.getProperty("property_zh")));
    }

    @Test(expected = MgnlInstantiationException.class)
    public void mockI18nContentSupportWithMagnoliaImplicit() throws RepositoryException {
        // Beim Mocken eines WebContext wird kein I18nContentSuport angelegt:
        MockUtil.getMockContext(true);
        assertThat(Components.getComponent(I18nContentSupport.class), notNullValue());
    }
}
