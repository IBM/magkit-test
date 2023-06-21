package de.ibmix.magkit.test.cms.tamplating;

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
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.module.site.CssResourceDefinition;
import info.magnolia.module.site.ResourceDefinition;
import info.magnolia.module.site.theme.Theme;
import info.magnolia.module.site.theme.ThemeReference;
import info.magnolia.module.site.theme.registry.ThemeRegistry;
import org.junit.Before;
import org.junit.Test;

import static de.ibmix.magkit.test.cms.tamplating.ThemeMockUtils.mockThemeReference;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing ThemeMockUtils.
 *
 * @author wolf.bubenik
 * @since 05.12.14
 */
public class ThemeMockUtilsTest {

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void testMockThemeReference() {
        ThemeStubbingOperation op = mock(ThemeStubbingOperation.class);
        ThemeReference themeReference = mockThemeReference("test", op);
        assertThat(themeReference, notNullValue());
        assertThat(themeReference.getName(), is("test"));

        // check that we have a SiteModule provider stubbed with a matching theme:
        ThemeRegistry themeRegistry = ComponentsMockUtils.getComponentSingleton(ThemeRegistry.class);
        assertThat(themeRegistry, notNullValue());
        Theme theme = themeRegistry.getProvider("test").get();
        assertThat(theme, notNullValue());
        assertThat(theme.getName(), is("test"));
        assertThat(theme.getCssFiles(), notNullValue());
        assertThat(theme.getCssFiles().size(), is(0));
        assertThat(theme.getJsFiles(), notNullValue());
        assertThat(theme.getJsFiles().size(), is(0));
        // check that provided stubbing operations have been executed for the theme mock
        verify(op, times(1)).of(theme);
    }

    @Test
    public void testMockCssFile() {
        ResourceDefinition file = ThemeMockUtils.mockResource("link", "comment");
        assertThat(file, notNullValue());
        assertThat(file.getLink(), is("link"));
        assertThat(file.getConditionalComment(), is("comment"));
    }

    @Test
    public void testMockResource() {
        CssResourceDefinition file = ThemeMockUtils.mockCssFile("link", "media", "comment");
        assertThat(file, notNullValue());
        assertThat(file.getLink(), is("link"));
        assertThat(file.getMedia(), is("media"));
        assertThat(file.getConditionalComment(), is("comment"));
    }
}
