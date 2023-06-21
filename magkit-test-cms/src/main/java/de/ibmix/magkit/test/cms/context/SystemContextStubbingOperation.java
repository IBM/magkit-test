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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Locale;

import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 * @since 31.05.13
 */
public abstract class SystemContextStubbingOperation {
    public abstract void of(SystemContext context) throws RepositoryException;

    public static SystemContextStubbingOperation stubLocale(final Locale locale) {
        return new SystemContextStubbingOperation() {

            @Override
            public void of(SystemContext context) {
                assertThat(context, notNullValue());
                when(context.getLocale()).thenReturn(locale);
            }
        };
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getAccessManager(repositoryId) and getAccessManager(repositoryId, workspaceId) to return the provided value.
     *
     * @param repositoryId the repository ID/name as java.lang.String
     * @param am           the info.magnolia.cms.security.AccessManager to be returned
     * @return a new WebContextStubbingOperation instance
     */
    public static SystemContextStubbingOperation stubAccessManager(final String repositoryId, final AccessManager am) {
        return new SystemContextStubbingOperation() {

            public void of(SystemContext context) {
                assertThat(context, notNullValue());
                String repository = isBlank(repositoryId) ? WEBSITE : repositoryId;
                when(context.getAccessManager(repository)).thenReturn(am);
            }
        };
    }

    public static SystemContextStubbingOperation stubJcrSession(final String repositoryId, final Session session) {
        return new SystemContextStubbingOperation() {

            public void of(SystemContext context) throws RepositoryException {
                assertThat(context, notNullValue());
                String repository = isBlank(repositoryId) ? WEBSITE : repositoryId;
                when(context.getJCRSession(repository)).thenReturn(session);
            }
        };
    }

    public static SystemContextStubbingOperation stubJcrSession(final String repositoryId) {
        return new SystemContextStubbingOperation() {

            public void of(SystemContext context) throws RepositoryException {
                String repository = isBlank(repositoryId) ? WEBSITE : repositoryId;
                Session session = SessionMockUtils.mockSession(repository);
                stubJcrSession(repository, session).of(context);
            }
        };
    }

}
