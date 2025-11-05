package de.ibmix.magkit.test.cms.site;

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
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.Collection;

import static de.ibmix.magkit.test.cms.site.SiteManagerStubbingOperation.stubAssignedSite;
import static de.ibmix.magkit.test.cms.site.SiteManagerStubbingOperation.stubCurrentSite;
import static de.ibmix.magkit.test.cms.site.SiteManagerStubbingOperation.stubDefaultSite;
import static de.ibmix.magkit.test.cms.site.SiteManagerStubbingOperation.stubSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSiteManager;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testing SiteManagerStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-07-24
 */
public class SiteManagerStubbingOperationTest {

    private SiteManager _siteManager;

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _siteManager = mockSiteManager();
    }

    @Test
    public void testStubDefaultSite() throws RepositoryException {
        assertNull(_siteManager.getDefaultSite());
        assertNull(_siteManager.getSite("mySite"));
        assertNotNull(_siteManager.getSites());
        assertEquals(0, _siteManager.getSites().size());

        Site site = mockSite("mySite");
        SiteManagerStubbingOperation.stubDefaultSite(site).of(_siteManager);
        assertNotNull(_siteManager.getDefaultSite());
        assertSame(site, _siteManager.getDefaultSite());
        assertNotNull(_siteManager.getSite("mySite"));
        assertSame(site, _siteManager.getSite("mySite"));
        assertNotNull(_siteManager.getSites());
        assertEquals(1, _siteManager.getSites().size());
    }

    @Test
    public void testStubAssignedSiteForDomain() throws RepositoryException {
        String domain = "domain";
        String uri = "http://test.aperto.de/site_manager";
        assertNull(_siteManager.getAssignedSite(domain, uri));
        assertNull(_siteManager.getSite("aperto.de"));
        assertNotNull(_siteManager.getSites());
        assertEquals(0, _siteManager.getSites().size());

        Site site = mockSite("aperto.de");
        stubAssignedSite(domain, uri, site).of(_siteManager);
        assertNotNull(_siteManager.getAssignedSite(domain, uri));
        assertSame(site, _siteManager.getAssignedSite(domain, uri));
        assertNotNull(_siteManager.getSite("aperto.de"));
        assertSame(site, _siteManager.getSite("aperto.de"));
        assertNotNull(_siteManager.getSites());
        assertEquals(1, _siteManager.getSites().size());
    }

    @Test
    public void stubCurrentSiteByIdCreatesAndRegistersSite() throws Exception {
        assertNull(_siteManager.getCurrentSite());
        stubCurrentSite("currentSite").of(_siteManager);
        Site current = _siteManager.getCurrentSite();
        assertNotNull(current);
        assertSame(current, _siteManager.getSite("currentSite"));
        assertNotNull(_siteManager.getSites());
        assertTrue(_siteManager.getSites().contains(current));
    }

    @Test
    public void stubCurrentSiteNull() {
        stubCurrentSite(null).of(_siteManager);
        assertNull(_siteManager.getCurrentSite());
        assertTrue(_siteManager.getSites().isEmpty());
    }

    @Test
    public void stubDefaultSiteById() throws Exception {
        assertNull(_siteManager.getDefaultSite());
        stubDefaultSite("defSite").of(_siteManager);
        Site def = _siteManager.getDefaultSite();
        assertNotNull(def);
        assertSame(def, _siteManager.getSite("defSite"));
    }

    @Test
    public void stubAssignedSiteByDomainUriById() throws Exception {
        String domain = "example.com";
        String uri = "/foo";
        assertNull(_siteManager.getAssignedSite(domain, uri));
        stubAssignedSite(domain, uri, "domainSite").of(_siteManager);
        Site assigned = _siteManager.getAssignedSite(domain, uri);
        assertNotNull(assigned);
        assertSame(assigned, _siteManager.getSite("domainSite"));
    }

    @Test
    public void stubSiteNullIsNoOp() {
        Collection<Site> original = _siteManager.getSites();
        stubSite(null).of(_siteManager);
        assertTrue(_siteManager.getSites().isEmpty());
    }

    @Test
    public void stubSiteReplacesExistingWithSameName() {
        Site s1 = mock(Site.class);
        when(s1.getName()).thenReturn("dupSite");
        stubSite(s1).of(_siteManager);
        assertSame(s1, _siteManager.getSite("dupSite"));
        assertEquals(1, _siteManager.getSites().size());

        Site s2 = mock(Site.class);
        when(s2.getName()).thenReturn("dupSite");
        stubSite(s2).of(_siteManager);
        assertSame(s2, _siteManager.getSite("dupSite"));
        assertEquals(1, _siteManager.getSites().size());
        assertSame(s2, _siteManager.getSites().iterator().next());
    }

    @Test
    public void stubAssignedSiteRecursiveChildren() throws Exception {
        Node root = mockNode("root");
        Node child1 = mockNode("root/child1");
        Node child2 = mockNode("root/child1");

        Site site = mockSite("recursive");
        stubAssignedSite(root, site).of(_siteManager);

        assertSame(site, _siteManager.getAssignedSite(root));
        assertSame(site, _siteManager.getAssignedSite(child1));
        assertSame(site, _siteManager.getAssignedSite(child2));
        assertSame(site, _siteManager.getSite("recursive"));
    }

    @Test
    public void stubAssignedSiteWithNullSiteDoesNotRegister() throws Exception {
        Node root = mockNode();
        stubAssignedSite(root, null).of(_siteManager);
        assertNull(_siteManager.getAssignedSite(root));
        assertTrue(_siteManager.getSites().isEmpty());
    }

    @Test
    public void stubCurrentSiteByIdNullManager() {
        assertThrows(IllegalArgumentException.class, () -> stubCurrentSite("x").of(null));
    }

    @Test
    public void stubDefaultSiteByIdNullManager() {
        assertThrows(IllegalArgumentException.class, () -> stubDefaultSite("y").of(null));
    }

    @Test
    public void stubAssignedSiteByDomainNullManager() {
        assertThrows(IllegalArgumentException.class, () -> stubAssignedSite("d", "/u", "s").of(null));
    }

    @Test
    public void stubSiteByIdNullManager() {
        assertThrows(IllegalArgumentException.class, () -> stubSite("z").of(null));
    }

    @Test
    public void stubAssignedSiteNodeNullManager() throws RepositoryException {
        Node n = mockNode();
        assertThrows(IllegalArgumentException.class, () -> stubAssignedSite(n, mockSite("nodeSite")).of(null));
    }

    @Test
    public void stubAssignedSiteNodeByIdNullManager() throws RepositoryException {
        Node n = mockNode();
        assertThrows(IllegalArgumentException.class, () -> stubAssignedSite(n, "nodeSite2").of(null));
    }
}
