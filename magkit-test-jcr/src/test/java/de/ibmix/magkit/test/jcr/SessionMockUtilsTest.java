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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing SessionMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public class SessionMockUtilsTest {

    @Before
    public void setUp() {
    }

    /**
     * Test of mockSession method, of class SessionMockUtils.
     */
    @Test
    public void testMockSessionForWorkspace() throws RepositoryException {
        SessionStubbingOperation op1 = mock(SessionStubbingOperation.class);
        SessionStubbingOperation op2 = mock(SessionStubbingOperation.class);
        Session session = SessionMockUtils.mockSession("testRepository", op1, op2);
        assertThat(session, notNullValue());
        verify(op1, times(1)).of(session);
        verify(op2, times(1)).of(session);
        assertThat(session.getRepository(), is(RepositoryMockUtils.mockRepository()));
        assertThat(session, is(RepositoryMockUtils.mockRepository().login("testRepository")));
        assertThat(session.getWorkspace().getName(), is("testRepository"));
    }

    @Test
    public void testMockPlainSession() throws RepositoryException {
        Session session = SessionMockUtils.mockPlainSession();
        assertThat(session, notNullValue());
        Node root = session.getRootNode();
        assertThat(root, notNullValue());
        assertThat(root.getPath(), is("/"));
        assertThat(root.getName(), is(""));
        assertThat(root.getIdentifier(), is("cafebabe-cafe-babe-cafe-babecafebabe"));
        assertThat(root.getPrimaryNodeType(), notNullValue());
        assertThat(root.getPrimaryNodeType().getName(), is("rep:root"));
        assertThat(session.getNode("/"), is(root));
        assertThat(session.getItem("/"), is(root));
        assertThat(session.getNodeByIdentifier("cafebabe-cafe-babe-cafe-babecafebabe"), is(root));
        assertThat(session.itemExists("/"), is(true));
        assertThat(session.nodeExists("/"), is(true));
        assertThat(session.getWorkspace(), nullValue());
        assertThat(session.getRepository(), nullValue());
        assertThat(session.getRetentionManager(), nullValue());
    }
}
