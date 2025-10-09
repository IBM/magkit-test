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

import org.junit.After;
import org.junit.Test;
import org.junit.Assert;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
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

    @After
    public void tearDown() {
        RepositoryMockUtils.cleanRepository();
    }

    @Test
    public void firstCallCreatesNewMock() throws RepositoryException {
        RepositoryMockUtils.cleanRepository();
        Repository repository = RepositoryMockUtils.mockRepository();
        assertThat("Repository mock should be created.", repository, notNullValue());
    }

    @Test
    public void subsequentCallsReuseSameInstance() throws RepositoryException {
        Repository first = RepositoryMockUtils.mockRepository();
        Repository second = RepositoryMockUtils.mockRepository();
        assertThat("Repository mock should be reused within the same thread.", first == second, is(true));
    }

    @Test
    public void cleanRepositoryCreatesNewInstance() throws RepositoryException {
        Repository first = RepositoryMockUtils.mockRepository();
        RepositoryMockUtils.cleanRepository();
        Repository second = RepositoryMockUtils.mockRepository();
        assertThat("Repository mock should be recreated after cleanRepository().", first == second, is(false));
    }

    @Test
    public void customStubbingOperationApplied() throws RepositoryException {
        Repository repository = RepositoryMockUtils.mockRepository(stubDescriptor("foo", "bar"));
        assertThat(repository.getDescriptor("foo"), is("bar"));
    }

    @Test
    public void multipleStubbingsAccumulateAcrossCalls() throws RepositoryException {
        RepositoryMockUtils.cleanRepository();
        Repository first = RepositoryMockUtils.mockRepository(stubDescriptor("a", "1"));
        Repository second = RepositoryMockUtils.mockRepository(stubDescriptor("b", "2"));
        assertThat("Same instance expected while accumulating stubbings.", first == second, is(true));
        assertThat(second.getDescriptor("a"), is("1"));
        assertThat(second.getDescriptor("b"), is("2"));
    }

    @Test
    public void multipleStubbingsSingleCallApplied() throws RepositoryException {
        RepositoryMockUtils.cleanRepository();
        Repository repository = RepositoryMockUtils.mockRepository(
            stubDescriptor("x", "X"),
            stubDescriptor("y", "Y")
        );
        assertThat(repository.getDescriptor("x"), is("X"));
        assertThat(repository.getDescriptor("y"), is("Y"));
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
                Assert.fail("RepositoryException should not occur in other thread: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        t.start();
        latch.await();
        assertThat(otherThreadRepo.get(), notNullValue());
        assertThat("Repository mocks must differ across threads.", mainThreadRepo == otherThreadRepo.get(), is(false));
    }

    @Test
    public void stubLoginNullSession() throws RepositoryException {
        Repository repository = RepositoryMockUtils.mockRepository(RepositoryStubbingOperation.stubLogin(null));
        assertThat(repository.login(), nullValue());
        assertThat(repository.login("anyWorkspace"), nullValue());
    }

    @Test
    public void stubLoginSessionWithoutWorkspace() throws RepositoryException {
        Session session = mock(Session.class);
        Repository repository = RepositoryMockUtils.mockRepository(RepositoryStubbingOperation.stubLogin(session));
        assertThat(repository.login(), is(session));
        assertThat(session.getRepository(), is(repository));
        assertThat(repository.login("ws"), nullValue());
    }

    @Test
    public void stubLoginSessionWithWorkspace() throws RepositoryException {
        Session session = mock(Session.class);
        Workspace workspace = mock(Workspace.class);
        when(workspace.getName()).thenReturn("myWorkspace");
        when(session.getWorkspace()).thenReturn(workspace);
        Repository repository = RepositoryMockUtils.mockRepository(RepositoryStubbingOperation.stubLogin(session));
        assertThat(repository.login(), is(session));
        assertThat(session.getRepository(), is(repository));
        assertThat(repository.login("myWorkspace"), is(session));
    }

    @Test
    public void exceptionPropagation() {
        RepositoryMockUtils.cleanRepository();
        try {
            RepositoryMockUtils.mockRepository(new RepositoryStubbingOperation() {
                @Override
                public void of(final Repository repo) throws RepositoryException {
                    throw new RepositoryException("boom");
                }
            });
            Assert.fail("Expected RepositoryException not thrown.");
        } catch (RepositoryException e) {
            assertThat(e.getMessage(), is("boom"));
        }
        try {
            assertThat(RepositoryMockUtils.mockRepository(), notNullValue());
        } catch (RepositoryException e) {
            Assert.fail("Unexpected exception after failed stubbing: " + e.getMessage());
        }
    }
}
