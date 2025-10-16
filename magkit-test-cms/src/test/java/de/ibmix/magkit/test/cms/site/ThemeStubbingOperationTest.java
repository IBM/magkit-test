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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test ThemeStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-10-30
 */
public class ThemeStubbingOperationTest {

    private Theme _theme;

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _theme = mock(Theme.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubCssFiles() {
        assertTrue(_theme.getCssFiles().isEmpty());

        ThemeStubbingOperation.stubCssFiles("/file-1.css", "/test.css").of(_theme);
        assertEquals(2, _theme.getCssFiles().size());
        assertEquals("/file-1.css", _theme.getCssFiles().get(0).getLink());
        assertEquals("", _theme.getCssFiles().get(0).getMedia());
        assertEquals("", _theme.getCssFiles().get(0).getConditionalComment());
        assertEquals("/test.css", _theme.getCssFiles().get(1).getLink());
        assertEquals("", _theme.getCssFiles().get(1).getMedia());
        assertEquals("", _theme.getCssFiles().get(1).getConditionalComment());
    }

    @Test
    public void stubJsFiles() {
        assertTrue(_theme.getJsFiles().isEmpty());

        ThemeStubbingOperation.stubJsFiles("/file-1.js", "/test.js").of(_theme);
        assertEquals(2, _theme.getJsFiles().size());
        assertEquals("/file-1.js", _theme.getJsFiles().get(0).getLink());
        assertEquals("", _theme.getJsFiles().get(0).getConditionalComment());
        assertEquals("/test.js", _theme.getJsFiles().get(1).getLink());
        assertEquals("", _theme.getJsFiles().get(1).getConditionalComment());
    }

    @Test
    public void stubImagingSupport() {
        assertNull(_theme.getImaging());

        ImagingSupport imagingSupport = mock(ImagingSupport.class);
        ThemeStubbingOperation.stubImagingSupport(imagingSupport).of(_theme);
        assertSame(imagingSupport, _theme.getImaging());
    }
}
