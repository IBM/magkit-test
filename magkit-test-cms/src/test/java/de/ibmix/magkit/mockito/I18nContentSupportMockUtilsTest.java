package de.ibmix.magkit.mockito;

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
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import javax.jcr.Node;
import java.util.Locale;

import static de.ibmix.magkit.mockito.I18nContentSupportMockUtils.FIRST_ARGUMENT_AS_STRING;
import static de.ibmix.magkit.mockito.I18nContentSupportMockUtils.PROPERTY_FOR_NAME;
import static de.ibmix.magkit.mockito.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.mockito.MagnoliaNodeMockUtils.mockComponentNode;
import static de.ibmix.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing I18nContentSupportMockUtils.
 *
 * @author wolf.bubenik
 * @since 16.02.12
 */
public class I18nContentSupportMockUtilsTest {
    @Before
    public void setUp() throws Exception {
        I18nContentSupportMockUtils.cleanContext();
    }

    @Test
    public void testMockI18nContentSupport() throws Exception {
        I18nContentSupport support = mockI18nContentSupport();
        assertThat(support, notNullValue());

        // test default stubbing with answer
        assertThat(support.toI18NURI(null), nullValue());
        assertThat(support.toI18NURI("one String"), is("one String"));
        assertThat(support.toI18NURI("other String"), is("other String"));

        Node node1 = mockNode(stubProperty("key1", "value1"));
        assertThat(support.getProperty(null, "key1"), nullValue());
        assertThat(support.getProperty(node1, ""), nullValue());
        assertThat(support.getProperty(node1, "key1").getString(), is("value1"));

        Node node2 = mockNode(stubProperty("key2", "value2"));
        assertThat(support.getProperty(node2, "key2", Locale.ENGLISH).getString(), is("value2"));
        assertThat(support.getProperty(node2, "key2", Locale.CHINESE).getString(), is("value2"));

        Node node = mockComponentNode("test", stubProperty("prop", "propValue"));
        assertThat(support.getProperty(null, "prop"), nullValue());
        assertThat(support.getProperty(node, ""), nullValue());
        assertThat(support.getProperty(node, "prop").getString(), is("propValue"));
        assertThat(support.hasProperty(node, "prop"), is(true));
        assertThat(support.hasProperty(node, "key1"), is(false));
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
        assertThat(FIRST_ARGUMENT_AS_STRING.answer(invocation), nullValue());

        Object[] arguments = new Object[]{};
        when(invocation.getArguments()).thenReturn(arguments);
        assertThat(FIRST_ARGUMENT_AS_STRING.answer(invocation), nullValue());

        arguments = new Object[]{null};
        when(invocation.getArguments()).thenReturn(arguments);
        assertThat(FIRST_ARGUMENT_AS_STRING.answer(invocation), nullValue());

        arguments = new Object[]{"test"};
        when(invocation.getArguments()).thenReturn(arguments);
        assertThat(FIRST_ARGUMENT_AS_STRING.answer(invocation), is("test"));

        arguments = new Object[]{"test", "last"};
        when(invocation.getArguments()).thenReturn(arguments);
        assertThat(FIRST_ARGUMENT_AS_STRING.answer(invocation), is("test"));
    }

    @Test
    public void propertyForNameAnswerTest() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        assertThat(PROPERTY_FOR_NAME.answer(invocation), nullValue());

        Object[] arguments = new Object[]{};
        when(invocation.getArguments()).thenReturn(arguments);
        assertThat(PROPERTY_FOR_NAME.answer(invocation), nullValue());

        arguments = new Object[]{null};
        when(invocation.getArguments()).thenReturn(arguments);
        assertThat(PROPERTY_FOR_NAME.answer(invocation), nullValue());

        arguments = new Object[]{null, null};
        when(invocation.getArguments()).thenReturn(arguments);
        assertThat(PROPERTY_FOR_NAME.answer(invocation), nullValue());

        Node content = mockComponentNode("test");
        arguments = new Object[]{content, null};
        when(invocation.getArguments()).thenReturn(arguments);
        assertThat(PROPERTY_FOR_NAME.answer(invocation), nullValue());

        arguments = new Object[]{content, "wrongName"};
        when(invocation.getArguments()).thenReturn(arguments);
        assertThat(PROPERTY_FOR_NAME.answer(invocation), nullValue());

        stubProperty("name", "value").of(content);
        assertThat(PROPERTY_FOR_NAME.answer(invocation), nullValue());

        arguments = new Object[]{content, "name"};
        when(invocation.getArguments()).thenReturn(arguments);
        assertThat(PROPERTY_FOR_NAME.answer(invocation).getString(), is("value"));

        stubProperty("name", "other value").of(content);
        assertThat(PROPERTY_FOR_NAME.answer(invocation).getString(), is("other value"));
    }
}
