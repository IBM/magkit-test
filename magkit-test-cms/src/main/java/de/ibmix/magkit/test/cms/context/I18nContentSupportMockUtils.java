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
import org.apache.commons.lang3.StringUtils;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * An utility class to create Mockito mocks of an I18nContentSupport.
 * The Mock is stubbed to echo the input values.
 *
 * @author wolf.bubenik
 * @since 13.12.2010
 */
public final class I18nContentSupportMockUtils extends ComponentsMockUtils {

    static final Answer<Boolean> HAS_PROPERTY_ANSWER = invocation -> {
        Object[] arguments = invocation.getArguments();
        Node node = getNode(arguments);
        String propertyName = getName(arguments);
        return node != null && isNotBlank(propertyName) && node.hasProperty(propertyName);
    };

    static final Answer<String> FIRST_ARGUMENT_AS_STRING = new Answer<>() {
        @Override
        public String answer(InvocationOnMock invocation) {
            Object[] arguments = invocation.getArguments();
            return (isNotEmpty(arguments)) ? arguments[0].toString() : null;
        }

        private boolean isNotEmpty(Object[] array) {
            return array != null && array.length > 0 && array[0] != null;
        }
    };

    static final Answer<Property> PROPERTY_FOR_NAME = invocation -> {
        Object[] arguments = invocation.getArguments();
        Node content = getNode(arguments);
        String nodeDataName = getName(arguments);
        Property result = null;
        if (content != null && StringUtils.isNotBlank(nodeDataName)) {
            result = content.getProperty(nodeDataName);
        }
        return result;
    };

    private I18nContentSupportMockUtils() {
    }

    public static I18nContentSupport mockI18nContentSupport(I18nContentSupportStubbingOperation... stubbings) throws RepositoryException {
        I18nContentSupport result = mockI18nContentSupport();
        for (I18nContentSupportStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    public static I18nContentSupport mockI18nContentSupport() throws RepositoryException {
        I18nContentSupport result = mock(I18nContentSupport.class);
        when(result.toI18NURI(anyString())).thenAnswer(FIRST_ARGUMENT_AS_STRING);
        when(result.getProperty(Matchers.any(), anyString())).thenAnswer(PROPERTY_FOR_NAME);
        when(result.getProperty(Matchers.any(), anyString(), Matchers.any())).thenAnswer(PROPERTY_FOR_NAME);
        when(result.hasProperty(Matchers.any(), anyString())).thenAnswer(HAS_PROPERTY_ANSWER);

        mockComponentFactory(I18nContentSupport.class, result);
        return result;
    }

    public static Node getNode(Object[] arguments) {
        return arguments != null && arguments.length > 0 ? (Node) arguments[0] : null;
    }

    public static String getName(Object[] arguments) {
        return arguments != null && arguments.length > 1 ? (String) arguments[1] : EMPTY;
    }

    public static void cleanContext() {
        clearComponentProvider(I18nContentSupport.class);
    }
}
