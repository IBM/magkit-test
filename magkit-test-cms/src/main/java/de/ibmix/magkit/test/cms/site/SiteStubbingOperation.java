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

import de.ibmix.magkit.assertions.Require;
import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.Domain;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.theme.ThemeReference;

import javax.jcr.RepositoryException;
import java.util.Map;

import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

/**
 * Factory for building {@link Site} related {@link StubbingOperation} instances that enrich Mockito based site mocks
 * with frequently required Magnolia specific configuration (name, domains, i18n, theme, variations).
 * <p>Usage pattern: Obtain or create a {@link Site} mock (e.g. via {@link SiteMockUtils#mockSite(String, SiteStubbingOperation...)})
 * and pass one or more operations produced by this class; each returned operation invokes {@link StubbingOperation#of(Object)} to apply
 * its stubbing logic. Operations are small and composable to keep test setup readable and intention revealing.</p>
 * <p>Behaviour & design notes:</p>
 * <ul>
 *   <li>All operations assert that the supplied {@link Site} instance is non-null (fail-fast with IllegalArgumentException).</li>
 *   <li>Varargs parameters (e.g. domains, theme stubbings) may be empty; {@code null} arrays lead to adding a {@code null} list element only if explicitly passed (rare).</li>
 *   <li>{@link #stubVariation(Site)} mutates the site variation map obtained from {@link Site#getVariations()} and re-stubs the getter to preserve the change.</li>
 *   <li>{@link #stubI18n()} provides a convenience overload creating a default {@link I18nContentSupport} mock.</li>
 * </ul>
 * <p>Thread-safety: Not thread-safe – intended for single-threaded unit tests.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-11-17
 * @see SiteMockUtils
 * @see ThemeMockUtils
 */
public abstract class SiteStubbingOperation implements StubbingOperation<Site> {

    /**
     * Creates an operation that stubs {@link Site#getName()} to return the provided value.
     *
     * @param value site name to set (may be {@code null} if such a state is desired for a negative test)
     * @return stubbing operation affecting {@link Site#getName()}
     */
    public static SiteStubbingOperation stubName(final String value) {
        return new SiteStubbingOperation() {
            @Override
            public void of(final Site site) {
                Require.Argument.notNull(site, "site should not be null");
                when(site.getName()).thenReturn(value);
            }
        };
    }

    /**
     * Creates an operation that stubs {@link Site#getDomains()} to return a list containing the supplied domains in order.
     * The list instance is created with {@link java.util.Arrays#asList(Object...)} and returned directly (no defensive copy).
     *
     * @param domains zero or more {@link Domain} definitions; may contain {@code null} entries
     * @return stubbing operation affecting {@link Site#getDomains()}
     */
    public static SiteStubbingOperation stubDomains(final Domain... domains) {
        return new SiteStubbingOperation() {
            @Override
            public void of(final Site site) {
                Require.Argument.notNull(site, "site should not be null");
                when(site.getDomains()).thenReturn(asList(domains));
            }
        };
    }

    /**
     * Convenience operation that creates a default {@link I18nContentSupport} mock via
     * {@link de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils#mockI18nContentSupport()} and delegates to
     * {@link #stubI18n(I18nContentSupport)}.
     *
     * @return stubbing operation setting {@link Site#getI18n()}
     * @throws RepositoryException if the underlying creation of {@link I18nContentSupport} throws (unlikely in tests)
     */
    public static SiteStubbingOperation stubI18n() throws RepositoryException {
        I18nContentSupport i18n = mockI18nContentSupport();
        return stubI18n(i18n);
    }

    /**
     * Creates an operation that stubs {@link Site#getI18n()} to return the supplied {@link I18nContentSupport} instance.
     * Existing behaviour is replaced unconditionally.
     *
     * @param i18n i18n content support mock or instance (may be {@code null} to simulate absence)
     * @return stubbing operation affecting {@link Site#getI18n()}
     */
    public static SiteStubbingOperation stubI18n(final I18nContentSupport i18n) {
        return new SiteStubbingOperation() {
            @Override
            public void of(final Site site) {
                Require.Argument.notNull(site, "site should not be null");
                when(site.getI18n()).thenReturn(i18n);
            }
        };
    }

    /**
     * Creates an operation that stubs {@link Site#getTheme()} to return the specified {@link ThemeReference}.
     *
     * @param theme theme reference to associate (may be {@code null})
     * @return stubbing operation affecting {@link Site#getTheme()}
     */
    public static SiteStubbingOperation stubTheme(final ThemeReference theme) {
        return new SiteStubbingOperation() {
            @Override
            public void of(final Site site) {
                Require.Argument.notNull(site, "site should not be null");
                when(site.getTheme()).thenReturn(theme);
            }
        };
    }

    /**
     * Convenience overload producing a {@link ThemeReference} mock (optionally enriched via {@link ThemeStubbingOperation}s)
     * and stubbing {@link Site#getTheme()} to return it.
     *
     * @param themeName required name of the theme reference mock
     * @param themeStubbings optional operations applied to the underlying {@link info.magnolia.module.site.theme.Theme}
     * @return stubbing operation affecting {@link Site#getTheme()}
     */
    public static SiteStubbingOperation stubTheme(final String themeName, ThemeStubbingOperation... themeStubbings) {
        return stubTheme(ThemeMockUtils.mockThemeReference(themeName, themeStubbings));
    }

    /**
     * Creates an operation adding (or updating) a site variation inside {@link Site#getVariations()} using the name of
     * the supplied variation {@link Site}. The existing map instance is mutated; afterwards the getter is re-stubbed to
     * return the mutated map ensuring Mockito does not lose changes if it would otherwise provide a fresh map.
     *
     * @param value variation site instance to add; if {@code null} the operation is a no-op
     * @return stubbing operation mutating {@link Site#getVariations()}
     */
    public static SiteStubbingOperation stubVariation(final Site value) {
        return new SiteStubbingOperation() {
            @Override
            public void of(final Site site) {
                Require.Argument.notNull(site, "site should not be null");
                Map<String, Site> variations = site.getVariations();
                Require.Argument.notNull(variations, "site variations should not be null");
                if (value != null) {
                    variations.put(value.getName(), value);
                    when(site.getVariations()).thenReturn(variations);
                }
            }
        };
    }

    /**
     * Convenience overload creating a variation {@link Site} (via {@link SiteMockUtils#mockSite(String, SiteStubbingOperation...)})
     * and delegating to {@link #stubVariation(Site)}.
     *
     * @param name variation site name
     * @param stubbings optional stubbing operations applied to the created variation site
     * @return stubbing operation mutating {@link Site#getVariations()}
     * @throws RepositoryException if underlying site creation/stubbing raises it
     */
    public static SiteStubbingOperation stubVariation(final String name, SiteStubbingOperation... stubbings) throws RepositoryException {
        Site value = SiteMockUtils.mockSite(name, stubbings);
        return stubVariation(value);
    }
}
