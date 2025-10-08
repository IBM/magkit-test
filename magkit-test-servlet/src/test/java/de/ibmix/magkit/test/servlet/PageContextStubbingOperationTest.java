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

import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import static de.ibmix.magkit.test.servlet.PageContextStubbingOperation.stubHttpServletRequest;
import static de.ibmix.magkit.test.servlet.PageContextStubbingOperation.stubHttpServletResponse;
import static de.ibmix.magkit.test.servlet.PageContextStubbingOperation.stubHttpSession;
import static de.ibmix.magkit.test.servlet.PageContextStubbingOperation.stubServletContext;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletResponse;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpSession;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockPageContext;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockServletContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        assertThat(_pageContext.getRequest(), is(request));

        // test that the session provided with the request will be set to page context:
        HttpServletRequestStubbingOperation.stubHttpSession("id").of(request);
        stubHttpServletRequest(request).of(_pageContext);
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
        HttpServletResponse response = mockHttpServletResponse();
        stubHttpServletResponse(response).of(_pageContext);
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
        verify(op2, times(1)).of(response);
    }

    @Test
    public void testStubHttpServletRequestCreatesNewWhenAbsent() {
        // raw mock: getRequest() returns null
        PageContext pc = mock(PageContext.class);
        HttpServletRequestStubbingOperation op1 = mock(HttpServletRequestStubbingOperation.class);
        HttpServletRequestStubbingOperation op2 = mock(HttpServletRequestStubbingOperation.class);

        stubHttpServletRequest(op1, op2).of(pc);

        HttpServletRequest created = (HttpServletRequest) pc.getRequest();
        assertThat("Request should be created and set on page context", created, notNullValue());
        // operations executed exactly once during mockHttpServletRequest()
        verify(op1, times(1)).of(created);
        verify(op2, times(1)).of(created);
    }

    @Test
    public void testStubHttpServletResponseCreatesNewWhenAbsent() {
        // getResponse() returns null
        PageContext pc = mock(PageContext.class);
        HttpServletResponseStubbingOperation op = mock(HttpServletResponseStubbingOperation.class);

        stubHttpServletResponse(op).of(pc);

        HttpServletResponse created = (HttpServletResponse) pc.getResponse();
        assertThat("Response should be created and set on page context", created, notNullValue());
        // applied once inside mock factory
        verify(op, times(1)).of(created);
    }

    @Test
    public void testStubHttpSessionCreatesSessionWhenAbsent() {
        // getSession() returns null
        PageContext pc = mock(PageContext.class);
        HttpSessionStubbingOperation op = mock(HttpSessionStubbingOperation.class);

        stubHttpSession("newSession", op).of(pc);

        // getSession() on raw mock still null (operation only ensured a request+session exists on the request)
        // therefore inspect the created request
        HttpServletRequest req = (HttpServletRequest) pc.getRequest();
        assertThat("Request should be created", req, notNullValue());
        HttpSession session = req.getSession();
        assertThat("Session should be created on request", session, notNullValue());
        assertThat("Session id should match", session.getId(), is("newSession"));
        verify(op, times(1)).of(session);
    }

    @Test
    public void testStubServletContextCreatesContextWhenAbsent() {
        PageContext pc = mock(PageContext.class); // getServletContext() returns null
        ServletContextStubbingOperation op1 = mock(ServletContextStubbingOperation.class);
        ServletContextStubbingOperation op2 = mock(ServletContextStubbingOperation.class);

        stubServletContext(op1, op2).of(pc);

        // PageContext#getServletContext still null (not wired because raw mock), verify ops executed with some ServletContext
        verify(op1, times(1)).of(any(ServletContext.class));
        verify(op2, times(1)).of(any(ServletContext.class));
        // ensure a request + session were created indirectly
        HttpServletRequest req = (HttpServletRequest) pc.getRequest();
        assertThat(req, notNullValue());
        assertThat(req.getSession(), notNullValue());
    }

    @Test
    public void testStubServletContextWithProvidedContextCreatesSessionWhenAbsent() {
        PageContext pc = mock(PageContext.class);
        ServletContext provided = mockServletContext();

        stubServletContext(provided).of(pc);

        HttpServletRequest req = (HttpServletRequest) pc.getRequest();
        assertThat("Request should be created", req, notNullValue());
        HttpSession session = req.getSession();
        assertThat("Session should be created", session, notNullValue());
        assertThat("ServletContext of session should be the provided one", session.getServletContext(), is(provided));
    }
}
