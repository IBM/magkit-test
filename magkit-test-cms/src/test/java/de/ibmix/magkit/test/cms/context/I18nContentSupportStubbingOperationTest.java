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

import info.magnolia.cms.i18n.I18nContentSupport;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubDefaultLocale;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubFallbackLocale;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubLocale;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubbToI18nUri;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Testing I18nContentSupportStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 25.07.12
 */
public class I18nContentSupportStubbingOperationTest {

    private I18nContentSupport _i18nContentSupport;

    @Before
    public void setUp() throws Exception {
        I18nContentSupportMockUtils.clearComponentProvider();
        _i18nContentSupport = I18nContentSupportMockUtils.mockI18nContentSupport();
    }

    @Test
    public void testStubLocale() throws Exception {
        assertThat(_i18nContentSupport.getLocale(), nullValue());
        assertThat(_i18nContentSupport.getLocales().size(), is(0));

        stubLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertThat(_i18nContentSupport.getLocale(), is(Locale.JAPAN));
        assertThat(_i18nContentSupport.getLocales().size(), is(1));

        stubLocale(Locale.GERMAN).of(_i18nContentSupport);
        assertThat(_i18nContentSupport.getLocales().size(), is(2));

        stubLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertThat(_i18nContentSupport.getLocales().size(), is(2));
    }

    @Test
    public void testStubDefaultLocale() throws Exception {
        assertThat(_i18nContentSupport.getDefaultLocale(), nullValue());
        assertThat(_i18nContentSupport.getLocales().size(), is(0));

        stubDefaultLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertThat(_i18nContentSupport.getDefaultLocale(), is(Locale.JAPAN));
        assertThat(_i18nContentSupport.getLocales().size(), is(1));

        stubDefaultLocale(Locale.GERMAN).of(_i18nContentSupport);
        assertThat(_i18nContentSupport.getLocales().size(), is(2));

        stubDefaultLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertThat(_i18nContentSupport.getLocales().size(), is(2));
    }

    @Test
    public void testStubFallbackLocale() throws Exception {
        assertThat(_i18nContentSupport.getFallbackLocale(), nullValue());
        assertThat(_i18nContentSupport.getLocales().size(), is(0));

        stubFallbackLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertThat(_i18nContentSupport.getFallbackLocale(), is(Locale.JAPAN));
        assertThat(_i18nContentSupport.getLocales().size(), is(1));

        stubFallbackLocale(Locale.GERMAN).of(_i18nContentSupport);
        assertThat(_i18nContentSupport.getLocales().size(), is(2));

        stubFallbackLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertThat(_i18nContentSupport.getLocales().size(), is(2));
    }

    @Test
    public void testStubbToI18nUri() throws Exception {
        assertThat(_i18nContentSupport.toI18NURI("test.aperto.de"), is("test.aperto.de"));
        assertThat(_i18nContentSupport.toI18NURI("any string"), is("any string"));

        stubbToI18nUri("test.aperto.jp").of(_i18nContentSupport);
        assertThat(_i18nContentSupport.toI18NURI("test.aperto.de"), is("test.aperto.jp"));
        assertThat(_i18nContentSupport.toI18NURI("any string"), is("test.aperto.jp"));
    }
}
