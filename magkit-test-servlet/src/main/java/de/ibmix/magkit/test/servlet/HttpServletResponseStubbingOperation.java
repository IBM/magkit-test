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
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for {@code HttpServletResponseStubbingOperation}.
 * <p>
 * Each static factory method returns a lightweight "stubbing operation" that can be applied to
 * an existing Mockito {@link HttpServletResponse} mock via {@link #of(HttpServletResponse)}.
 * This keeps tests concise and avoids repeating boilerplate {@code when(...).thenReturn(...)} calls.
 * </p>
 * <p>Typical usage examples:</p>
 * <pre>
 * HttpServletResponse response = ServletMockUtils.mockHttpServletResponse(
 *             HttpServletResponseStubbingOperation.stubContentType("text/html"),
 *             HttpServletResponseStubbingOperation.stubCharacterEncoding("UTF-8")
 *      );
 * </pre>
 * <p>
 * Multiple operations can be combined to configure a mock in a readable, declarative style.
 * All operations perform a null assertion on the provided response to fail fast in tests.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-03-10
 */
public abstract class HttpServletResponseStubbingOperation {
    /**
     * Applies this stubbing operation to the supplied {@link HttpServletResponse}.
     * Implementations typically invoke Mockito {@code when(...)} to set up method return values.
     *
     * @param response the (mocked) response to configure; must not be {@code null}
     */
    public abstract void of(HttpServletResponse response);

    /**
     * Creates an operation that stubs {@link HttpServletResponse#getContentType()} to return the provided value.
     *
     * @param value content type to be returned; may be {@code null}
     * @return a non-null stubbing operation
     */
    public static HttpServletResponseStubbingOperation stubContentType(final String value) {
        return new HttpServletResponseStubbingOperation() {
            @Override
            public void of(HttpServletResponse response) {
                Require.Argument.notNull(response, "response must not be null");
                when(response.getContentType()).thenReturn(value);
            }
        };
    }

    /**
     * Creates an operation that stubs {@link HttpServletResponse#getOutputStream()} to return the provided {@link ServletOutputStream}.
     * The underlying call to {@code getOutputStream()} is wrapped in a try/catch purely because the servlet API declares
     * {@link IOException}; no actual I/O is performed here, so an exception is ignored safely.
     *
     * @param value the output stream to be returned; may be a mock or custom implementation
     * @return a non-null stubbing operation
     */
    public static HttpServletResponseStubbingOperation stubOutputStream(final ServletOutputStream value) {
        return new HttpServletResponseStubbingOperation() {
            @Override
            public void of(HttpServletResponse response) {
                Require.Argument.notNull(response, "response must not be null");
                try {
                    when(response.getOutputStream()).thenReturn(value);
                } catch (IOException e) {
                    // ignore, no IO operations involved
                }
            }
        };
    }

    /**
     * Creates an operation that stubs {@link HttpServletResponse#getWriter()} to return the provided {@link PrintWriter}.
     * The checked {@link IOException} from the servlet API is ignored because no real I/O occurs during stubbing.
     *
     * @param value the writer to return; may be a mock or wrapping a {@link java.io.StringWriter}
     * @return a non-null stubbing operation
     */
    public static HttpServletResponseStubbingOperation stubWriter(final PrintWriter value) {
        return new HttpServletResponseStubbingOperation() {
            @Override
            public void of(HttpServletResponse response) {
                Require.Argument.notNull(response, "response must not be null");
                try {
                    when(response.getWriter()).thenReturn(value);
                } catch (IOException e) {
                    // ignore, no IO operations involved
                }
            }
        };
    }

    /**
     * Creates an operation that stubs {@link HttpServletResponse#getCharacterEncoding()} to return the given encoding.
     *
     * @param value the character encoding to return (e.g. "UTF-8"); may be {@code null}
     * @return a non-null stubbing operation
     */
    public static HttpServletResponseStubbingOperation stubCharacterEncoding(final String value) {
        return new HttpServletResponseStubbingOperation() {
            @Override
            public void of(HttpServletResponse response) {
                Require.Argument.notNull(response, "response must not be null");
                when(response.getCharacterEncoding()).thenReturn(value);
            }
        };
    }

    /**
     * Creates an operation that stubs {@link HttpServletResponse#getLocale()} to return the specified locale.
     *
     * @param value the locale to return; may be {@code null}
     * @return a non-null stubbing operation
     */
    public static HttpServletResponseStubbingOperation stubLocale(final Locale value) {
        return new HttpServletResponseStubbingOperation() {
            @Override
            public void of(HttpServletResponse response) {
                Require.Argument.notNull(response, "response must not be null");
                when(response.getLocale()).thenReturn(value);
            }
        };
    }
}
