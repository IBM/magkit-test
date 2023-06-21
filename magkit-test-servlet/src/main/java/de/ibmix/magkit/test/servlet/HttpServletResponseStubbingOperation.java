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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for HttpServletResponseStubbingOperation.
 * Stubbing operations to be used as parameters in ServletMockUtils.mockHttpServletResponse(...).
 *
 * @author wolf.bubenik
 * @since 10.03.11
 */
public abstract class HttpServletResponseStubbingOperation {
    public abstract void of(HttpServletResponse response);

    public static HttpServletResponseStubbingOperation stubContentType(final String value) {
        return new HttpServletResponseStubbingOperation() {

            @Override
            public void of(HttpServletResponse response) {
                assertThat(response, notNullValue());
                when(response.getContentType()).thenReturn(value);
            }
        };
    }

    public static HttpServletResponseStubbingOperation stubOutputStream(final ServletOutputStream value) {
        return new HttpServletResponseStubbingOperation() {

            @Override
            public void of(HttpServletResponse response) {
                assertThat(response, notNullValue());
                try {
                    when(response.getOutputStream()).thenReturn(value);
                } catch (IOException e) {
                    // ignore, no io operations involved
                }
            }
        };
    }

    public static HttpServletResponseStubbingOperation stubWriter(final PrintWriter value) {
        return new HttpServletResponseStubbingOperation() {

            @Override
            public void of(HttpServletResponse response) {
                assertThat(response, notNullValue());
                try {
                    when(response.getWriter()).thenReturn(value);
                } catch (IOException e) {
                    // ignore, no io operations involved
                }
            }
        };
    }

    public static HttpServletResponseStubbingOperation stubCharacterEncoding(final String value) {
        return new HttpServletResponseStubbingOperation() {

            @Override
            public void of(HttpServletResponse response) {
                assertThat(response, notNullValue());
                when(response.getCharacterEncoding()).thenReturn(value);
            }
        };
    }

    public static HttpServletResponseStubbingOperation stubLocale(final Locale value) {
        return new HttpServletResponseStubbingOperation() {

            @Override
            public void of(HttpServletResponse response) {
                assertThat(response, notNullValue());
                when(response.getLocale()).thenReturn(value);
            }
        };
    }
}
