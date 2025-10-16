package de.ibmix.magkit.test.cms.examples;

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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import de.ibmix.magkit.test.cms.context.WebContextStubbingOperation;
import de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-04-05
 */
public class MockWebContext {

    @AfterEach
    public void cleanUp() {
        ContextMockUtils.cleanContext();
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void mockWebContextWithMagkit() throws RepositoryException {
        // So mockt man einen Magnolia WebContext mit einer bestimmten Sprache (die Sprache ist optional):
        WebContext ctx = ContextMockUtils.mockWebContext(Locale.GERMAN);
        // Der Webcontext hat die gewünschte Sprache ...
        assertEquals(Locale.GERMAN, ctx.getLocale());
        // ... und die Mock-Instanz ist im MagnoliaContext hinterlegt:
        assertSame(ctx, MgnlContext.getWebContext());
        // ... and available as Component for injection:
        assertSame(ctx, Components.getComponent(WebContext.class));
        // Wiederhohltes mocken eines WebContext liefert die erste Instanz zurück (get- or- create)
        WebContext ctx2 = ContextMockUtils.mockWebContext(Locale.FRENCH);
        assertSame(ctx, ctx2);
        // ... mit geänderter Sprache:
        assertEquals(Locale.FRENCH, ctx.getLocale());
        // Beim mocken eines WebContext wird immer auch ein I18nContentSupport-Mock erzeugt:
        assertNotNull(Components.getComponent(I18nContentSupport.class));
        // ... einen Request-Mock ...
        assertNotNull(ctx.getRequest());
        // ... einen Response-Mock ...
        assertNotNull(ctx.getResponse());
        // Egal wo Attribute, Parameter oder der ContextPath gestubbt werden, bleibt die Konsistenz zwischen den Mocks gewahrt:
        // Attribute des WebContext passen zu denen des Request ...
        WebContextStubbingOperation.stubAttribute("attribute_1", "attributeValue_1").of(ctx);
        assertEquals("attributeValue_1", ctx.getRequest().getAttribute("attribute_1"));
        // ... und Request-Attribute zu denen des WebContext:
        HttpServletRequestStubbingOperation.stubAttribute("attribute_2", "attributeValue_2").of(ctx.getRequest());
        assertEquals("attributeValue_2", ctx.getAttribute("attribute_2"));
        // Parameter des WebContext passen zu denen des Request ...
        WebContextStubbingOperation.stubParameter("parameter_1", "parameterValue_1").of(ctx);
        assertEquals("parameterValue_1", ctx.getRequest().getParameter("parameter_1"));
        // ... und Request-Parameter zu denen des WebContext:
        HttpServletRequestStubbingOperation.stubParameter("parameter_2", "parameterValue_2").of(ctx.getRequest());
        assertEquals("parameterValue_2", ctx.getParameter("parameter_2"));
        // Ebenso der ContextPath:
        WebContextStubbingOperation.stubContextPath("/test").of(ctx);
        assertEquals("/test", ctx.getRequest().getContextPath());
        // und anders herum:
        HttpServletRequestStubbingOperation.stubContextPath("/other").of(ctx.getRequest());
        assertEquals("/other", ctx.getContextPath());
        // Dafür haben wir noch keinen AggregationState:
        assertNull(ctx.getAggregationState());
    }

    @Test
    public void mockWebContextWithMagnolia() {
        // So mockt man einen neuen Magnolia Context (ist immer ein WebContext):
        WebContext ctx = (WebContext) MockUtil.getMockContext(true);

        // Die Sprache muss extra gesetzt werden:
        ctx.setLocale(Locale.GERMAN);
        // ... und die Mock-Instanz ist im MagnoliaContext hinterlegt:
        assertSame(ctx, MgnlContext.getWebContext());

        // Wiederhohltes mocken eines WebContext kann die erste Instanz zurück liefern (get- or- create)
        WebContext ctx2 = (WebContext) MockUtil.getMockContext(false);
        ctx2.setLocale(Locale.FRENCH);
        assertSame(ctx, ctx2);
        // ... mit geänderter Sprache:
        assertEquals(Locale.FRENCH, ctx.getLocale());

        // Beim mocken eines WebContext wird TestContext als Implementierung für SystemContext im ComponentsProvider angegeben...
        assertNotNull(Components.getComponent(SystemContext.class));
        // ... aber kein I18nContentSupport-Mock:
        // Components.getComponent(I18nContentSupport.class) wirft eine MgnlInstantiationException

        // ... keinen Request-Mock ...
        assertNull(ctx.getRequest());
        // ... keinen Response-Mock ...
        assertNull(ctx.getResponse());
        // ... und auch keinen HttpSession-mock ...
        assertNull(ctx.getServletContext());

        // ... und für Konsistenz zwischen den HTTP-Klassen und dem TestWebContext muss man selber sorgen.

        // Dafür haben wir immer einen AggregationState:
        assertNotNull(ctx.getAggregationState());
    }
}
