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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * WebContextStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-07-27
 */
public class WebContextStubbingOperationTest {

    private WebContext _context;

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _context = mockWebContext();
    }

    @Test
    public void testStubLocale() throws Exception {
        assertThat(_context.getLocale(), nullValue());

        stubLocale(Locale.CHINESE).of(_context);
        assertThat(_context.getLocale(), is(Locale.CHINESE));

        stubLocale(null).of(_context);
        assertThat(_context.getLocale(), nullValue());
    }

    @Test
    public void testStubAggregationState() throws Exception {
        assertThat(_context.getAggregationState(), nullValue());

        AggregationState state = mock(AggregationState.class);
        stubAggregationState(state).of(_context);
        assertThat(_context.getAggregationState(), is(state));

        stubAggregationState(null).of(_context);
        assertThat(_context.getAggregationState(), nullValue());
    }

    @Test
    public void testStubAccessManager() throws Exception {
        assertThat(_context.getAccessManager(WEBSITE), nullValue());
        assertThat(_context.getAccessManager("other repository"), nullValue());

        AccessManager manager = mock(AccessManager.class);
        stubAccessManager(null, manager).of(_context);
        assertThat(_context.getAccessManager(WEBSITE), is(manager));
        assertThat(_context.getAccessManager("other repository"), nullValue());

        AccessManager manager2 = mock(AccessManager.class);
        stubAccessManager(EMPTY, manager2).of(_context);
        assertThat(_context.getAccessManager(WEBSITE), is(manager2));
        assertThat(_context.getAccessManager("other repository"), nullValue());

        AccessManager manager3 = mock(AccessManager.class);
        stubAccessManager("  ", manager3).of(_context);
        assertThat(_context.getAccessManager(WEBSITE), is(manager3));
        assertThat(_context.getAccessManager("other repository"), nullValue());

        AccessManager manager4 = mock(AccessManager.class);
        stubAccessManager("other repository", manager4).of(_context);
        assertThat(_context.getAccessManager(WEBSITE), is(manager3));
        assertThat(_context.getAccessManager("other repository"), is(manager4));
    }

    @Test
    public void testStubRequest() throws Exception {
        assertThat(_context.getRequest(), notNullValue());
        assertThat(_context.getParameters(), notNullValue());
        assertThat(_context.getParameters().size(), is(0));

        HttpServletRequest request = mockHttpServletRequest(
            HttpServletRequestStubbingOperation.stubParameter("key1", "value11", "value12", "value13"),
            HttpServletRequestStubbingOperation.stubParameter("key2", "value21", "value22", "value23"),
            HttpServletRequestStubbingOperation.stubContextPath("stairway.to.hell")
        );
        stubRequest(request).of(_context);
        assertThat(_context.getRequest(), is(request));
        assertThat(_context.getParameters(), notNullValue());
        assertThat(_context.getParameters().size(), is(2));
        assertThat(_context.getParameterValues("key1"), notNullValue());
        assertThat(_context.getParameterValues("key1").length, is(3));
        assertThat(_context.getParameter("key2"), is("value21"));
        assertThat(_context.getContextPath(), is("stairway.to.hell"));

        stubRequest(null).of(_context);
        assertThat(_context.getRequest(), nullValue());
        assertThat(_context.getParameters(), nullValue());
        assertThat(_context.getContextPath(), nullValue());
    }

    @Test
    public void stubExistingRequestTest() throws RepositoryException {
        assertThat(_context.getRequest(), notNullValue());

        stubExistingRequest(HttpServletRequestStubbingOperation.stubContextPath("test/aperto")).of(_context);
        HttpServletRequest request = _context.getRequest();
        assertThat(request, notNullValue());
        assertThat(request.getContextPath(), is("test/aperto"));

        stubExistingRequest(HttpServletRequestStubbingOperation.stubMethod("test")).of(_context);
        assertThat(_context.getRequest(), is(request));
        assertThat(_context.getRequest().getContextPath(), is("test/aperto"));
        assertThat(_context.getRequest().getMethod(), is("test"));
    }

    @Test
    public void testStubParameters() throws Exception {
        assertThat(_context.getParameters(), notNullValue());
        assertThat(_context.getParameters().size(), is(0));

        Map<String, String[]> parameters = new HashMap<>(2);
        parameters.put("name1", new String[]{"value1"});
        parameters.put("name2", new String[]{"value2a", "value2b"});
        stubParameters(parameters).of(_context);
        assertThat(_context.getParameter("name1"), is("value1"));
        assertThat(_context.getParameter("name2"), is("value2a"));
        assertThat(_context.getRequest().getParameter("name1"), is("value1"));
        assertThat(_context.getRequest().getParameter("name2"), is("value2a"));

        assertThat(_context.getParameterValues("name1"), notNullValue());
        assertThat(_context.getParameterValues("name1").length, is(1));
        assertThat(_context.getParameterValues("name1")[0], is("value1"));
        assertThat(_context.getRequest().getParameterValues("name1")[0], is("value1"));

        assertThat(_context.getParameterValues("name2"), notNullValue());
        assertThat(_context.getParameterValues("name2").length, is(2));
        assertThat(_context.getParameterValues("name2")[0], is("value2a"));
        assertThat(_context.getRequest().getParameterValues("name2")[0], is("value2a"));
        assertThat(_context.getParameterValues("name2")[1], is("value2b"));
        assertThat(_context.getRequest().getParameterValues("name2")[1], is("value2b"));
    }

    @Test
    public void testStubParameter() throws Exception {
        assertThat(_context.getParameter("test"), nullValue());

        stubParameter("test", "value").of(_context);
        assertThat(_context.getParameter("test"), is("value"));
        assertThat(_context.getRequest().getParameter("test"), is("value"));

        assertThat(_context.getParameterValues("test"), notNullValue());
        assertThat(_context.getParameterValues("test").length, is(1));
        assertThat(_context.getParameterValues("test")[0], is("value"));
        assertThat(_context.getRequest().getParameterValues("test")[0], is("value"));
    }

    @Test
    public void testStubAttribute() throws Exception {
        assertThat(_context.getAttribute("test"), nullValue());

        stubAttribute("name", "value").of(_context);
        assertThat(_context.getAttribute("name"), is((Object) "value"));
        assertThat(_context.getAttribute("name", Context.LOCAL_SCOPE), is((Object) "value"));
        assertThat(_context.getRequest().getAttribute("name"), is("value"));
        assertThat(_context.getRequest().getAttributeNames(), notNullValue());
        assertThat(_context.getRequest().getAttributeNames().nextElement(), is((Object) "name"));

        stubAttribute("name", null).of(_context);
        assertThat(_context.getAttribute("name"), nullValue());
        assertThat(_context.getAttribute("name", Context.LOCAL_SCOPE), nullValue());
        assertThat(_context.getRequest().getAttribute("name"), nullValue());
        assertThat(_context.getRequest().getAttributeNames(), notNullValue());
        assertThat(_context.getRequest().getAttributeNames().hasMoreElements(), is(false));
    }

    @Test
    public void testStubScopedAttribute() throws Exception {
        assertThat(_context.getAttribute("test", Context.LOCAL_SCOPE), nullValue());
        assertThat(_context.getAttribute("test", Context.SESSION_SCOPE), nullValue());
        assertThat(_context.getAttributes(), notNullValue());
        assertThat(_context.getAttributes().size(), is(0));
        assertThat(_context.getAttributes(Context.SESSION_SCOPE), notNullValue());
        assertThat(_context.getAttributes(Context.SESSION_SCOPE).size(), is(0));
        assertThat(_context.getAttributes(Context.LOCAL_SCOPE), notNullValue());
        assertThat(_context.getAttributes(Context.LOCAL_SCOPE).size(), is(0));
        assertThat(_context.getAttributes(Context.APPLICATION_SCOPE), notNullValue());
        assertThat(_context.getAttributes(Context.APPLICATION_SCOPE).size(), is(0));

        stubAttribute("name", "session", Context.SESSION_SCOPE).of(_context);
        assertThat(_context.getAttribute("name", Context.LOCAL_SCOPE), nullValue());
        assertThat(_context.getAttribute("name", Context.SESSION_SCOPE), is((Object) "session"));
        assertThat(_context.getAttribute("name"), is((Object) "session"));
        assertThat(_context.getAttributes().size(), is(1));
        assertThat(_context.getAttributes().get("name"), is("session"));
        assertThat(_context.getAttributes(Context.SESSION_SCOPE).size(), is(1));
        assertThat(_context.getAttributes(Context.SESSION_SCOPE).get("name"), is("session"));
        assertThat(_context.getAttributes(Context.LOCAL_SCOPE).size(), is(0));

        stubAttribute("name", "request", Context.LOCAL_SCOPE).of(_context);
        assertThat(_context.getAttribute("name", Context.LOCAL_SCOPE), is((Object) "request"));
        assertThat(_context.getAttribute("name", Context.SESSION_SCOPE), is((Object) "session"));
        assertThat(_context.getAttribute("name"), is((Object) "request"));
        assertThat(_context.getAttributes().size(), is(1));
        assertThat(_context.getAttributes().get("name"), is("session"));
        assertThat(_context.getAttributes(Context.SESSION_SCOPE).size(), is(1));
        assertThat(_context.getAttributes(Context.SESSION_SCOPE).get("name"), is("session"));
        assertThat(_context.getAttributes(Context.LOCAL_SCOPE).size(), is(1));
        assertThat(_context.getAttributes(Context.LOCAL_SCOPE).get("name"), is("request"));
    }

    @Test
    public void testStubResponse() throws Exception {
        HttpServletResponse response = _context.getResponse();
        assertThat(response, notNullValue());

        HttpServletResponse newResponse = mockHttpServletResponse();
        stubResponse(newResponse).of(_context);
        assertThat(_context.getResponse(), notNullValue());
        assertThat(_context.getResponse(), is(newResponse));

        stubResponse(null).of(_context);
        assertThat(_context.getResponse(), nullValue());
    }

    @Test
    public void testStubContextPath() throws Exception {
        assertThat(_context.getContextPath(), nullValue());

        stubContextPath("hasta la vista").of(_context);
        assertThat(_context.getContextPath(), is("hasta la vista"));

        stubContextPath(null).of(_context);
        assertThat(_context.getContextPath(), nullValue());
    }

    @Test
    public void testStubNewJcrSession() throws RepositoryException {
        assertThat(_context.getJCRSession("repository"), nullValue());

        Session session = mock(Session.class);
        WebContextStubbingOperation.stubJcrSession("repository", session).of(_context);
        assertThat(_context.getJCRSession("repository"), is(session));
    }

    @Test
    public void stubExistingJcrSession() throws RepositoryException {
        Session session = _context.getJCRSession("test");
        assertThat(session, nullValue());

        stubJcrSession("test").of(_context);
        session = _context.getJCRSession("test");
        assertThat(session, notNullValue());
        assertThat(_context.getJCRSession("test").getAttribute("name"), nullValue());

        stubJcrSession("test", SessionStubbingOperation.stubAttribute("name", "value")).of(_context);
        assertThat(_context.getJCRSession("test"), is(session));
        assertThat(_context.getJCRSession("test").getAttribute("name"), is("value"));
    }

    @Test
    public void testStubServletContext() {
        ServletContext servletContext = mock(ServletContext.class);
        WebContextStubbingOperation.stubServletContext(servletContext).of(_context);
        assertThat(_context.getServletContext(), is(servletContext));
        assertThat(_context.getRequest().getServletContext(), is(servletContext));
        assertThat(_context.getRequest().getSession().getServletContext(), is(servletContext));
    }

    @Test
    public void testStubServletContextNull() {
        // ensure request/session exist and have a servlet context initially
        assertThat(_context.getRequest(), notNullValue());
        // call with null should set servlet context of session to null but not remove request/session
        WebContextStubbingOperation.stubServletContext(null).of(_context);
        assertThat(_context.getRequest(), notNullValue());
        assertThat(_context.getRequest().getSession(), notNullValue());
        assertThat(_context.getServletContext(), nullValue());
        assertThat(_context.getRequest().getSession().getServletContext(), nullValue());
    }

    @Test
    public void testStubUser() {
        assertThat(_context.getUser(), nullValue());

        User user = mock(User.class);
        WebContextStubbingOperation.stubUser(user).of(_context);
        assertThat(_context.getUser(), is(user));
    }

    @Test
    public void stubExistingRequestCreatesNewWhenAbsent() throws Exception {
        // remove existing request
        WebContextStubbingOperation.stubRequest(null).of(_context);
        assertThat(_context.getRequest(), nullValue());
        stubExistingRequest(HttpServletRequestStubbingOperation.stubContextPath("/created")).of(_context);
        assertThat(_context.getRequest(), notNullValue());
        assertThat(_context.getRequest().getContextPath(), is("/created"));
    }

    @Test
    public void stubExistingResponseCreatesAndAugments() throws Exception {
        // initial response present
        HttpServletResponse initial = _context.getResponse();
        assertThat(initial, notNullValue());
        // remove response
        WebContextStubbingOperation.stubResponse(null).of(_context);
        assertThat(_context.getResponse(), nullValue());
        // create new with encoding UTF-8
        WebContextStubbingOperation.stubExistingResponse(HttpServletResponseStubbingOperation.stubCharacterEncoding("UTF-8")).of(_context);
        HttpServletResponse first = _context.getResponse();
        assertThat(first, notNullValue());
        assertThat(first.getCharacterEncoding(), is("UTF-8"));
        // augment existing response changing encoding
        WebContextStubbingOperation.stubExistingResponse(HttpServletResponseStubbingOperation.stubCharacterEncoding("ISO-8859-1")).of(_context);
        assertThat(_context.getResponse(), is(first));
        assertThat(_context.getResponse().getCharacterEncoding(), is("ISO-8859-1"));
    }

    @Test
    public void stubContextPathCreatesRequestWhenNone() throws Exception {
        WebContextStubbingOperation.stubRequest(null).of(_context);
        assertThat(_context.getRequest(), nullValue());
        stubContextPath("/auto").of(_context);
        assertThat(_context.getRequest(), notNullValue());
        assertThat(_context.getContextPath(), is("/auto"));
    }

    @Test
    public void stubParametersNullMapDoesNothing() throws Exception {
        // ensure request present
        assertThat(_context.getRequest(), notNullValue());
        Map<String, String> beforeMap = _context.getParameters();
        int before = beforeMap == null ? 0 : beforeMap.size();
        WebContextStubbingOperation.stubParameters(null).of(_context);
        Map<String, String> afterMap = _context.getParameters();
        int after = afterMap == null ? 0 : afterMap.size();
        assertThat(after, is(before));
    }

    @Test
    public void stubParameterNullValuesRemovesParameter() throws Exception {
        stubParameter("temp", "x", "y").of(_context);
        assertThat(_context.getParameterValues("temp").length, is(2));
        stubParameter("temp", (String[]) null).of(_context);
        assertThat(_context.getParameter("temp"), nullValue());
        assertThat(_context.getParameterValues("temp"), nullValue());
    }

    @Test
    public void stubAttributeApplicationScopeNoOp() throws Exception {
        stubAttribute("appKey", "val", Context.APPLICATION_SCOPE).of(_context);
        // application scope not implemented -> should not appear
        assertThat(_context.getAttribute("appKey"), nullValue());
        assertThat(_context.getAttributes(Context.APPLICATION_SCOPE).containsKey("appKey"), is(false));
    }

    @Test
    public void stubSessionAttributeRemoval() throws Exception {
        stubAttribute("toRemove", "sessionVal", Context.SESSION_SCOPE).of(_context);
        assertThat(_context.getAttribute("toRemove", Context.SESSION_SCOPE), is((Object) "sessionVal"));
        stubAttribute("toRemove", null, Context.SESSION_SCOPE).of(_context);
        assertThat(_context.getAttribute("toRemove", Context.SESSION_SCOPE), nullValue());
        assertThat(_context.getAttributes(Context.SESSION_SCOPE).containsKey("toRemove"), is(false));
    }

    @Test
    public void stubJcrSessionAddsAdditionalStubbingsOnExisting() throws Exception {
        stubJcrSession("extra").of(_context);
        Session first = _context.getJCRSession("extra");
        assertThat(first, notNullValue());
        stubJcrSession("extra", SessionStubbingOperation.stubAttribute("k", "v")).of(_context);
        Session again = _context.getJCRSession("extra");
        assertThat(again, is(first));
        assertThat(again.getAttribute("k"), is("v"));
    }

    @Test
    public void stubAttributeInvalidScopeNoOp() {
        int localSizeBefore = _context.getAttributes(Context.LOCAL_SCOPE).size();
        int sessionSizeBefore = _context.getAttributes(Context.SESSION_SCOPE).size();
        int appSizeBefore = _context.getAttributes(Context.APPLICATION_SCOPE).size();
        WebContextStubbingOperation.stubAttribute("invalid", "value", -123).of(_context);
        assertThat(_context.getAttribute("invalid"), nullValue());
        assertThat(_context.getAttributes(Context.LOCAL_SCOPE).size(), is(localSizeBefore));
        assertThat(_context.getAttributes(Context.SESSION_SCOPE).size(), is(sessionSizeBefore));
        assertThat(_context.getAttributes(Context.APPLICATION_SCOPE).size(), is(appSizeBefore));
    }

    @Test
    public void stubJcrSessionOverrideReplacesPrevious() throws RepositoryException {
        Session first = mock(Session.class);
        WebContextStubbingOperation.stubJcrSession("overrideRepo", first).of(_context);
        assertThat(_context.getJCRSession("overrideRepo"), is(first));
        Session second = mock(Session.class);
        WebContextStubbingOperation.stubJcrSession("overrideRepo", second).of(_context);
        assertThat(_context.getJCRSession("overrideRepo"), is(second));
    }

    @After
    public void tearDown() {
        cleanContext();
    }
}
