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

import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.MgnlInstantiationException;
import info.magnolia.test.mock.MockComponentProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.MockUtil.isMock;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 06.06.2012
 */
public abstract class ComponentsMockUtils {

    public static MockComponentProvider getComponentProvider() {
        ComponentProvider result = Components.getComponentProvider();
        if (!(result instanceof MockComponentProvider)) {
            result = new MockComponentProvider();
            Components.setComponentProvider(result);
        }
        return (MockComponentProvider) result;
    }

    public static <T> T mockComponentInstance(Class<T> type) {
        T result = getComponentSingleton(type);
        if (result == null) {
            result = mock(type);
            setComponentInstance(type, result);
        }
        return result;
    }

    public static <T> void mockComponentFactory(Class<T> type, T instance) {
        ComponentFactory<T> factory = mock(ComponentFactory.class);
        when(factory.newInstance()).thenReturn(instance);
        getComponentProvider().setInstanceFactory(type, factory);
        setComponentInstance(type, instance);
    }

    protected static <T> void setComponentInstance(Class<T> type, T instance) {
        getComponentProvider().setInstance(type, instance);
    }

    public static <T> T getComponentSingleton(Class<T> type) {
        T result = null;
        try {
            result = getComponentProvider().getComponent(type);
            // if <T> has not been mocked and if it can be instantiated,
            // we get an ordinary instance of <T> that cannot be used for mocking and stubbing.
            // Return null in this case.
            // Note that somehow the class name of Mockito mocks changed with java version (oracle8 -> openJDK11) and identical mockito version (1.10.19)
            if (result != null && !isMock(result)) {
                result = null;
            }
        } catch (MgnlInstantiationException e) {
            // ignore
        }
        return result;
    }

    public static void clearComponentProvider() {
        getComponentProvider().clear();
    }

    public static <T> void clearComponentProvider(Class<T> type) {
        getComponentProvider().setInstance(type, null);
    }
}
