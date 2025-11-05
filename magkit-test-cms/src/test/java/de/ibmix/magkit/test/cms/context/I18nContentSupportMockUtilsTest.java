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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import javax.jcr.Node;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.FIRST_ARGUMENT_AS_STRING;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.PROPERTY_FOR_NAME;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockComponentNode;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing I18nContentSupportMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-02-16
 */
public class I18nContentSupportMockUtilsTest {
    @BeforeEach
    public void setUp() throws Exception {
        I18nContentSupportMockUtils.cleanContext();
    }

    @Test
    public void testMockI18nContentSupport() throws Exception {
        I18nContentSupport support = mockI18nContentSupport();
        assertNotNull(support);
        assertNull(support.toI18NURI(null));
        assertEquals("one String", support.toI18NURI("one String"));
        assertEquals("other String", support.toI18NURI("other String"));
        Node node1 = mockNode(stubProperty("key1", "value1"));
        assertNull(support.getProperty(null, "key1"));
        assertNull(support.getProperty(node1, ""));
        assertEquals("value1", support.getProperty(node1, "key1").getString());
        Node node2 = mockNode(stubProperty("key2", "value2"));
        assertEquals("value2", support.getProperty(node2, "key2", Locale.ENGLISH).getString());
        assertEquals("value2", support.getProperty(node2, "key2", Locale.CHINESE).getString());
        Node node = mockComponentNode("test", stubProperty("prop", "propValue"));
        assertNull(support.getProperty(null, "prop"));
        assertNull(support.getProperty(node, ""));
        assertEquals("propValue", support.getProperty(node, "prop").getString());
        assertTrue(support.hasProperty(node, "prop"));
        assertFalse(support.hasProperty(node, "key1"));
    }

    @Test
    public void testMockI18nContentSupportWithOperations() throws Exception {
        I18nContentSupportStubbingOperation op1 = mock(I18nContentSupportStubbingOperation.class);
        I18nContentSupportStubbingOperation op2 = mock(I18nContentSupportStubbingOperation.class);
        I18nContentSupport support = mockI18nContentSupport(op1, op2);
        verify(op1, times(1)).of(support);
        verify(op2, times(1)).of(support);
    }

    @Test
    public void argumentAsStringAnswerTest() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        assertNull(FIRST_ARGUMENT_AS_STRING.answer(invocation));
        Object[] arguments = new Object[]{};
        when(invocation.getArguments()).thenReturn(arguments);
        assertNull(FIRST_ARGUMENT_AS_STRING.answer(invocation));
        arguments = new Object[]{null};
        when(invocation.getArguments()).thenReturn(arguments);
        assertNull(FIRST_ARGUMENT_AS_STRING.answer(invocation));
        arguments = new Object[]{"test"};
        when(invocation.getArguments()).thenReturn(arguments);
        assertEquals("test", FIRST_ARGUMENT_AS_STRING.answer(invocation));
        arguments = new Object[]{"test", "last"};
        when(invocation.getArguments()).thenReturn(arguments);
        assertEquals("test", FIRST_ARGUMENT_AS_STRING.answer(invocation));
    }

    @Test
    public void propertyForNameAnswerTest() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        assertNull(PROPERTY_FOR_NAME.answer(invocation));
        Object[] arguments = new Object[]{};
        when(invocation.getArguments()).thenReturn(arguments);
        assertNull(PROPERTY_FOR_NAME.answer(invocation));
        arguments = new Object[]{null};
        when(invocation.getArguments()).thenReturn(arguments);
        assertNull(PROPERTY_FOR_NAME.answer(invocation));
        arguments = new Object[]{null, null};
        when(invocation.getArguments()).thenReturn(arguments);
        assertNull(PROPERTY_FOR_NAME.answer(invocation));
        Node content = mockComponentNode("test");
        arguments = new Object[]{content, null};
        when(invocation.getArguments()).thenReturn(arguments);
        assertNull(PROPERTY_FOR_NAME.answer(invocation));
        arguments = new Object[]{content, "wrongName"};
        when(invocation.getArguments()).thenReturn(arguments);
        assertNull(PROPERTY_FOR_NAME.answer(invocation));
        stubProperty("name", "value").of(content);
        assertNull(PROPERTY_FOR_NAME.answer(invocation));
        arguments = new Object[]{content, "name"};
        when(invocation.getArguments()).thenReturn(arguments);
        assertEquals("value", PROPERTY_FOR_NAME.answer(invocation).getString());
        stubProperty("name", "other value").of(content);
        assertEquals("other value", PROPERTY_FOR_NAME.answer(invocation).getString());
    }
}
