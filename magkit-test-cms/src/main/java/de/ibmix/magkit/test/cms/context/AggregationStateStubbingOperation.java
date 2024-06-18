package de.ibmix.magkit.test.cms.context;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

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

import de.ibmix.magkit.test.ExceptionStubbingOperation;
import de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils;
import de.ibmix.magkit.test.jcr.NodeStubbingOperation;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Channel;

/**
 * Utility class that provides factory methods for AggregationStateStubbingOperations.
 * Stubbing operations to be used as parameters in ContextMockUtils.mockAggregationState(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-09-01
 */
public abstract class AggregationStateStubbingOperation implements ExceptionStubbingOperation<AggregationState, RepositoryException> {

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getMainContentNode() to return a given Node.
     * If mainContentNode is not null,
     * - the method aggregationState.getHandle() is stubbed to return the node path and
     * - the method aggregationState.getRepository() is stubbed to return the name of the Workspace of the given node.
     *
     * @param value the Node to be returned by aggregationState.getMainContentNode()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubMainContentNode(final Node value) {
        return new AggregationStateStubbingOperation() {

            @Override
            public void of(final AggregationState state) throws RepositoryException {
                assertThat(state, notNullValue());
                when(state.getMainContentNode()).thenReturn(value);
                if (value != null) {
                    stubHandle(value.getPath()).of(state);
                    stubRepository(value.getSession().getWorkspace().getName()).of(state);
                }
            }
        };
    }

    /**
     * Creates a node with NodeTypes.Page in website repository and returns an AggregationStateStubbingOperation that stubs method getMainContentNode() to return this Node.
     *
     * @param path      the repository path of the node
     * @param stubbings NodeStubbingOperations to be performed when mocking the node
     * @return AggregationStateStubbingOperation that stubs method getMainContentNode() to return this Node
     * @throws RepositoryException never
     */
    public static AggregationStateStubbingOperation stubMainContentNode(String path, NodeStubbingOperation... stubbings) throws RepositoryException {
        Node node = MagnoliaNodeMockUtils.mockPageNode(path, stubbings);
        return stubMainContentNode(node);
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getCurrentContentNode() to return a given Node.
     * If currentContentNode is not null,
     * - the method aggregationState.getHandle() is stubbed to return the node path and
     * - the method aggregationState.getRepository() is stubbed to return the name of the Workspace of the given node.
     *
     * @param value the Node to be returned by aggregationState.getCurrentContentNode()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubCurrentContentNode(final Node value) {
        return new AggregationStateStubbingOperation() {

            @Override
            public void of(final AggregationState state) throws RepositoryException {
                assertThat(state, notNullValue());
                when(state.getCurrentContentNode()).thenReturn(value);
                if (value != null) {
                    stubHandle(value.getPath()).of(state);
                    stubRepository(value.getSession().getWorkspace().getName()).of(state);
                }
            }
        };
    }

    /**
     * Creates a node with NodeTypes.Component in website repository and returns an AggregationStateStubbingOperation that stubs method stubCurrentContentNode() to return this Node.
     *
     * @param path      the repository path of the node
     * @param stubbings NodeStubbingOperations to be performed when mocking the node
     * @return AggregationStateStubbingOperation that stubs method stubCurrentContentNode() to return this Node
     * @throws RepositoryException never
     */
    public static AggregationStateStubbingOperation stubCurrentContentNode(String path, NodeStubbingOperation... stubbings) throws RepositoryException {
        Node node = MagnoliaNodeMockUtils.mockPageNode(path, stubbings);
        return stubCurrentContentNode(node);
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getSelectors() to return the provided value.
     *
     * @param selectors the String[] to be returned by aggregationState.getSelectors()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubSelectors(final String[] selectors) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) throws RepositoryException {
                assertThat(state, notNullValue());
                when(state.getSelectors()).thenReturn(selectors);
                // Magnolia provides the first selector value als request attribute:
                if (selectors != null) {
                    for (String selector : selectors) {
                        String[] nameValue = selector.split("=");
                        if (nameValue.length == 2 && mockWebContext().getAttribute(nameValue[0]) == null) {
                            mockWebContext(WebContextStubbingOperation.stubAttribute(nameValue[0], nameValue[1]));
                        }
                    }
                }
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getSelector() to return the provided value.
     * Stubbs getSelectors() as well.
     *
     * @param selector the String to be returned by aggregationState.getSelector()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubSelector(final String selector) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) throws RepositoryException {
                assertThat(state, notNullValue());
                when(state.getSelector()).thenReturn(selector);
                String[] selectors = isNotBlank(selector) ? selector.split("~") : new String[0];
                stubSelectors(selectors).of(state);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getExtension() to return the provided value.
     *
     * @param extension the String to be returned by aggregationState.getExtension()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubExtension(final String extension) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.getExtension()).thenReturn(extension);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getHandle() to return the provided value.
     *
     * @param handle the String to be returned by aggregationState.getHandle()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubHandle(final String handle) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.getHandle()).thenReturn(handle);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getCharacterEncoding() to return the provided value.
     *
     * @param encoding the String to be returned by aggregationState.getCharacterEncoding()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubCharacterEncoding(final String encoding) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.getCharacterEncoding()).thenReturn(encoding);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getRepository() to return the provided value.
     *
     * @param repository the String to be returned by aggregationState.getRepository()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubRepository(final String repository) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.getRepository()).thenReturn(repository);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getLocale() to return the provided value.
     *
     * @param locale the Locale to be returned by aggregationState.getLocale()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubLocale(final Locale locale) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.getLocale()).thenReturn(locale);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getCurrentURI() to return the provided value.
     *
     * @param uri the String to be returned by aggregationState.getCurrentURI()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubCurrentUri(final String uri) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.getCurrentURI()).thenReturn(uri);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method isPreviewMode() to return the provided value.
     *
     * @param value boolean value to be returned by aggregationState.isPreviewMode()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubPreviewMode(final boolean value) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.isPreviewMode()).thenReturn(value);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getOriginalBrowserURI() to return the provided value.
     *
     * @param value String value to be returned by aggregationState.getOriginalBrowserURI()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubOriginalBrowserUri(final String value) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.getOriginalBrowserURI()).thenReturn(value);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getOriginalBrowserURL() to return the provided value.
     *
     * @param value boolean value to be returned by aggregationState.getOriginalBrowserURL()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubOriginalBrowserUrl(final String value) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.getOriginalBrowserURL()).thenReturn(value);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getOriginalURI() to return the provided value.
     *
     * @param value String value to be returned by aggregationState.getOriginalURI()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubOriginalUri(final String value) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.getOriginalURI()).thenReturn(value);
            }
        };
    }

    /**
     * Creates an AggregationStateStubbingOperation that stubs method getOriginalURL() to return the provided value.
     *
     * @param value boolean value to be returned by aggregationState.getOriginalURL()
     * @return an AggregationStateStubbingOperation that performs the stubbing
     */
    public static AggregationStateStubbingOperation stubOriginalUrl(final String value) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                assertThat(state, notNullValue());
                when(state.getOriginalURL()).thenReturn(value);
            }
        };
    }

    public static AggregationStateStubbingOperation stubChannel(final String channelName) {
        return new AggregationStateStubbingOperation() {
            @Override
            public void of(final AggregationState state) {
                assertThat(state, notNullValue());
                Channel value = mock(Channel.class);
                when(value.getName()).thenReturn(channelName);
                when(state.getChannel()).thenReturn(value);
            }
        };
    }
}
