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

import info.magnolia.cms.i18n.I18nContentSupport;
import org.mockito.stubbing.Answer;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.apache.commons.lang3.StringUtils; // added for JavaDoc link resolution

/**
 * Utility class providing factory methods to create and register Mockito based mocks of {@link I18nContentSupport}.
 * <p>
 * The produced mock echoes most input values and resolves JCR properties directly from the provided {@link Node}:
 * <ul>
 *   <li>{@link I18nContentSupport#toI18NURI(String)} returns the given URI unchanged.</li>
 *   <li>{@link I18nContentSupport#getProperty(Node, String)} returns the JCR {@link Property} with the given name if present.</li>
 *   <li>{@link I18nContentSupport#getProperty(Node, String, Locale)} first tries a locale specific property name suffix ("name_language"), then falls back to the base property name.</li>
 *   <li>{@link I18nContentSupport#hasProperty(Node, String)} delegates to {@link Node#hasProperty(String)}.</li>
 * </ul>
 * All created mocks are automatically registered in the Magnolia component system via {@link ComponentsMockUtils#mockComponentFactory(Class, Object)}
 * so that regular component lookup returns the mocked instance during a test.
 * <p>
 * Typical usage example:
 * <pre>{@code
 * I18nContentSupport support = I18nContentSupportMockUtils.mockI18nContentSupport();
 * Property title = support.getProperty(node, "title", Locale.ENGLISH);
 * }
 * </pre>
 * Use {@link #mockI18nContentSupport(I18nContentSupportStubbingOperation...)} when additional stubbing is required after the default behavior.
 * Call {@link #cleanContext()} to deregister the mock from the component provider if necessary.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010.12.13
 */
public final class I18nContentSupportMockUtils extends ComponentsMockUtils {

