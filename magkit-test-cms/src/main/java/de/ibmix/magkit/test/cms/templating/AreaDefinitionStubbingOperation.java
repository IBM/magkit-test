package de.ibmix.magkit.test.cms.templating;

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

import de.ibmix.magkit.assertations.Require;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.InheritanceConfiguration;

import javax.jcr.Node;
import java.util.Comparator;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Fluent factory for stubbing Magnolia {@link AreaDefinition} specific aspects on mocked area definition instances.
 * Extends {@link TemplateDefinitionStubbingOperation} to inherit the generic template related operations while
 * providing additional operations only meaningful for areas.
 * <ul>
 *   <li>Each static method returns a new immutable {@code AreaDefinitionStubbingOperation}.</li>
 *   <li>The type specific {@link #of(AreaDefinition)} method performs the Mockito stubbing.</li>
 *   <li>Multiple operations can be applied sequentially to compose complex stubbing in tests.</li>
 * </ul>
 * Usage example:
 * <pre>{@code
 * AreaDefinition area = mock(AreaDefinition.class);
 * AreaDefinitionStubbingOperation.stubEnabled(true).of(area);
 * AreaDefinitionStubbingOperation.stubMaxComponents(5).of(area);
 * }</pre>
 * All operations validate that the supplied area is not {@code null} using hamcrest assertions for clear failure messages.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-04-14
 */
public abstract class AreaDefinitionStubbingOperation extends TemplateDefinitionStubbingOperation<AreaDefinition> {

    /**
     * Stub {@link AreaDefinition#getAvailableComponents()} to return the supplied map of component availabilities.
     *
     * @param value map keyed by component id containing availability descriptors; may be {@code null}
     * @return operation stubbing available components
     */
    public static AreaDefinitionStubbingOperation stubAvailableComponents(final Map<String, ComponentAvailability> value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(AreaDefinition areaDefinition) {
                Require.Argument.notNull(areaDefinition, "areaDefinition should not be null");
                doReturn(value).when(areaDefinition).getAvailableComponents();
            }
        };
    }

    /**
     * Stub {@link AreaDefinition#getContentStructure()} to return the given structure descriptor string.
     *
     * @param value structure descriptor; may be {@code null}
     * @return operation stubbing the content structure
     */
    public static AreaDefinitionStubbingOperation stubContentStructure(final String value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(AreaDefinition areaDefinition) {
                Require.Argument.notNull(areaDefinition, "areaDefinition should not be null");
                doReturn(value).when(areaDefinition).getContentStructure();
            }
        };
    }

    /**
     * Stub {@link AreaDefinition#getCreateAreaNode()} to return the supplied flag controlling node creation.
     *
     * @param value {@code Boolean} indicating if the area node should be created automatically; may be {@code null}
     * @return operation stubbing the createAreaNode flag
     */
    public static AreaDefinitionStubbingOperation stubCreateAreaNode(final Boolean value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(AreaDefinition areaDefinition) {
                Require.Argument.notNull(areaDefinition, "areaDefinition should not be null");
                doReturn(value).when(areaDefinition).getCreateAreaNode();
            }
        };
    }

    /**
     * Stub {@link AreaDefinition#getEnabled()} to return the supplied enabled flag.
     *
     * @param value {@code Boolean} specifying if the area is enabled; may be {@code null}
     * @return operation stubbing the enabled flag
     */
    public static AreaDefinitionStubbingOperation stubEnabled(final Boolean value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(AreaDefinition areaDefinition) {
                Require.Argument.notNull(areaDefinition, "areaDefinition should not be null");
                doReturn(value).when(areaDefinition).getEnabled();
            }
        };
    }

    /**
     * Stub {@link AreaDefinition#getInheritance()} to return the provided {@link InheritanceConfiguration} instance.
     *
     * @param value inheritance configuration (may be {@code null})
     * @return operation stubbing the inheritance configuration
     */
    public static AreaDefinitionStubbingOperation stubInheritance(final InheritanceConfiguration value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(AreaDefinition areaDefinition) {
                Require.Argument.notNull(areaDefinition, "areaDefinition should not be null");
                doReturn(value).when(areaDefinition).getInheritance();
            }
        };
    }

    /**
     * Convenience overload creating a mocked {@link InheritanceConfiguration} from the supplied boolean arguments
     * and stubbing {@link AreaDefinition#getInheritance()} to return it. A component predicate and comparator are
     * mocked as generic instances.
     *
     * @param isEnabled whether inheritance is enabled
     * @param isInheritsProperties whether properties should be inherited
     * @param isInheritsComponents whether components should be inherited
     * @return operation stubbing inheritance based on the given flags
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static AreaDefinitionStubbingOperation stubInheritance(final Boolean isEnabled, Boolean isInheritsProperties, Boolean isInheritsComponents) {
        InheritanceConfiguration configuration = mock(InheritanceConfiguration.class);
        doReturn(isEnabled).when(configuration).isEnabled();
        doReturn(isInheritsProperties).when(configuration).isInheritsProperties();
        doReturn(isInheritsComponents).when(configuration).isInheritsComponents();
        AbstractPredicate<Node> predicate = mock(AbstractPredicate.class);
        doReturn(predicate).when(configuration).getComponentPredicate();
        Comparator<Node> comparator = mock(Comparator.class);
        doReturn(comparator).when(configuration).getComponentComparator();
        return stubInheritance(configuration);
    }

    /**
     * Stub {@link AreaDefinition#getMaxComponents()} to return the maximum number of allowed components.
     *
     * @param value maximum number; may be {@code null}
     * @return operation stubbing the maxComponents value
     */
    public static AreaDefinitionStubbingOperation stubMaxComponents(final Integer value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(AreaDefinition areaDefinition) {
                Require.Argument.notNull(areaDefinition, "areaDefinition should not be null");
                doReturn(value).when(areaDefinition).getMaxComponents();
            }
        };
    }

    /**
     * Stub {@link AreaDefinition#getOptional()} to return the supplied optional flag.
     *
     * @param value {@code Boolean} indicating if the area is optional; may be {@code null}
     * @return operation stubbing the optional flag
     */
    public static AreaDefinitionStubbingOperation stubOptional(final Boolean value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(AreaDefinition areaDefinition) {
                Require.Argument.notNull(areaDefinition, "areaDefinition should not be null");
                doReturn(value).when(areaDefinition).getOptional();
            }
        };
    }

    /**
     * Stub {@link AreaDefinition#getType()} to return the supplied type (overrides more generic template type in an area context).
     *
     * @param value area type string; may be {@code null}
     * @return operation stubbing the area specific type
     */
    public static AreaDefinitionStubbingOperation stubType(final String value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(AreaDefinition areaDefinition) {
                Require.Argument.notNull(areaDefinition, "areaDefinition should not be null");
                doReturn(value).when(areaDefinition).getType();
            }
        };
    }
}
