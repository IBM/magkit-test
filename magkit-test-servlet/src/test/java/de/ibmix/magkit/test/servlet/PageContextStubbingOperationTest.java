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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @BeforeEach
    public void setUp() {
        _pageContext = mockPageContext();
    }

    @Test
    public void testStubServletContext() {
        ServletContext context = mockServletContext();
        stubServletContext(context).of(_pageContext);
        assertEquals(context, _pageContext.getServletContext());
    }

    @Test
    public void testStubServletContext2() {
        ServletContextStubbingOperation op1 = mock(ServletContextStubbingOperation.class);
        ServletContextStubbingOperation op2 = mock(ServletContextStubbingOperation.class);
        stubServletContext(op1, op2).of(_pageContext);

        ServletContext context = _pageContext.getServletContext();
        assertNotNull(context);
        verify(op1, times(1)).of(context);
        verify(op2, times(1)).of(context);
    }

    @Test
    public void testStubHttpSession() {
        HttpSession session = mockHttpSession("id");
        stubHttpSession(session).of(_pageContext);
        assertEquals(session, _pageContext.getSession());
        assertEquals(session.getServletContext(), _pageContext.getServletContext());

        HttpSession session2 = mockHttpSession("id2");
        stubHttpSession(session2).of(_pageContext);
        assertEquals(session2, _pageContext.getSession());
        assertEquals(_pageContext.getServletContext(), session2.getServletContext());
    }

    @Test
    public void testStubHttpSession2() {
        HttpSessionStubbingOperation op1 = mock(HttpSessionStubbingOperation.class);
        HttpSessionStubbingOperation op2 = mock(HttpSessionStubbingOperation.class);

        stubHttpSession("id", op1, op2).of(_pageContext);
        HttpSession session = _pageContext.getSession();
        assertNotNull(session);
        verify(op1, times(1)).of(session);
        verify(op2, times(1)).of(session);
    }

    @Test
    public void testStubHttpServletRequest() {
        HttpServletRequest request = mockHttpServletRequest();
        stubHttpServletRequest(request).of(_pageContext);
        assertEquals(request, _pageContext.getRequest());

        HttpServletRequestStubbingOperation.stubHttpSession("id").of(request);
        stubHttpServletRequest(request).of(_pageContext);
        assertNotNull(_pageContext.getSession());
        assertEquals("id", _pageContext.getSession().getId());

        HttpServletRequest request2 = mockHttpServletRequest();
        stubHttpServletRequest(request2).of(_pageContext);
        assertNotNull(request2.getSession());
        assertEquals("test", request2.getSession().getId());
    }

    @Test
    public void testStubHttpServletRequest2() {
        HttpServletRequestStubbingOperation op1 = mock(HttpServletRequestStubbingOperation.class);
        HttpServletRequestStubbingOperation op2 = mock(HttpServletRequestStubbingOperation.class);

        stubHttpServletRequest(op1, op2).of(_pageContext);
        HttpServletRequest request = (HttpServletRequest) _pageContext.getRequest();
        assertNotNull(request);
        verify(op1, times(1)).of(request);
        verify(op2, times(1)).of(request);
    }

    @Test
    public void testStubHttpServletResponse() {
        HttpServletResponse response = mockHttpServletResponse();
        stubHttpServletResponse(response).of(_pageContext);
        assertEquals(response, _pageContext.getResponse());
    }

    @Test
    public void testStubHttpServletResponse2() {
        HttpServletResponseStubbingOperation op1 = mock(HttpServletResponseStubbingOperation.class);
        HttpServletResponseStubbingOperation op2 = mock(HttpServletResponseStubbingOperation.class);

        stubHttpServletResponse(op1, op2).of(_pageContext);
        HttpServletResponse response = (HttpServletResponse) _pageContext.getResponse();
        assertNotNull(response);
        verify(op1, times(1)).of(response);
        verify(op2, times(1)).of(response);
    }

    @Test
    public void testStubHttpServletRequestCreatesNewWhenAbsent() {
        PageContext pc = mock(PageContext.class);
        HttpServletRequestStubbingOperation op1 = mock(HttpServletRequestStubbingOperation.class);
        HttpServletRequestStubbingOperation op2 = mock(HttpServletRequestStubbingOperation.class);

        stubHttpServletRequest(op1, op2).of(pc);

        HttpServletRequest created = (HttpServletRequest) pc.getRequest();
        assertNotNull(created, "Request should be created and set on page context");
        verify(op1, times(1)).of(created);
        verify(op2, times(1)).of(created);
    }

    @Test
    public void testStubHttpServletResponseCreatesNewWhenAbsent() {
        PageContext pc = mock(PageContext.class);
        HttpServletResponseStubbingOperation op = mock(HttpServletResponseStubbingOperation.class);

        stubHttpServletResponse(op).of(pc);

        HttpServletResponse created = (HttpServletResponse) pc.getResponse();
        assertNotNull(created, "Response should be created and set on page context");
        verify(op, times(1)).of(created);
    }

    @Test
    public void testStubHttpSessionCreatesSessionWhenAbsent() {
        PageContext pc = mock(PageContext.class);
        HttpSessionStubbingOperation op = mock(HttpSessionStubbingOperation.class);

        stubHttpSession("newSession", op).of(pc);

        HttpServletRequest req = (HttpServletRequest) pc.getRequest();
        assertNotNull(req, "Request should be created");
        HttpSession session = req.getSession();
        assertNotNull(session, "Session should be created on request");
        assertEquals("newSession", session.getId(), "Session id should match");
        verify(op, times(1)).of(session);
    }

    @Test
    public void testStubServletContextCreatesContextWhenAbsent() {
        PageContext pc = mock(PageContext.class);
        ServletContextStubbingOperation op1 = mock(ServletContextStubbingOperation.class);
        ServletContextStubbingOperation op2 = mock(ServletContextStubbingOperation.class);

        stubServletContext(op1, op2).of(pc);

        verify(op1, times(1)).of(any(ServletContext.class));
        verify(op2, times(1)).of(any(ServletContext.class));
        HttpServletRequest req = (HttpServletRequest) pc.getRequest();
        assertNotNull(req);
        assertNotNull(req.getSession());
    }

    @Test
    public void testStubServletContextWithProvidedContextCreatesSessionWhenAbsent() {
        PageContext pc = mock(PageContext.class);
        ServletContext provided = mockServletContext();

        stubServletContext(provided).of(pc);

        HttpServletRequest req = (HttpServletRequest) pc.getRequest();
        assertNotNull(req, "Request should be created");
        HttpSession session = req.getSession();
        assertNotNull(session, "Session should be created");
        assertEquals(provided, session.getServletContext(), "ServletContext of session should be the provided one");
    }
}
