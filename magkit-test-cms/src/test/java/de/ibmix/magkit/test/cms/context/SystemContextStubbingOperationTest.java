package de.ibmix.magkit.test.cms.context;

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

import de.ibmix.magkit.test.jcr.SessionMockUtils;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.SystemContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;

public class SystemContextStubbingOperationTest {

    private SystemContext _context;

    @Before
    public void setUp() throws Exception {
        _context = mock(SystemContext.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void stubLocale() {
        assertThat(_context.getLocale(), nullValue());

        SystemContextStubbingOperation.stubLocale(Locale.FRENCH).of(_context);
        assertThat(_context.getLocale(), is(Locale.FRENCH));
    }

    @Test
    public void stubAccessManager() {
        assertThat(_context.getAccessManager("repository"), nullValue());

        AccessManager am = mock(AccessManager.class);
        SystemContextStubbingOperation.stubAccessManager("repository", am).of(_context);
        assertThat(_context.getAccessManager("repository"), is(am));
    }

    @Test
    public void stubJcrSession() throws RepositoryException {
        assertThat(_context.getJCRSession("repository"), nullValue());

        Session session = mock(Session.class);
        SystemContextStubbingOperation.stubJcrSession("repository", session).of(_context);
        assertThat(_context.getJCRSession("repository"), is(session));
    }

    @Test
    public void testStubJcrSession() throws RepositoryException {
        assertThat(_context.getJCRSession("repository"), nullValue());

        SystemContextStubbingOperation.stubJcrSession("repository").of(_context);
        Session session = SessionMockUtils.mockSession("repository");
        assertThat(_context.getJCRSession("repository"), is(session));
    }
}
