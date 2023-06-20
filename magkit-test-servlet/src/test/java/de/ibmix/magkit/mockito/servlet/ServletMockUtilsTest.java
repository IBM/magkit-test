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

import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockCookie;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockHttpServletResponse;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockHttpSession;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockPageContext;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockServletContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test ServletMockUtilsTest.
 *
 * @author wolf.bubenik
 * @since 17.03.2011
 */
public class ServletMockUtilsTest {

    @Test
    public void testMockHttpServletRequest() {
        HttpServletRequestStubbingOperation op1 = mock(HttpServletRequestStubbingOperation.class);
        HttpServletRequestStubbingOperation op2 = mock(HttpServletRequestStubbingOperation.class);
        HttpServletRequest result = mockHttpServletRequest(op1, op2);
        assertThat(result, notNullValue());
        verify(op1, times(1)).of(result);
        verify(op2, times(1)).of(result);
    }

    @Test(expected = AssertionError.class)
    public void testMockHttpServletRequestForNull() {
        mockHttpServletRequest(null);
    }

    @Test
    public void testMockHttpServletResponse() throws Exception {
        HttpServletResponseStubbingOperation op1 = mock(HttpServletResponseStubbingOperation.class);
        HttpServletResponseStubbingOperation op2 = mock(HttpServletResponseStubbingOperation.class);
        HttpServletResponse result = mockHttpServletResponse(op1, op2);
        assertThat(result, notNullValue());
        assertThat(result.getWriter(), notNullValue());
        assertThat(result.getOutputStream(), notNullValue());
        assertThat(result.encodeRedirectURL("some.test/url"), is("some.test/url"));
        assertThat(result.encodeURL("some.test/url"), is("some.test/url"));
        verify(op1, times(1)).of(result);
        verify(op2, times(1)).of(result);
    }

    @Test(expected = AssertionError.class)
    public void testMockHttpServletResponseForNull() {
        mockHttpServletResponse(null);
    }

    @Test
    public void testMockServletContext() {
        ServletContextStubbingOperation op1 = mock(ServletContextStubbingOperation.class);
        ServletContextStubbingOperation op2 = mock(ServletContextStubbingOperation.class);
        ServletContext result = mockServletContext(op1, op2);
        assertThat(result, notNullValue());
        assertThat(result.getAttributeNames(), notNullValue());
        assertThat(result.getAttributeNames().hasMoreElements(), is(false));
        assertThat(result.getInitParameterNames(), notNullValue());
        assertThat(result.getInitParameterNames().hasMoreElements(), is(false));
        verify(op1, times(1)).of(result);
        verify(op2, times(1)).of(result);
    }

    @Test(expected = AssertionError.class)
    public void testMockServletContextForNull() {
        mockServletContext(null);
    }

    @Test
    public void testMockPageContext() {
        PageContextStubbingOperation op1 = mock(PageContextStubbingOperation.class);
        PageContextStubbingOperation op2 = mock(PageContextStubbingOperation.class);
        PageContext result = mockPageContext(op1, op2);
        assertThat(result, notNullValue());
        verify(op1, times(1)).of(result);
        verify(op2, times(1)).of(result);
    }

    @Test(expected = AssertionError.class)
    public void testMockPageContextForNull() {
        mockPageContext(null);
    }

    @Test
    public void testMockHttpSession() {
        HttpSessionStubbingOperation op1 = mock(HttpSessionStubbingOperation.class);
        HttpSessionStubbingOperation op2 = mock(HttpSessionStubbingOperation.class);
        HttpSession result = mockHttpSession("id", op1, op2);
        assertThat(result, notNullValue());
        assertThat(result.getId(), is("id"));
        assertThat(result.getAttributeNames(), notNullValue());
        assertThat(result.getAttributeNames().hasMoreElements(), is(false));
        verify(op1, times(1)).of(result);
        verify(op2, times(1)).of(result);
    }

    @Test(expected = AssertionError.class)
    public void testMockHttpSessionForNull() {
        mockHttpSession(null, null);
    }

    @Test
    public void testMockCookie() {
        CookieStubbingOperation op1 = mock(CookieStubbingOperation.class);
        CookieStubbingOperation op2 = mock(CookieStubbingOperation.class);

        Cookie cookie = mockCookie("cookieName", "cookieValue", op1, op2);
        assertThat(cookie, notNullValue());
        assertThat(cookie.getName(), is("cookieName"));
        assertThat(cookie.getValue(), is("cookieValue"));
        verify(op1, times(1)).of(cookie);
        verify(op2, times(1)).of(cookie);
    }
}
