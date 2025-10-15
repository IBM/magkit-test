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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
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

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _node = NodeMockUtils.mockNode("node");
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubTemplate() throws RepositoryException {
        assertThat(_node.hasProperty("mgnl:template"), is(false));
        assertThat(_node.getProperty("mgnl:template"), nullValue());

        TemplateDefinitionStubbingOperation op = mock(TemplateDefinitionStubbingOperation.class);
        PageNodeStubbingOperation.stubTemplate("test", op).of(_node);
        TemplateDefinition def = Components.getComponent(TemplateDefinitionRegistry.class).getProvider("test").get();
        assertThat(def, notNullValue());
        verify(op, times(1)).of(def);
        assertThat(_node.hasProperty("mgnl:template"), is(true));
        assertThat(_node.getProperty("mgnl:template"), notNullValue());
        assertThat(_node.getProperty("mgnl:template").getValue().getString(), is("test"));

    }

    @Test
    public void stubAreaTemplate() throws RepositoryException {
        assertThat(_node.hasProperty("mgnl:template"), is(false));
        assertThat(_node.getProperty("mgnl:template"), nullValue());

        AreaDefinitionStubbingOperation op = mock(AreaDefinitionStubbingOperation.class);
        AreaNodeStubbingOperation.stubAreaTemplate("test", op).of(_node);
        AreaDefinition def =  (AreaDefinition) Components.getComponent(TemplateDefinitionRegistry.class).getProvider("test").get();
        assertThat(def, notNullValue());
        verify(op, times(1)).of(def);
        assertThat(_node.hasProperty("mgnl:template"), is(true));
        assertThat(_node.getProperty("mgnl:template"), notNullValue());
        assertThat(_node.getProperty("mgnl:template").getValue().getString(), is("test"));
    }

    @Test
    public void stubCreated() throws RepositoryException {
        assertThat(_node.hasProperty("mgnl:created"), is(false));
        assertThat(_node.getProperty("mgnl:created"), nullValue());

        Calendar now = Calendar.getInstance();
        MagnoliaNodeStubbingOperation.stubCreated(now).of(_node);
        assertThat(_node.hasProperty("mgnl:created"), is(true));
        assertThat(_node.getProperty("mgnl:created").getValue().getDate(), is(now));
    }

    @Test
    public void stubCreatedBy() throws RepositoryException {
        assertThat(_node.hasProperty("mgnl:createdBy"), is(false));
        assertThat(_node.getProperty("mgnl:createdBy"), nullValue());

        MagnoliaNodeStubbingOperation.stubCreatedBy("test").of(_node);
        assertThat(_node.hasProperty("mgnl:createdBy"), is(true));
        assertThat(_node.getProperty("mgnl:createdBy").getValue().getString(), is("test"));
    }

    @Test
    public void stubLastModified() throws RepositoryException {
        assertThat(_node.hasProperty("mgnl:lastModified"), is(false));
        assertThat(_node.getProperty("mgnl:lastModified"), nullValue());

        Calendar now = Calendar.getInstance();
        MagnoliaNodeStubbingOperation.stubLastModified(now).of(_node);
        assertThat(_node.hasProperty("mgnl:lastModified"), is(true));
        assertThat(_node.getProperty("mgnl:lastModified").getValue().getDate(), is(now));
    }

    @Test
    public void stubLastModifiedBy() throws RepositoryException {
        assertThat(_node.hasProperty("mgnl:lastModifiedBy"), is(false));
        assertThat(_node.getProperty("mgnl:lastModifiedBy"), nullValue());

        MagnoliaNodeStubbingOperation.stubLastModifiedBy("test").of(_node);
        assertThat(_node.hasProperty("mgnl:lastModifiedBy"), is(true));
        assertThat(_node.getProperty("mgnl:lastModifiedBy").getValue().getString(), is("test"));
    }

    @Test
    public void stubLastActivated() throws RepositoryException {
        assertThat(_node.hasProperty("mgnl:lastActivated"), is(false));
        assertThat(_node.getProperty("mgnl:lastActivated"), nullValue());

        Calendar now = Calendar.getInstance();
        MagnoliaNodeStubbingOperation.stubLastActivated(now).of(_node);
        assertThat(_node.hasProperty("mgnl:lastActivated"), is(true));
        assertThat(_node.getProperty("mgnl:lastActivated").getValue().getDate(), is(now));
    }

    @Test
    public void stubLastActivatedBy() throws RepositoryException {
        assertThat(_node.hasProperty("mgnl:lastActivatedBy"), is(false));
        assertThat(_node.getProperty("mgnl:lastActivatedBy"), nullValue());

        MagnoliaNodeStubbingOperation.stubLastActivatedBy("test").of(_node);
        assertThat(_node.hasProperty("mgnl:lastActivatedBy"), is(true));
        assertThat(_node.getProperty("mgnl:lastActivatedBy").getValue().getString(), is("test"));
    }

    @Test
    public void stubActivationStatus() throws RepositoryException {
        assertThat(_node.hasProperty("mgnl:activationStatus"), is(false));
        assertThat(_node.getProperty("mgnl:activationStatus"), nullValue());

        MagnoliaNodeStubbingOperation.stubActivationStatus(true).of(_node);
        assertThat(_node.hasProperty("mgnl:activationStatus"), is(true));
        assertThat(_node.getProperty("mgnl:activationStatus").getValue().getBoolean(), is(true));
    }
}
