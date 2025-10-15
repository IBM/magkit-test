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
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.module.site.CssResourceDefinition;
import info.magnolia.module.site.ResourceDefinition;
import info.magnolia.module.site.theme.Theme;
import info.magnolia.module.site.theme.ThemeReference;
import info.magnolia.module.site.theme.registry.ThemeRegistry;
import org.junit.Before;
import org.junit.Test;

import static de.ibmix.magkit.test.cms.site.ThemeMockUtils.mockPlainTheme;
import static de.ibmix.magkit.test.cms.site.ThemeMockUtils.mockTheme;
import static de.ibmix.magkit.test.cms.site.ThemeMockUtils.mockThemeReference;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing ThemeMockUtils.
 *
 * Additional tests cover idempotent reuse of theme/provider, handling of null vararg and null elements
 * and behaviour of mockPlainTheme.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2014-12-05
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

    /**
     * Ensure calling mockTheme twice with same name reuses the same Theme instance (provider already present)
     * and applies additional stubbings. Also covers skipping of null elements inside the stubbings vararg.
     */
    @Test
    public void testMockThemeIdempotentReuseAndNullElementSkipping() {
        Theme themeFirst = mockTheme("reuse", ThemeStubbingOperation.stubCssFiles("/a.css"));
        assertThat(themeFirst, notNullValue());
        int initialCss = themeFirst.getCssFiles().size();
        // second call: provider exists -> else branch; include null element to verify it is ignored
        Theme themeSecond = mockTheme("reuse", null, ThemeStubbingOperation.stubJsFiles("/x.js", "/y.js"));
        assertThat(themeSecond, notNullValue());
        assertThat(themeSecond == themeFirst, is(true));
        // css list unchanged, js list now has two entries
        assertThat(themeSecond.getCssFiles().size(), is(initialCss));
        assertThat(themeSecond.getJsFiles().size(), is(2));
    }

    /**
     * Ensure passing an explicit null vararg array for stubbings is handled gracefully (no NPE, no operations).
     */
    @Test
    public void testMockThemeWithNullStubbingsArray() {
        Theme theme = mockTheme("nullArray", (ThemeStubbingOperation[]) null);
        assertThat(theme, notNullValue());
        assertThat(theme.getName(), is("nullArray"));
    }

    /**
     * Verify mockPlainTheme returns an unregistered theme (no provider in ThemeRegistry) while name is stubbed.
     */
    @Test
    public void testMockPlainThemeNotRegistered() {
        // ensure a registry mock exists (may be newly created here)
        ThemeRegistry registry = ComponentsMockUtils.mockComponentInstance(ThemeRegistry.class);
        Theme plain = mockPlainTheme("plainTheme");
        assertThat(plain, notNullValue());
        assertThat(plain.getName(), is("plainTheme"));
        assertThat(registry.getProvider("plainTheme"), nullValue());
    }
}
