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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.imaging.ImagingSupport;
import info.magnolia.module.site.theme.Theme;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;

/**
 * Test ThemeStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-10-30
 */
public class ThemeStubbingOperationTest {

    private Theme _theme;

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _theme = mock(Theme.class);
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubCssFiles() {
        assertThat(_theme.getCssFiles().isEmpty(), is(true));

        ThemeStubbingOperation.stubCssFiles("/file-1.css", "/test.css").of(_theme);
        assertThat(_theme.getCssFiles().size(), is(2));
        assertThat(_theme.getCssFiles().get(0).getLink(), is("/file-1.css"));
        assertThat(_theme.getCssFiles().get(0).getMedia(), is(""));
        assertThat(_theme.getCssFiles().get(0).getConditionalComment(), is(""));
        assertThat(_theme.getCssFiles().get(1).getLink(), is("/test.css"));
        assertThat(_theme.getCssFiles().get(1).getMedia(), is(""));
        assertThat(_theme.getCssFiles().get(1).getConditionalComment(), is(""));
    }

    @Test
    public void stubJsFiles() {
        assertThat(_theme.getJsFiles().isEmpty(), is(true));

        ThemeStubbingOperation.stubJsFiles("/file-1.js", "/test.js").of(_theme);
        assertThat(_theme.getJsFiles().size(), is(2));
        assertThat(_theme.getJsFiles().get(0).getLink(), is("/file-1.js"));
        assertThat(_theme.getJsFiles().get(0).getConditionalComment(), is(""));
        assertThat(_theme.getJsFiles().get(1).getLink(), is("/test.js"));
        assertThat(_theme.getJsFiles().get(1).getConditionalComment(), is(""));
    }

    @Test
    public void stubImagingSupport() {
        assertThat(_theme.getImaging(), nullValue());

        ImagingSupport imagingSupport = mock(ImagingSupport.class);
        ThemeStubbingOperation.stubImagingSupport(imagingSupport).of(_theme);
        assertThat(_theme.getImaging(), is(imagingSupport));
    }
}
