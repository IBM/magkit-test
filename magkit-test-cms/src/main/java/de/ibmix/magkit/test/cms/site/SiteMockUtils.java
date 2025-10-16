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

import de.ibmix.magkit.assertations.Require;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Factory / utility methods for creating and enriching Mockito based Magnolia {@link Site} and {@link SiteManager} mocks.
 * <p>Goals:</p>
 * <ul>
 *   <li>Reduce boilerplate when setting up site related test fixtures (current site, default site, assigned site, named site).</li>
 *   <li>Ensure consistent registration of a {@link SiteManager} mock in the Magnolia component provider (via {@link ComponentsMockUtils}).</li>
 *   <li>Offer composable customization through {@link SiteStubbingOperation} and {@link SiteManagerStubbingOperation} instances.</li>
 * </ul>
 * <p>Behavioural notes:</p>
 * <ul>
 *   <li>Lazy creation: If a requested site already exists in the current mocked {@link SiteManager}, it is reused (idempotent behavior per test context).</li>
 *   <li>Default initialization for new sites: name, empty variations map (capacity 4), default i18n support (see {@link SiteStubbingOperation#stubI18n()}).</li>
 *   <li>Vararg stubbing arrays must be non-null (enforced by assertion) but may be empty.</li>
 *   <li>The first invocation creating a site ensures the site is registered with the manager via {@link SiteManagerStubbingOperation#stubSite(Site)}.</li>
 * </ul>
 * <p>Thread-safety: ComponentProvider is backed by ThreadLocal and therefore thread-safe; intended for multithreaded test initialization code.</p>
 * <p>Error handling: Uses Hamcrest {@code assertThat} for defensive null checks on stubbing arrays. Failing assertions raise {@link IllegalArgumentException}.</p>
 * <p>Typical usage:</p>
 * <pre>{@code
 * Site site = SiteMockUtils.mockSite("corporate",
 *     SiteStubbingOperation.stubName("corporate"),
 *     SiteStubbingOperation.stubTheme("corporate-theme")
 * );
 * }
 * </pre>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-11-17
 * @see SiteStubbingOperation
 * @see SiteManagerStubbingOperation
 * @see ThemeMockUtils
 */
public final class SiteMockUtils extends ComponentsMockUtils {

    private SiteMockUtils() {
    }

    /**
     * Obtain (or create) the current site from a mocked {@link SiteManager} and apply additional stubbing operations.
     * <p>Behaviour:</p>
     * <ul>
     *   <li>Ensures a {@link SiteManager} mock is present (created via {@link #mockPlainSiteManager()} if needed).</li>
     *   <li>If {@link SiteManager#getCurrentSite()} returns {@code null}, a site is created via {@link #mockSite(String, SiteStubbingOperation...)}.</li>
     *   <li>Registers the new site as current via {@link SiteManagerStubbingOperation#stubCurrentSite(Site)}.</li>
     *   <li>Applies each provided {@link SiteStubbingOperation} in order.</li>
     * </ul>
     *
     * @param name fallback site name used when a new site must be created (may be {@code null} or blank -> delegated logic decides)
     * @param stubbings non-null vararg of site stubbing operations (may be empty)
     * @return current site mock (existing or newly created)
     * @throws RepositoryException if underlying site creation or stubbing throws
     * @throws IllegalArgumentException if {@code stubbings} is null
     */
    public static Site mockCurrentSite(String name, SiteStubbingOperation... stubbings) throws RepositoryException {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
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

    /**
     * Obtain (or create) a named site through the mocked {@link SiteManager} and apply optional stubbings.
     * <p>Behaviour:</p>
     * <ul>
     *   <li>Blank or null {@code name} is normalized to "default".</li>
     *   <li>If the manager has no site with that name, a fresh {@link Site} mock is created and initialized:</li>
     *   <li>-- name property stubbed</li>
     *   <li>-- variations map initialized (capacity 4)</li>
     *   <li>-- default i18n support added</li>
     *   <li>-- registered with the manager via {@link SiteManagerStubbingOperation#stubSite(Site)}</li>
     *   <li>Provided {@link SiteStubbingOperation} instances are applied sequentially afterwards.</li>
     * </ul>
     *
     * @param name desired logical site name (null/blank -> "default")
     * @param stubbings optional site stubbing operations (non-null vararg)
     * @return existing or newly created site mock with applied stubbings
     * @throws RepositoryException if site creation or i18n stubbing raises it
     */
    public static Site mockSite(String name, SiteStubbingOperation... stubbings) throws RepositoryException {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
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

    /**
     * Obtain (or create) the default site (named "default") and apply the provided stubbings.
     * <p>If {@link SiteManager#getDefaultSite()} returns {@code null}, delegates to {@link #mockSite(String, SiteStubbingOperation...)} and
     * registers the result via {@link SiteManagerStubbingOperation#stubDefaultSite(Site)}.</p>
     *
     * @param stubbings non-null vararg of site stubbing operations (may be empty)
     * @return default site mock
     * @throws RepositoryException if creation or stubbing fails
     * @throws IllegalArgumentException if {@code stubbings} is null
     */
    public static Site mockDefaultSite(SiteStubbingOperation... stubbings) throws RepositoryException {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
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

    /**
     * Obtain (or create) the site assigned to a given content node using {@link SiteManager#getAssignedSite(Node)}.
     * <p>Behaviour:</p>
     * <ul>
     *   <li>If no site is assigned yet, a site with id {@code id} is created and registered for node {@code c}.</li>
     *   <li>If a site exists, each child node of {@code c} is also stubbed to return the same assigned site (mirrors Magnolia traversal behaviour in some setups).</li>
     *   <li>All provided {@link SiteStubbingOperation} instances are applied to the resulting site.</li>
     * </ul>
     *
     * @param c content node whose site assignment is required (must not be {@code null})
     * @param id fallback site name if a new site must be created
     * @param stubbings non-null vararg of site stubbing operations
     * @return assigned site mock
     * @throws RepositoryException if node iteration or site creation fails
     * @throws IllegalArgumentException if {@code stubbings} is null
     */
    public static Site mockAssignedSite(Node c, String id, SiteStubbingOperation... stubbings) throws RepositoryException {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
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

    /**
     * Create (or reuse) a {@link SiteManager} mock and apply optional stubbings.
     * <p>Creates a fresh manager via {@link #mockPlainSiteManager()} on every invocation; reuse depends on component provider caching
     * semantics (the factory registers the instance). All provided {@link SiteManagerStubbingOperation} operations are executed in order.</p>
     *
     * @param stubbings non-null vararg of site manager stubbing operations
     * @return site manager mock
     * @throws RepositoryException if a stubbing operation throws it
     * @throws IllegalArgumentException if {@code stubbings} is null
     */
    public static SiteManager mockSiteManager(SiteManagerStubbingOperation... stubbings) {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        SiteManager siteManager = mockPlainSiteManager();
        for (SiteManagerStubbingOperation stubbing : stubbings) {
            stubbing.of(siteManager);
        }
        return siteManager;
    }

    /**
     * Remove any registered {@link SiteManager} mock from the Magnolia component provider. Use between tests to avoid leakage.
     */
    public static void cleanSiteManager() {
        clearComponentProvider(SiteManager.class);
    }

    /**
     * Create a plain {@link SiteManager} mock (based on {@link DefaultSiteManager}) and register it in the component provider
     * both as concrete type and interface (via factory). Does not configure any sites by itself.
     *
     * @return newly created or existing (if already registered) site manager mock
     */
    public static SiteManager mockPlainSiteManager() {
        SiteManager result = mockComponentInstance(DefaultSiteManager.class);
        mockComponentFactory(SiteManager.class, result);
        return result;
    }
}
