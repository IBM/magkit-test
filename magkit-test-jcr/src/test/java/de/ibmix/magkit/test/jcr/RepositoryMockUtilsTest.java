package de.ibmix.magkit.test.jcr;

/*-
 * #%L
 * magkit-test-jcr Magnolia Module
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RepositoryMockUtils} ensuring correct lifecycle, thread-local isolation and application of stubbing operations.
 * Covered aspects:
 * <ul>
 *     <li>Initial mock creation when none exists.</li>
 *     <li>Reuse of the same mock instance within a single thread.</li>
 *     <li>Reset behavior via {@link RepositoryMockUtils#cleanRepository()} producing a new instance.</li>
 *     <li>Application and accumulation of multiple {@link RepositoryStubbingOperation} instances.</li>
 *     <li>Thread-local isolation across different threads.</li>
 *     <li>Behavior of {@link RepositoryStubbingOperation#stubLogin(Session)} with various session/workspace combinations.</li>
 *     <li>Exception propagation from stubbing operations.</li>
 * </ul>
 *
 * @author GitHub Copilot supervised by wolf.bubenik@ibmix.de
 */
public class RepositoryMockUtilsTest {

    /**
     * Helper to create a stubbing operation for a repository descriptor key/value pair.
     */
    private RepositoryStubbingOperation stubDescriptor(final String key, final String value) {
        return new RepositoryStubbingOperation() {
            @Override
            public void of(final Repository repo) throws RepositoryException {
                when(repo.getDescriptor(key)).thenReturn(value);
            }
        };
    }

    @AfterEach
    public void tearDown() {
        RepositoryMockUtils.cleanRepository();
    }

    @Test
    public void firstCallCreatesNewMock() throws RepositoryException {
        RepositoryMockUtils.cleanRepository();
        Repository repository = RepositoryMockUtils.mockRepository();
        assertNotNull(repository, "Repository mock should be created.");
    }

    @Test
    public void subsequentCallsReuseSameInstance() throws RepositoryException {
        Repository first = RepositoryMockUtils.mockRepository();
        Repository second = RepositoryMockUtils.mockRepository();
        assertTrue(first == second, "Repository mock should be reused within the same thread.");
    }

    @Test
    public void cleanRepositoryCreatesNewInstance() throws RepositoryException {
        Repository first = RepositoryMockUtils.mockRepository();
        RepositoryMockUtils.cleanRepository();
        Repository second = RepositoryMockUtils.mockRepository();
        assertFalse(first == second, "Repository mock should be recreated after cleanRepository().");
    }

    @Test
    public void customStubbingOperationApplied() throws RepositoryException {
        Repository repository = RepositoryMockUtils.mockRepository(stubDescriptor("foo", "bar"));
        assertEquals("bar", repository.getDescriptor("foo"));
    }

    @Test
    public void multipleStubbingsAccumulateAcrossCalls() throws RepositoryException {
        RepositoryMockUtils.cleanRepository();
        Repository first = RepositoryMockUtils.mockRepository(stubDescriptor("a", "1"));
        Repository second = RepositoryMockUtils.mockRepository(stubDescriptor("b", "2"));
        assertTrue(first == second, "Same instance expected while accumulating stubbings.");
        assertEquals("1", second.getDescriptor("a"));
        assertEquals("2", second.getDescriptor("b"));
    }

    @Test
    public void multipleStubbingsSingleCallApplied() throws RepositoryException {
        RepositoryMockUtils.cleanRepository();
        Repository repository = RepositoryMockUtils.mockRepository(
            stubDescriptor("x", "X"),
            stubDescriptor("y", "Y")
        );
        assertEquals("X", repository.getDescriptor("x"));
        assertEquals("Y", repository.getDescriptor("y"));
    }

    @Test
    public void threadLocalIsolationCreatesDistinctInstances() throws Exception {
        Repository mainThreadRepo = RepositoryMockUtils.mockRepository();
        AtomicReference<Repository> otherThreadRepo = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                otherThreadRepo.set(RepositoryMockUtils.mockRepository());
            } catch (RepositoryException e) {
                Assertions.fail("RepositoryException should not occur in other thread: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        t.start();
        latch.await();
        assertNotNull(otherThreadRepo.get());
        assertFalse(mainThreadRepo == otherThreadRepo.get(), "Repository mocks must differ across threads.");
    }

    @Test
    public void stubLoginNullSession() throws RepositoryException {
        Repository repository = RepositoryMockUtils.mockRepository(RepositoryStubbingOperation.stubLogin(null));
        assertNull(repository.login());
        assertNull(repository.login("anyWorkspace"));
    }

    @Test
    public void stubLoginSessionWithoutWorkspace() throws RepositoryException {
        Session session = mock(Session.class);
        Repository repository = RepositoryMockUtils.mockRepository(RepositoryStubbingOperation.stubLogin(session));
        assertEquals(session, repository.login());
        assertEquals(repository, session.getRepository());
        assertNull(repository.login("ws"));
    }

    @Test
    public void stubLoginSessionWithWorkspace() throws RepositoryException {
        Session session = mock(Session.class);
        Workspace workspace = mock(Workspace.class);
        when(workspace.getName()).thenReturn("myWorkspace");
        when(session.getWorkspace()).thenReturn(workspace);
        Repository repository = RepositoryMockUtils.mockRepository(RepositoryStubbingOperation.stubLogin(session));
        assertEquals(session, repository.login());
        assertEquals(repository, session.getRepository());
        assertEquals(session, repository.login("myWorkspace"));
    }

    @Test
    public void exceptionPropagation() {
        RepositoryMockUtils.cleanRepository();
        assertThrows(javax.jcr.RepositoryException.class, () ->
            RepositoryMockUtils.mockRepository(new RepositoryStubbingOperation() {
                @Override
                public void of(final javax.jcr.Repository repo) throws javax.jcr.RepositoryException {
                    throw new javax.jcr.RepositoryException("boom");
                }
            })
        );
        try {
            assertNotNull(RepositoryMockUtils.mockRepository());
        } catch (javax.jcr.RepositoryException e) {
            Assertions.fail("Unexpected exception after failed stubbing: " + e.getMessage());
        }
    }
}
