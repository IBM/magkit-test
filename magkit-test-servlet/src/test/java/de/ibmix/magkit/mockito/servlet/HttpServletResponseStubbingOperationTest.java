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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Testing HttpServletResponseStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 21.09.2011
 */
public class HttpServletResponseStubbingOperationTest {

    private HttpServletResponse _response;

    @Before
    public void setUp() {
        _response = mock(HttpServletResponse.class);
    }

    @Test
    public void testStubContentType() {
        HttpServletResponseStubbingOperation.stubContentType("type").of(_response);
        assertThat(_response.getContentType(), is("type"));

        HttpServletResponseStubbingOperation.stubContentType(null).of(_response);
        assertThat(_response.getContentType(), nullValue());
    }

    @Test(expected = AssertionError.class)
    public void testStubContentTypeForNull() {
        HttpServletResponseStubbingOperation.stubContentType("type").of(null);
    }

    @Test
    public void testStubOutputStream() throws Exception {
        ServletOutputStream out = Mockito.mock(ServletOutputStream.class);
        HttpServletResponseStubbingOperation.stubOutputStream(out).of(_response);
        assertThat(_response.getOutputStream(), is(out));

        HttpServletResponseStubbingOperation.stubOutputStream(null).of(_response);
        assertThat(_response.getOutputStream(), nullValue());
    }

    @Test(expected = AssertionError.class)
    public void testStubOutputStreamForNull() {
        ServletOutputStream out = Mockito.mock(ServletOutputStream.class);
        HttpServletResponseStubbingOperation.stubOutputStream(out).of(null);
    }

    @Test
    public void testStubWriter() throws Exception {
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        HttpServletResponseStubbingOperation.stubWriter(writer).of(_response);
        assertThat(_response.getWriter(), is(writer));

        HttpServletResponseStubbingOperation.stubWriter(null).of(_response);
        assertThat(_response.getWriter(), nullValue());
    }

    @Test(expected = AssertionError.class)
    public void testStubWriterForNull() {
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        HttpServletResponseStubbingOperation.stubWriter(writer).of(null);
    }

    @Test
    public void testStubCharacterEncoding() {
        HttpServletResponseStubbingOperation.stubCharacterEncoding("UTF-8").of(_response);
        assertThat(_response.getCharacterEncoding(), is("UTF-8"));

        HttpServletResponseStubbingOperation.stubCharacterEncoding(null).of(_response);
        assertThat(_response.getCharacterEncoding(), nullValue());
    }

    @Test(expected = AssertionError.class)
    public void testStubCharacterEncodingForNull() {
        HttpServletResponseStubbingOperation.stubCharacterEncoding("UTF-8").of(null);
    }

    @Test
    public void testStubLocale() {
        HttpServletResponseStubbingOperation.stubLocale(Locale.GERMAN).of(_response);
        assertThat(_response.getLocale(), is(Locale.GERMAN));

        HttpServletResponseStubbingOperation.stubLocale(null).of(_response);
        assertThat(_response.getLocale(), nullValue());
    }

    @Test(expected = AssertionError.class)
    public void testStubLocaleForNull() {
        HttpServletResponseStubbingOperation.stubLocale(Locale.GERMAN).of(null);
    }
}
