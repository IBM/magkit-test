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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;

/**
 * Testing RowStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-10-16
 */
public class RowStubbingOperationTest {

    private Row _row;

    @Before
    public void setUp() throws Exception {
        SessionMockUtils.cleanSession();
        _row = mock(Row.class);
    }

    @After
    public void tearDown() throws Exception {
        SessionMockUtils.cleanSession();
    }

    @Test
    public void stubScore() throws RepositoryException {
        assertThat(_row.getScore(), is(0.0));

        RowStubbingOperation.stubScore(0.8).of(_row);
        assertThat(_row.getScore(), is(0.8));
    }

    @Test
    public void stubScoreWithSelector() throws RepositoryException {
        assertThat(_row.getScore("test"), is(0.0));

        RowStubbingOperation.stubScore("test", 0.8).of(_row);
        assertThat(_row.getScore("test"), is(0.8));
    }

    @Test
    public void stubValue() throws RepositoryException {
        assertThat(_row.getValue("test"), nullValue());

        Value value = mock(Value.class);
        RowStubbingOperation.stubValue("test", value).of(_row);
        assertThat(_row.getValue("test"), is(value));
    }

    @Test
    public void testStubStringValue() throws RepositoryException {
        assertThat(_row.getValue("test"), nullValue());

        RowStubbingOperation.stubValue("test", "value").of(_row);
        assertThat(_row.getValue("test").getString(), is("value"));
    }

    @Test
    public void stubValues() throws RepositoryException {
        assertThat(_row.getValues(), nullValue());

        RowStubbingOperation.stubValues(mock(Value.class), mock(Value.class)).of(_row);
        assertThat(_row.getValues(), notNullValue());
        assertThat(_row.getValues().length, is(2));
    }

    @Test
    public void stubPath() throws RepositoryException {
        assertThat(_row.getPath(), nullValue());

        RowStubbingOperation.stubPath("/some/path").of(_row);
        assertThat(_row.getPath(), is("/some/path"));
    }

    @Test
    public void stubPathWithSelector() throws RepositoryException {
        assertThat(_row.getPath("test"), nullValue());

        RowStubbingOperation.stubPath("test", "/some/path").of(_row);
        assertThat(_row.getPath("test"), is("/some/path"));
    }

    @Test
    public void stubNode() {
    }
}
