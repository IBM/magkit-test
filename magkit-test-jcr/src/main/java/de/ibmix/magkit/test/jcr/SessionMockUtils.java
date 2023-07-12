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

import org.mockito.stubbing.Answer;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static de.ibmix.magkit.test.jcr.RepositoryStubbingOperation.stubLogin;
import static de.ibmix.magkit.test.jcr.SessionStubbingOperation.stubRootNode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class for creating mockito mocks of javax.jcr.Session.
 *
 * @author wolf.bubenik
 * @since 03.08.12
 */
public final class SessionMockUtils {

    private SessionMockUtils() {
    }

    public static final Answer<Property> PROPERTY_ANSWER = invocation -> {
        Session s = (Session) invocation.getMock();
        String path = (String) invocation.getArguments()[0];
        return s == null ? null : (Property) s.getItem(path);
    };

    public static Session mockSession(String workspace, SessionStubbingOperation... stubbings) throws RepositoryException {
        Repository repository = RepositoryMockUtils.mockRepository();
        Session result = repository.login(workspace);
        if (result == null) {
            result = mockPlainSession();
            WorkspaceMockUtils.mockWorkspace(workspace, WorkspaceStubbingOperation.stubSession(result));
            stubLogin(result).of(repository);
            doAnswer(PROPERTY_ANSWER).when(result).getProperty(anyString());
        }
        for (SessionStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    public static Session mockPlainSession() throws RepositoryException {
        Session result = mock(Session.class);
        Node root = NodeMockUtils.mockPlainNode("/");
        when(root.getName()).thenReturn("");
        stubRootNode(root).of(result);
        return result;
    }

    public static void cleanSession() {
        RepositoryMockUtils.cleanRepository();
    }
}
