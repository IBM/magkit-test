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

import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.InheritanceConfiguration;
import info.magnolia.rendering.template.TemplateDefinition;

import javax.jcr.Node;
import java.util.Comparator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Operations to stubb behaviour of a AreaDefinition mock.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-04-14
 */
public abstract class AreaDefinitionStubbingOperation extends TemplateDefinitionStubbingOperation {
    public abstract void of(AreaDefinition area);

    public static AreaDefinitionStubbingOperation stubAvailableComponents(final Map<String, ComponentAvailability> value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(TemplateDefinition template) {
                throw new UnsupportedOperationException("Not allowed for TemplateDefinition");
            }

            public void of(AreaDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getAvailableComponents();
            }
        };
    }

    public static AreaDefinitionStubbingOperation stubContentStructure(final String value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(TemplateDefinition template) {
                throw new UnsupportedOperationException("Not allowed for TemplateDefinition");
            }

            public void of(AreaDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getContentStructure();
            }
        };
    }

    public static AreaDefinitionStubbingOperation stubCreateAreaNode(final Boolean value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(TemplateDefinition template) {
                throw new UnsupportedOperationException("Not allowed for TemplateDefinition");
            }

            public void of(AreaDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getCreateAreaNode();
            }
        };
    }

    public static AreaDefinitionStubbingOperation stubEnabled(final Boolean value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(TemplateDefinition template) {
                throw new UnsupportedOperationException("Not allowed for TemplateDefinition");
            }

            public void of(AreaDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getEnabled();
            }
        };
    }

    public static AreaDefinitionStubbingOperation stubInheritance(final InheritanceConfiguration value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(TemplateDefinition template) {
                throw new UnsupportedOperationException("Not allowed for TemplateDefinition");
            }

            public void of(AreaDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getInheritance();
            }
        };
    }

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

    public static AreaDefinitionStubbingOperation stubMaxComponents(final Integer value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(TemplateDefinition template) {
                throw new UnsupportedOperationException("Not allowed for TemplateDefinition");
            }

            public void of(AreaDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getMaxComponents();
            }
        };
    }

    public static AreaDefinitionStubbingOperation stubOptional(final Boolean value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(TemplateDefinition template) {
                throw new UnsupportedOperationException("Not allowed for TemplateDefinition");
            }

            public void of(AreaDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getOptional();
            }
        };
    }

    public static AreaDefinitionStubbingOperation stubType(final String value) {
        return new AreaDefinitionStubbingOperation() {

            @Override
            public void of(TemplateDefinition template) {
                throw new UnsupportedOperationException("Not allowed for TemplateDefinition");
            }

            public void of(AreaDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getType();
            }
        };
    }
}
