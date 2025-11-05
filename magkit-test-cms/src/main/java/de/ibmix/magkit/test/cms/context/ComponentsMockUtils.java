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
 * A utility class for creating and managing Mockito mocks that are registered in the Magnolia Components provider.
 * This class provides methods to create mocks, register them as components, and manage the component provider
 * for testing purposes in Magnolia CMS applications.
 *
 * <p>This utility is particularly useful for unit testing where you need to mock Magnolia components
 * and have them injected through the IoC container.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-06-06
 */
public abstract class ComponentsMockUtils {

    /**
     * Retrieves or creates a MockComponentProvider instance.
     * If the current component provider is not a MockComponentProvider,
     * it creates a new one and sets it as the current provider.
     *
     * @return the current MockComponentProvider instance
     */
    public static MockComponentProvider getComponentProvider() {
        ComponentProvider result = Components.getComponentProvider();
        if (!(result instanceof MockComponentProvider)) {
            result = new MockComponentProvider();
            Components.setComponentProvider(result);
        }
        return (MockComponentProvider) result;
    }

    /**
     * Creates or retrieves a mock instance for the specified type and registers it as a component.
     * If a mock instance already exists for the given type, it returns the existing instance.
     * Otherwise, it creates a new mock using Mockito and registers it in the component provider.
     *
     * @param <T> the type of the component to mock
     * @param type the Class object representing the type to mock
     * @return a mock instance of the specified type
     * @throws IllegalArgumentException if type is null
     */
    public static <T> T mockComponentInstance(Class<T> type) {
        T result = getComponentSingleton(type);
        if (result == null) {
            result = mock(type);
            setComponentInstance(type, result);
        }
        return result;
    }

    /**
     * Creates a mock ComponentFactory for the specified type and instance.
     * This method creates a mock factory that will return the provided instance
     * when newInstance() is called, and registers both the factory and the instance
     * in the component provider.
     *
     * @param <T> the type of the component
     * @param type the Class object representing the type
     * @param instance the instance that the factory should return
     * @throws IllegalArgumentException if type or instance is null
     */
    public static <T> void mockComponentFactory(Class<T> type, T instance) {
        ComponentFactory<T> factory = mock(ComponentFactory.class);
        when(factory.newInstance()).thenReturn(instance);
        getComponentProvider().setInstanceFactory(type, factory);
        setComponentInstance(type, instance);
    }

    /**
     * Sets a component instance in the component provider.
     * This is a protected utility method used internally by other methods
     * to register instances in the MockComponentProvider.
     *
     * @param <T> the type of the component
     * @param type the Class object representing the type
     * @param instance the instance to register
     */
    protected static <T> void setComponentInstance(Class<T> type, T instance) {
        getComponentProvider().setInstance(type, instance);
    }

    /**
     * Retrieves a singleton component instance from the component provider.
     * This method only returns mock instances. If the component is not mocked
     * or cannot be instantiated, it returns null.
     *
     * @param <T> the type of the component to retrieve
     * @param type the Class object representing the type to retrieve
     * @return the mock instance if it exists and is a mock, null otherwise
     */
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

    /**
     * Clears all instances from the component provider.
     * This method removes all registered components and should typically
     * be called in test cleanup methods to ensure a clean state for subsequent tests.
     */
    public static void clearComponentProvider() {
        getComponentProvider().clear();
    }

    /**
     * Clears a specific component instance from the component provider.
     * This method removes only the specified component type by setting its instance to null.
     *
     * @param <T> the type of the component to clear
     * @param type the Class object representing the type to clear
     */
    public static <T> void clearComponentProvider(Class<T> type) {
        getComponentProvider().setInstance(type, null);
    }
}
