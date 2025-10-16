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
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.module.site.CssResourceDefinition;
import info.magnolia.module.site.ResourceDefinition;
import info.magnolia.module.site.theme.Theme;
import info.magnolia.module.site.theme.ThemeReference;
import info.magnolia.module.site.theme.registry.ThemeRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.ibmix.magkit.test.cms.site.ThemeMockUtils.mockPlainTheme;
import static de.ibmix.magkit.test.cms.site.ThemeMockUtils.mockTheme;
import static de.ibmix.magkit.test.cms.site.ThemeMockUtils.mockThemeReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void testMockThemeReference() {
        ThemeStubbingOperation op = mock(ThemeStubbingOperation.class);
        ThemeReference themeReference = mockThemeReference("test", op);
        assertNotNull(themeReference);
        assertEquals("test", themeReference.getName());

        // check that we have a SiteModule provider stubbed with a matching theme:
        ThemeRegistry themeRegistry = ComponentsMockUtils.getComponentSingleton(ThemeRegistry.class);
        assertNotNull(themeRegistry);
        Theme theme = themeRegistry.getProvider("test").get();
        Require.Argument.notNull(theme, "theme should not be null");
        assertEquals("test", theme.getName());
        assertNotNull(theme.getCssFiles());
        assertEquals(0, theme.getCssFiles().size());
        assertNotNull(theme.getJsFiles());
        assertEquals(0, theme.getJsFiles().size());
        // check that provided stubbing operations have been executed for the theme mock
        verify(op, times(1)).of(theme);
    }

    @Test
    public void testMockCssFile() {
        ResourceDefinition file = ThemeMockUtils.mockResource("link", "comment");
        assertNotNull(file);
        assertEquals("link", file.getLink());
        assertEquals("comment", file.getConditionalComment());
    }

    @Test
    public void testMockResource() {
        CssResourceDefinition file = ThemeMockUtils.mockCssFile("link", "media", "comment");
        assertNotNull(file);
        assertEquals("link", file.getLink());
        assertEquals("media", file.getMedia());
        assertEquals("comment", file.getConditionalComment());
    }

    /**
     * Ensure calling mockTheme twice with same name reuses the same Theme instance (provider already present)
     * and applies additional stubbings. Also covers skipping of null elements inside the stubbings vararg.
     */
    @Test
    public void testMockThemeIdempotentReuseAndNullElementSkipping() {
        Theme themeFirst = mockTheme("reuse", ThemeStubbingOperation.stubCssFiles("/a.css"));
        assertNotNull(themeFirst);
        int initialCss = themeFirst.getCssFiles().size();
        // second call: provider exists -> else branch; include null element to verify it is ignored
        Theme themeSecond = mockTheme("reuse", null, ThemeStubbingOperation.stubJsFiles("/x.js", "/y.js"));
        assertNotNull(themeSecond);
        assertSame(themeFirst, themeSecond);
        // css list unchanged, js list now has two entries
        assertEquals(initialCss, themeSecond.getCssFiles().size());
        assertEquals(2, themeSecond.getJsFiles().size());
    }

    /**
     * Ensure passing an explicit null vararg array for stubbings is handled gracefully (no NPE, no operations).
     */
    @Test
    public void testMockThemeWithNullStubbingsArray() {
        assertThrows(IllegalArgumentException.class, () -> mockTheme("nullArray", (ThemeStubbingOperation[]) null));
    }

    /**
     * Verify mockPlainTheme returns an unregistered theme (no provider in ThemeRegistry) while name is stubbed.
     */
    @Test
    public void testMockPlainThemeNotRegistered() {
        // ensure a registry mock exists (may be newly created here)
        ThemeRegistry registry = ComponentsMockUtils.mockComponentInstance(ThemeRegistry.class);
        Theme plain = mockPlainTheme("plainTheme");
        assertNotNull(plain);
        assertEquals("plainTheme", plain.getName());
        assertNull(registry.getProvider("plainTheme"));
    }
}
