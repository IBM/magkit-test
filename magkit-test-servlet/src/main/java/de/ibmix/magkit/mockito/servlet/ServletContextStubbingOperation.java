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
import java.util.List;

import static org.apache.commons.collections4.IteratorUtils.arrayIterator;
import static org.apache.commons.collections4.IteratorUtils.toList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for ServletContextStubbingOperation.
 * Stubbing operations to be used as parameters in ServletMockUtils.mockServletContext(...).
 *
 * @author wolf.bubenik
 * @since 10.03.11
 */
public abstract class ServletContextStubbingOperation {
    public abstract void of(ServletContext context);

    public static ServletContextStubbingOperation stubContextPath(final String value) {
        return new ServletContextStubbingOperation() {

            @Override
            public void of(ServletContext context) {
                assertThat(context, notNullValue());
                when(context.getContextPath()).thenReturn(value);
            }
        };
    }

    public static ServletContextStubbingOperation stubAttribute(final String name, final Object value) {
        return new ServletContextStubbingOperation() {

            @Override
            public void of(ServletContext context) {
                assertThat(context, notNullValue());
                when(context.getAttribute(name)).thenReturn(value);
                IteratorEnumeration nameEnum = (IteratorEnumeration) context.getAttributeNames();
                ((ResettableIterator) nameEnum.getIterator()).reset();
                List<String> names = toList(nameEnum.getIterator());
                names.add(name);
                nameEnum.setIterator(arrayIterator(names.toArray()));
            }
        };
    }

    public static ServletContextStubbingOperation stubInitParameter(final String name, final String value) {
        return new ServletContextStubbingOperation() {

            @Override
            public void of(ServletContext context) {
                assertThat(context, notNullValue());
                when(context.getInitParameter(name)).thenReturn(value);
                IteratorEnumeration nameEnum = (IteratorEnumeration) context.getInitParameterNames();
                ((ResettableIterator) nameEnum.getIterator()).reset();
                List<String> names = toList(nameEnum.getIterator());
                names.add(name);
                nameEnum.setIterator(arrayIterator(names.toArray()));
            }
        };
    }
}
