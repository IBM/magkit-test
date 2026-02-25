package de.ibmix.magkit.test.servlet;

/*-
 * #%L
 * magkit-test-servlet Magnolia Module
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
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.IteratorEnumeration;

import java.util.List;

import static org.apache.commons.collections4.IteratorUtils.arrayIterator;
import static org.apache.commons.collections4.IteratorUtils.toList;
import static org.mockito.Mockito.when;

/**
 * Provides a light-weight functional style API (factory methods returning operations) for stubbing {@link HttpSession}
 * objects in unit tests using Mockito. Each static factory method returns an immutable {@code HttpSessionStubbingOperation}
 * which can be applied to a (usually Mockito created) {@link HttpSession} mock via {@link #of(HttpSession)}.
 * <p>
 * Typical usage patterns:
 * <pre>
 *     HttpSession session = mock(HttpSession.class, RETURNS_DEEP_STUBS);
 *     // Apply several stubbings
 *     stubServletContext(context).of(session);
 *     stubAttribute("userId", 17L).of(session);
 *     stubIsNew(false).of(session);
 * </pre>
 * or by leveraging helper utilities:
 * <pre>
 *     HttpSession session = ServletMockUtils.mockHttpSession(
 *         stubServletContext(stubInitParameter("appName", "demo")),
 *         stubAttribute("flag", Boolean.TRUE)
 *     );
 * </pre>
 * <p>
 * Chaining can be achieved by simply invoking {@code of(session)} multiple times with the returned operations. The
 * class itself is abstract; only the provided static factories (or custom anonymous extensions) should be used.
 * <p>
 * Thread-safety: Operations themselves are stateless and thread-safe; however applying them ({@link #of(HttpSession)})
 * is only as thread-safe as the underlying mock object.
 * <p>
 * Design intent: Encapsulate repetitive Mockito {@code when(...)} boilerplate and keep test code concise (DRY) while
 * providing expressive, discoverable method names.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-03-10
 */
public abstract class HttpSessionStubbingOperation {

    /**
     * Apply the stubbing operation to the supplied {@link HttpSession} mock.
     * <p>
     * Implementations are expected to perform null checks (defensive) and then configure Mockito expectations.
     * Users normally do not override this; instead they use the provided static factory methods of this class.
     *
     * @param session the (Mockito) {@link HttpSession} mock to be configured; must not be {@code null}
     */
    public abstract void of(HttpSession session);

    /**
     * Creates an operation that stubs {@link HttpSession#getServletContext()} to return the given context.
     *
     * @param context the {@link ServletContext} to be returned (may be {@code null} if a null context is desired)
     * @return operation configuring the session's servlet context
     */
    public static HttpSessionStubbingOperation stubServletContext(final ServletContext context) {
        return new HttpSessionStubbingOperation() {
            @Override
            public void of(HttpSession session) {
                Require.Argument.notNull(session, "session must not be null");
                when(session.getServletContext()).thenReturn(context);
            }
        };
    }

    /**
     * Convenience overload that first creates a mock {@link ServletContext} using provided stubbing operations
     * (delegating to {@link ServletMockUtils#mockServletContext(ServletContextStubbingOperation...)}) and then
     * returns an operation stubbing that context on a target {@link HttpSession}.
     *
     * @param stubbings zero or more {@link ServletContextStubbingOperation} instances to configure the mock context
     * @return operation configuring the session's servlet context with a freshly mocked and prepared context
     */
    public static HttpSessionStubbingOperation stubServletContext(final ServletContextStubbingOperation... stubbings) {
        return stubServletContext(ServletMockUtils.mockServletContext(stubbings));
    }

    /**
     * Creates an operation that stubs a named session attribute via {@link HttpSession#getAttribute(String)} and
     * keeps the attribute name enumeration ({@link HttpSession#getAttributeNames()}) in sync. If {@code value} is
     * {@code null} the attribute name is removed from the enumeration, otherwise it is added (unless already present).
     * <p>
     * Note: This assumes the {@code HttpSession} mock returns an {@link IteratorEnumeration} created by
     * {@link ServletMockUtils} that exposes a {@link ResettableIterator}. If that is not the case, the update of
     * attribute names may fail (ClassCastException). Ensure consistent usage of the utilities.
     *
     * @param name  attribute name; must not be {@code null}
     * @param value attribute value to stub (may be {@code null} to simulate removal)
     * @return operation configuring the attribute retrieval behavior
     */
    public static HttpSessionStubbingOperation stubAttribute(final String name, final Object value) {
        Require.Argument.notNull(name, "name must not be null");
        return new HttpSessionStubbingOperation() {
            @Override
            public void of(HttpSession session) {
                Require.Argument.notNull(session, "session must not be null");
                when(session.getAttribute(name)).thenReturn(value);
                IteratorEnumeration<String> nameEnum = (IteratorEnumeration<String>) session.getAttributeNames();
                @SuppressWarnings("unchecked")
                ResettableIterator<String> iter = (ResettableIterator<String>) nameEnum.getIterator();
                iter.reset();
                List<String> names = toList(iter);
                if (names.contains(name) && value == null) {
                    names.remove(name);
                } else if (value != null && !names.contains(name)) {
                    names.add(name);
                }
                nameEnum.setIterator(arrayIterator(names.toArray(new String[0])));
            }
        };
    }

    /**
     * Creates an operation stubbing {@link HttpSession#getLastAccessedTime()} with a fixed value.
     *
     * @param time the timestamp (usually from {@link System#currentTimeMillis()}) to return
     * @return operation configuring last accessed time
     */
    public static HttpSessionStubbingOperation stubLastAccessedTime(final long time) {
        return new HttpSessionStubbingOperation() {
            @Override
            public void of(final HttpSession session) {
                Require.Argument.notNull(session, "session must not be null");
                when(session.getLastAccessedTime()).thenReturn(time);
            }
        };
    }

    /**
     * Creates an operation stubbing {@link HttpSession#getCreationTime()} with a fixed value.
     *
     * @param time the creation timestamp to return
     * @return operation configuring creation time
     */
    public static HttpSessionStubbingOperation stubCreationTime(final long time) {
        return new HttpSessionStubbingOperation() {
            @Override
            public void of(final HttpSession session) {
                Require.Argument.notNull(session, "session must not be null");
                when(session.getCreationTime()).thenReturn(time);
            }
        };
    }

    /**
     * Creates an operation stubbing {@link HttpSession#isNew()} with the supplied boolean.
     *
     * @param value {@code true} to indicate a new session, {@code false} otherwise
     * @return operation configuring new-session flag
     */
    public static HttpSessionStubbingOperation stubIsNew(final boolean value) {
        return new HttpSessionStubbingOperation() {
            @Override
            public void of(final HttpSession session) {
                Require.Argument.notNull(session, "session must not be null");
                when(session.isNew()).thenReturn(value);
            }
        };
    }
}
