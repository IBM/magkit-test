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

import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RowStubbingOperation}. These tests exercise all factory methods and the conditional
 * branches (empty vs. non-empty selectors) to ensure complete line coverage and correct stub behaviour.
 * The tests rely on Mockito defaults (e.g. primitive zero values) to verify that methods are *not* stubbed
 * when an empty selector short-circuits logic.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-10-16
 */
public class RowStubbingOperationTest {

    /**
     * Verifies stubbing of the default selector score.
     */
    @Test
    public void stubScoreDefaultSelector() throws RepositoryException {
        Row row = mock(Row.class);
        RowStubbingOperation.stubScore(0.75d).of(row);
        assertEquals(0.75d, row.getScore(), 0.0001);
    }

    /**
     * Verifies stubbing of a named selector score.
     */
    @Test
    public void stubScoreNamedSelector() throws RepositoryException {
        Row row = mock(Row.class);
        RowStubbingOperation.stubScore("mySelector", 1.23d).of(row);
        assertEquals(1.23d, row.getScore("mySelector"), 0.0001);
        // default selector not stubbed
        assertEquals(0.0d, row.getScore(), 0.0001);
    }

    /**
     * Empty selector must fall back to default getScore().
     */
    @Test
    public void stubScoreEmptySelectorFallsBack() throws RepositoryException {
        Row row = mock(Row.class);
        RowStubbingOperation.stubScore("", 2.5d).of(row);
        assertEquals(2.5d, row.getScore(), 0.0001);
        // row.getScore("") is not stubbed -> Mockito default
        assertEquals(0.0d, row.getScore(""), 0.0001);
    }

    /**
     * Null selector must trigger assertion in factory method.
     */
    @Test
    public void stubScoreNullSelectorFailsFast() {
        assertThrows(IllegalArgumentException.class, () -> RowStubbingOperation.stubScore(null, 0.1d));
    }

    /**
     * Verifies stubbing a value with non-empty selector.
     */
    @Test
    public void stubValueNonEmptySelector() throws RepositoryException {
        Row row = mock(Row.class);
        Value value = ValueMockUtils.mockValue("textValue");
        RowStubbingOperation.stubValue("prop", value).of(row);
        assertSame(value, row.getValue("prop"));
        assertNull(row.getValue("other"));
    }

    /**
     * Empty selector must result in no stubbing (method does nothing).
     */
    @Test
    public void stubValueEmptySelectorDoesNothing() throws RepositoryException {
        Row row = mock(Row.class);
        Value value = ValueMockUtils.mockValue("x");
        RowStubbingOperation.stubValue("", value).of(row);
        assertNull(row.getValue(""));
    }

    /**
     * Null selector fails fast.
     */
    @Test
    public void stubValueNullSelectorFailsFast() throws RepositoryException {
        assertThrows(IllegalArgumentException.class, () -> RowStubbingOperation.stubValue((String) null, ValueMockUtils.mockValue("x")));
    }

    /**
     * Verifies convenience overload creating a Value from a String.
     */
    @Test
    public void stubValueStringOverload() throws RepositoryException {
        Row row = mock(Row.class);
        RowStubbingOperation.stubValue("title", "Hello World").of(row);
        Value v = row.getValue("title");
        assertNotNull(v);
        assertEquals("Hello World", v.getString());
    }

    /**
     * Verifies stubbing of multiple values via getValues().
     */
    @Test
    public void stubValuesArray() throws RepositoryException {
        Row row = mock(Row.class);
        Value v1 = ValueMockUtils.mockValue("one");
        Value v2 = ValueMockUtils.mockValue("two");
        RowStubbingOperation.stubValues(v1, v2).of(row);
        Value[] result = row.getValues();
        assertEquals(2, result.length);
        assertArrayEquals(new Value[]{v1, v2}, result);
    }

    /**
     * Verifies stubbing with an empty values array.
     */
    @Test
    public void stubValuesEmptyArray() throws RepositoryException {
        Row row = mock(Row.class);
        RowStubbingOperation.stubValues().of(row);
        Value[] result = row.getValues();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     * Verifies path stubbing for default selector.
     */
    @Test
    public void stubPathDefault() throws RepositoryException {
        Row row = mock(Row.class);
        RowStubbingOperation.stubPath("/content/sample").of(row);
        assertEquals("/content/sample", row.getPath());
    }

    /**
     * Verifies path stubbing with null path.
     */
    @Test
    public void stubPathDefaultNullPath() throws RepositoryException {
        Row row = mock(Row.class);
        RowStubbingOperation.stubPath((String) null).of(row);
        assertNull(row.getPath());
    }

    /**
     * Verifies path stubbing for a named selector.
     */
    @Test
    public void stubPathNamedSelector() throws RepositoryException {
        Row row = mock(Row.class);
        RowStubbingOperation.stubPath("s", "/content/sel").of(row);
        assertEquals("/content/sel", row.getPath("s"));
        assertNull(row.getPath());
    }

    /**
     * Empty selector must fall back to default getPath().
     */
    @Test
    public void stubPathEmptySelectorFallsBack() throws RepositoryException {
        Row row = mock(Row.class);
        RowStubbingOperation.stubPath("", "/content/default").of(row);
        assertEquals("/content/default", row.getPath());
        assertNull(row.getPath(""));
    }

    /**
     * Verifies stubbing the relationship between a Row and a backing Node (all dynamic answers).
     */
    @Test
    public void stubNodeRelationship() throws RepositoryException {
        Row row = mock(Row.class);
        Node backing = mock(Node.class);
        Node child = mock(Node.class);
        Property prop = mock(Property.class);
        Value propValue = ValueMockUtils.mockValue("propVal");

        when(backing.getPath()).thenReturn("/content/parent");
        when(child.getPath()).thenReturn("/content/parent/child");
        when(backing.getNode("child")).thenReturn(child);
        when(backing.getProperty("myProp")).thenReturn(prop);
        when(prop.getValue()).thenReturn(propValue);

        RowStubbingOperation.stubNode(backing).of(row);

        // default node and path
        assertSame(backing, row.getNode());
        assertEquals("/content/parent", row.getPath());
        // relative child lookup delegated to backing
        assertSame(child, row.getNode("child"));
        assertEquals("/content/parent/child", row.getPath("child"));
        // property lookup forwarded
        Value resolved = row.getValue("myProp");
        assertSame(propValue, resolved);

        // verify delegation occurred (child node accessed twice: once for getNode, once for getPath)
        verify(backing, times(2)).getNode("child");
        verify(backing).getProperty("myProp");
    }

    /**
     * Verify stubbing a value with a null Value instance (selector non-empty returns null).
     */
    @Test
    public void stubValueNonEmptySelectorWithNullValue() throws RepositoryException {
        Row row = mock(Row.class);
        RowStubbingOperation.stubValue("prop", (Value) null).of(row);
        assertNull(row.getValue("prop"));
    }

    /**
     * Null node must fail fast.
     */
    @Test
    public void stubNodeNullFailsFast() throws RepositoryException {
        Row row = mock(Row.class);
        assertThrows(IllegalArgumentException.class, () -> RowStubbingOperation.stubNode(null).of(row));
    }
}
