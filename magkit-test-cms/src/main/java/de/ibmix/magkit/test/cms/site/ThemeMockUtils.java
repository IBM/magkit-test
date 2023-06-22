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

import java.util.ArrayList;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock util class for creating Theme and ThemeReference mocks and register them in a ThemeRegistry mock.
 *
 * @author wolf.bubenik
 * @since 05.06.2013
 */
public final class ThemeMockUtils {

    /**
     * Creates a ThemeReference mock and Theme mock for the given theme name.
     * Used when mocking a Theme for a Site mock.
     *
     * @param name the name of the Theme
     * @param stubbings stubbing operations to stub the theme that will be mocked
     * @return the mockito mock of a ThemeReference
     */
    public static ThemeReference mockThemeReference(String name, ThemeStubbingOperation... stubbings) {
        ThemeReference result = mock(ThemeReference.class);
        doReturn(name).when(result).getName();
        mockTheme(name, stubbings);
        return result;
    }

    /**
     * Applies the stubbing operations on the theme mock registered at the ThemeRegistry with the given name.
     * If the Theme does not exist already a new mock will be created.
     * If no ThemeRegistry exists in the Components a new ThemeRegistry mock will be created.
     *
     * @param name the name of the Theme
     * @param stubbings stubbing operations to stub the theme that will be mocked
     * @return a Mockito mock for a Theme with the given name
     */
    public static Theme mockTheme(String name, ThemeStubbingOperation... stubbings) {
        Theme theme = null;
        ThemeRegistry themeRegistry = ComponentsMockUtils.mockComponentInstance(ThemeRegistry.class);
        DefinitionProvider<Theme> definitionProvider = themeRegistry.getProvider(name);
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

        for (ThemeStubbingOperation stubbing : stubbings) {
            stubbing.of(theme);
        }
        return theme;
    }

    /**
     * Creates a new Theme mock with the given name.
     * It will NOT be registered at a ThemeRegistry.
     *
     * @param name the name of the theme
     * @return a new Theme mock instance
     */
    public static Theme mockPlainTheme(String name) {
        Theme theme = mock(Theme.class);
        doReturn(name).when(theme).getName();
        doReturn(new ArrayList<CssResourceDefinition>()).when(theme).getCssFiles();
        doReturn(new ArrayList<ResourceDefinition>()).when(theme).getJsFiles();
        return theme;
    }

    /**
     * Creates a new CssResourceDefinition mock.
     *
     * @param link the css resource link as String
     * @param media the css resource media as String
     * @param conditionalComment the css resources conditional comment
     * @return the new CssResourceDefinition mock
     */
    public static CssResourceDefinition mockCssFile(String link, String media, String conditionalComment) {
        CssResourceDefinition result = mock(CssResourceDefinition.class);
        doReturn(link).when(result).getLink();
        doReturn(media).when(result).getMedia();
        doReturn(conditionalComment).when(result).getConditionalComment();
        return result;
    }

    /**
     * Creates a new ResourceDefinition mock.
     *
     * @param link the resource link as String
     * @param conditionalComment the resources conditional comment
     * @return the new CssResourceDefinition mock
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
