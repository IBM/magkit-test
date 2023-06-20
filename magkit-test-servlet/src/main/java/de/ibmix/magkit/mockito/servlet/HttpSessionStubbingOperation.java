package de.ibmix.magkit.mockito.servlet;

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

import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.IteratorEnumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.List;

import static org.apache.commons.collections4.IteratorUtils.arrayIterator;
import static org.apache.commons.collections4.IteratorUtils.toList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for HttpSessionStubbingOperation.
 * Stubbing operations to be used as parameters in ServletMockUtils.mockHttpSession(...).
 *
 * @author wolf.bubenik
 * @since 10.03.11
 */
public abstract class HttpSessionStubbingOperation {
    public abstract void of(HttpSession session);

    public static HttpSessionStubbingOperation stubServletContext(final ServletContext context) {
        return new HttpSessionStubbingOperation() {

            @Override
            public void of(HttpSession session) {
                assertThat(session, notNullValue());
                when(session.getServletContext()).thenReturn(context);
            }
        };
    }

    public static HttpSessionStubbingOperation stubServletContext(final ServletContextStubbingOperation... stubbings) {
        return stubServletContext(ServletMockUtils.mockServletContext(stubbings));
    }

    public static HttpSessionStubbingOperation stubAttribute(final String name, final Object value) {
        return new HttpSessionStubbingOperation() {
            @Override
            public void of(HttpSession session) {
                assertThat(session, notNullValue());
                assertThat(name, notNullValue());
                when(session.getAttribute(name)).thenReturn(value);
                IteratorEnumeration nameEnum = (IteratorEnumeration) session.getAttributeNames();
                ((ResettableIterator) nameEnum.getIterator()).reset();
                List<String> names = toList(nameEnum.getIterator());
                if (names.contains(name) && value == null) {
                    names.remove(name);
                } else if (value != null) {
                    names.add(name);
                }
                nameEnum.setIterator(arrayIterator(names.toArray()));
            }
        };
    }

    public static HttpSessionStubbingOperation stubLastAccessedTime(final long time) {
        return new HttpSessionStubbingOperation() {

            @Override
            public void of(final HttpSession session) {
                assertThat(session, notNullValue());
                when(session.getLastAccessedTime()).thenReturn(time);
            }
        };
    }

    public static HttpSessionStubbingOperation stubCreationTime(final long time) {
        return new HttpSessionStubbingOperation() {

            @Override
            public void of(final HttpSession session) {
                assertThat(session, notNullValue());
                when(session.getCreationTime()).thenReturn(time);
            }
        };
    }

    public static HttpSessionStubbingOperation stubIsNew(final boolean value) {
        return new HttpSessionStubbingOperation() {

            @Override
            public void of(final HttpSession session) {
                assertThat(session, notNullValue());
                when(session.isNew()).thenReturn(value);
            }
        };
    }
}
