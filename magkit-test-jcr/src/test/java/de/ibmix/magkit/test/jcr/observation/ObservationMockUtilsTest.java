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

import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.observation.ObservationManager;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test ObservationMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-12-06
 */
public class ObservationMockUtilsTest {

    @Test
    public void mockObservationManager() throws RepositoryException {
        ObservationManagerStubbingOperation op1 = mock(ObservationManagerStubbingOperation.class);
        ObservationManagerStubbingOperation op2 = mock(ObservationManagerStubbingOperation.class);
        ObservationManager manager = ObservationMockUtils.mockObservationManager(op1, op2);
        assertThat(manager, notNullValue());
        verify(op1, atLeastOnce()).of(manager);
        verify(op2, atLeastOnce()).of(manager);
        assertThat(manager.getRegisteredEventListeners(), notNullValue());
        assertFalse(manager.getRegisteredEventListeners().hasNext());
        assertThat(manager.getEventJournal(), nullValue());
        assertThat(manager.getEventJournal(123, "path", true, null, null), nullValue());
    }
}
