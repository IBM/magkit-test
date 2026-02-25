package de.ibmix.magkit.test.cms.context;

/*-
 * #%L
 * magkit-test-cms Magnolia Module
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

import de.ibmix.magkit.test.jcr.SessionStubbingOperation;
import de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation;
import de.ibmix.magkit.test.servlet.HttpServletResponseStubbingOperation;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.WebContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAccessManager;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAggregationState;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAttribute;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubExistingRequest;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubLocale;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubParameter;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubParameters;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubRequest;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubResponse;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletResponse;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * WebContextStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-07-27
 */
public class WebContextStubbingOperationTest {

    private WebContext _context;

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _context = mockWebContext();
    }

    @Test
    public void testStubLocale() throws Exception {
        assertNull(_context.getLocale());
        stubLocale(Locale.CHINESE).of(_context);
        assertEquals(Locale.CHINESE, _context.getLocale());
        stubLocale(null).of(_context);
        assertNull(_context.getLocale());
    }

    @Test
    public void testStubAggregationState() throws Exception {
        assertNull(_context.getAggregationState());
        AggregationState state = mock(AggregationState.class);
        stubAggregationState(state).of(_context);
        assertEquals(state, _context.getAggregationState());
        stubAggregationState(null).of(_context);
        assertNull(_context.getAggregationState());
    }

    @Test
    public void testStubAccessManager() throws Exception {
        assertNull(_context.getAccessManager(WEBSITE));
        assertNull(_context.getAccessManager("other repository"));
        AccessManager manager = mock(AccessManager.class);
        stubAccessManager(null, manager).of(_context);
        assertEquals(manager, _context.getAccessManager(WEBSITE));
        assertNull(_context.getAccessManager("other repository"));
        AccessManager manager2 = mock(AccessManager.class);
        stubAccessManager(EMPTY, manager2).of(_context);
        assertEquals(manager2, _context.getAccessManager(WEBSITE));
        assertNull(_context.getAccessManager("other repository"));
        AccessManager manager3 = mock(AccessManager.class);
        stubAccessManager("  ", manager3).of(_context);
        assertEquals(manager3, _context.getAccessManager(WEBSITE));
        assertNull(_context.getAccessManager("other repository"));
        AccessManager manager4 = mock(AccessManager.class);
        stubAccessManager("other repository", manager4).of(_context);
        assertEquals(manager3, _context.getAccessManager(WEBSITE));
        assertEquals(manager4, _context.getAccessManager("other repository"));
    }

    @Test
    public void testStubRequest() throws Exception {
        assertNotNull(_context.getRequest());
        assertNotNull(_context.getParameters());
        assertEquals(0, _context.getParameters().size());
        HttpServletRequest request = mockHttpServletRequest(
            HttpServletRequestStubbingOperation.stubParameter("key1", "value11", "value12", "value13"),
            HttpServletRequestStubbingOperation.stubParameter("key2", "value21", "value22", "value23"),
            HttpServletRequestStubbingOperation.stubContextPath("stairway.to.hell")
        );
        stubRequest(request).of(_context);
        assertEquals(request, _context.getRequest());
        assertNotNull(_context.getParameters());
        assertEquals(2, _context.getParameters().size());
        assertNotNull(_context.getParameterValues("key1"));
        assertEquals(3, _context.getParameterValues("key1").length);
        assertEquals("value21", _context.getParameter("key2"));
        assertEquals("stairway.to.hell", _context.getContextPath());
        stubRequest(null).of(_context);
        assertNull(_context.getRequest());
        assertNull(_context.getParameters());
        assertNull(_context.getContextPath());
    }

    @Test
    public void stubExistingRequestTest() throws RepositoryException {
        assertNotNull(_context.getRequest());
        stubExistingRequest(HttpServletRequestStubbingOperation.stubContextPath("test/aperto")).of(_context);
        HttpServletRequest request = _context.getRequest();
        assertNotNull(request);
        assertEquals("test/aperto", request.getContextPath());
        stubExistingRequest(HttpServletRequestStubbingOperation.stubMethod("test")).of(_context);
        assertEquals(request, _context.getRequest());
        assertEquals("test/aperto", _context.getRequest().getContextPath());
        assertEquals("test", _context.getRequest().getMethod());
    }

    @Test
    public void testStubParameters() throws Exception {
        assertNotNull(_context.getParameters());
        assertEquals(0, _context.getParameters().size());
        Map<String, String[]> parameters = new HashMap<>(2);
        parameters.put("name1", new String[]{"value1"});
        parameters.put("name2", new String[]{"value2a", "value2b"});
        stubParameters(parameters).of(_context);
        assertEquals("value1", _context.getParameter("name1"));
        assertEquals("value2a", _context.getParameter("name2"));
        assertEquals("value1", _context.getRequest().getParameter("name1"));
        assertEquals("value2a", _context.getRequest().getParameter("name2"));
        assertNotNull(_context.getParameterValues("name1"));
        assertEquals(1, _context.getParameterValues("name1").length);
        assertEquals("value1", _context.getParameterValues("name1")[0]);
        assertEquals("value1", _context.getRequest().getParameterValues("name1")[0]);
        assertNotNull(_context.getParameterValues("name2"));
        assertEquals(2, _context.getParameterValues("name2").length);
        assertEquals("value2a", _context.getParameterValues("name2")[0]);
        assertEquals("value2a", _context.getRequest().getParameterValues("name2")[0]);
        assertEquals("value2b", _context.getParameterValues("name2")[1]);
        assertEquals("value2b", _context.getRequest().getParameterValues("name2")[1]);
    }

    @Test
    public void testStubParameter() throws Exception {
        assertNull(_context.getParameter("test"));
        stubParameter("test", "value").of(_context);
        assertEquals("value", _context.getParameter("test"));
        assertEquals("value", _context.getRequest().getParameter("test"));
        assertNotNull(_context.getParameterValues("test"));
        assertEquals(1, _context.getParameterValues("test").length);
        assertEquals("value", _context.getParameterValues("test")[0]);
        assertEquals("value", _context.getRequest().getParameterValues("test")[0]);
    }

    @Test
    public void testStubAttribute() throws Exception {
        assertNull(_context.getAttribute("test"));
        stubAttribute("name", "value").of(_context);
        assertEquals("value", _context.getAttribute("name"));
        assertEquals("value", _context.getAttribute("name", Context.LOCAL_SCOPE));
        assertEquals("value", _context.getRequest().getAttribute("name"));
        assertNotNull(_context.getRequest().getAttributeNames());
        assertEquals("name", _context.getRequest().getAttributeNames().nextElement());
        stubAttribute("name", null).of(_context);
        assertNull(_context.getAttribute("name"));
        assertNull(_context.getAttribute("name", Context.LOCAL_SCOPE));
        assertNull(_context.getRequest().getAttribute("name"));
        assertNotNull(_context.getRequest().getAttributeNames());
        assertFalse(_context.getRequest().getAttributeNames().hasMoreElements());
    }

    @Test
    public void testStubScopedAttribute() throws Exception {
        assertNull(_context.getAttribute("test", Context.LOCAL_SCOPE));
        assertNull(_context.getAttribute("test", Context.SESSION_SCOPE));
        assertNotNull(_context.getAttributes());
        assertEquals(0, _context.getAttributes().size());
        assertNotNull(_context.getAttributes(Context.SESSION_SCOPE));
        assertEquals(0, _context.getAttributes(Context.SESSION_SCOPE).size());
        assertNotNull(_context.getAttributes(Context.LOCAL_SCOPE));
        assertEquals(0, _context.getAttributes(Context.LOCAL_SCOPE).size());
        assertNotNull(_context.getAttributes(Context.APPLICATION_SCOPE));
        assertEquals(0, _context.getAttributes(Context.APPLICATION_SCOPE).size());
        stubAttribute("name", "session", Context.SESSION_SCOPE).of(_context);
        assertNull(_context.getAttribute("name", Context.LOCAL_SCOPE));
        assertEquals("session", _context.getAttribute("name", Context.SESSION_SCOPE));
        assertEquals("session", _context.getAttribute("name"));
        assertEquals(1, _context.getAttributes().size());
        assertEquals("session", _context.getAttributes().get("name"));
        assertEquals(1, _context.getAttributes(Context.SESSION_SCOPE).size());
        assertEquals("session", _context.getAttributes(Context.SESSION_SCOPE).get("name"));
        assertEquals(0, _context.getAttributes(Context.LOCAL_SCOPE).size());
        stubAttribute("name", "request", Context.LOCAL_SCOPE).of(_context);
        assertEquals("request", _context.getAttribute("name", Context.LOCAL_SCOPE));
        assertEquals("session", _context.getAttribute("name", Context.SESSION_SCOPE));
        assertEquals("request", _context.getAttribute("name"));
        assertEquals(1, _context.getAttributes().size());
        assertEquals("session", _context.getAttributes().get("name"));
        assertEquals(1, _context.getAttributes(Context.SESSION_SCOPE).size());
        assertEquals("session", _context.getAttributes(Context.SESSION_SCOPE).get("name"));
        assertEquals(1, _context.getAttributes(Context.LOCAL_SCOPE).size());
        assertEquals("request", _context.getAttributes(Context.LOCAL_SCOPE).get("name"));
    }

    @Test
    public void testStubResponse() throws Exception {
        HttpServletResponse response = _context.getResponse();
        assertNotNull(response);
        HttpServletResponse newResponse = mockHttpServletResponse();
        stubResponse(newResponse).of(_context);
        assertNotNull(_context.getResponse());
        assertEquals(newResponse, _context.getResponse());
        stubResponse(null).of(_context);
        assertNull(_context.getResponse());
    }

    @Test
    public void testStubContextPath() throws Exception {
        assertNull(_context.getContextPath());
        stubContextPath("hasta la vista").of(_context);
        assertEquals("hasta la vista", _context.getContextPath());
        stubContextPath(null).of(_context);
        assertNull(_context.getContextPath());
    }

    @Test
    public void testStubNewJcrSession() throws RepositoryException {
        assertNull(_context.getJCRSession("repository"));
        Session session = mock(Session.class);
        WebContextStubbingOperation.stubJcrSession("repository", session).of(_context);
        assertEquals(session, _context.getJCRSession("repository"));
    }

    @Test
    public void stubExistingJcrSession() throws RepositoryException {
        Session session = _context.getJCRSession("test");
        assertNull(session);
        stubJcrSession("test").of(_context);
        session = _context.getJCRSession("test");
        assertNotNull(session);
        assertNull(_context.getJCRSession("test").getAttribute("name"));
        stubJcrSession("test", SessionStubbingOperation.stubAttribute("name", "value")).of(_context);
        assertEquals(session, _context.getJCRSession("test"));
        assertEquals("value", _context.getJCRSession("test").getAttribute("name"));
    }

    @Test
    public void testStubServletContext() {
        ServletContext servletContext = mock(ServletContext.class);
        WebContextStubbingOperation.stubServletContext(servletContext).of(_context);
        assertEquals(servletContext, _context.getServletContext());
        assertEquals(servletContext, _context.getRequest().getServletContext());
        assertEquals(servletContext, _context.getRequest().getSession().getServletContext());
    }

    @Test
    public void testStubServletContextNull() {
        assertNotNull(_context.getRequest());
        WebContextStubbingOperation.stubServletContext(null).of(_context);
        assertNotNull(_context.getRequest());
        assertNotNull(_context.getRequest().getSession());
        assertNull(_context.getServletContext());
        assertNull(_context.getRequest().getSession().getServletContext());
    }

    @Test
    public void testStubUser() {
        assertNull(_context.getUser());
        User user = mock(User.class);
        WebContextStubbingOperation.stubUser(user).of(_context);
        assertEquals(user, _context.getUser());
    }

    @Test
    public void stubExistingRequestCreatesNewWhenAbsent() throws Exception {
        WebContextStubbingOperation.stubRequest(null).of(_context);
        assertNull(_context.getRequest());
        stubExistingRequest(HttpServletRequestStubbingOperation.stubContextPath("/created")).of(_context);
        assertNotNull(_context.getRequest());
        assertEquals("/created", _context.getRequest().getContextPath());
    }

    @Test
    public void stubExistingResponseCreatesAndAugments() throws Exception {
        HttpServletResponse initial = _context.getResponse();
        assertNotNull(initial);
        WebContextStubbingOperation.stubResponse(null).of(_context);
        assertNull(_context.getResponse());
        WebContextStubbingOperation.stubExistingResponse(HttpServletResponseStubbingOperation.stubCharacterEncoding("UTF-8")).of(_context);
        HttpServletResponse first = _context.getResponse();
        assertNotNull(first);
        assertEquals("UTF-8", first.getCharacterEncoding());
        WebContextStubbingOperation.stubExistingResponse(HttpServletResponseStubbingOperation.stubCharacterEncoding("ISO-8859-1")).of(_context);
        assertEquals(first, _context.getResponse());
        assertEquals("ISO-8859-1", _context.getResponse().getCharacterEncoding());
    }

    @Test
    public void stubContextPathCreatesRequestWhenNone() throws Exception {
        WebContextStubbingOperation.stubRequest(null).of(_context);
        assertNull(_context.getRequest());
        stubContextPath("/auto").of(_context);
        assertNotNull(_context.getRequest());
        assertEquals("/auto", _context.getContextPath());
    }

    @Test
    public void stubParametersNullMapDoesNothing() throws Exception {
        assertNotNull(_context.getRequest());
        Map<String, String> beforeMap = _context.getParameters();
        int before = beforeMap == null ? 0 : beforeMap.size();
        WebContextStubbingOperation.stubParameters(null).of(_context);
        Map<String, String> afterMap = _context.getParameters();
        int after = afterMap == null ? 0 : afterMap.size();
        assertEquals(before, after);
    }

    @Test
    public void stubParameterNullValuesRemovesParameter() throws Exception {
        stubParameter("temp", "x", "y").of(_context);
        assertEquals(2, _context.getParameterValues("temp").length);
        stubParameter("temp", (String[]) null).of(_context);
        assertNull(_context.getParameter("temp"));
        assertNull(_context.getParameterValues("temp"));
    }

    @Test
    public void stubAttributeApplicationScopeNoOp() throws Exception {
        stubAttribute("appKey", "val", Context.APPLICATION_SCOPE).of(_context);
        assertNull(_context.getAttribute("appKey"));
        assertFalse(_context.getAttributes(Context.APPLICATION_SCOPE).containsKey("appKey"));
    }

    @Test
    public void stubSessionAttributeRemoval() throws Exception {
        stubAttribute("toRemove", "sessionVal", Context.SESSION_SCOPE).of(_context);
        assertEquals("sessionVal", _context.getAttribute("toRemove", Context.SESSION_SCOPE));
        stubAttribute("toRemove", null, Context.SESSION_SCOPE).of(_context);
        assertNull(_context.getAttribute("toRemove", Context.SESSION_SCOPE));
        assertFalse(_context.getAttributes(Context.SESSION_SCOPE).containsKey("toRemove"));
    }

    @Test
    public void stubJcrSessionAddsAdditionalStubbingsOnExisting() throws Exception {
        stubJcrSession("extra").of(_context);
        Session first = _context.getJCRSession("extra");
        assertNotNull(first);
        stubJcrSession("extra", SessionStubbingOperation.stubAttribute("k", "v")).of(_context);
        Session again = _context.getJCRSession("extra");
        assertEquals(first, again);
        assertEquals("v", again.getAttribute("k"));
    }

    @Test
    public void stubAttributeInvalidScopeNoOp() {
        int localSizeBefore = _context.getAttributes(Context.LOCAL_SCOPE).size();
        int sessionSizeBefore = _context.getAttributes(Context.SESSION_SCOPE).size();
        int appSizeBefore = _context.getAttributes(Context.APPLICATION_SCOPE).size();
        WebContextStubbingOperation.stubAttribute("invalid", "value", -123).of(_context);
        assertNull(_context.getAttribute("invalid"));
        assertEquals(localSizeBefore, _context.getAttributes(Context.LOCAL_SCOPE).size());
        assertEquals(sessionSizeBefore, _context.getAttributes(Context.SESSION_SCOPE).size());
        assertEquals(appSizeBefore, _context.getAttributes(Context.APPLICATION_SCOPE).size());
    }

    @Test
    public void stubJcrSessionOverrideReplacesPrevious() throws RepositoryException {
        Session first = mock(Session.class);
        WebContextStubbingOperation.stubJcrSession("overrideRepo", first).of(_context);
        assertEquals(first, _context.getJCRSession("overrideRepo"));
        Session second = mock(Session.class);
        WebContextStubbingOperation.stubJcrSession("overrideRepo", second).of(_context);
        assertEquals(second, _context.getJCRSession("overrideRepo"));
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }
}
