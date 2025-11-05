package de.ibmix.magkit.test.cms.templating;

/*-
 * #%L
 * magkit-test-cms Magnolia Module
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

import info.magnolia.rendering.template.TemplateDefinition;

/**
 * Generic stubbing operation for Magnolia template related definitions.
 *
 * @param <T> concrete template definition type
 * @author wolf.bubenik@ibmix.de
 * @since 2016-04-14
 */
public interface StubbingOperation<T extends TemplateDefinition> {

    /**
     * Apply this stubbing operation to the given definition instance.
     *
     * @param definition definition instance (may be a Mockito mock)
     */
    void of(T definition);
}
