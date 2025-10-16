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

import de.ibmix.magkit.assertions.Require;
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

import static org.mockito.Mockito.doReturn;

/**
 * Abstract utility class providing factory methods for creating stubbing operations on JCR ObservationManager mocks.
 * <p>
 * This class implements the Stubbing Operation pattern to provide a fluent and type-safe way to configure
 * Mockito mocks of {@link javax.jcr.observation.ObservationManager} instances. Each factory method returns
 * a configured stubbing operation that can be applied to an ObservationManager mock to define specific
 * behavior for observation-related functionality.
 * </p>
 * <p>
 * The ObservationManager is a critical component in JCR that handles event registration, listener management,
 * and event journal access. This utility class simplifies the process of mocking these complex interactions
 * for unit testing purposes.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Type-safe stubbing operations for ObservationManager mocks</li>
 *   <li>Support for event listener registration and management</li>
 *   <li>Event journal configuration with flexible parameter combinations</li>
 *   <li>Fluent API design for readable test code</li>
 *   <li>Integration with existing EventListener and EventJournal mocks</li>
 * </ul>
 *
 * <h3>Usage Patterns:</h3>
 * <pre>{@code
 * // Configure ObservationManager with registered event listeners
 * EventListener listener1 = mock(EventListener.class);
 * EventListener listener2 = mock(EventListener.class);
 * ObservationManager manager = ObservationMockUtils.mockObservationManager(
 *     ObservationManagerStubbingOperation.stubRegisteredEventListeners(listener1, listener2)
 * );
 *
 * // Configure event journal access
 * EventJournal journal = mock(EventJournal.class);
 * ObservationManager manager = ObservationMockUtils.mockObservationManager(
 *     ObservationManagerStubbingOperation.stubEventJournal(journal),
 *     ObservationManagerStubbingOperation.stubEventJournal(journal, Event.NODE_ADDED, "/content", true, null, null)
 * );
 * }</pre>
 *
 * <h3>Thread Safety:</h3>
 * <p>
 * This class is thread-safe as it only contains static factory methods and does not maintain any shared state.
 * The returned stubbing operations are also thread-safe as they only encapsulate configuration data.
 * However, the ObservationManager mocks they configure may not be thread-safe depending on usage.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-12-06
 * @see javax.jcr.observation.ObservationManager
 * @see javax.jcr.observation.EventListener
 * @see javax.jcr.observation.EventJournal
 * @see de.ibmix.magkit.test.ExceptionStubbingOperation
 */
public abstract class ObservationManagerStubbingOperation implements ExceptionStubbingOperation<ObservationManager, RepositoryException> {

