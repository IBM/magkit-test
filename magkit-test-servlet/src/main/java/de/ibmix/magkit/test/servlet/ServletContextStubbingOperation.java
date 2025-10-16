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
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.IteratorEnumeration;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.List;

import static org.apache.commons.collections4.IteratorUtils.arrayIterator;
import static org.apache.commons.collections4.IteratorUtils.toList;
import static org.mockito.Mockito.when;

/**
 * Provides factory methods that create reusable stubbing operations for a {@link ServletContext} mock.
 * <p>
 * The produced {@code ServletContextStubbingOperation} instances encapsulate a specific piece of stubbing
 * logic that can be applied to a Mockito based {@link ServletContext} mock via {@link #of(ServletContext)}.
 * This helps writing concise and intention revealing test setups while avoiding duplication.
 * </p>
 * <p><strong>Typical usage:</strong></p>
 * <pre>{@code
 * ServletContext context = Mockito.mock(ServletContext.class, RETURNS_DEEP_STUBS);
 * ServletContextStubbingOperation.stubContextPath("/app").of(context);
 * ServletContextStubbingOperation.stubInitParameter("encoding", "UTF-8").of(context);
 * ServletContextStubbingOperation.stubAttribute("featureFlag", Boolean.TRUE).of(context);
 * }</pre>
 * <p>
 * For attributes and init parameters the enumeration returned by {@link ServletContext#getAttributeNames()}
 * and {@link ServletContext#getInitParameterNames()} respectively is updated to also expose the stubbed
 * name. This relies on the enumeration instance being an {@link IteratorEnumeration} whose iterator is a
 * {@link ResettableIterator}. This is the case when the mock has been created using the utilities that
 * accompany this library (e.g. {@code ServletMockUtils}). If a different mocking approach is used the casts
 * may fail – this is an intentional design trade-off for test convenience.
 * </p>
 * <p><strong>Thread-safety:</strong> Instances are stateless and can be reused across tests, but applying them
 * concurrently on the same mock is not recommended.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-03-10
 */
public abstract class ServletContextStubbingOperation {

    /**
     * Apply this stubbing operation to the supplied {@link ServletContext} mock.
     * <p>
     * Implementations are expected to use Mockito {@code when(...)} syntax and may perform assertions to
     * guard against invalid (e.g. {@code null}) input. The passed {@code context} must be a Mockito mock.
     * </p>
     *
     * @param context the {@link ServletContext} mock to configure; must not be {@code null}
     */
    public abstract void of(ServletContext context);

    /**
     * Create an operation that stubs {@link ServletContext#getContextPath()} with the given value.
     *
     * @param value the context path to return (may be {@code null} if tests expect that)
     * @return a stubbing operation configuring the context path
     */
    public static ServletContextStubbingOperation stubContextPath(final String value) {
        return new ServletContextStubbingOperation() {
            @Override
            public void of(ServletContext context) {
                Require.Argument.notNull(context, "context must not be null");
                when(context.getContextPath()).thenReturn(value);
            }
        };
    }

    /**
     * Create an operation that stubs returning an attribute by name via {@link ServletContext#getAttribute(String)}
     * and augments the enumeration returned by {@link ServletContext#getAttributeNames()} so that it also contains
     * the provided name.
     *
     * @param name  the attribute name (must not be {@code null})
     * @param value the attribute value to return (may be {@code null})
     * @return a stubbing operation adding the attribute
     */
    public static ServletContextStubbingOperation stubAttribute(final String name, final Object value) {
        Require.Argument.notNull(name, "name must not be null");
        return new ServletContextStubbingOperation() {
            @Override
            public void of(ServletContext context) {
                Require.Argument.notNull(context, "context must not be null");
                when(context.getAttribute(name)).thenReturn(value);
                Enumeration<String> enumeration = context.getAttributeNames();
                Require.State.isInstanceof(enumeration, IteratorEnumeration.class, "enumeration must be IteratorEnumeration");
                @SuppressWarnings("unchecked")
                IteratorEnumeration<String> nameEnum = (IteratorEnumeration<String>) enumeration;
                augmentEnumeration(nameEnum, name);
            }
        };
    }

    /**
     * Create an operation that stubs returning an init parameter by name via {@link ServletContext#getInitParameter(String)}
     * and augments the enumeration returned by {@link ServletContext#getInitParameterNames()} so that it also contains
     * the provided name.
     *
     * @param name  the init parameter name (must not be {@code null})
     * @param value the init parameter value to return (may be {@code null})
     * @return a stubbing operation adding the init parameter
     */
    public static ServletContextStubbingOperation stubInitParameter(final String name, final String value) {
        Require.Argument.notNull(name, "name must not be null");
        return new ServletContextStubbingOperation() {
            @Override
            public void of(ServletContext context) {
                Require.Argument.notNull(context, "context must not be null");
                when(context.getInitParameter(name)).thenReturn(value);
                Enumeration<String> enumeration = context.getInitParameterNames();
                Require.State.isInstanceof(enumeration, IteratorEnumeration.class, "enumeration must be IteratorEnumeration");
                @SuppressWarnings("unchecked")
                IteratorEnumeration<String> nameEnum = (IteratorEnumeration<String>) enumeration;
                augmentEnumeration(nameEnum, name);
            }
        };
    }

    /**
     * Augment the provided {@link IteratorEnumeration} by adding the given name to the underlying iterator.
     * <p>
     * This method assumes that the enumeration was created for test purposes and its iterator is a
     * {@link ResettableIterator}. It rewinds the iterator, copies existing elements into a modifiable list,
     * conditionally appends the new name if not already present and finally installs a fresh iterator
     * containing the updated set of names.
     * </p>
     * <p>
     * Design note: We intentionally accept the casting cost and a single localized {@code @SuppressWarnings}
     * because this utility targets controlled test doubles where the structure is guaranteed by
     * {@code ServletMockUtils}. An alternative would be a custom Enumeration implementation, which would add
     * complexity without additional test value.
     * </p>
     *
     * @param enumeration the enumeration to augment (must not be {@code null})
     * @param name the name to add (must not be {@code null})
     */
    private static void augmentEnumeration(IteratorEnumeration<String> enumeration, String name) {
        Require.State.notNull(enumeration, "enumeration must not be null");
        Require.Argument.notNull(name, "name must not be null");
        Require.State.isInstanceof(enumeration.getIterator(), ResettableIterator.class, "Iterator must be ResettableIterator");
        @SuppressWarnings("unchecked")
        ResettableIterator<String> iterator = (ResettableIterator<String>) enumeration.getIterator();
        iterator.reset();
        List<String> names = toList(iterator);
        if (!names.contains(name)) {
            names.add(name);
            enumeration.setIterator(arrayIterator(names.toArray(new String[0])));
        }
    }
}
