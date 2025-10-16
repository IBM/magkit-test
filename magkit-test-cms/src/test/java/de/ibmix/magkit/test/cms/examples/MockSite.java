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
import de.ibmix.magkit.test.cms.site.SiteMockUtils;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.Components;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 * Demonstrate how to mock a Magnolia Site for testing code that uses Site and SiteManager.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-03-07
 */
public class MockSite {

    @BeforeEach
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockPlainSiteWithMagkit() throws RepositoryException {
        // 1) Mock a Site:
        Site site = SiteMockUtils.mockSite("mySite");
        assertEquals("mySite", site.getName());
        assertSame(site, Components.getComponent(SiteManager.class).getSite("mySite"));
        assertNotNull(site.getI18n());

        // 2) Mock the default Site:
        Site defaultSite = SiteMockUtils.mockDefaultSite();
        assertEquals("default", defaultSite.getName());
        assertSame(defaultSite, Components.getComponent(SiteManager.class).getDefaultSite());
        assertSame(defaultSite, Components.getComponent(SiteManager.class).getSite("default"));

        // 3) Mock the current Site:
        Site currentSite = SiteMockUtils.mockCurrentSite("currentSite");
        assertEquals("currentSite", currentSite.getName());
        assertSame(currentSite, Components.getComponent(SiteManager.class).getCurrentSite());
        assertSame(currentSite, Components.getComponent(SiteManager.class).getSite("currentSite"));

        // 4) Mock the assigned Site of a Node:
        Node node = mockNode("any/node");
        Site assignedSite = SiteMockUtils.mockAssignedSite(node, "assignedSite");
        assertEquals("assignedSite", assignedSite.getName());
        assertSame(assignedSite, Components.getComponent(SiteManager.class).getAssignedSite(node));
        assertSame(assignedSite, Components.getComponent(SiteManager.class).getSite("assignedSite"));
    }

    @Test
    public void mockPlainSiteWithMagnolia() {
        // The Magnolia TestUtils do not support mocking Site and SiteManager.
    }
}