    /**
     * Creates a stubbing operation that configures registered event listeners for an ObservationManager mock.
     * <p>
     * This factory method creates a stubbing operation that adds the specified event listeners to the
     * collection of registered listeners returned by {@link ObservationManager#getRegisteredEventListeners()}.
     * If the mock already has registered listeners, the new listeners will be added to the existing collection.
     * If no listeners are currently registered, a new collection will be created containing only the
     * specified listeners.
     * </p>
     *
     * <h4>Behavior Details:</h4>
     * <ul>
     *   <li>Preserves any existing registered event listeners in the mock</li>
     *   <li>Adds the new listeners to the end of the collection in the order provided</li>
     *   <li>Returns an {@link EventListenerIterator} that can iterate over all registered listeners</li>
     *   <li>Handles null and empty listener arrays gracefully</li>
     * </ul>
     *
     * <h4>Usage Examples:</h4>
     * <pre>{@code
     * // Register a single event listener
     * EventListener listener = mock(EventListener.class);
     * ObservationManager manager = ObservationMockUtils.mockObservationManager(
     *     stubRegisteredEventListeners(listener)
     * );
     *
     * // Register multiple event listeners
     * EventListener listener1 = mock(EventListener.class);
     * EventListener listener2 = mock(EventListener.class);
     * ObservationManager manager = ObservationMockUtils.mockObservationManager(
     *     stubRegisteredEventListeners(listener1, listener2)
     * );
     *
     * // Register no listeners (creates empty collection)
     * ObservationManager manager = ObservationMockUtils.mockObservationManager(
     *     stubRegisteredEventListeners()
     * );
     * }</pre>
     *
     * @param listeners varargs array of EventListener instances to register with the ObservationManager.
     *                  Can be empty to create an empty listener collection. Individual listeners can be null,
     *                  but this is generally not recommended as it may cause issues in consuming code.
     * @return a new ObservationManagerStubbingOperation that will configure the registered event listeners
     *         when applied to an ObservationManager mock
     *
     * @see javax.jcr.observation.ObservationManager#getRegisteredEventListeners()
     * @see javax.jcr.observation.EventListener
     * @see org.apache.jackrabbit.commons.iterator.EventListenerIteratorAdapter
     */
    public static ObservationManagerStubbingOperation stubRegisteredEventListeners(final EventListener... listeners) {
        return new ObservationManagerStubbingOperation() {
            @Override
            public void of(ObservationManager mock) throws RepositoryException {
                Require.Argument.notNull(mock, "observationManager must not be null");
                EventListenerIterator listenerIterator = mock.getRegisteredEventListeners();
                List<EventListener> listenerList = new ArrayList<>();
                if (listenerIterator != null) {
                    @SuppressWarnings("unchecked")
                    List<EventListener> existingListeners = (List<EventListener>) IteratorUtils.toList(listenerIterator);
                    listenerList.addAll(existingListeners);
                }
                listenerList.addAll(Arrays.asList(listeners));
                EventListenerIteratorAdapter iteratorAdapter = new EventListenerIteratorAdapter(listenerList);
                doReturn(iteratorAdapter).when(mock).getRegisteredEventListeners();
            }
        };
    }

    /**
     * Creates a stubbing operation that configures the default event journal for an ObservationManager mock.
     * <p>
     * This factory method creates a stubbing operation that configures the ObservationManager mock to return
     * the specified EventJournal when {@link ObservationManager#getEventJournal()} is called without parameters.
     * This is typically used to provide access to a complete event history without any filtering criteria.
     * </p>
     *
     * <h4>Behavior Details:</h4>
     * <ul>
     *   <li>Configures the parameterless {@code getEventJournal()} method</li>
     *   <li>Returns the same EventJournal instance on every call</li>
     *   <li>Does not affect the behavior of parameterized {@code getEventJournal()} method calls</li>
     *   <li>Can be combined with parameterized event journal stubbing for comprehensive coverage</li>
     * </ul>
     *
     * <h4>Usage Examples:</h4>
     * <pre>{@code
     * // Configure default event journal access
     * EventJournal journal = mock(EventJournal.class);
     * ObservationManager manager = ObservationMockUtils.mockObservationManager(
     *     stubEventJournal(journal)
     * );
     *
     * // Access the configured journal
     * EventJournal retrievedJournal = manager.getEventJournal();
     * assertThat(retrievedJournal, is(journal));
     * }</pre>
     *
     * @param journal the EventJournal instance that should be returned by the mock's getEventJournal() method.
     *                Should not be null as this would likely cause issues in consuming code that expects
     *                a valid journal instance.
     * @return a new ObservationManagerStubbingOperation that will configure the default event journal access
     *         when applied to an ObservationManager mock
     *
     * @see javax.jcr.observation.ObservationManager#getEventJournal()
     * @see javax.jcr.observation.EventJournal
     */
    public static ObservationManagerStubbingOperation stubEventJournal(final EventJournal journal) {
        return new ObservationManagerStubbingOperation() {
            @Override
            public void of(ObservationManager mock) throws RepositoryException {
                Require.Argument.notNull(mock, "observationManager must not be null");
                doReturn(journal).when(mock).getEventJournal();
            }
        };
    }

