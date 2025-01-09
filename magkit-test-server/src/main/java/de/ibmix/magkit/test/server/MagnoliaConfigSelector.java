package de.ibmix.magkit.test.server;

/*-
 * #%L
 * magkit-test-server Maven Module
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tells {@link MagnoliaTomcatExtension} which configuration selectors to use,
 * see https://git.magnolia-cms.com/projects/PLATFORM/repos/main/browse/magnolia-core/src/main/java/info/magnolia/init/DefaultMagnoliaPropertiesResolver.java#107 .
 * Empty values result in the corresponding system property *not* to be set.
 * @author joerg.frantzius
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MagnoliaConfigSelector {

    /**
     * The MAGNOLIA_PROFILE to use.
     */
    String magnoliaProfile() default "";

    /**
     * The MAGNOLIA_INSTANCE_TYPE to use.
     */
    String magnoliaInstanceType() default "";

    /**
     * The MAGNOLIA_STAGE to use.
     */
    String magnoliaStage() default "";
}
