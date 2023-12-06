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

import javax.jcr.RepositoryException;
import javax.jcr.observation.ObservationManager;

import static de.ibmix.magkit.test.jcr.observation.ObservationManagerStubbingOperation.stubRegisteredEventListeners;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;

/**
 * Utility class for creating mocks of javax.jcr.observation.ObservationManager.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-12-06
 */
public abstract class ObservationMockUtils {

    public static ObservationManager mockObservationManager(ObservationManagerStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        final ObservationManager observationManager = mock(ObservationManager.class);
        stubRegisteredEventListeners().of(observationManager);
        for (ObservationManagerStubbingOperation stubbing : stubbings) {
            stubbing.of(observationManager);
        }
        return observationManager;
    }
}
