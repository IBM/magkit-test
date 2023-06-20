package com.aperto.magkit.mockito;

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


import info.magnolia.imaging.ImagingSupport;
import info.magnolia.module.site.CssResourceDefinition;
import info.magnolia.module.site.ResourceDefinition;
import info.magnolia.module.site.theme.Theme;
import org.mockito.Mockito;

import java.util.List;

import static com.aperto.magkit.mockito.ThemeMockUtils.mockCssFile;
import static com.aperto.magkit.mockito.ThemeMockUtils.mockResource;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 * @since 05.06.13
 */
public abstract class ThemeStubbingOperation {

    public static ThemeStubbingOperation stubCssFiles(final String... links) {
        return new ThemeStubbingOperation() {

            @Override
            public void of(final Theme theme) {
                assertThat(theme, notNullValue());
                if (links != null) {
                    CssResourceDefinition[] files = new CssResourceDefinition[links.length];
                    for (int i = 0; i < links.length; i++) {
                        files[i] = mockCssFile(links[i], EMPTY, EMPTY);
                    }
                    stubCssFiles(files).of(theme);
                }
            }
        };
    }

    public static ThemeStubbingOperation stubCssFiles(final CssResourceDefinition... values) {
        return new ThemeStubbingOperation() {

            @Override
            public void of(final Theme theme) {
                assertThat(theme, notNullValue());
                List<CssResourceDefinition> files = theme.getCssFiles();
                files.addAll(asList(values));
            }
        };
    }

    public static ThemeStubbingOperation stubJsFiles(final ResourceDefinition... values) {
        return new ThemeStubbingOperation() {

            @Override
            public void of(final Theme theme) {
                assertThat(theme, notNullValue());
                List<ResourceDefinition> files = theme.getJsFiles();
                files.addAll(asList(values));
            }
        };
    }

    public static ThemeStubbingOperation stubJsFiles(final String... links) {
        return new ThemeStubbingOperation() {

            @Override
            public void of(final Theme theme) {
                assertThat(theme, notNullValue());
                if (links != null) {
                    ResourceDefinition[] files = new ResourceDefinition[links.length];
                    for (int i = 0; i < links.length; i++) {
                        files[i] = mockResource(links[i], EMPTY);
                    }
                    stubJsFiles(files).of(theme);
                }
            }
        };
    }

    public static ThemeStubbingOperation stubImagingSupport(ImagingSupport imagingSupport) {
        return new ThemeStubbingOperation() {
            @Override
            public void of(Theme theme) {
                assertThat(theme, notNullValue());
                Mockito.doReturn(imagingSupport).when(theme).getImaging();
            }
        };
    }

    public abstract void of(Theme theme);
}
