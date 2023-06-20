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

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import static de.ibmix.magkit.mockito.servlet.PageContextStubbingOperation.stubHttpServletRequest;
import static de.ibmix.magkit.mockito.servlet.PageContextStubbingOperation.stubHttpServletResponse;
import static de.ibmix.magkit.mockito.servlet.PageContextStubbingOperation.stubHttpSession;
import static de.ibmix.magkit.mockito.servlet.PageContextStubbingOperation.stubServletContext;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockHttpServletResponse;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockHttpSession;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockPageContext;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockServletContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing PageContextStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 21.02.2012
 */
public class PageContextStubbingOperationTest {

    private PageContext _pageContext;

    @Before
    public void setUp() {
        _pageContext = mockPageContext();
    }

    @Test
    public void testStubServletContext() {
        ServletContext context = mockServletContext();
        stubServletContext(context).of(_pageContext);
        assertThat(_pageContext.getServletContext(), is(context));
    }

    @Test
    public void testStubServletContext2() {
        ServletContextStubbingOperation op1 = mock(ServletContextStubbingOperation.class);
        ServletContextStubbingOperation op2 = mock(ServletContextStubbingOperation.class);
        stubServletContext(op1, op2).of(_pageContext);

        ServletContext context = _pageContext.getServletContext();
        assertThat(context, notNullValue());
        verify(op1, times(1)).of(context);
        verify(op2, times(1)).of(context);
    }

    @Test
    public void testStubHttpSession() {
        HttpSession session = mockHttpSession("id");
        stubHttpSession(session).of(_pageContext);
        assertThat(_pageContext.getSession(), is(session));
        assertThat(_pageContext.getServletContext(), is(session.getServletContext()));

        HttpSession session2 = mockHttpSession("id2");
        stubHttpSession(session2).of(_pageContext);
        assertThat(_pageContext.getSession(), is(session2));
        assertThat(session2.getServletContext(), is(_pageContext.getServletContext()));
    }

    @Test
    public void testStubHttpSession2() {
        HttpSessionStubbingOperation op1 = mock(HttpSessionStubbingOperation.class);
        HttpSessionStubbingOperation op2 = mock(HttpSessionStubbingOperation.class);

        stubHttpSession("id", op1, op2).of(_pageContext);
        HttpSession session = _pageContext.getSession();
        assertThat(session, notNullValue());
        verify(op1, times(1)).of(session);
        verify(op2, times(1)).of(session);
    }

    @Test
    public void testStubHttpServletRequest() {
        HttpServletRequest request = mockHttpServletRequest();
        stubHttpServletRequest(request).of(_pageContext);
        assertThat(_pageContext.getRequest(), is((ServletRequest) request));

        // test that the session provided with the request will be set to page context:
        HttpServletRequestStubbingOperation.stubHttpSession("id").of((HttpServletRequest) request);
        stubHttpServletRequest((HttpServletRequest) request).of(_pageContext);
        assertThat(_pageContext.getSession(), notNullValue());
        assertThat(_pageContext.getSession().getId(), is("id"));

        //test that the session set to pageContext will be set to request if it has no session
        HttpServletRequest request2 = mockHttpServletRequest();
        stubHttpServletRequest(request2).of(_pageContext);
        assertThat(request2.getSession(), notNullValue());
        assertThat(request2.getSession().getId(), is("test"));
    }

    @Test
    public void testStubHttpServletRequest2() {
        HttpServletRequestStubbingOperation op1 = mock(HttpServletRequestStubbingOperation.class);
        HttpServletRequestStubbingOperation op2 = mock(HttpServletRequestStubbingOperation.class);

        stubHttpServletRequest(op1, op2).of(_pageContext);
        HttpServletRequest request = (HttpServletRequest) _pageContext.getRequest();
        assertThat(request, notNullValue());
        verify(op1, times(1)).of(request);
        verify(op2, times(1)).of(request);
    }

    @Test
    public void testStubHttpServletResponse() {
        ServletResponse response = mockHttpServletResponse();
        stubHttpServletResponse((HttpServletResponse) response).of(_pageContext);
        assertThat(_pageContext.getResponse(), is(response));
    }

    @Test
    public void testStubHttpServletResponse2() {
        HttpServletResponseStubbingOperation op1 = mock(HttpServletResponseStubbingOperation.class);
        HttpServletResponseStubbingOperation op2 = mock(HttpServletResponseStubbingOperation.class);

        stubHttpServletResponse(op1, op2).of(_pageContext);
        HttpServletResponse response = (HttpServletResponse) _pageContext.getResponse();
        assertThat(response, notNullValue());
        verify(op1, times(1)).of(response);
    }
}
