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
 * Utility class for stubbing mocks of javax.jcr.query.Row.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-10-16
 */
public abstract class RowStubbingOperation implements ExceptionStubbingOperation<Row, RepositoryException> {

    public static RowStubbingOperation stubScore(final double score) {
        return new RowStubbingOperation() {
            @Override
            public void of(final Row mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                doReturn(score).when(mock).getScore();
            }
        };
    }

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

    public static RowStubbingOperation stubValue(final String selector, String value) throws RepositoryException {
        return stubValue(selector, ValueMockUtils.mockValue(value));
    }

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

    public static RowStubbingOperation stubPath(final String path) {
        return new RowStubbingOperation() {
            @Override
            public void of(final Row mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                doReturn(path).when(mock).getPath();
            }
        };
    }

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
     * Factory method for a RowStubbingOperation that stubbs the node of a Row mock.
     * Note that the methods
     * getPath(), getPath(String selector), getNode(String selector) and getValue(String selector)
     * are mocked to return the values from the given Node.
     *
     * @param node the new node beeing the value source of a row mock
     * @return the RowStubbingOperation, never null
     */
    public static RowStubbingOperation stubNode(final Node node) {
        return new RowStubbingOperation() {
            @Override
            public void of(final Row mock) throws RepositoryException {
                assertThat(mock, notNullValue());
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
