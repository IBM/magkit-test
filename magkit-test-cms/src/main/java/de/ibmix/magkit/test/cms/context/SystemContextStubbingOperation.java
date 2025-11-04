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

import de.ibmix.magkit.assertions.Require;
import de.ibmix.magkit.test.StubbingOperation;
import de.ibmix.magkit.test.jcr.SessionMockUtils;
import de.ibmix.magkit.test.jcr.SessionStubbingOperation;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.SystemContext;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Locale;

import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.mockito.Mockito.when;

/**
 * Factory holder for reusable {@code SystemContextStubbingOperation} instances used to configure a mocked {@link SystemContext} in tests.
 * <p>
 * Each static method returns a lightweight operation object whose {@link de.ibmix.magkit.test.StubbingOperation#of(Object) of(mock)} method applies the described Mockito stubbings
 * to the supplied {@link SystemContext} mock. Designed for composability when used with
 * {@code ContextMockUtils.mockSystemContext(...)}.
 * <p>
 * Typical usage:
 * <pre>{@code
 * SystemContext context = ContextMockUtils.mockSystemContext(
 *     SystemContextStubbingOperation.stubLocale(Locale.GERMAN),
 *     SystemContextStubbingOperation.stubAccessManager("website", myAccessManager),
 *     SystemContextStubbingOperation.stubJcrSession("website")
 * );
 * }</pre>
 * <p>
 * Contract and behaviour:
 * <ul>
 *   <li>Idempotent: invoking the same operation multiple times re-applies the same stubbing without accumulating side effects.</li>
 *   <li>Null handling: passing {@code null} for locale or access manager results in the getter returning {@code null}.</li>
 *   <li>Blank repository IDs: any blank (null/empty/whitespace) {@code repositoryId} is replaced by {@link info.magnolia.repository.RepositoryConstants#WEBSITE}.</li>
 *   <li>Overloads for JCR session: one overload accepts an existing {@link Session}, the other creates a mock via {@link de.ibmix.magkit.test.jcr.SessionMockUtils#mockSession(String, SessionStubbingOperation...)}.</li>
 *   <li>Exceptions: {@link RepositoryException} is caught and ignored in JCR session stubbings (Magnolia API normally does not throw for mocked access).</li>
 * </ul>
 * <p>
 * Thread-safety: Not thread-safe; intended for single-threaded unit tests manipulating Magnolia static state.
 * <p>
 * Side effects: JCR session stubbing may create additional session mocks via {@link de.ibmix.magkit.test.jcr.SessionMockUtils}.
 * <p>
 * Combine multiple operations in a single call to reduce boilerplate and keep tests expressive.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-05-31
 */
public abstract class SystemContextStubbingOperation implements StubbingOperation<SystemContext> {

    /**
     * Stubs {@link SystemContext#getLocale()} to return the provided {@link Locale}.
     *
     * @param locale value returned by {@link SystemContext#getLocale()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static SystemContextStubbingOperation stubLocale(final Locale locale) {
        return new SystemContextStubbingOperation() {

            @Override
            public void of(SystemContext context) {
                Require.Argument.notNull(context, "context should not be null");
                when(context.getLocale()).thenReturn(locale);
            }
        };
    }

    /**
     * Stubs {@link SystemContext#getAccessManager(String)} for the given repository id.
     * Blank repository ids are mapped to {@link info.magnolia.repository.RepositoryConstants#WEBSITE}.
     * Only the single-argument getter is stubbed (Magnolia provides additional overloads that are ignored here).
     *
     * @param repositoryId repository ID/name (blank treated as {@code WEBSITE})
     * @param am the {@link AccessManager} instance to be returned (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static SystemContextStubbingOperation stubAccessManager(final String repositoryId, final AccessManager am) {
        return new SystemContextStubbingOperation() {

            public void of(SystemContext context) {
                Require.Argument.notNull(context, "context should not be null");
                String repository = isBlank(repositoryId) ? WEBSITE : repositoryId;
                when(context.getAccessManager(repository)).thenReturn(am);
            }
        };
    }

    /**
     * Stubs {@link SystemContext#getJCRSession(String)} to return a provided {@link Session} for the given repository id.
     * Blank repository ids are replaced by {@link info.magnolia.repository.RepositoryConstants#WEBSITE}.
     * {@link RepositoryException} is caught and ignored (not expected for mocks).
     *
     * @param repositoryId repository ID/name (blank treated as {@code WEBSITE})
     * @param session pre-created {@link Session} mock or instance to return (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static SystemContextStubbingOperation stubJcrSession(final String repositoryId, final Session session) {
        return new SystemContextStubbingOperation() {

            public void of(SystemContext context) {
                Require.Argument.notNull(context, "context should not be null");
                String repository = isBlank(repositoryId) ? WEBSITE : repositoryId;
                try {
                    when(context.getJCRSession(repository)).thenReturn(session);
                } catch (RepositoryException e) {
                    // ignored by contract: Magnolia normally does not throw for mocked access.
                }
            }
        };
    }

    /**
     * Creates and stubs a JCR {@link Session} for the given repository id, delegating to {@link #stubJcrSession(String, Session)}.
     * The session is created via {@link de.ibmix.magkit.test.jcr.SessionMockUtils#mockSession(String, SessionStubbingOperation...)} and then registered in the context.
     * Blank repository ids are mapped to {@link info.magnolia.repository.RepositoryConstants#WEBSITE}.
     *
     * @param repositoryId repository ID/name (blank treated as {@code WEBSITE})
     * @return operation applying the described stubbing
     */
    public static SystemContextStubbingOperation stubJcrSession(final String repositoryId) {
        return new SystemContextStubbingOperation() {

            public void of(SystemContext context) {
                Require.Argument.notNull(context, "context should not be null");
                String repository = isBlank(repositoryId) ? WEBSITE : repositoryId;
                try {
                    Session session = SessionMockUtils.mockSession(repository);
                    stubJcrSession(repository, session).of(context);
                } catch (RepositoryException e) {
                    // ignored by contract: Magnolia normally does not throw for mocked access.
                }
            }
        };
    }
}
