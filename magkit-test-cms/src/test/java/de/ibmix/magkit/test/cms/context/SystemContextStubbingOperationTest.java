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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class SystemContextStubbingOperationTest {

    private SystemContext _context;

    @BeforeEach
    public void setUp() throws Exception {
        _context = mock(SystemContext.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void stubLocale() {
        assertNull(_context.getLocale());
        SystemContextStubbingOperation.stubLocale(Locale.FRENCH).of(_context);
        assertEquals(Locale.FRENCH, _context.getLocale());
    }

    @Test
    public void stubAccessManager() {
        assertNull(_context.getAccessManager("repository"));
        AccessManager am = mock(AccessManager.class);
        SystemContextStubbingOperation.stubAccessManager("repository", am).of(_context);
        assertEquals(am, _context.getAccessManager("repository"));
    }

    @Test
    public void stubJcrSession() throws RepositoryException {
        assertNull(_context.getJCRSession("repository"));
        Session session = mock(Session.class);
        SystemContextStubbingOperation.stubJcrSession("repository", session).of(_context);
        assertEquals(session, _context.getJCRSession("repository"));
    }

    @Test
    public void testStubJcrSession() throws RepositoryException {
        assertNull(_context.getJCRSession("repository"));
        SystemContextStubbingOperation.stubJcrSession("repository").of(_context);
        Session session = SessionMockUtils.mockSession("repository");
        assertEquals(session, _context.getJCRSession("repository"));
    }
}
