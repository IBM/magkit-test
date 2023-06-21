package de.ibmix.magkit.test.jcr;

/*-
 * #%L
 * Aperto Mockito Test-Utils - JCR
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

import de.ibmix.magkit.test.StubbingOperation;
import org.apache.commons.lang3.ArrayUtils;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Utility class for stubbing mocks of javax.jcr.Session.
 *
 * @author wolf.bubenik
 * @since 03.08.12
 */
public abstract class SessionStubbingOperation implements StubbingOperation<Session> {

    private SessionStubbingOperation() {
    }

    public static SessionStubbingOperation stubAttribute(final String name, final Object value) {
        return new SessionStubbingOperation() {

            @Override
            public void of(Session session) {
                assertThat(session, notNullValue());
                when(session.getAttribute(name)).thenReturn(value);
                String[] names = ArrayUtils.add(session.getAttributeNames(), name);
                when(session.getAttributeNames()).thenReturn(names);
            }
        };
    }

    public static SessionStubbingOperation stubItem(final Item item) {
        return new SessionStubbingOperation() {

            @Override
            public void of(Session session) throws RepositoryException {
                assertThat(session, notNullValue());
                when(session.getItem(item.getPath())).thenReturn(item);
                when(session.itemExists(item.getPath())).thenReturn(true);
                if (item.isNode()) {
                    Node node = (Node) item;
                    when(session.getNode(node.getPath())).thenReturn(node);
                    when(session.nodeExists(node.getPath())).thenReturn(true);
                    String uuid = node.getIdentifier();
                    if (isNotEmpty(uuid)) {
                        when(session.getNodeByUUID(uuid)).thenReturn(node);
                        when(session.getNodeByIdentifier(uuid)).thenReturn(node);
                    }
                }
            }
        };
    }

    public static SessionStubbingOperation stubRemoveItem(final Item item) {
        return new SessionStubbingOperation() {

            @Override
            public void of(Session session) throws RepositoryException {
                assertThat(session, notNullValue());
                when(session.getItem(item.getPath())).thenReturn(null);
                when(session.itemExists(item.getPath())).thenReturn(false);
                if (item.isNode()) {
                    when(session.getNode(item.getPath())).thenReturn(null);
                    when(session.nodeExists(item.getPath())).thenReturn(false);
                    Node node = (Node) item;
                    String uuid = node.getIdentifier();
                    if (isNotEmpty(uuid)) {
                        when(session.getNodeByUUID(uuid)).thenReturn(null);
                        when(session.getNodeByIdentifier(uuid)).thenReturn(null);
                    }
                    NodeIterator nodes = node.getNodes();
                    while (nodes.hasNext()) {
                        stubRemoveItem(nodes.nextNode()).of(session);
                    }
                    PropertyIterator properties = node.getProperties();
                    while (properties.hasNext()) {
                        Property p = properties.nextProperty();
                        stubRemoveItem(p).of(session);
                        when(session.getProperty(p.getPath())).thenReturn(null);
                    }
                }
            }
        };
    }

    // For internal use only. Will always be executed when a new session mock is created.
    static SessionStubbingOperation stubRootNode(final Node node) {
        return new SessionStubbingOperation() {

            @Override
            public void of(Session session) throws RepositoryException {
                assertThat(session, notNullValue());
                doReturn(node).when(session).getRootNode();
                doReturn(session).when(node).getSession();
            }
        };
    }

    public static SessionStubbingOperation stubRepository(final Repository value) {
        return new SessionStubbingOperation() {

            @Override
            public void of(Session session) {
                assertThat(session, notNullValue());
                when(session.getRepository()).thenReturn(value);
            }
        };
    }

    public static SessionStubbingOperation stubWorkspace(final Workspace value) {
        return new SessionStubbingOperation() {

            @Override
            public void of(Session session) {
                assertThat(session, notNullValue());
                when(session.getWorkspace()).thenReturn(value);
            }
        };
    }

    public static SessionStubbingOperation stubValueFactory(final ValueFactory valueFactory) {
        return new SessionStubbingOperation() {
            @Override
            public void of(final Session context) throws RepositoryException {
                assertThat(context, notNullValue());
                when(context.getValueFactory()).thenReturn(valueFactory);
            }
        };
    }

    public static SessionStubbingOperation stubValueFactory(final ValueFactoryStubbingOperation... stubbings) {
        return new SessionStubbingOperation() {
            @Override
            public void of(final Session context) throws RepositoryException {
                assertThat(context, notNullValue());
                ValueFactory factory = context.getValueFactory();
                if (factory == null) {
                    factory = ValueFactoryMockUtils.mockValueFactory(stubbings);
                    when(context.getValueFactory()).thenReturn(factory);
                } else {
                    for (ValueFactoryStubbingOperation stub : stubbings) {
                        stub.of(factory);
                    }
                }
            }
        };
    }
}
