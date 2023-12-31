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
import info.magnolia.module.site.theme.ThemeReference;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSite;
import static de.ibmix.magkit.test.cms.site.SiteStubbingOperation.stubDomains;
import static de.ibmix.magkit.test.cms.site.SiteStubbingOperation.stubName;
import static de.ibmix.magkit.test.cms.site.SiteStubbingOperation.stubTheme;
import static de.ibmix.magkit.test.cms.site.SiteStubbingOperation.stubVariation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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

    @Before
    public void setUp() throws Exception {
        cleanContext();
        _site = mock(Site.class);
    }

    @Test
    public void testStubName() {
        assertThat(_site.getName(), nullValue());

        stubName("test").of(_site);
        assertThat(_site.getName(), is("test"));

    }

    @Test
    public void testStubDomains() {
        assertThat(_site.getDomains().size(), is(0));

        Domain domain = mock(Domain.class);
        stubDomains(domain).of(_site);
        assertThat(_site.getDomains(), notNullValue());
        assertThat(_site.getDomains().size(), is(1));
        assertThat(_site.getDomains().iterator().next(), is(domain));
    }

    @Test
    public void testStubTheme() {
        assertThat(_site.getTheme(), nullValue());

        ThemeReference theme = mock(ThemeReference.class);
        stubTheme(theme).of(_site);
        assertThat(_site.getTheme(), is(theme));

    }

    @Test
    public void stubDomainsTest() {
        assertThat(_site.getDomains(), notNullValue());
        assertThat(_site.getDomains().size(), is(0));

        Domain d1 = mock(Domain.class);
        Domain d2 = mock(Domain.class);
        Domain d3 = mock(Domain.class);
        stubDomains(d1, d2, d3).of(_site);
        assertThat(_site.getDomains(), notNullValue());
        assertThat(_site.getDomains().size(), is(3));
    }

    @Test
    public void stubVariationTest() throws RepositoryException {
        assertThat(_site.getVariations(), notNullValue());
        assertThat(_site.getVariations().size(), is(0));

        Site testVariation = mockSite("test");
        stubVariation(testVariation).of(_site);

        assertThat(_site.getVariations().size(), is(1));
        assertThat(_site.getVariations().get("test"), notNullValue());
        assertThat(_site.getVariations().get("test").getName(), is("test"));
    }

    @Test
    public void stubVariationWithNameTest() throws RepositoryException {
        assertThat(_site.getVariations(), notNullValue());
        assertThat(_site.getVariations().size(), is(0));

        SiteStubbingOperation op = mock(SiteStubbingOperation.class);
        stubVariation("newTest", op).of(_site);

        assertThat(_site.getVariations().size(), is(1));
        Site variation = _site.getVariations().get("newTest");
        assertThat(variation, notNullValue());
        assertThat(variation.getName(), is("newTest"));
        verify(op, times(1)).of(variation);
        verify(op, times(0)).of(_site);
    }
}
