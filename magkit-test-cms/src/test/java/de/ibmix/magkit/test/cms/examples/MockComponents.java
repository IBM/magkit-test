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

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Compare Magnolia JCR Mock-Objects with ibmix MockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 18.02.2016.
 */
public class MockComponents {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    /**
     * This test demonstrates, how to create a mock af any class and get it injected into another class using the ComponentsMockUtils of the Magkit.
     */
    @Test
    public void mockMagkitMagnoliaComponent() {
        // 1. Create a mock instance of the DamTemplatingFunctions and register it as Magnolia Component:
        DamTemplatingFunctions dtf = mockComponentInstance(DamTemplatingFunctions.class);

        // Now we can access this DamTemplatingFunctions-mock directly from magnolia Components ...
        assertThat(Components.getComponent(DamTemplatingFunctions.class), is(dtf));

        // 2. ... or have it injected into another class that is managed by the Magnolia Components:
        ServiceWithInjectedDamTemplatingFunctions service = Components.getComponentProvider().newInstance(ServiceWithInjectedDamTemplatingFunctions.class);
        assertThat(service.getDtf(), is(dtf));
        // Note, that injection only works using the class constructor.
        // Field injection is not supported by the Magnolia MockComponentProvider - the common base of both MockUtils.
    }

    /**
     * This test demonstrates, how to create a mock af any class and get it injected into another class using the Magnolia ComponentsTestUtil.
     */
    @Test
    public void mockMagnoliaTestComponent() {
        // 1. Create a mock instance of the DamTemplatingFunctions and register it as Magnolia Component:
        DamTemplatingFunctions dtf = Mockito.mock(DamTemplatingFunctions.class);
        ComponentsTestUtil.setInstance(DamTemplatingFunctions.class, dtf);

        /// Now we can access this DamTemplatingFunctions-mock directly from magnolia Components ...
        assertThat(Components.getComponent(DamTemplatingFunctions.class), is(dtf));

        // 2. ... or have it injected into another class that is managed by the Magnolia Components:
        ServiceWithInjectedDamTemplatingFunctions service = Components.getComponentProvider().newInstance(ServiceWithInjectedDamTemplatingFunctions.class);
        assertThat(service.getDtf(), is(dtf));
        // Again, injection only works using the class constructor.
        // Field injection is not supported by the Magnolia MockComponentProvider - the common base of both MockUtils.
    }

    @After
    public void cleanUp() {
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        MgnlContext.setInstance(null);
    }

    // Scenario: We want to test a class that internally uses the DamTemplatingFunctions.
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
