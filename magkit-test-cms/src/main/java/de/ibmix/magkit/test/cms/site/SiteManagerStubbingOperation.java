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

import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Site manager stubs.
 *
 * @author wolf.bubenik
 * @since 17.11.2010
 */
public abstract class SiteManagerStubbingOperation {

    public abstract void of(SiteManager manager) throws RepositoryException;

    public static SiteManagerStubbingOperation stubCurrentSite(final String siteId, final SiteStubbingOperation... stubbings) throws RepositoryException {
        Site site = SiteMockUtils.mockSite(siteId, stubbings);
        return stubCurrentSite(site);
    }

    public static SiteManagerStubbingOperation stubCurrentSite(final Site site) {
        return new SiteManagerStubbingOperation() {

            public void of(SiteManager manager) throws RepositoryException {
                assertThat(manager, notNullValue());
                when(manager.getCurrentSite()).thenReturn(site);
                stubSite(site).of(manager);
            }
        };
    }

    public static SiteManagerStubbingOperation stubDefaultSite(final String siteId, final SiteStubbingOperation... stubbings) throws RepositoryException {
        Site site = SiteMockUtils.mockSite(siteId, stubbings);
        return stubDefaultSite(site);
    }

    public static SiteManagerStubbingOperation stubDefaultSite(final Site site) {
        return new SiteManagerStubbingOperation() {

            public void of(SiteManager manager) throws RepositoryException {
                assertThat(manager, notNullValue());
                when(manager.getDefaultSite()).thenReturn(site);
                stubSite(site).of(manager);
            }
        };
    }

    public static SiteManagerStubbingOperation stubAssignedSite(final Node content, final String siteId, final SiteStubbingOperation... stubbings) throws RepositoryException {
        Site site = SiteMockUtils.mockSite(siteId, stubbings);
        return stubAssignedSite(content, site);
    }

    public static SiteManagerStubbingOperation stubAssignedSite(final Node content, final Site site) {
        return new SiteManagerStubbingOperation() {

            public void of(SiteManager manager) throws RepositoryException {
                assertThat(manager, notNullValue());
                when(manager.getAssignedSite(content)).thenReturn(site);
                stubSite(site).of(manager);
                NodeIterator children = content.getNodes();
                while (children.hasNext()) {
                    stubAssignedSite(children.nextNode(), site).of(manager);
                }
            }
        };
    }

    public static SiteManagerStubbingOperation stubAssignedSite(final String domain, final String uri, final String siteId, final SiteStubbingOperation... stubbings) throws RepositoryException {
        Site site = SiteMockUtils.mockSite(siteId, stubbings);
        return stubAssignedSite(domain, uri, site);
    }

    public static SiteManagerStubbingOperation stubAssignedSite(final String domain, final String uri, final Site site) {
        return new SiteManagerStubbingOperation() {

            public void of(SiteManager manager) throws RepositoryException {
                assertThat(manager, notNullValue());
                when(manager.getAssignedSite(domain, uri)).thenReturn(site);
                stubSite(site).of(manager);
            }
        };
    }

    public static SiteManagerStubbingOperation stubSite(final String siteId, final SiteStubbingOperation... stubbings) throws RepositoryException {
        Site site = SiteMockUtils.mockSite(siteId, stubbings);
        return stubSite(site);
    }

    public static SiteManagerStubbingOperation stubSite(final Site site) {
        return new SiteManagerStubbingOperation() {
            @Override
            public void of(final SiteManager manager) {
                assertThat(manager, notNullValue());
                if (site != null) {
                    when(manager.getSite(site.getName())).thenReturn(site);
                    Collection<Site> sites = manager.getSites();
                    if (sites == null || sites.isEmpty()) {
                        sites = new ArrayList<>();
                        when(manager.getSites()).thenReturn(sites);
                    }
                    // First remove site with name of provided site from site collection (if existing).
                    sites.removeIf(existing -> StringUtils.equals(existing.getName(), site.getName()));
                    sites.add(site);
                }
            }
        };
    }
}
