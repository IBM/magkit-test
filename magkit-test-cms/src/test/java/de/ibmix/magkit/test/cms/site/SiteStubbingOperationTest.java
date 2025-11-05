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

import info.magnolia.module.site.Domain;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.theme.Theme;
import info.magnolia.module.site.theme.ThemeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSite;
import static de.ibmix.magkit.test.cms.site.SiteStubbingOperation.stubDomains;
import static de.ibmix.magkit.test.cms.site.SiteStubbingOperation.stubName;
import static de.ibmix.magkit.test.cms.site.SiteStubbingOperation.stubTheme;
import static de.ibmix.magkit.test.cms.site.SiteStubbingOperation.stubVariation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing SiteStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-06-06
 */
public class SiteStubbingOperationTest {

    private Site _site;

    @BeforeEach
    public void setUp() throws Exception {
        cleanContext();
        _site = mock(Site.class);
    }

    @Test
    public void testStubName() {
        assertNull(_site.getName());

        stubName("test").of(_site);
        assertEquals("test", _site.getName());

    }

    @Test
    public void testStubDomains() {
        assertEquals(0, _site.getDomains().size());

        Domain domain = mock(Domain.class);
        stubDomains(domain).of(_site);
        assertNotNull(_site.getDomains());
        assertEquals(1, _site.getDomains().size());
        assertSame(domain, _site.getDomains().iterator().next());
    }

    @Test
    public void testStubTheme() {
        assertNull(_site.getTheme());

        ThemeReference theme = mock(ThemeReference.class);
        stubTheme(theme).of(_site);
        assertSame(theme, _site.getTheme());

        stubTheme(null).of(_site);
        assertNull(_site.getTheme());
    }

    @Test
    public void testStubThemeWithName() {
        assertNull(_site.getTheme());

        ThemeStubbingOperation stubTheme = mock(ThemeStubbingOperation.class);
        stubTheme("myTheme", stubTheme).of(_site);
        assertNotNull(_site.getTheme());
        assertEquals("myTheme", _site.getTheme().getName());
        verify(stubTheme, times(1)).of(any(Theme.class));
    }

    @Test
    public void stubDomainsTest() {
        assertNotNull(_site.getDomains());
        assertEquals(0, _site.getDomains().size());

        Domain d1 = mock(Domain.class);
        Domain d2 = mock(Domain.class);
        Domain d3 = mock(Domain.class);
        stubDomains(d1, d2, d3).of(_site);
        assertNotNull(_site.getDomains());
        assertEquals(3, _site.getDomains().size());
    }

    @Test
    public void stubVariationTest() throws RepositoryException {
        assertNotNull(_site.getVariations());
        assertEquals(0, _site.getVariations().size());

        Site testVariation = mockSite("test");
        stubVariation(testVariation).of(_site);

        assertEquals(1, _site.getVariations().size());
        assertNotNull(_site.getVariations().get("test"));
        assertEquals("test", _site.getVariations().get("test").getName());
    }

    @Test
    public void stubVariationWithNameTest() throws RepositoryException {
        assertNotNull(_site.getVariations());
        assertEquals(0, _site.getVariations().size());

        SiteStubbingOperation op = mock(SiteStubbingOperation.class);
        stubVariation("newTest", op).of(_site);

        assertEquals(1, _site.getVariations().size());
        Site variation = _site.getVariations().get("newTest");
        assertNotNull(variation);
        assertEquals("newTest", variation.getName());
        verify(op, times(1)).of(variation);
        verify(op, times(0)).of(_site);
    }
}
