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

import de.ibmix.magkit.test.ExceptionStubbingOperation;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.jackrabbit.commons.iterator.EventListenerIteratorAdapter;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventJournal;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.doReturn;

/**
 * Utility class for stubbing mocks of javax.jcr.observation.ObservationManager.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-12-06
 */
public abstract class ObservationManagerStubbingOperation implements ExceptionStubbingOperation<ObservationManager, RepositoryException> {

    public static ObservationManagerStubbingOperation stubRegisteredEventListeners(final EventListener... listeners) {
        return new ObservationManagerStubbingOperation() {
            @Override
            public void of(ObservationManager mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                EventListenerIterator listenerIterator = mock.getRegisteredEventListeners();
                List<EventListener> listenerList = listenerIterator == null ? new ArrayList<>() : IteratorUtils.toList(listenerIterator);
                listenerList.addAll(Arrays.asList(listeners));
                EventListenerIteratorAdapter iteratorAdapter = new EventListenerIteratorAdapter(listenerList);
                doReturn(iteratorAdapter).when(mock).getRegisteredEventListeners();
            }
        };
    }

    public static ObservationManagerStubbingOperation stubEventJournal(final EventJournal journal) {
        return new ObservationManagerStubbingOperation() {
            @Override
            public void of(ObservationManager mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                doReturn(journal).when(mock).getEventJournal();
            }
        };
    }

    public static ObservationManagerStubbingOperation stubEventJournal(final EventJournal journal, int eventTypes, final String absPath, boolean isDeep, final String[] uuid, final String[] nodeTypeName) {
        return new ObservationManagerStubbingOperation() {
            @Override
            public void of(ObservationManager mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                doReturn(journal).when(mock).getEventJournal(eventTypes, absPath, isDeep, uuid, nodeTypeName);
            }
        };
    }
}
