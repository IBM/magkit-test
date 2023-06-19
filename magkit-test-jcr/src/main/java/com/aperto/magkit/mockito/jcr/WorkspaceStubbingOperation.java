package com.aperto.magkit.mockito.jcr;

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


import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.QueryManager;

import static com.aperto.magkit.mockito.jcr.SessionMockUtils.mockSession;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class for stubbing javax.jcr.Workspace.
 *
 * @author wolf.bubenik
 * @since 03.08.12
 */
public abstract class WorkspaceStubbingOperation {
    public abstract void of(Workspace context) throws RepositoryException;

    private WorkspaceStubbingOperation() {
    }

    public static WorkspaceStubbingOperation stubName(final String name) {
        return new WorkspaceStubbingOperation() {
            @Override
            public void of(final Workspace ws) {
                assertThat(ws, notNullValue());
                when(ws.getName()).thenReturn(name);
            }
        };
    }

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
