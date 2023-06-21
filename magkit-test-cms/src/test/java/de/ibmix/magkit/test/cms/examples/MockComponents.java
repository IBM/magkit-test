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

import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.dam.templating.functions.DamTemplatingFunctions;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 *
 * @author wolf.bubenik
 * @since 18.02.16.
 */
public class MockComponents {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    // Die DamTemplatingFunctions-Instanz wird von Magnolia im guice IoC container gemanaged und in unsere Klasse injeziert.
    @Test
    public void mockMagkitMagnoliaComponent() {
        // 1. Mock-Instanz der DamTemplatingFunctions erzeugen und als Magnolia Component registrieren:
        DamTemplatingFunctions dtf = ComponentsMockUtils.mockComponentInstance(DamTemplatingFunctions.class);

        // Jetzt können wir den DamTemplatingFunctions-Mock direkt über die Components beziehen...
        assertThat(Components.getComponent(DamTemplatingFunctions.class), is(dtf));

        // 2. ... oder beim Erzeugen unserer Klasse in den annotierten Constructor injezieren lassen:
        ServiceWithInjectedDamTemplatingFunctions service = Components.getComponentProvider().newInstance(ServiceWithInjectedDamTemplatingFunctions.class);
        assertThat(service.getDtf(), is(dtf));
        // Injection geht nur über den Constructor.
        // Wir verwenden hier den MockComponentProvider von Magnolia. Dieser unterstützt nicht Injection in annotierte Methoden und Felder.
    }

    @Test
    public void mockMagnoliaTestComponent() {
        // 1. Mock-Instanz der DamTemplatingFunctions erzeugen und als Magnolia Component registrieren:
        DamTemplatingFunctions dtf = Mockito.mock(DamTemplatingFunctions.class);
        ComponentsTestUtil.setInstance(DamTemplatingFunctions.class, dtf);

        /// Jetzt können wir den DamTemplatingFunctions-Mock direkt über die Components beziehen...
        assertThat(Components.getComponent(DamTemplatingFunctions.class), is(dtf));

        // 2. ... oder beim Erzeugen unserer Klasse in den annotierten Constructor injezieren lassen:
        ServiceWithInjectedDamTemplatingFunctions service = Components.getComponentProvider().newInstance(ServiceWithInjectedDamTemplatingFunctions.class);
        assertThat(service.getDtf(), is(dtf));
        // Injection geht nur über den Constructor.
        // Der Magnolia MockComponentProvider unterstützt nicht Injection in annotierte Methoden und Felder.
    }

    @After
    public void cleanUp() {
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        MgnlContext.setInstance(null);
    }

    // Scenario: Wir wollen eine Klasse testen, die DamTemplatingFunctions verwendet.
    private class ServiceWithInjectedDamTemplatingFunctions {
        private DamTemplatingFunctions _dtf;

        @Inject
        ServiceWithInjectedDamTemplatingFunctions(DamTemplatingFunctions dtf) {
            _dtf = dtf;
        }

        public DamTemplatingFunctions getDtf() {
            return _dtf;
        }
    }

}
