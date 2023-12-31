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

import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.site.SiteManagerStubbingOperation.stubAssignedSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSiteManager;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
}
