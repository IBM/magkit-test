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

import de.ibmix.magkit.test.ExceptionStubbingOperation;
import info.magnolia.cms.i18n.I18nContentSupport;
import org.mockito.Mockito;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides operations for stubbing of an I18nContentSupport mock.
 *
 * @author wolf.bubenik
 * @since 13.12.2010
 */
public abstract class I18nContentSupportStubbingOperation implements ExceptionStubbingOperation<I18nContentSupport, RepositoryException> {

    public static I18nContentSupportStubbingOperation stubProperty(final Node node, final String propertyName, final Property value) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) throws RepositoryException {
                assertThat(contentSupport, notNullValue());
                when(contentSupport.getProperty(node, propertyName)).thenReturn(value);
            }
        };
    }

    public static I18nContentSupportStubbingOperation stubProperty(final Node node, final String propertyName, final Locale locale, final Property value) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) throws RepositoryException {
                assertThat(contentSupport, notNullValue());
                when(contentSupport.getProperty(node, propertyName, locale)).thenReturn(value);
            }
        };
    }

    public static I18nContentSupportStubbingOperation stubLocale(final Locale locale) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) throws RepositoryException {
                assertThat(contentSupport, notNullValue());
                when(contentSupport.getLocale()).thenReturn(locale);
                addToLocales(contentSupport, locale);
            }
        };
    }

    public static I18nContentSupportStubbingOperation stubFallbackLocale(final Locale locale) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) throws RepositoryException {
                assertThat(contentSupport, notNullValue());
                when(contentSupport.getFallbackLocale()).thenReturn(locale);
                addToLocales(contentSupport, locale);
            }
        };
    }

    public static I18nContentSupportStubbingOperation stubDefaultLocale(final Locale locale) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) throws RepositoryException {
                assertThat(contentSupport, notNullValue());
                when(contentSupport.getDefaultLocale()).thenReturn(locale);
                addToLocales(contentSupport, locale);
            }
        };
    }

    public static I18nContentSupportStubbingOperation stubDetermineLocale(final Locale locale) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) {
                assertThat(contentSupport, notNullValue());
                when(contentSupport.determineLocale()).thenReturn(locale);
            }
        };
    }

    public static I18nContentSupportStubbingOperation stubbLocales(final Locale... locales) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) {
                assertThat(contentSupport, notNullValue());
                when(contentSupport.getLocales()).thenReturn(new ArrayList<>(Arrays.asList(locales)));
            }
        };
    }

    public static I18nContentSupportStubbingOperation stubbToI18nUri(final String value) {
        return new I18nContentSupportStubbingOperation() {

            public void of(I18nContentSupport contentSupport) {
                assertThat(contentSupport, notNullValue());
                when(contentSupport.toI18NURI(Mockito.anyString())).thenReturn(value);
            }
        };
    }

    private static void addToLocales(final I18nContentSupport contentSupport, final Locale locale) throws RepositoryException {
        Collection<Locale> locales = contentSupport.getLocales();
        if (locales == null) {
            stubbLocales(locale).of(contentSupport);
        } else if (!locales.contains(locale)) {
            locales.add(locale);
            stubbLocales(locales.toArray(new Locale[0])).of(contentSupport);
        }
    }
}
