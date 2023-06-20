package de.ibmix.magkit.mockito.examples;

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

import de.ibmix.magkit.mockito.ContextMockUtils;
import de.ibmix.magkit.mockito.SiteMockUtils;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.Components;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 * Demonstrate how to mock a Magnolia Site for testing code that uses Site and SiteManager.
 *
 * @author wolf.bubenik
 * @since 07.03.16.
 */
public class MockSite {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockPlainSiteWithMagkit() throws RepositoryException {
        // 1) Eine beliebige Site mocken:
        Site site = SiteMockUtils.mockSite("mySite");
        assertThat(site.getName(), is("mySite"));
        assertThat(Components.getComponent(SiteManager.class).getSite("mySite"), is(site));
        assertThat(site.getI18n(), notNullValue());

        // 2) Die Default-Site mocken:
        Site defaultSite = SiteMockUtils.mockDefaultSite();
        assertThat(defaultSite.getName(), is("default"));
        assertThat(Components.getComponent(SiteManager.class).getDefaultSite(), is(defaultSite));
        assertThat(Components.getComponent(SiteManager.class).getSite("default"), is(defaultSite));

        // 3) Die CurrentSite mocken:
        Site currentSite = SiteMockUtils.mockCurrentSite("currentSite");
        assertThat(currentSite.getName(), is("currentSite"));
        assertThat(Components.getComponent(SiteManager.class).getCurrentSite(), is(currentSite));
        assertThat(Components.getComponent(SiteManager.class).getSite("currentSite"), is(currentSite));

        // 4) Die assigned Site für einen Knoten mocken:
        Node node = mockNode("any/node");
        Site assignedSite = SiteMockUtils.mockAssignedSite(node, "assignedSite");
        assertThat(assignedSite.getName(), is("assignedSite"));
        assertThat(Components.getComponent(SiteManager.class).getAssignedSite(node), is(assignedSite));
        assertThat(Components.getComponent(SiteManager.class).getSite("assignedSite"), is(assignedSite));
    }

    @Test
    public void mockPlainSiteWithMagnolia() {
        // Magnolia bietet keinerlei Unterstützung für das Testen von Code, der Sites und SiteManager verwendet.
    }
}
