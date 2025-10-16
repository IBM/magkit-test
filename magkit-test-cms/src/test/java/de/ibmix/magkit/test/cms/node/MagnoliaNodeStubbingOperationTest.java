package de.ibmix.magkit.test.cms.node;

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
import de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation;
import de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation;
import de.ibmix.magkit.test.jcr.NodeMockUtils;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test MagnoliaNodeStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-03
 */
public class MagnoliaNodeStubbingOperationTest {

    private Node _node;

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _node = NodeMockUtils.mockNode("node");
    }

    @AfterEach
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubTemplate() throws RepositoryException {
        assertFalse(_node.hasProperty("mgnl:template"));
        assertNull(_node.getProperty("mgnl:template"));

        TemplateDefinitionStubbingOperation op = mock(TemplateDefinitionStubbingOperation.class);
        PageNodeStubbingOperation.stubTemplate("test", op).of(_node);
        TemplateDefinition def = Components.getComponent(TemplateDefinitionRegistry.class).getProvider("test").get();
        assertNotNull(def);
        verify(op, times(1)).of(def);
        assertTrue(_node.hasProperty("mgnl:template"));
        assertNotNull(_node.getProperty("mgnl:template"));
        assertEquals("test", _node.getProperty("mgnl:template").getValue().getString());
    }

    @Test
    public void stubAreaTemplate() throws RepositoryException {
        assertFalse(_node.hasProperty("mgnl:template"));
        assertNull(_node.getProperty("mgnl:template"));

        AreaDefinitionStubbingOperation op = mock(AreaDefinitionStubbingOperation.class);
        AreaNodeStubbingOperation.stubAreaTemplate("test", op).of(_node);
        AreaDefinition def = (AreaDefinition) Components.getComponent(TemplateDefinitionRegistry.class).getProvider("test").get();
        assertNotNull(def);
        verify(op, times(1)).of(def);
        assertTrue(_node.hasProperty("mgnl:template"));
        assertNotNull(_node.getProperty("mgnl:template"));
        assertEquals("test", _node.getProperty("mgnl:template").getValue().getString());
    }

    // Parameterized tests for date properties to avoid repetition.
    @ParameterizedTest
    @MethodSource("datePropertyArguments")
    public void stubDateProperty(Function<Calendar, MagnoliaNodeStubbingOperation> opFactory, String propertyName) throws RepositoryException {
        assertFalse(_node.hasProperty(propertyName));
        assertNull(_node.getProperty(propertyName));
        Calendar now = Calendar.getInstance();
        opFactory.apply(now).of(_node);
        assertTrue(_node.hasProperty(propertyName));
        assertEquals(now, _node.getProperty(propertyName).getValue().getDate());
    }

    private static Stream<Arguments> datePropertyArguments() {
        return Stream.of(
            Arguments.of((Function<Calendar, MagnoliaNodeStubbingOperation>) MagnoliaNodeStubbingOperation::stubCreated, "mgnl:created"),
            Arguments.of((Function<Calendar, MagnoliaNodeStubbingOperation>) MagnoliaNodeStubbingOperation::stubLastModified, "mgnl:lastModified"),
            Arguments.of((Function<Calendar, MagnoliaNodeStubbingOperation>) MagnoliaNodeStubbingOperation::stubLastActivated, "mgnl:lastActivated")
        );
    }

    // Parameterized tests for string properties.
    @ParameterizedTest
    @MethodSource("stringPropertyArguments")
    public void stubStringProperty(Function<String, MagnoliaNodeStubbingOperation> opFactory, String propertyName) throws RepositoryException {
        assertFalse(_node.hasProperty(propertyName));
        assertNull(_node.getProperty(propertyName));
        MagnoliaNodeStubbingOperation op = opFactory.apply("test");
        op.of(_node);
        assertTrue(_node.hasProperty(propertyName));
        assertEquals("test", _node.getProperty(propertyName).getValue().getString());
    }

    private static Stream<Arguments> stringPropertyArguments() {
        return Stream.of(
            Arguments.of((Function<String, MagnoliaNodeStubbingOperation>) MagnoliaNodeStubbingOperation::stubCreatedBy, "mgnl:createdBy"),
            Arguments.of((Function<String, MagnoliaNodeStubbingOperation>) MagnoliaNodeStubbingOperation::stubLastModifiedBy, "mgnl:lastModifiedBy"),
            Arguments.of((Function<String, MagnoliaNodeStubbingOperation>) MagnoliaNodeStubbingOperation::stubLastActivatedBy, "mgnl:lastActivatedBy")
        );
    }

    @Test
    public void stubActivationStatus() throws RepositoryException {
        assertFalse(_node.hasProperty("mgnl:activationStatus"));
        assertNull(_node.getProperty("mgnl:activationStatus"));

        MagnoliaNodeStubbingOperation.stubActivationStatus(true).of(_node);
        assertTrue(_node.hasProperty("mgnl:activationStatus"));
        assertTrue(_node.getProperty("mgnl:activationStatus").getValue().getBoolean());
    }
}
