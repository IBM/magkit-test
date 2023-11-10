/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ibmix.magkit.test.jcr;

/*-
 * #%L
 * Aperto Mockito Test-Utils - JCR
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

import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing WorkspaceStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public class WorkspaceStubbingOperationTest {

    private Workspace _ws;

    @Before
    public void setUp() {
        RepositoryMockUtils.cleanRepository();
        _ws = mock(Workspace.class);
        doReturn("test").when(_ws).getName();
    }

    /**
     * Test of stubSession method, of class WorkspaceStubbingOperation.
     */
    @Test
    public void testStubSession() throws RepositoryException {
        assertThat(_ws.getSession(), nullValue());
        Session s = SessionMockUtils.mockSession("repo");
        WorkspaceStubbingOperation.stubSession(s).of(_ws);
        assertThat(_ws.getSession(), is(s));
        assertThat(s.getWorkspace(), is(_ws));
    }

    @Test
    public void testStubSessionWithOperations() throws RepositoryException {
        assertThat(_ws.getSession(), nullValue());
        SessionStubbingOperation op1 = mock(SessionStubbingOperation.class);
        SessionStubbingOperation op2 = mock(SessionStubbingOperation.class);
        WorkspaceStubbingOperation.stubSession(op1, op2).of(_ws);
        Session s = _ws.getSession();
        assertThat(s, notNullValue());
        assertThat(s.getWorkspace(), is(_ws));
        verify(op1, times(1)).of(s);
        verify(op2, times(1)).of(s);

        SessionStubbingOperation op3 = mock(SessionStubbingOperation.class);
        SessionStubbingOperation op4 = mock(SessionStubbingOperation.class);
        WorkspaceStubbingOperation.stubSession(op3, op4).of(_ws);
        assertThat(_ws.getSession(), is(s));
        assertThat(s.getWorkspace(), is(_ws));
        verify(op3, times(1)).of(s);
        verify(op4, times(1)).of(s);
    }
}
