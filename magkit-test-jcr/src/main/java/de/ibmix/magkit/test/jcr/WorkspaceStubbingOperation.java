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


import de.ibmix.magkit.test.ExceptionStubbingOperation;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.QueryManager;

import static de.ibmix.magkit.test.jcr.SessionMockUtils.mockSession;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class for stubbing javax.jcr.Workspace.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public abstract class WorkspaceStubbingOperation implements ExceptionStubbingOperation<Workspace, RepositoryException> {

    private WorkspaceStubbingOperation() {
    }

    /**
     * Factory method to create a StubbingOperation that sets the name of a Workspace mock.
     *
     * @param name the new workspace name as String
     * @return the WorkspaceStubbingOperation
     */
    public static WorkspaceStubbingOperation stubName(final String name) {
        return new WorkspaceStubbingOperation() {
            @Override
            public void of(final Workspace ws) {
                assertThat(ws, notNullValue());
                when(ws.getName()).thenReturn(name);
            }
        };
    }

    /**
     * Factory method to create a StubbingOperation that sets the Session of a Workspace mock.
     * The session.getWorkspace() method is stubbed to return the workspace.
     *
     * @param session the new jcr Session for the workspace
     * @return the WorkspaceStubbingOperation
     */
    public static WorkspaceStubbingOperation stubSession(final Session session) {
        return new WorkspaceStubbingOperation() {
            @Override
            public void of(final Workspace ws) {
                assertThat(ws, notNullValue());
                when(ws.getSession()).thenReturn(session);
                when(session.getWorkspace()).thenReturn(ws);
            }
        };
    }

    /**
     * Factory method to create a StubbingOperation that stubbs the behaviour of the Session of a Workspace mock.
     * If the workspace already has a session, this session wil be mocked.
     * If the workspace has no session, a new session mock will be created.
     *
     * @param stubbings the SessionStubbingOperation to be applied to the session
     * @return the WorkspaceStubbingOperation
     */
    public static WorkspaceStubbingOperation stubSession(final SessionStubbingOperation... stubbings) {
        return new WorkspaceStubbingOperation() {
            @Override
            public void of(final Workspace context) throws RepositoryException {
                assertThat(context, notNullValue());
                Session s = context.getSession();
                if (s == null) {
                    s = mockSession(context.getName(), stubbings);
                    stubSession(s).of(context);
                } else {
                    for (SessionStubbingOperation stub : stubbings) {
                        stub.of(s);
                    }
                }
            }
        };
    }

    /**
     * Factory method to create a StubbingOperation that sets the QueryManager of a Workspace mock.
     *
     * @param queryManager the new QueryManager
     * @return the WorkspaceStubbingOperation
     */
    public static WorkspaceStubbingOperation stubQueryManager(final QueryManager queryManager) {
        return new WorkspaceStubbingOperation() {
            @Override
            public void of(final Workspace ws) throws RepositoryException {
                assertThat(ws, notNullValue());
                when(ws.getQueryManager()).thenReturn(queryManager);
            }
        };
    }
}
