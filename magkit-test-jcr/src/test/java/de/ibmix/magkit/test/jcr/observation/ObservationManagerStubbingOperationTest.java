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
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventJournal;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertNull(manager.getRegisteredEventListeners());
        ObservationManagerStubbingOperation.stubRegisteredEventListeners().of(manager);
        assertNotNull(manager.getRegisteredEventListeners());
        assertFalse(manager.getRegisteredEventListeners().hasNext());
        ObservationManagerStubbingOperation.stubRegisteredEventListeners(mock(EventListener.class), mock(EventListener.class)).of(manager);
        assertTrue(manager.getRegisteredEventListeners().hasNext());
    }

    @Test
    public void stubEventJournal() throws RepositoryException {
        ObservationManager manager = mock(ObservationManager.class);
        assertNull(manager.getEventJournal());
        EventJournal journal = mock(EventJournal.class);
        ObservationManagerStubbingOperation.stubEventJournal(journal).of(manager);
        assertEquals(journal, manager.getEventJournal());
        ObservationManagerStubbingOperation.stubEventJournal(null).of(manager);
        assertNull(manager.getEventJournal());
    }

    @Test
    public void testStubEventJournal() throws RepositoryException {
        ObservationManager manager = mock(ObservationManager.class);
        assertNull(manager.getEventJournal(123, "test", true, null, null));
        EventJournal journal = mock(EventJournal.class);
        ObservationManagerStubbingOperation.stubEventJournal(journal, 123, "test", true, null, null).of(manager);
        assertEquals(journal, manager.getEventJournal(123, "test", true, null, null));
        assertNull(manager.getEventJournal(123, "other", true, null, null));
        ObservationManagerStubbingOperation.stubEventJournal(null, 123, "test", true, null, null).of(manager);
        assertNull(manager.getEventJournal(123, "test", true, null, null));
    }
}