    /**
     * Creates a stubbing operation that configures filtered event journal access for an ObservationManager mock.
     * <p>
     * This factory method creates a stubbing operation that configures the ObservationManager mock to return
     * the specified EventJournal when {@link ObservationManager#getEventJournal(int, String, boolean, String[], String[])}
     * is called with the exact parameter combination provided. This allows for fine-grained control over
     * event journal access based on specific filtering criteria.
     * </p>
     *
     * <h4>Parameter Details:</h4>
     * <ul>
     *   <li><strong>eventTypes:</strong> Bitmask of event types to include (e.g., Event.NODE_ADDED | Event.NODE_REMOVED)</li>
     *   <li><strong>absPath:</strong> Absolute path filter - only events under this path will be included</li>
     *   <li><strong>isDeep:</strong> Whether to include events from descendant paths (deep observation)</li>
     *   <li><strong>uuid:</strong> Array of node UUIDs to filter by, or null for no UUID filtering</li>
     *   <li><strong>nodeTypeName:</strong> Array of node type names to filter by, or null for no type filtering</li>
     * </ul>
     *
     * <h4>Usage Examples:</h4>
     * <pre>{@code
     * // Configure filtered event journal for node additions under /content
     * EventJournal journal = mock(EventJournal.class);
     * ObservationManager manager = ObservationMockUtils.mockObservationManager(
     *     stubEventJournal(journal, Event.NODE_ADDED, "/content", true, null, null)
     * );
     *
     * // Configure event journal for specific node types
     * String[] nodeTypes = {"nt:file", "nt:folder"};
     * ObservationManager manager = ObservationMockUtils.mockObservationManager(
     *     stubEventJournal(journal, Event.NODE_ADDED | Event.NODE_REMOVED, "/", false, null, nodeTypes)
     * );
     *
     * // Access the configured journal with matching parameters
     * EventJournal retrievedJournal = manager.getEventJournal(Event.NODE_ADDED, "/content", true, null, null);
     * assertThat(retrievedJournal, is(journal));
     * }</pre>
     *
     * @param journal the EventJournal instance that should be returned when the exact parameter combination is used.
     *                Should not be null as this would likely cause issues in consuming code.
     * @param eventTypes bitmask indicating the event types to be included in the journal (e.g., Event.NODE_ADDED).
     *                   Must be a valid combination of event type constants from {@link javax.jcr.observation.Event}.
     * @param absPath absolute path specifying the location in the repository tree where events should be observed.
     *               Must be a valid absolute path starting with "/" or null for repository root.
     * @param isDeep flag indicating whether events from descendant paths should be included (true) or only
     *              direct child events (false).
     * @param uuid array of node UUIDs for filtering events to specific nodes, or null to disable UUID filtering.
     *            Each UUID should be a valid node identifier.
     * @param nodeTypeName array of node type names for filtering events to specific node types, or null to
     *                     disable node type filtering. Each name should be a valid node type identifier.
     * @return a new ObservationManagerStubbingOperation that will configure filtered event journal access
     *         when applied to an ObservationManager mock
     *
     * @see javax.jcr.observation.ObservationManager#getEventJournal(int, String, boolean, String[], String[])
     * @see javax.jcr.observation.EventJournal
     * @see javax.jcr.observation.Event
     */
    public static ObservationManagerStubbingOperation stubEventJournal(final EventJournal journal, int eventTypes, final String absPath, boolean isDeep, final String[] uuid, final String[] nodeTypeName) {
        return new ObservationManagerStubbingOperation() {
            @Override
            public void of(ObservationManager mock) throws RepositoryException {
                Require.Argument.notNull(mock, "observationManager must not be null");
                doReturn(journal).when(mock).getEventJournal(eventTypes, absPath, isDeep, uuid, nodeTypeName);
            }
        };
    }

    /**
     * Private constructor to prevent direct instantiation of this abstract utility class.
     * <p>
     * This class follows the abstract factory pattern and should only be used through its static
     * factory methods. Direct instantiation is prevented to enforce proper usage patterns.
     * </p>
     */
    private ObservationManagerStubbingOperation() {
        // Abstract utility class - no direct instantiation allowed
    }
}
