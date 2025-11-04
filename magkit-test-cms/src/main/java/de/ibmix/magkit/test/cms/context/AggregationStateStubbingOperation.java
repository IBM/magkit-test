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

import de.ibmix.magkit.assertions.Require;
import de.ibmix.magkit.test.ExceptionStubbingOperation;
import de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils;
import de.ibmix.magkit.test.cms.node.PageNodeStubbingOperation;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Channel;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Factory holder for reusable {@code AggregationStateStubbingOperation} instances used to configure a mocked {@link AggregationState} in tests.
 * <p>
 * Each static method returns a lightweight operation object whose {@link ExceptionStubbingOperation#of(Object) of(mock)} method applies a specific set of Mockito stubbings
 * to the supplied {@link AggregationState} mock. The design follows a fluent, composable pattern:
 * <pre>{@code
 * AggregationState state = ContextMockUtils.mockAggregationState(
 *     AggregationStateStubbingOperation.stubMainContentNode(pageNode),
 *     AggregationStateStubbingOperation.stubSelector("device=mobile~lang=de"),
 *     AggregationStateStubbingOperation.stubExtension("html"),
 *     AggregationStateStubbingOperation.stubPreviewMode(true)
 * );
 * }</pre>
 * <p>
 * Side-effects and coupling:
 * <ul>
 *   <li>Some operations also stub derived properties (e.g. {@code stubMainContentNode} stubs handle and repository).</li>
 *   <li>{@link #stubSelectors(String[])} may interact with a mocked {@link info.magnolia.context.WebContext} by exposing selector key/value pairs as request attributes when absent.</li>
 * </ul>
 * Null handling:
 * <ul>
 *   <li>If a provided {@link Node} value is {@code null}, only its direct method (e.g. {@code getMainContentNode()}) is stubbed; dependent stubbings (handle, repository) are skipped.</li>
 *   <li>String parameters may be {@code null}; the corresponding getter will then return {@code null}.</li>
 * </ul>
 * Thread-safety: Thread-safe, intended for multithreaded unit tests. Manipulated static Magnolia runtime state is backed with ThreadLocal&lt;Context&gt;.
 * <p>
 * Error handling: All operations delegate to Mockito; no checked exceptions are thrown unless declared (repository access in node based stubbings).
 * <p>
 * Usage recommendation: Combine multiple operations in a single {@code mockAggregationState(...)} call for readability and to avoid intermediate mutable states.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-09-01
 */
public abstract class AggregationStateStubbingOperation implements ExceptionStubbingOperation<AggregationState, RepositoryException> {

    /**
     * Creates a stubbing operation that configures {@link AggregationState#getMainContentNode()}.
     * When {@code value != null} this also stubs {@link AggregationState#getHandle()} with the node path and
     * {@link AggregationState#getRepository()} with the node workspace name.
     * Contract:
     * <ul>
     *   <li>Idempotent: calling {@code of(state)} multiple times re-applies the same stubbings.</li>
     *   <li>Does not create nodes; caller supplies an existing mocked or real node.</li>
     * </ul>
     *
     * @param value node returned by {@link AggregationState#getMainContentNode()} (may be {@code null})
     * @return operation applying the described stubbings
     */
    public static AggregationStateStubbingOperation stubMainContentNode(final Node value) {
        return new AggregationStateStubbingOperation() {
            @Override
            public void of(final AggregationState state) throws RepositoryException {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getMainContentNode()).thenReturn(value);
                if (value != null) {
                    stubHandle(value.getPath()).of(state);
                    stubRepository(value.getSession().getWorkspace().getName()).of(state);
                }
            }
        };
    }

    /**
     * Convenience operation creating a mocked page node (website workspace) at the given path and delegating to {@link #stubMainContentNode(Node)}.
     *
     * @param path repository path for the page node
     * @param stubbings optional node stubbing operations applied when creating the mocked node
     * @return operation configuring {@link AggregationState#getMainContentNode()}
     * @throws RepositoryException if underlying node mocking signals an error (normally not thrown)
     */
    public static AggregationStateStubbingOperation stubMainContentNode(String path, PageNodeStubbingOperation... stubbings) throws RepositoryException {
        Node node = MagnoliaNodeMockUtils.mockPageNode(path, stubbings);
        return stubMainContentNode(node);
    }

    /**
     * Creates a stubbing operation that configures {@link AggregationState#getCurrentContentNode()}.
     * When {@code value != null} this also stubs {@link AggregationState#getHandle()} and {@link AggregationState#getRepository()} analogous to {@link #stubMainContentNode(Node)}.
     *
     * @param value node returned by {@link AggregationState#getCurrentContentNode()} (may be {@code null})
     * @return operation applying the described stubbings
     */
    public static AggregationStateStubbingOperation stubCurrentContentNode(final Node value) {
        return new AggregationStateStubbingOperation() {
            @Override
            public void of(final AggregationState state) throws RepositoryException {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getCurrentContentNode()).thenReturn(value);
                if (value != null) {
                    stubHandle(value.getPath()).of(state);
                    stubRepository(value.getSession().getWorkspace().getName()).of(state);
                }
            }
        };
    }

    /**
     * Convenience operation creating a mocked page node (website workspace) at the given path and delegating to {@link #stubCurrentContentNode(Node)}.
     * (Note: despite previous documentation mentioning a component node, current implementation uses a page node.)
     *
     * @param path repository path for the page node
     * @param stubbings optional node stubbing operations applied during node creation
     * @return operation configuring {@link AggregationState#getCurrentContentNode()}
     * @throws RepositoryException if node mocking fails
     */
    public static AggregationStateStubbingOperation stubCurrentContentNode(String path, PageNodeStubbingOperation... stubbings) throws RepositoryException {
        Node node = MagnoliaNodeMockUtils.mockPageNode(path, stubbings);
        return stubCurrentContentNode(node);
    }

    /**
     * Creates a stubbing operation for {@link AggregationState#getSelectors()} returning the supplied array.
     * Additionally publishes key/value style selectors (format: {@code key=value}) as web context request attributes if not already set.
     * Selector splitting logic does not validate keys or values; malformed entries are ignored for attribute exposure.
     *
     * @param selectors array returned by {@link AggregationState#getSelectors()} (may be {@code null})
     * @return operation applying the described stubbings
     */
    public static AggregationStateStubbingOperation stubSelectors(final String[] selectors) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) throws RepositoryException {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getSelectors()).thenReturn(selectors);
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
     * Creates a stubbing operation for {@link AggregationState#getSelector()} and also configures {@link AggregationState#getSelectors()}.
     * Splits the single selector string by {@code '~'} into an array; empty or blank input yields an empty array.
     *
     * @param selector value returned by {@link AggregationState#getSelector()} (may be {@code null})
     * @return operation applying the described stubbings
     */
    public static AggregationStateStubbingOperation stubSelector(final String selector) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) throws RepositoryException {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getSelector()).thenReturn(selector);
                String[] selectors = isNotBlank(selector) ? selector.split("~") : new String[0];
                stubSelectors(selectors).of(state);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getExtension()}.
     *
     * @param extension returned by {@link AggregationState#getExtension()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubExtension(final String extension) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getExtension()).thenReturn(extension);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getHandle()}.
     *
     * @param handle returned by {@link AggregationState#getHandle()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubHandle(final String handle) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getHandle()).thenReturn(handle);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getCharacterEncoding()}.
     *
     * @param encoding returned by {@link AggregationState#getCharacterEncoding()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubCharacterEncoding(final String encoding) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getCharacterEncoding()).thenReturn(encoding);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getRepository()}.
     *
     * @param repository returned by {@link AggregationState#getRepository()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubRepository(final String repository) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getRepository()).thenReturn(repository);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getLocale()}.
     *
     * @param locale returned by {@link AggregationState#getLocale()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubLocale(final Locale locale) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getLocale()).thenReturn(locale);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getCurrentURI()}.
     *
     * @param uri returned by {@link AggregationState#getCurrentURI()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubCurrentUri(final String uri) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getCurrentURI()).thenReturn(uri);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#isPreviewMode()}.
     *
     * @param value boolean returned by {@link AggregationState#isPreviewMode()}
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubPreviewMode(final boolean value) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.isPreviewMode()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getOriginalBrowserURI()}.
     *
     * @param value returned by {@link AggregationState#getOriginalBrowserURI()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubOriginalBrowserUri(final String value) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getOriginalBrowserURI()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getOriginalBrowserURL()}.
     *
     * @param value returned by {@link AggregationState#getOriginalBrowserURL()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubOriginalBrowserUrl(final String value) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getOriginalBrowserURL()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getOriginalURI()}.
     *
     * @param value returned by {@link AggregationState#getOriginalURI()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubOriginalUri(final String value) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getOriginalURI()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getOriginalURL()}.
     *
     * @param value returned by {@link AggregationState#getOriginalURL()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubOriginalUrl(final String value) {
        return new AggregationStateStubbingOperation() {
            public void of(AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                when(state.getOriginalURL()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs {@link AggregationState#getChannel()} by creating a simple {@link Channel} mock returning the given channel name.
     * Does not stub additional channel properties.
     *
     * @param channelName returned by {@link Channel#getName()} (may be {@code null})
     * @return operation applying the described stubbing
     */
    public static AggregationStateStubbingOperation stubChannel(final String channelName) {
        return new AggregationStateStubbingOperation() {
            @Override
            public void of(final AggregationState state) {
                Require.Argument.notNull(state, "state should not be null");
                Channel value = mock(Channel.class);
                when(value.getName()).thenReturn(channelName);
                when(state.getChannel()).thenReturn(value);
            }
        };
    }
}
