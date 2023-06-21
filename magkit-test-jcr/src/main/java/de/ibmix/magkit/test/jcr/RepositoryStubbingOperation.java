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

import de.ibmix.magkit.test.StubbingOperation;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Factory class for RepositoryStubbingOperation instances.
 *
 * @author wolf.bubenik
 * @since 04.02.14
 */
public abstract class RepositoryStubbingOperation implements StubbingOperation<Repository> {

    /**
     * Creates a RepositoryStubbingOperation that stubbes the login()- methods of a Repository mock.
     * repository.login() will be stubbed to return the given Session.
     * If the given Session has a Repository (session.getWorkspace() is not null)
     * repository.login(String workspaceName) will be stubbed to return the given Session for its workspace name.
     *
     * @param session the Session to be returned by the login() methods
     * @return the RepositoryStubbingOperation, never null
     */
    public static RepositoryStubbingOperation stubLogin(final Session session) {
        return new RepositoryStubbingOperation() {

            @Override
            public void of(final Repository repository) throws RepositoryException {
                assertThat(repository, notNullValue());
                when(repository.login()).thenReturn(session);
                if (session != null) {
                    when(session.getRepository()).thenReturn(repository);
                    if (session.getWorkspace() != null) {
                        when(repository.login(session.getWorkspace().getName())).thenReturn(session);
                    }
                }
            }
        };
    }
}
