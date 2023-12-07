package de.ibmix.magkit.test.jcr.observation;

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

/**
 * Test ObservationManagerStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-12-06
 */
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventJournal;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;

/**
 * Test ObservationManagerStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-12-06
 */
public class ObservationManagerStubbingOperationTest {

    @Test
    public void stubRegisteredEventListeners() throws RepositoryException {
        ObservationManager manager = mock(ObservationManager.class);
        assertThat(manager.getRegisteredEventListeners(), nullValue());

        ObservationManagerStubbingOperation.stubRegisteredEventListeners().of(manager);
        assertThat(manager.getRegisteredEventListeners(), notNullValue());
        assertFalse(manager.getRegisteredEventListeners().hasNext());

        ObservationManagerStubbingOperation.stubRegisteredEventListeners(mock(EventListener.class), mock(EventListener.class)).of(manager);
        assertTrue(manager.getRegisteredEventListeners().hasNext());
    }

    @Test
    public void stubEventJournal() throws RepositoryException {
        ObservationManager manager = mock(ObservationManager.class);
        assertThat(manager.getEventJournal(), nullValue());

        EventJournal journal = mock(EventJournal.class);
        ObservationManagerStubbingOperation.stubEventJournal(journal).of(manager);
        assertThat(manager.getEventJournal(), is(journal));

        ObservationManagerStubbingOperation.stubEventJournal(null).of(manager);
        assertThat(manager.getEventJournal(), nullValue());
    }

    @Test
    public void testStubEventJournal() throws RepositoryException {
        ObservationManager manager = mock(ObservationManager.class);
        assertThat(manager.getEventJournal(123, "test", true, null, null), nullValue());

        EventJournal journal = mock(EventJournal.class);
        ObservationManagerStubbingOperation.stubEventJournal(journal, 123, "test", true, null, null).of(manager);
        assertThat(manager.getEventJournal(123, "test", true, null, null), is(journal));
        assertThat(manager.getEventJournal(123, "other", true, null, null), nullValue());

        ObservationManagerStubbingOperation.stubEventJournal(null, 123, "test", true, null, null).of(manager);
        assertThat(manager.getEventJournal(123, "test", true, null, null), nullValue());
    }
}
