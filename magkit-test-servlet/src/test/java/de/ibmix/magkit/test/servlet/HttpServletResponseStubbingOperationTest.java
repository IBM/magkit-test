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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Testing HttpServletResponseStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 21.09.2011
 */
public class HttpServletResponseStubbingOperationTest {

    private HttpServletResponse _response;

    @BeforeEach
    public void setUp() {
        _response = mock(HttpServletResponse.class);
    }

    @Test
    public void testStubContentType() {
        HttpServletResponseStubbingOperation.stubContentType("type").of(_response);
        assertEquals("type", _response.getContentType());

        HttpServletResponseStubbingOperation.stubContentType(null).of(_response);
        assertNull(_response.getContentType());
    }

    @Test
    public void testStubContentTypeForNull() {
        assertThrows(IllegalArgumentException.class, () -> HttpServletResponseStubbingOperation.stubContentType("type").of(null));
    }

    @Test
    public void testStubOutputStream() throws Exception {
        ServletOutputStream out = Mockito.mock(ServletOutputStream.class);
        HttpServletResponseStubbingOperation.stubOutputStream(out).of(_response);
        assertEquals(out, _response.getOutputStream());

        HttpServletResponseStubbingOperation.stubOutputStream(null).of(_response);
        assertNull(_response.getOutputStream());
    }

    @Test
    public void testStubOutputStreamForNull() {
        ServletOutputStream out = Mockito.mock(ServletOutputStream.class);
        assertThrows(IllegalArgumentException.class, () -> HttpServletResponseStubbingOperation.stubOutputStream(out).of(null));
    }

    @Test
    public void testStubWriter() throws Exception {
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        HttpServletResponseStubbingOperation.stubWriter(writer).of(_response);
        assertEquals(writer, _response.getWriter());

        HttpServletResponseStubbingOperation.stubWriter(null).of(_response);
        assertNull(_response.getWriter());
    }

    @Test
    public void testStubWriterForNull() {
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        assertThrows(IllegalArgumentException.class, () -> HttpServletResponseStubbingOperation.stubWriter(writer).of(null));
    }

    @Test
    public void testStubCharacterEncoding() {
        HttpServletResponseStubbingOperation.stubCharacterEncoding("UTF-8").of(_response);
        assertEquals("UTF-8", _response.getCharacterEncoding());

        HttpServletResponseStubbingOperation.stubCharacterEncoding(null).of(_response);
        assertNull(_response.getCharacterEncoding());
    }

    @Test
    public void testStubCharacterEncodingForNull() {
        assertThrows(IllegalArgumentException.class, () -> HttpServletResponseStubbingOperation.stubCharacterEncoding("UTF-8").of(null));
    }

    @Test
    public void testStubLocale() {
        HttpServletResponseStubbingOperation.stubLocale(Locale.GERMAN).of(_response);
        assertEquals(Locale.GERMAN, _response.getLocale());

        HttpServletResponseStubbingOperation.stubLocale(null).of(_response);
        assertNull(_response.getLocale());
    }

    @Test
    public void testStubLocaleForNull() {
        assertThrows(IllegalArgumentException.class, () -> HttpServletResponseStubbingOperation.stubLocale(Locale.GERMAN).of(null));
    }
}
