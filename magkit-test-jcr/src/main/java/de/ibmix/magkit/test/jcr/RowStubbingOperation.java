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

import de.ibmix.magkit.test.ExceptionStubbingOperation;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

/**
 * Utility {@code ExceptionStubbingOperation} implementations for configuring Mockito stubs on {@link Row} mocks.
 * <p>
 * Each static factory method returns a {@link RowStubbingOperation} that performs a focused stubbing (score, value,
 * values array, path or node related data). Operations are designed to be composable – callers can create several
 * operations and apply them sequentially to the same {@link Row} mock to build up the required behaviour.
 * </p>
 * <p>
 * Typical usage:
 * <pre>{@code
 * Row row = mock(Row.class);
 * RowStubbingOperation.stubScore(0.75d).of(row);
 * RowStubbingOperation.stubPath("/content/sample").of(row);
 * }
 * </pre>
 * </p>
 * <p>
 * Defensive null checks (via Hamcrest {@code assertThat}) are applied to fail fast during test setup when mandatory
 * arguments are missing. All methods return non-null operations.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-10-16
 */
public abstract class RowStubbingOperation implements ExceptionStubbingOperation<Row, RepositoryException> {

    /**
     * Creates an operation stubbing {@link Row#getScore()} for the (single / default) selector.
     *
     * @param score the score value to return
     * @return a non-null stubbing operation
     */
    public static RowStubbingOperation stubScore(final double score) {
        return new RowStubbingOperation() {
            @Override
            public void of(final Row mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                doReturn(score).when(mock).getScore();
            }
        };
    }

    /**
     * Creates an operation stubbing {@link Row#getScore(String)} if a non-empty selector is given; otherwise
     * falls back to stubbing {@link Row#getScore()}.
     *
     * @param selector the selector name (may be empty to indicate default selector, must not be null)
     * @param score    the score value to return
     * @return a non-null stubbing operation
     */
    public static RowStubbingOperation stubScore(final String selector, final double score) {
        assertThat(selector, notNullValue());
        return new RowStubbingOperation() {
            @Override
            public void of(final Row mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                if (isNotEmpty(selector)) {
                    doReturn(score).when(mock).getScore(selector);
                } else {
                    doReturn(score).when(mock).getScore();
                }
            }
        };
    }

    /**
     * Creates an operation stubbing {@link Row#getValue(String)} for the given selector.
     * Does nothing if the selector is empty (only non-null is enforced); in that case callers should prefer
     * other operations (e.g. {@link #stubValues(Value...)} if appropriate).
     *
     * @param selector the selector / property identifier (must not be null, may be empty)
     * @param value    the value to return (may be null depending on test scenario)
     * @return a non-null stubbing operation
     */
    public static RowStubbingOperation stubValue(final String selector, Value value) {
        assertThat(selector, notNullValue());
        return new RowStubbingOperation() {
            @Override
            public void of(final Row mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                if (isNotEmpty(selector)) {
                    doReturn(value).when(mock).getValue(selector);
                }
            }
        };
    }

    /**
     * Convenience overload creating an operation stubbing {@link Row#getValue(String)} with a simple String value.
     * Internally delegates to {@link ValueMockUtils#mockValue(String)} to create a {@link Value} instance.
     *
     * @param selector the selector / property identifier (must not be null, may be empty)
     * @param value    the string to wrap as {@link Value}
     * @return a non-null stubbing operation
     * @throws RepositoryException if the internal value creation requires throwing it
     */
    public static RowStubbingOperation stubValue(final String selector, String value) throws RepositoryException {
        return stubValue(selector, ValueMockUtils.mockValue(value));
    }

    /**
     * Creates an operation stubbing {@link Row#getValues()}.
     *
     * @param values the array to return (must not be null; may be empty)
     * @return a non-null stubbing operation
     */
    public static RowStubbingOperation stubValues(final Value... values) {
        assertThat(values, notNullValue());
        return new RowStubbingOperation() {
            @Override
            public void of(final Row mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                doReturn(values).when(mock).getValues();
            }
        };
    }

    /**
     * Creates an operation stubbing {@link Row#getPath()} for the default selector.
     *
     * @param path the path to return (may be null if the test explicitly asserts null handling)
     * @return a non-null stubbing operation
     */
    public static RowStubbingOperation stubPath(final String path) {
        return new RowStubbingOperation() {
            @Override
            public void of(final Row mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                doReturn(path).when(mock).getPath();
            }
        };
    }

    /**
     * Creates an operation stubbing {@link Row#getPath(String)} for a non-empty selector, otherwise
     * stubs {@link Row#getPath()}.
     *
     * @param selector the selector (may be empty, must not be null)
     * @param path     the path to return
     * @return a non-null stubbing operation
     */
    public static RowStubbingOperation stubPath(final String selector, final String path) {
        return new RowStubbingOperation() {
            @Override
            public void of(final Row mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                if (isNotEmpty(selector)) {
                    doReturn(path).when(mock).getPath(selector);
                } else {
                    doReturn(path).when(mock).getPath();
                }
            }
        };
    }

    /**
     * Creates an operation stubbing the relation between a {@link Row} and a backing {@link Node}.
     * <p>
     * The following methods are stubbed based on the supplied {@code node}:
     * <ul>
     *   <li>{@link Row#getNode()}</li>
     *   <li>{@link Row#getPath()}</li>
     *   <li>{@link Row#getNode(String)}</li>
     *   <li>{@link Row#getPath(String)}</li>
     *   <li>{@link Row#getValue(String)}</li>
     * </ul>
     * Calls to the selector-based methods forward to the respective {@link Node} lookups / property access so that
     * relative path resolution and property retrieval use the actual {@link Node} implementation.
     * </p>
     *
     * @param node the source node whose data should be exposed through the row (must not be null)
     * @return a non-null stubbing operation
     */
    public static RowStubbingOperation stubNode(final Node node) {
        return new RowStubbingOperation() {
            @Override
            public void of(final Row mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                assertThat(node, notNullValue());
                doReturn(node).when(mock).getNode();
                doReturn(node.getPath()).when(mock).getPath();
                doAnswer(invocation -> {
                    String relPath = (String) invocation.getArguments()[0];
                    return node.getNode(relPath);
                }).when(mock).getNode(anyString());
                doAnswer(invocation -> {
                    String relPath = (String) invocation.getArguments()[0];
                    return node.getNode(relPath).getPath();
                }).when(mock).getPath(anyString());
                doAnswer(invocation -> {
                    String relPath = (String) invocation.getArguments()[0];
                    return node.getProperty(relPath).getValue();
                }).when(mock).getValue(anyString());
            }
        };
    }

    private RowStubbingOperation() {}
}
