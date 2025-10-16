package de.ibmix.magkit.test.cms.context;

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
import de.ibmix.magkit.test.ExceptionStubbingOperation;
import info.magnolia.cms.i18n.I18nContentSupport;
import org.mockito.Mockito;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static org.mockito.Mockito.when;

/**
 * Provides a set of reusable stubbing operations for a mocked {@link I18nContentSupport} instance.
 * <p>
 * Each static factory method returns an {@link I18nContentSupportStubbingOperation} that can be applied to an existing
 * mock (typically created via {@code I18nContentSupportMockUtils.mockI18nContentSupport(...)}). Operations encapsulate
 * the Mockito stubbing logic for specific aspects of internationalization support like current locale, fallback
 * locale resolution or URI translation.
 * <p>
 * Usage example:
 * <pre>{@code
 * I18nContentSupport support = I18nContentSupportMockUtils.mockI18nContentSupport(
 *     I18nContentSupportStubbingOperation.stubDefaultLocale(Locale.ENGLISH),
 *     I18nContentSupportStubbingOperation.stubFallbackLocale(Locale.GERMAN),
 *     I18nContentSupportStubbingOperation.stubToI18nUri("/content/path")
 * );
 * }
 * </pre>
 * Chaining multiple operations allows precise tailoring of the mock for a test scenario while keeping tests concise
 * and readable. Each locale related operation ensures the provided locale is contained in {@link I18nContentSupport#getLocales()}.
 * <p>
 * Thread-safety: These operations assume single-threaded test execution. Concurrent modifications of the same mock's
 * locale collection are not synchronized.
 * <p>
 * Error handling: All operations validate the target mock is not {@code null} (using a Hamcrest assertion) before
 * applying stubbing. Methods that may interact with JCR (through Magnolia API) declare {@link RepositoryException}.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-12-13
 */
public abstract class I18nContentSupportStubbingOperation implements ExceptionStubbingOperation<I18nContentSupport, RepositoryException> {

    /**
     * Stubs {@link I18nContentSupport#getLocale()} to return the provided locale and ensures it is present in the
     * collection returned by {@link I18nContentSupport#getLocales()}.
     *
     * @param locale the locale to be returned as the current locale; may be {@code null} if a test needs to simulate an unset state.
     * @return an operation that applies the stubbing when executed.
     */
    public static I18nContentSupportStubbingOperation stubLocale(final Locale locale) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) throws RepositoryException {
                Require.Argument.notNull(contentSupport, "contentSupport should not be null");
                when(contentSupport.getLocale()).thenReturn(locale);
                addToLocales(contentSupport, locale);
            }
        };
    }

    /**
     * Stubs {@link I18nContentSupport#getFallbackLocale()} to return the provided locale and adds it to the locale list if missing.
     *
     * @param locale the fallback locale; may be {@code null} to simulate absent fallback configuration.
     * @return an operation representing this fallback stubbing.
     */
    public static I18nContentSupportStubbingOperation stubFallbackLocale(final Locale locale) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) throws RepositoryException {
                Require.Argument.notNull(contentSupport, "contentSupport should not be null");
                when(contentSupport.getFallbackLocale()).thenReturn(locale);
                addToLocales(contentSupport, locale);
            }
        };
    }

    /**
     * Stubs {@link I18nContentSupport#getDefaultLocale()} to return the provided locale and ensures it is available in the locales collection.
     *
     * @param locale the default locale configured for the project under test; may be {@code null}.
     * @return the stubbing operation for default locale behavior.
     */
    public static I18nContentSupportStubbingOperation stubDefaultLocale(final Locale locale) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) throws RepositoryException {
                Require.Argument.notNull(contentSupport, "contentSupport should not be null");
                when(contentSupport.getDefaultLocale()).thenReturn(locale);
                addToLocales(contentSupport, locale);
            }
        };
    }

    /**
     * Stubs {@link I18nContentSupport#determineLocale()} to return the provided locale and adds it to the list of available locales.
     * <p>
     * This is useful when testing logic that relies on Magnolia's dynamic locale determination without invoking the actual resolution algorithm.
     *
     * @param locale the determined locale; may be {@code null} for edge case testing.
     * @return an operation configuring {@code determineLocale()} behavior.
     */
    public static I18nContentSupportStubbingOperation stubDetermineLocale(final Locale locale) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) throws RepositoryException {
                Require.Argument.notNull(contentSupport, "contentSupport should not be null");
                when(contentSupport.determineLocale()).thenReturn(locale);
                addToLocales(contentSupport, locale);
            }
        };
    }

    /**
     * Stubs {@link I18nContentSupport#getLocales()} to return exactly the provided ordered list of locales.
     * Replaces any previously configured locale collection.
     *
     * @param locales variable number of locales to expose; may be an empty array to simulate no configured locales.
     * @return operation replacing the locale collection stubbing.
     */
    public static I18nContentSupportStubbingOperation stubLocales(final Locale... locales) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) {
                Require.Argument.notNull(contentSupport, "contentSupport should not be null");
                when(contentSupport.getLocales()).thenReturn(new ArrayList<>(Arrays.asList(locales)));
            }
        };
    }

    /**
     * Stubs {@link I18nContentSupport#toI18NURI(String)} to always return the specified constant value, ignoring input arguments.
     * <p>
     * Useful for tests that do not depend on actual URI translation but require a stable, deterministic value.
     *
     * @param value the constant URI value to be returned; may be {@code null} to simulate failure/absence.
     * @return operation configuring URI translation behavior.
     */
    public static I18nContentSupportStubbingOperation stubToI18nUri(final String value) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) {
                Require.Argument.notNull(contentSupport, "contentSupport should not be null");
                when(contentSupport.toI18NURI(Mockito.anyString())).thenReturn(value);
            }
        };
    }

    /**
     * Ensures that the given locale is present in the collection returned by {@link I18nContentSupport#getLocales()}.
     * <p>
     * Behavior:
     * <ul>
     *   <li>If locales is {@code null}, delegates to {@link #stubLocales(Locale...)} with the provided locale.</li>
     *   <li>If the locale is not contained yet, it is appended and the collection is re-stubbed to reflect the new state.</li>
     *   <li>If the locale is {@code null}, no modification occurs.</li>
     * </ul>
     *
     * @param contentSupport the mock to update.
     * @param locale         the locale to ensure presence of; may be {@code null}.
     * @throws RepositoryException if stubbing via other operations triggers repository related exceptions.
     */
    private static void addToLocales(final I18nContentSupport contentSupport, final Locale locale) throws RepositoryException {
        Collection<Locale> locales = contentSupport.getLocales();
        if (locales == null) {
            stubLocales(locale).of(contentSupport);
        } else if (locale != null && !locales.contains(locale)) {
            locales.add(locale);
            stubLocales(locales.toArray(new Locale[0])).of(contentSupport);
        }
    }
}
