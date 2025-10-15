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

import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;

import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSite;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Factory methods producing {@link SiteManagerStubbingOperation} instances that configure Mockito based {@link SiteManager} mocks.
 * <p>Scope & purpose:</p>
 * <ul>
 *   <li>Provide concise operations to stub current, default and assigned sites.</li>
 *   <li>Register / update individual {@link Site} instances within a {@link SiteManager} (including its internal site collection).</li>
 *   <li>Support recursive assignment of a site to all descendant nodes (for node based assignment helper).</li>
 * </ul>
 * <p>Design notes:</p>
 * <ul>
 *   <li>Each factory returns a stateless operation implementing {@link StubbingOperation#of(Object)}.</li>
 *   <li>All operations assert the target {@link SiteManager} argument is non-null (fail fast in test setup).</li>
 *   <li>Methods with a {@code siteId} parameter delegate site creation to {@link SiteMockUtils#mockSite(String, SiteStubbingOperation...)} applying provided site stubbings first.</li>
 *   <li>{@link #stubSite(Site)} maintains the manager's site list: previous site with same name removed, new added.</li>
 *   <li>Recursive node assignment catches {@link RepositoryException} silently (documented improbable in mocked context).</li>
 * </ul>
 * <p>Error handling: Assertions only; repository exceptions are declared where underlying site creation may throw.</p>
 * <p>Typical usage:</p>
 * <pre>{@code
 * SiteManager manager = SiteMockUtils.mockPlainSiteManager();
 * SiteManagerStubbingOperation.stubCurrentSite("corporate",
 *     SiteStubbingOperation.stubTheme("corp-theme")
 * ).of(manager);
 * }
 * </pre>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-11-17
 * @see SiteMockUtils
 * @see SiteStubbingOperation
 */
public abstract class SiteManagerStubbingOperation implements StubbingOperation<SiteManager> {

    /**
     * Create an operation that sets the manager's current site to a newly created (or existing) site with the given id.
     * Provided {@link SiteStubbingOperation}s are applied to the site before it is registered as current.
     *
     * @param siteId logical site name (may be null/blank -> passed through to {@link SiteMockUtils#mockSite(String, SiteStubbingOperation...)})
     * @param stubbings optional site stubbing operations (may be empty)
     * @return operation stubbing {@link SiteManager#getCurrentSite()}
     * @throws RepositoryException if site creation or stubbing raises it
     */
    public static SiteManagerStubbingOperation stubCurrentSite(final String siteId, final SiteStubbingOperation... stubbings) throws RepositoryException {
        Site site = mockSite(siteId, stubbings);
        return stubCurrentSite(site);
    }

    /**
     * Create an operation that sets the manager's current site to the supplied {@link Site}.
     * Also ensures the site is registered in the manager via {@link #stubSite(Site)}.
     *
     * @param site site instance to register as current (may be null to simulate absence)
     * @return operation stubbing {@link SiteManager#getCurrentSite()}
     */
    public static SiteManagerStubbingOperation stubCurrentSite(final Site site) {
        return new SiteManagerStubbingOperation() {
            @Override
            public void of(SiteManager manager) {
                assertThat(manager, notNullValue());
                when(manager.getCurrentSite()).thenReturn(site);
                stubSite(site).of(manager);
            }
        };
    }

    /**
     * Create an operation that sets the manager's default site to a new (or existing) site with the given id.
     * The site is first created and customized using the provided stubbings.
     *
     * @param siteId logical site name
     * @param stubbings optional site stubbing operations
     * @return operation stubbing {@link SiteManager#getDefaultSite()}
     * @throws RepositoryException if site creation or stubbing raises it
     */
    public static SiteManagerStubbingOperation stubDefaultSite(final String siteId, final SiteStubbingOperation... stubbings) throws RepositoryException {
        Site site = mockSite(siteId, stubbings);
        return stubDefaultSite(site);
    }

    /**
     * Create an operation that sets the manager's default site to the provided site and registers it.
     *
     * @param site site instance to mark as default (may be null)
     * @return operation stubbing {@link SiteManager#getDefaultSite()}
     */
    public static SiteManagerStubbingOperation stubDefaultSite(final Site site) {
        return new SiteManagerStubbingOperation() {
            @Override
            public void of(SiteManager manager) {
                assertThat(manager, notNullValue());
                when(manager.getDefaultSite()).thenReturn(site);
                stubSite(site).of(manager);
            }
        };
    }

    /**
     * Create an operation that assigns a site (created for siteId) to the given content node and recursively to all its children.
     *
     * @param content root content node for assignment (must not be null; underlying calls may iterate its children)
     * @param siteId site name used for site creation
     * @param stubbings optional site stubbing operations
     * @return operation stubbing {@link SiteManager#getAssignedSite(Node)} for the node hierarchy
     * @throws RepositoryException if site creation or stubbing raises it
     */
    public static SiteManagerStubbingOperation stubAssignedSite(final Node content, final String siteId, final SiteStubbingOperation... stubbings) throws RepositoryException {
        Site site = mockSite(siteId, stubbings);
        return stubAssignedSite(content, site);
    }

    /**
     * Create an operation that assigns the given site to the content node and recursively to its descendants.
     * The site is also registered with the manager via {@link #stubSite(Site)}.
     *
     * @param content root content node (must not be null)
     * @param site site instance to assign (may be null)
     * @return operation stubbing {@link SiteManager#getAssignedSite(Node)} for the node subtree
     */
    public static SiteManagerStubbingOperation stubAssignedSite(final Node content, final Site site) {
        return new SiteManagerStubbingOperation() {
            @Override
            public void of(SiteManager manager) {
                assertThat(manager, notNullValue());
                when(manager.getAssignedSite(content)).thenReturn(site);
                stubSite(site).of(manager);
                try {
                    NodeIterator children = content.getNodes();
                    while (children.hasNext()) {
                        stubAssignedSite(children.nextNode(), site).of(manager);
                    }
                } catch (RepositoryException e) {
                    // Ignored: repository exceptions are not expected for mocked nodes.
                }
            }
        };
    }

    /**
     * Create an operation that assigns a site (created for siteId) to requests by domain + URI.
     *
     * @param domain incoming request domain (host name)
     * @param uri request URI path segment
     * @param siteId site name for site creation
     * @param stubbings optional site stubbing operations
     * @return operation stubbing {@link SiteManager#getAssignedSite(String, String)}
     * @throws RepositoryException if site creation or stubbing raises it
     */
    public static SiteManagerStubbingOperation stubAssignedSite(final String domain, final String uri, final String siteId, final SiteStubbingOperation... stubbings) throws RepositoryException {
        Site site = mockSite(siteId, stubbings);
        return stubAssignedSite(domain, uri, site);
    }

    /**
     * Create an operation that assigns the supplied site to lookups by domain + URI.
     * Also registers / updates the site in the manager.
     *
     * @param domain request domain
     * @param uri request URI
     * @param site site instance to assign
     * @return operation stubbing {@link SiteManager#getAssignedSite(String, String)}
     */
    public static SiteManagerStubbingOperation stubAssignedSite(final String domain, final String uri, final Site site) {
        return new SiteManagerStubbingOperation() {
            @Override
            public void of(SiteManager manager) {
                assertThat(manager, notNullValue());
                when(manager.getAssignedSite(domain, uri)).thenReturn(site);
                stubSite(site).of(manager);
            }
        };
    }

    /**
     * Create an operation that registers a newly created site (by id) within the manager's internal site collection and
     * stubs direct lookup via {@link SiteManager#getSite(String)}.
     *
     * @param siteId site name used for site creation
     * @param stubbings optional site stubbing operations
     * @return operation registering the site
     * @throws RepositoryException if site creation or stubbing raises it
     */
    public static SiteManagerStubbingOperation stubSite(final String siteId, final SiteStubbingOperation... stubbings) throws RepositoryException {
        Site site = mockSite(siteId, stubbings);
        return stubSite(site);
    }

    /**
     * Create an operation that registers (or replaces) the supplied site inside the manager's site list and stubs
     * {@link SiteManager#getSite(String)} to return it. If a site with the same name exists it is removed first.
     *
     * @param site site instance to register (may be null -> no-op)
     * @return operation updating the manager's site registry
     */
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
                    // Remove previous site with same name before adding updated instance.
                    sites.removeIf(existing -> StringUtils.equals(existing.getName(), site.getName()));
                    sites.add(site);
                }
            }
        };
    }
}
