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
import org.junit.Before;
import org.junit.Test;

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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _siteManager = mockSiteManager();
    }

    @Test
    public void testStubDefaultSite() throws RepositoryException {
        assertThat(_siteManager.getDefaultSite(), nullValue());
        assertThat(_siteManager.getSite("mySite"), nullValue());
        assertThat(_siteManager.getSites(), notNullValue());
        assertThat(_siteManager.getSites().size(), is(0));

        Site site = mockSite("mySite");
        SiteManagerStubbingOperation.stubDefaultSite(site).of(_siteManager);
        assertThat(_siteManager.getDefaultSite(), notNullValue());
        assertThat(_siteManager.getDefaultSite(), is(site));
        assertThat(_siteManager.getSite("mySite"), notNullValue());
        assertThat(_siteManager.getSite("mySite"), is(site));
        assertThat(_siteManager.getSites(), notNullValue());
        assertThat(_siteManager.getSites().size(), is(1));
    }

    @Test
    public void testStubAssignedSiteForDomain() throws RepositoryException {
        String domain = "domain";
        String uri = "http://test.aperto.de/site_manager";
        assertThat(_siteManager.getAssignedSite(domain, uri), nullValue());
        assertThat(_siteManager.getSite("aperto.de"), nullValue());
        assertThat(_siteManager.getSites(), notNullValue());
        assertThat(_siteManager.getSites().size(), is(0));

        Site site = mockSite("aperto.de");
        stubAssignedSite(domain, uri, site).of(_siteManager);
        assertThat(_siteManager.getAssignedSite(domain, uri), notNullValue());
        assertThat(_siteManager.getAssignedSite(domain, uri), is(site));
        assertThat(_siteManager.getSite("aperto.de"), notNullValue());
        assertThat(_siteManager.getSite("aperto.de"), is(site));
        assertThat(_siteManager.getSites(), notNullValue());
        assertThat(_siteManager.getSites().size(), is(1));
    }

    @Test
    public void stubCurrentSiteByIdCreatesAndRegistersSite() throws Exception {
        assertThat(_siteManager.getCurrentSite(), nullValue());
        stubCurrentSite("currentSite").of(_siteManager);
        Site current = _siteManager.getCurrentSite();
        assertThat(current, notNullValue());
        assertThat(_siteManager.getSite("currentSite"), is(current));
        assertThat(_siteManager.getSites(), notNullValue());
        assertThat(_siteManager.getSites().contains(current), is(true));
    }

    @Test
    public void stubCurrentSiteNull() throws Exception {
        stubCurrentSite((Site) null).of(_siteManager);
        assertThat(_siteManager.getCurrentSite(), nullValue());
        assertThat(_siteManager.getSites().isEmpty(), is(true));
    }

    @Test
    public void stubDefaultSiteById() throws Exception {
        assertThat(_siteManager.getDefaultSite(), nullValue());
        stubDefaultSite("defSite").of(_siteManager);
        Site def = _siteManager.getDefaultSite();
        assertThat(def, notNullValue());
        assertThat(_siteManager.getSite("defSite"), is(def));
    }

    @Test
    public void stubAssignedSiteByDomainUriById() throws Exception {
        String domain = "example.com";
        String uri = "/foo";
        assertThat(_siteManager.getAssignedSite(domain, uri), nullValue());
        stubAssignedSite(domain, uri, "domainSite").of(_siteManager);
        Site assigned = _siteManager.getAssignedSite(domain, uri);
        assertThat(assigned, notNullValue());
        assertThat(_siteManager.getSite("domainSite"), is(assigned));
    }

    @Test
    public void stubSiteNullIsNoOp() throws Exception {
        Collection<Site> original = _siteManager.getSites();
        stubSite((Site) null).of(_siteManager);
        assertThat(_siteManager.getSites(), is(original));
        assertThat(_siteManager.getSites().isEmpty(), is(true));
    }

    @Test
    public void stubSiteReplacesExistingWithSameName() throws Exception {
        Site s1 = mock(Site.class);
        when(s1.getName()).thenReturn("dupSite");
        stubSite(s1).of(_siteManager);
        assertThat(_siteManager.getSite("dupSite"), is(s1));
        assertThat(_siteManager.getSites().size(), is(1));

        Site s2 = mock(Site.class);
        when(s2.getName()).thenReturn("dupSite");
        stubSite(s2).of(_siteManager);
        assertThat(_siteManager.getSite("dupSite"), is(s2));
        assertThat(_siteManager.getSites().size(), is(1));
        assertThat(_siteManager.getSites().iterator().next(), is(s2));
    }

    @Test
    public void stubAssignedSiteRecursiveChildren() throws Exception {
        Node root = mockNode("root");
        Node child1 = mockNode("root/child1");
        Node child2 = mockNode("root/child1");

        Site site = mockSite("recursive");
        stubAssignedSite(root, site).of(_siteManager);

        assertThat(_siteManager.getAssignedSite(root), is(site));
        assertThat(_siteManager.getAssignedSite(child1), is(site));
        assertThat(_siteManager.getAssignedSite(child2), is(site));
        assertThat(_siteManager.getSite("recursive"), is(site));
    }

    @Test
    public void stubAssignedSiteWithNullSiteDoesNotRegister() throws Exception {
        Node root = mockNode();
        stubAssignedSite(root, (Site) null).of(_siteManager);
        assertThat(_siteManager.getAssignedSite(root), nullValue());
        assertThat(_siteManager.getSites().isEmpty(), is(true));
    }

    @Test(expected = AssertionError.class)
    public void stubCurrentSiteByIdNullManager() throws Exception {
        stubCurrentSite("x").of(null);
    }

    @Test(expected = AssertionError.class)
    public void stubDefaultSiteByIdNullManager() throws Exception {
        stubDefaultSite("y").of(null);
    }

    @Test(expected = AssertionError.class)
    public void stubAssignedSiteByDomainNullManager() throws Exception {
        stubAssignedSite("d", "/u", "s").of(null);
    }

    @Test(expected = AssertionError.class)
    public void stubSiteByIdNullManager() throws Exception {
        stubSite("z").of(null);
    }

    @Test(expected = AssertionError.class)
    public void stubAssignedSiteNodeNullManager() throws Exception {
        Node n = mockNode();
        stubAssignedSite(n, mockSite("nodeSite")).of(null);
    }

    @Test(expected = AssertionError.class)
    public void stubAssignedSiteNodeByIdNullManager() throws Exception {
        Node n = mockNode();
        stubAssignedSite(n, "nodeSite2").of(null);
    }
}
