
package de.ibmix.magkit.test.webapp;
/*-
 * #%L
 * magkit-test-webapp Magnolia Webapp that runs a TomcatTest
 * %%
 * Copyright (C) 2023 - 2024 IBM iX
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
 

import java.io.IOException;

import org.apache.catalina.LifecycleException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.ibmix.magkit.test.server.TomcatTest;

public class TomcatTestTest extends TomcatTest {
    
    @BeforeAll
    public static void startTomcat() throws LifecycleException, IOException {
        TomcatTest.startTomcat();
    }

    @AfterAll
    public static void shutdownTomcat() throws LifecycleException {
        TomcatTest.shutdownTomcat();
    }

    @Test
    public void testRestAssuredExample() throws Exception {
        super.testLocalSuperuserLogin();
    }
    
    
    @Test
    public void testMagnoliaConfigExample() throws Exception {
        super.testMagnoliaConfig();
    }
    
}
