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

import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.module.site.DefaultSiteManager;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.HashMap;

import static de.ibmix.magkit.test.cms.site.SiteManagerStubbingOperation.stubSite;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Site mocking.
 *
 * @author wolf.bubenik
 * @since 17.11.2010
 */
public final class SiteMockUtils extends ComponentsMockUtils {

    private SiteMockUtils() {
    }

    public static Site mockCurrentSite(String name, SiteStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        SiteManager siteManager = mockPlainSiteManager();
        Site site = siteManager.getCurrentSite();
        if (site == null) {
            site = mockSite(name);
            SiteManagerStubbingOperation.stubCurrentSite(site).of(siteManager);
        }
        for (SiteStubbingOperation stubbing : stubbings) {
            stubbing.of(site);
        }
        return site;
    }

    public static Site mockSite(String name, SiteStubbingOperation... stubbings) throws RepositoryException {
        String siteName = isBlank(name) ? "default" : name;
        SiteManager siteManager = mockPlainSiteManager();
        Site result = siteManager.getSite(siteName);
        if (result == null) {
            result = mock(Site.class);
            SiteStubbingOperation.stubName(siteName).of(result);
            when(result.getVariations()).thenReturn(new HashMap<>(4));
            SiteStubbingOperation.stubI18n().of(result);
            stubSite(result).of(siteManager);
        }
        for (SiteStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    public static Site mockDefaultSite(SiteStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        SiteManager siteManager = mockPlainSiteManager();
        Site site = siteManager.getDefaultSite();
        if (site == null) {
            site = mockSite("default");
            SiteManagerStubbingOperation.stubDefaultSite(site).of(siteManager);
        }
        for (SiteStubbingOperation stubbing : stubbings) {
            stubbing.of(site);
        }
        return site;
    }

    public static Site mockAssignedSite(Node c, String id, SiteStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        SiteManager siteManager = mockPlainSiteManager();
        Site site = siteManager.getAssignedSite(c);
        if (site == null) {
            site = mockSite(id);
            SiteManagerStubbingOperation.stubAssignedSite(c, site).of(siteManager);
        } else {
            NodeIterator children = c.getNodes();
            while (children.hasNext()) {
                SiteManagerStubbingOperation.stubAssignedSite(children.nextNode(), site).of(siteManager);
            }
        }
        for (SiteStubbingOperation stubbing : stubbings) {
            stubbing.of(site);
        }
        return site;
    }

    public static SiteManager mockSiteManager(SiteManagerStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        SiteManager siteManager = mockPlainSiteManager();
        for (SiteManagerStubbingOperation stubbing : stubbings) {
            stubbing.of(siteManager);
        }
        return siteManager;
    }

    public static void cleanSiteManager() {
        clearComponentProvider(SiteManager.class);
    }

    public static SiteManager mockPlainSiteManager() {
        SiteManager result = mockComponentInstance(DefaultSiteManager.class);
        mockComponentFactory(SiteManager.class, result);
        return result;
    }
}
