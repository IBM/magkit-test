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
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.Domain;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.theme.ThemeReference;

import javax.jcr.RepositoryException;
import java.util.Map;

import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for SiteStubbingOperation.
 * Stubbing operations to be used as parameters in SiteMockUtils.mock...(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-11-17
 */
public abstract class SiteStubbingOperation implements StubbingOperation<Site> {

    public static SiteStubbingOperation stubName(final String value) {
        return new SiteStubbingOperation() {

            @Override
            public void of(final Site site) {
                assertThat(site, notNullValue());
                when(site.getName()).thenReturn(value);
            }
        };
    }

    public static SiteStubbingOperation stubDomains(final Domain... domains) {
        return new SiteStubbingOperation() {

            @Override
            public void of(final Site site) {
                assertThat(site, notNullValue());
                when(site.getDomains()).thenReturn(asList(domains));
            }
        };
    }

    public static SiteStubbingOperation stubI18n() throws RepositoryException {
        I18nContentSupport i18n = mockI18nContentSupport();
        return stubI18n(i18n);
    }

    public static SiteStubbingOperation stubI18n(final I18nContentSupport i18n) {
        return new SiteStubbingOperation() {

            @Override
            public void of(final Site site) {
                assertThat(site, notNullValue());
                when(site.getI18n()).thenReturn(i18n);
            }
        };
    }

    public static SiteStubbingOperation stubTheme(final ThemeReference theme) {
        return new SiteStubbingOperation() {

            @Override
            public void of(final Site site) {
                assertThat(site, notNullValue());
                when(site.getTheme()).thenReturn(theme);
            }
        };
    }

    public static SiteStubbingOperation stubTheme(final String themeName, ThemeStubbingOperation... themeStubbings) {
        return stubTheme(ThemeMockUtils.mockThemeReference(themeName, themeStubbings));
    }

    public static SiteStubbingOperation stubVariation(final Site value) {
        return new SiteStubbingOperation() {
            @Override
            public void of(final Site site) {
                assertThat(site, notNullValue());
                Map<String, Site> variations = site.getVariations();
                assertThat(variations, notNullValue());
                if (value != null) {
                    variations.put(value.getName(), value);
                    when(site.getVariations()).thenReturn(variations);
                }
            }
        };
    }

    public static SiteStubbingOperation stubVariation(final String name, SiteStubbingOperation... stubbings) throws RepositoryException {
        Site value = SiteMockUtils.mockSite(name, stubbings);
        return stubVariation(value);
    }
}
