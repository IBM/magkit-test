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
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.module.site.CssResourceDefinition;
import info.magnolia.module.site.ResourceDefinition;
import info.magnolia.module.site.theme.Theme;
import info.magnolia.module.site.theme.ThemeReference;
import info.magnolia.module.site.theme.registry.ThemeRegistry;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class providing concise factory and helper methods to create Mockito mocks for {@link Theme},
 * {@link ThemeReference} and associated resource definition objects used in Magnolia site related tests.
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Create and register {@link Theme} mocks inside a (mocked) {@link ThemeRegistry} held in Magnolia's component provider.</li>
 *   <li>Provide lightweight helper factories for {@link CssResourceDefinition} and generic {@link ResourceDefinition} mocks.</li>
 *   <li>Optionally apply one or more {@link ThemeStubbingOperation} instances to enrich a newly created or existing theme mock.</li>
 * </ul>
 * <p>Registry handling: If no {@link ThemeRegistry} mock is registered yet, one is created via {@link ComponentsMockUtils#mockComponentInstance(Class)}.
 * When requesting a theme by name, an existing {@link DefinitionProvider} is reused; otherwise a new provider mock is created and registered.</p>
 * <p>Thread safety: ComponentProvider is backed by ThreadLocal and therefore thread-safe; intended for multithreaded test initialization code.</p>
 * <p>Null handling: The vararg parameters for stubbing operations are treated as optional (may be {@code null}). Individual elements are applied in order; {@code null} elements are ignored.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-06-05
 * @see ThemeStubbingOperation
 */
public final class ThemeMockUtils {

    /**
     * Create a {@link ThemeReference} mock (returning the supplied name) and ensure a corresponding {@link Theme} mock
     * is available and optionally stubbed via provided operations.
     * <p>Behavior:</p>
     * <ul>
     *   <li>Registers or reuses a theme mock named {@code name} inside the mocked {@link ThemeRegistry}.</li>
     *   <li>Applies each non-null {@link ThemeStubbingOperation} to the theme mock in the given order.</li>
     *   <li>Returns only the {@link ThemeReference}; access the theme via {@link #mockTheme(String, ThemeStubbingOperation...)} if needed.</li>
     * </ul>
     *
     * @param name required theme name (must not be {@code null})
     * @param stubbings optional sequence of stubbing operations; may be {@code null}
     * @return Mockito mock of {@link ThemeReference} bound to a (possibly newly created) theme mock
     * @see #mockTheme(String, ThemeStubbingOperation...)
     */
    public static ThemeReference mockThemeReference(String name, ThemeStubbingOperation... stubbings) {
        ThemeReference result = mock(ThemeReference.class);
        doReturn(name).when(result).getName();
        mockTheme(name, stubbings);
        return result;
    }

    /**
     * Obtain (or create) a {@link Theme} mock with the given name and apply optional stubbing operations.
     * <p>Implementation details:</p>
     * <ul>
     *   <li>Fetches the {@link ThemeRegistry} mock via {@link ComponentsMockUtils#mockComponentInstance(Class)} (creating it if absent).</li>
     *   <li>If no {@link DefinitionProvider} for {@code name} exists, a new provider mock is created and registered.</li>
     *   <li>If the provider currently returns {@code null}, a fresh theme mock is created and wired to the provider.</li>
     *   <li>Each non-null {@link ThemeStubbingOperation} is applied sequentially.</li>
     * </ul>
     * <p>Idempotency: Calling this repeatedly with the same name returns the same underlying theme instance (unless the registry mock was replaced externally).</p>
     *
     * @param name required theme name (must not be {@code null})
     * @param stubbings optional sequence of stubbing operations; may be {@code null}
     * @return existing or newly created theme mock with applied stubbings
     * @see ThemeStubbingOperation
     */
    @SuppressWarnings("unchecked")
    public static Theme mockTheme(String name, ThemeStubbingOperation... stubbings) {
        ThemeRegistry themeRegistry = ComponentsMockUtils.mockComponentInstance(ThemeRegistry.class);
        DefinitionProvider<Theme> definitionProvider = themeRegistry.getProvider(name);
        Theme theme;
        if (definitionProvider == null) {
            definitionProvider = mock(DefinitionProvider.class);
            theme = definitionProvider.get();
            if (theme == null) {
                theme = mockPlainTheme(name);
                when(definitionProvider.get()).thenReturn(theme);
            }
            when(themeRegistry.getProvider(name)).thenReturn(definitionProvider);
        } else {
            theme = definitionProvider.get();
        }

        if (stubbings != null) {
            for (ThemeStubbingOperation stubbing : stubbings) {
                if (stubbing != null) {
                    stubbing.of(theme);
                }
            }
        }
        return theme;
    }

    /**
     * Create a new standalone {@link Theme} mock with its {@link Theme#getName()} method stubbed to return the supplied name.
     * The theme is NOT registered in any {@link ThemeRegistry}.
     *
     * @param name required theme name (must not be {@code null})
     * @return new theme mock instance (unregistered)
     */
    public static Theme mockPlainTheme(String name) {
        Theme theme = mock(Theme.class);
        doReturn(name).when(theme).getName();
        return theme;
    }

    /**
     * Create a {@link CssResourceDefinition} mock with its key accessors stubbed.
     * No further behavior is simulated.
     *
     * @param link css resource link (may be {@code null})
     * @param media css media attribute (may be {@code null})
     * @param conditionalComment conditional comment (IE) value (may be {@code null})
     * @return mock of {@link CssResourceDefinition}
     */
    public static CssResourceDefinition mockCssFile(String link, String media, String conditionalComment) {
        CssResourceDefinition result = mock(CssResourceDefinition.class);
        doReturn(link).when(result).getLink();
        doReturn(media).when(result).getMedia();
        doReturn(conditionalComment).when(result).getConditionalComment();
        return result;
    }

    /**
     * Create a generic {@link ResourceDefinition} mock, typically used for JavaScript resources.
     *
     * @param link resource link (may be {@code null})
     * @param conditionalComment conditional comment (may be {@code null})
     * @return mock of {@link ResourceDefinition}
     */
    public static ResourceDefinition mockResource(String link, String conditionalComment) {
        ResourceDefinition result = mock(ResourceDefinition.class);
        doReturn(link).when(result).getLink();
        doReturn(conditionalComment).when(result).getConditionalComment();
        return result;
    }

    private ThemeMockUtils() {
    }
}
