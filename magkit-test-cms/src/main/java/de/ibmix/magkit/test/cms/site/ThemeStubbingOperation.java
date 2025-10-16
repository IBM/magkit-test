package de.ibmix.magkit.test.cms.site;

/*-
 * #%L
 * magkit-test-cms Magnolia Module
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

import de.ibmix.magkit.assertations.Require;
import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.imaging.ImagingSupport;
import info.magnolia.module.site.CssResourceDefinition;
import info.magnolia.module.site.ResourceDefinition;
import info.magnolia.module.site.theme.Theme;

import java.util.List;

import static de.ibmix.magkit.test.cms.site.ThemeMockUtils.mockCssFile;
import static de.ibmix.magkit.test.cms.site.ThemeMockUtils.mockResource;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.Mockito.doReturn;

/**
 * Provides factory methods creating {@link ThemeStubbingOperation} instances used to enrich mocked {@link Theme} objects
 * with additional resource definitions like CSS and JavaScript files or imaging support.
 * <p>
 * Each static method returns a {@link StubbingOperation} that can be applied to an existing Mockito mock of {@link Theme}.
 * These operations mutate the mock by adding the provided resource definitions to the lists returned by
 * {@link Theme#getCssFiles()} and {@link Theme#getJsFiles()} or by stubbing {@link Theme#getImaging()}.
 * <p>
 * Rationale: Basic Mockito mocks of {@link Theme} typically return a fresh (empty) list for resource getters on each invocation.
 * The operations returned here capture the mutated list and re-stub the corresponding getter so added definitions are retained.
 * <p>
 * Typical usage:
 * <pre>{@code
 * Theme theme = mock(Theme.class, RETURNS_DEEP_STUBS);
 * ThemeMockUtils.mockTheme(theme, ThemeStubbingOperation.stubCssFiles("/foo.css", "/bar.css"));
 * // OR apply directly:
 * ThemeStubbingOperation.stubJsFiles("/app.js").of(theme);
 * }</pre>
 * Null handling: Vararg methods ignore a {@code null} array (no-op). Individual {@code null} elements are not filtered.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-06-05
 */
public abstract class ThemeStubbingOperation implements StubbingOperation<Theme> {

    /**
     * Creates a stubbing operation that adds CSS resources specified only by their link/path strings.
     * For every provided link a {@link CssResourceDefinition} is created via {@link ThemeMockUtils#mockCssFile(String, String, String)}
     * using empty media and conditional attributes.
     * <p>
     * Behaviour:
     * <ul>
     *   <li>If {@code links} is {@code null}, the operation is a no-op.</li>
     *   <li>If {@code links} is empty, no files are added.</li>
     *   <li>Returned operation delegates to {@link #stubCssFiles(CssResourceDefinition...)} after creating definitions.</li>
     * </ul>
     *
     * <pre>{@code
     * ThemeStubbingOperation op = ThemeStubbingOperation.stubCssFiles("/style.css", "/theme.css");
     * op.of(themeMock);
     * }</pre>
     *
     * @param links one or more CSS resource links (may be {@code null} or empty)
     * @return stubbing operation adding the specified CSS resources
     */
    public static ThemeStubbingOperation stubCssFiles(final String... links) {
        return new ThemeStubbingOperation() {

            @Override
            public void of(final Theme theme) {
                Require.Argument.notNull(theme, "theme should not be null");
                if (links != null) {
                    CssResourceDefinition[] files = new CssResourceDefinition[links.length];
                    for (int i = 0; i < links.length; i++) {
                        files[i] = mockCssFile(links[i], EMPTY, EMPTY);
                    }
                    stubCssFiles(files).of(theme);
                }
            }
        };
    }

    /**
     * Creates a stubbing operation that appends provided {@link CssResourceDefinition} instances to a mocked {@link Theme}.
     * The underlying list returned by {@link Theme#getCssFiles()} is mutated and then re-stubbed to ensure persistence across calls
     * (Mockito default behaviour would otherwise return a new empty list for subsequent invocations depending on configuration).
     * <p>
     * No defensive copies are made; the caller should avoid external modification of the passed definitions after stubbing.
     *
     * @param values zero or more CSS resource definitions to add (may contain {@code null} entries which are added as-is)
     * @return stubbing operation adding the given definitions
     */
    public static ThemeStubbingOperation stubCssFiles(final CssResourceDefinition... values) {
        return new ThemeStubbingOperation() {

            @Override
            public void of(final Theme theme) {
                Require.Argument.notNull(theme, "theme should not be null");
                List<CssResourceDefinition> files = theme.getCssFiles();
                files.addAll(asList(values));
                // Simple mockito mocks return a new empty LinkedList per default.
                // We have to stub it again to not lose the values.
                doReturn(files).when(theme).getCssFiles();
            }
        };
    }

    /**
     * Creates a stubbing operation that appends provided JavaScript {@link ResourceDefinition}s to a mocked {@link Theme}.
     * The list instance obtained from {@link Theme#getJsFiles()} is updated and its getter is re-stubbed to keep the augmented list.
     *
     * @param values zero or more resource definitions describing JS files
     * @return stubbing operation adding the provided JS resource definitions
     */
    public static ThemeStubbingOperation stubJsFiles(final ResourceDefinition... values) {
        return new ThemeStubbingOperation() {

            @Override
            public void of(final Theme theme) {
                Require.Argument.notNull(theme, "theme should not be null");
                List<ResourceDefinition> files = theme.getJsFiles();
                files.addAll(asList(values));
                // Simple mockito mocks return a new empty LinkedList per default.
                // We have to stub it again to not lose the values.
                doReturn(files).when(theme).getJsFiles();
            }
        };
    }

    /**
     * Creates a stubbing operation that adds JavaScript resources by link/path. For each path a {@link ResourceDefinition} is
     * created via {@link ThemeMockUtils#mockResource(String, String)} with an empty type value, then delegated to
     * {@link #stubJsFiles(ResourceDefinition...)}.
     *
     * @param links JavaScript resource links (may be {@code null} or empty)
     * @return stubbing operation adding the specified JS resources
     */
    public static ThemeStubbingOperation stubJsFiles(final String... links) {
        return new ThemeStubbingOperation() {

            @Override
            public void of(final Theme theme) {
                Require.Argument.notNull(theme, "theme should not be null");
                if (links != null) {
                    ResourceDefinition[] files = new ResourceDefinition[links.length];
                    for (int i = 0; i < links.length; i++) {
                        files[i] = mockResource(links[i], EMPTY);
                    }
                    stubJsFiles(files).of(theme);
                }
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the {@link ImagingSupport} instance returned by {@link Theme#getImaging()}.
     * Existing behaviour is replaced unconditionally.
     *
     * @param imagingSupport imaging support mock or instance to be returned by the theme
     * @return stubbing operation stubbing {@link Theme#getImaging()}
     */
    public static ThemeStubbingOperation stubImagingSupport(ImagingSupport imagingSupport) {
        return new ThemeStubbingOperation() {
            @Override
            public void of(Theme theme) {
                Require.Argument.notNull(theme, "theme should not be null");
                doReturn(imagingSupport).when(theme).getImaging();
            }
        };
    }
}