    /**
     * Creates and registers a mocked {@link I18nContentSupport} instance with default stubbing and applies additional stubbing operations.
     * <p>
     * Default stubbing covers: {@link I18nContentSupport#toI18NURI(String)}, {@link I18nContentSupport#getProperty(Node, String)},
     * {@link I18nContentSupport#getProperty(Node, String, Locale)} and {@link I18nContentSupport#hasProperty(Node, String)}.
     * Additional stubbings are executed in the order provided.
     *
     * @param stubbings optional ordered set of custom stubbing operations to enhance or override the defaults.
     * @return the fully stubbed and registered mock instance.
     * @throws RepositoryException if underlying JCR operations during initialization throw an exception.
     */
    public static I18nContentSupport mockI18nContentSupport(I18nContentSupportStubbingOperation... stubbings) throws RepositoryException {
        I18nContentSupport result = mockI18nContentSupport();
        for (I18nContentSupportStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    /**
     * Creates and registers a mocked {@link I18nContentSupport} instance with default stubbing only.
     * <p>
     * Registration is performed via {@link ComponentsMockUtils#mockComponentFactory(Class, Object)} so that component lookup returns this mock.
     *
     * @return a mock of {@link I18nContentSupport} with predefined Answers.
     * @throws RepositoryException if any JCR access required for property resolution fails during setup.
     */
    public static I18nContentSupport mockI18nContentSupport() throws RepositoryException {
        I18nContentSupport result = mock(I18nContentSupport.class);
        when(result.toI18NURI(anyString())).thenAnswer(FIRST_ARGUMENT_AS_STRING);
        when(result.getProperty(any(), anyString())).thenAnswer(PROPERTY_FOR_NAME);
        when(result.getProperty(any(), anyString(), any())).thenAnswer(PROPERTY_FOR_NAME_LOCALE);
        when(result.hasProperty(any(), anyString())).thenAnswer(HAS_PROPERTY_ANSWER);

        mockComponentFactory(I18nContentSupport.class, result);
        return result;
    }

    /**
     * Extracts the {@link Node} argument from an invocation arguments array at index 0.
     *
     * @param arguments invocation arguments.
     * @return the {@link Node} if present, otherwise {@code null}.
     */
    private static Node getNode(Object[] arguments) {
        return hasObject(arguments, 0) ? (Node) arguments[0] : null;
    }

    /**
     * Extracts the property name argument from an invocation arguments array at index 1.
     *
     * @param arguments invocation arguments.
     * @return the property name or {@link StringUtils#EMPTY} if not present.
     */
    private static String getName(Object[] arguments) {
        return hasObject(arguments, 1) ? (String) arguments[1] : EMPTY; // JavaDoc refers to StringUtils#EMPTY
    }

    /**
     * Extracts the {@link Locale} argument from an invocation arguments array at index 2.
     *
     * @param arguments invocation arguments.
     * @return the locale or {@code null} if not present.
     */
    private static Locale getLocal(Object[] arguments) {
        return hasObject(arguments, 2) ? (Locale) arguments[2] : null;
    }

    /**
     * Checks presence and non-null state of an argument at the given index.
     *
     * @param arguments invocation arguments.
     * @param index     index to check.
     * @return {@code true} if the array contains a non-null value at the given index.
     */
    private static boolean hasObject(Object[] arguments, int index) {
        return arguments != null && arguments.length > index && arguments[index] != null;
    }

    /**
     * Deregisters the mocked {@link I18nContentSupport} from the Magnolia component provider.
     * Invoke after a test to clean up the testing context.
     */
    public static void cleanContext() {
        clearComponentProvider(I18nContentSupport.class);
    }

    /**
     * Answer evaluating {@link I18nContentSupport#hasProperty(Node, String)} by delegating to the underlying {@link Node}.
     * Returns {@code true} only if the node is not {@code null}, the property name is not blank and {@link Node#hasProperty(String)} yields true.
     */
    static final Answer<Boolean> HAS_PROPERTY_ANSWER = invocation -> {
        Object[] arguments = invocation.getArguments();
        Node node = getNode(arguments);
        String propertyName = getName(arguments);
        return node != null && isNotBlank(propertyName) && node.hasProperty(propertyName);
    };

    /**
     * Generic echo Answer used for {@link I18nContentSupport#toI18NURI(String)} returning the first argument as {@link String}.
     * If no argument is present, {@code null} is returned.
     */
    static final Answer<String> FIRST_ARGUMENT_AS_STRING = invocation -> {
        Object[] arguments = invocation.getArguments();
        return (hasObject(arguments, 0)) ? arguments[0].toString() : null;
    };

    /**
     * Retrieves a property from the provided {@link Node} using the raw property name without locale augmentation.
     * Used to stub {@link I18nContentSupport#getProperty(Node, String)}.
     */
    static final Answer<Property> PROPERTY_FOR_NAME = invocation -> {
        Object[] arguments = invocation.getArguments();
        Node content = getNode(arguments);
        String propertyName = getName(arguments);
        Property result = null;
        if (content != null && isNotBlank(propertyName)) {
            result = content.getProperty(propertyName);
        }
        return result;
    };

    /**
     * Retrieves a property from the provided {@link Node} taking locale into account.
     * <p>Resolution order:
     * <ol>
     *   <li>Try property name with appended "_" and {@link Locale#getLanguage()}.</li>
     *   <li>Fallback to the base property name.</li>
     * </ol>
     * Used to stub {@link I18nContentSupport#getProperty(Node, String, Locale)}.
     */
    static final Answer<Property> PROPERTY_FOR_NAME_LOCALE = invocation -> {
        Object[] arguments = invocation.getArguments();
        Node content = getNode(arguments);
        String propertyName = getName(arguments);
        Locale local = getLocal(arguments);

        Property result = null;
        if (content != null && isNotBlank(propertyName)) {
            if (local != null) {
                result = content.getProperty(propertyName + '_' + local.getLanguage());
            }
            if (result == null) {
                result = content.getProperty(propertyName);
            }
        }
        return result;
    };

    /**
     * Hidden constructor to prevent instantiation. This is a pure utility holder.
     */
    private I18nContentSupportMockUtils() {
    }
}
