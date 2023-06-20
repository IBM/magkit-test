package de.ibmix.magkit.mockito.jcr;

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

import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import static org.mockito.Mockito.mock;

/**
 * Utility class for mocking a javax.jcr.Repository.
 * Repository mock ich stored in a ThreadLocale instance and holds complete jcr context (mocked session, workspace...).
 * <p/>
 * !! Note that you have to call RepositoryMockUtils.cleanRepository() to to get new mock instances of Repository, Session, Workspace, Node... . !!
 *
 * @author wolf.bubenik
 * @since 04.02.14
 */
public final class RepositoryMockUtils {
    private RepositoryMockUtils() {
    }

    private static final ThreadLocal<Repository> REPOSITORY = new ThreadLocal<Repository>();

    public static Repository mockRepository(RepositoryStubbingOperation... stubbings) throws RepositoryException {
        Repository result = REPOSITORY.get();
        if (result == null) {
            result = mock(Repository.class);
            REPOSITORY.set(result);
        }
        for (RepositoryStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    public static void cleanRepository() {
        REPOSITORY.set(null);
    }
}
