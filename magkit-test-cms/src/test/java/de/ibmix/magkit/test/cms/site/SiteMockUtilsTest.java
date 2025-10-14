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
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.test.mock.MockComponentProvider;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.site.SiteMockUtils.cleanSiteManager;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockAssignedSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockCurrentSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockDefaultSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockPlainSiteManager;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSiteManager;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static info.magnolia.objectfactory.Components.getComponentProvider;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing SiteMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-05-24
 */
public class SiteMockUtilsTest {
    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void testMockDefaultSite() throws RepositoryException {
        SiteStubbingOperation op1 = mock(SiteStubbingOperation.class);
        SiteStubbingOperation op2 = mock(SiteStubbingOperation.class);

        Site s1 = mockDefaultSite(op1, op2);
        assertThat(s1, notNullValue());
        assertThat(s1.getName(), is("default"));
        verify(op1, times(1)).of(s1);
        verify(op2, times(1)).of(s1);
        assertThat(s1.getVariations(), notNullValue());
        assertThat(s1.getVariations().size(), is(0));
        assertThat(s1.getI18n(), notNullValue());

        Site s2 = mockDefaultSite(op1, op2);
        assertThat(s2, notNullValue());
        assertThat(s1, is(s2));
    }

    @Test
    public void testMockCurrentSite() throws RepositoryException {
        SiteStubbingOperation op1 = mock(SiteStubbingOperation.class);
        SiteStubbingOperation op2 = mock(SiteStubbingOperation.class);

        Site s1 = mockCurrentSite("test", op1, op2);
        assertThat(s1, notNullValue());
        assertThat(s1.getName(), is("test"));
        verify(op1, times(1)).of(s1);
        verify(op2, times(1)).of(s1);
        assertThat(s1.getVariations(), notNullValue());
        assertThat(s1.getVariations().size(), is(0));
        assertThat(s1.getI18n(), notNullValue());

        Site s2 = mockCurrentSite("test", op1, op2);
        assertThat(s2, notNullValue());
        assertThat(s1, is(s2));
        // assert that stubbing operations have been executed again:
        verify(op1, times(2)).of(s1);
        verify(op2, times(2)).of(s1);
    }

    @Test
    public void testMockAssignedSite() throws RepositoryException {
        // Arrange node hierarchy:
        Node root = mockNode("root");
        Node section = mockNode("root/section");
        Node page = mockNode("root/section/page");

        SiteStubbingOperation op1 = mock(SiteStubbingOperation.class);
        SiteStubbingOperation op2 = mock(SiteStubbingOperation.class);

        SiteManager manager = mockPlainSiteManager();
        assertThat(manager.getAssignedSite(root), nullValue());
        assertThat(manager.getAssignedSite(section), nullValue());
        assertThat(manager.getAssignedSite(page), nullValue());

        // First call: creates new site and assigns recursively (children already assigned)
        Site site1 = mockAssignedSite(root, "alpha", op1);
        assertThat(site1, notNullValue());
        assertThat(site1.getName(), is("alpha"));
        verify(op1, times(1)).of(site1);
        assertThat(manager.getAssignedSite(root), is(site1));
        assertThat(manager.getAssignedSite(section), is(site1));
        assertThat(manager.getAssignedSite(page), is(site1));

        // Second call: site exists (else branch) reassigns children and applies new stubbing
        Site site2 = mockAssignedSite(root, "alpha", op2);
        assertThat(site2, is(site1));
        verify(op2, times(1)).of(site1);
        assertThat(manager.getAssignedSite(section), is(site1));
        assertThat(manager.getAssignedSite(page), is(site1));
    }

    @Test
    public void testMockSiteBlankNameAndReuse() throws RepositoryException {
        SiteStubbingOperation op = mock(SiteStubbingOperation.class);
        // Blank name -> normalized to default
        Site s1 = mockSite("   ", op);
        assertThat(s1.getName(), is("default"));
        verify(op, times(1)).of(s1);
        // Reuse existing site (creation branch skipped) and apply stubbing again
        Site s2 = mockSite("default", op);
        assertThat(s2, is(s1));
        verify(op, times(2)).of(s1);
    }

    @Test
    public void testMockSiteManager() throws RepositoryException {
        SiteManagerStubbingOperation op1 = mock(SiteManagerStubbingOperation.class);
        SiteManagerStubbingOperation op2 = mock(SiteManagerStubbingOperation.class);
        SiteManager sm = mockSiteManager(op1, op2);
        assertThat(sm, notNullValue());
        verify(op1, times(1)).of(sm);
        verify(op2, times(1)).of(sm);
    }

    @Test
    public void testCleanSiteManager() {
        MockComponentProvider cp = (MockComponentProvider) getComponentProvider();
        SiteManager singleton = null;
        try {
            singleton = cp.getSingleton(SiteManager.class);
        } catch (Exception e) {
            // ignore
        }

        assertThat(singleton, nullValue());

        SiteManager sm = mock(SiteManager.class);
        Object other = new Object();
        cp.setInstance(SiteManager.class, sm);
        cp.setInstance(Object.class, other);
        assertThat(cp.getSingleton(SiteManager.class), is(sm));
        assertThat(cp.getSingleton(Object.class), is(other));

        cleanSiteManager();
        try {
            singleton = cp.getSingleton(SiteManager.class);
        } catch (Exception e) {
            // ignore
        }
        assertThat(singleton, nullValue());
        assertThat(cp.getSingleton(Object.class), is(other));
    }
}
