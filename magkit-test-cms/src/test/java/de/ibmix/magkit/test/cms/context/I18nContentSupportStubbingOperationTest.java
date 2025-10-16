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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubDefaultLocale;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubDetermineLocale;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubFallbackLocale;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubLocale;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubToI18nUri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testing I18nContentSupportStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-07-25
 */
public class I18nContentSupportStubbingOperationTest {

    private I18nContentSupport _i18nContentSupport;

    @BeforeEach
    public void setUp() throws Exception {
        I18nContentSupportMockUtils.clearComponentProvider();
        _i18nContentSupport = I18nContentSupportMockUtils.mockI18nContentSupport();
    }

    @Test
    public void testStubLocale() throws Exception {
        assertNull(_i18nContentSupport.getLocale());
        assertEquals(0, _i18nContentSupport.getLocales().size());

        stubLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertEquals(Locale.JAPAN, _i18nContentSupport.getLocale());
        assertEquals(1, _i18nContentSupport.getLocales().size());

        stubLocale(Locale.GERMAN).of(_i18nContentSupport);
        assertEquals(2, _i18nContentSupport.getLocales().size());

        stubLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertEquals(2, _i18nContentSupport.getLocales().size());
    }

    @Test
    public void testStubDefaultLocale() throws Exception {
        assertNull(_i18nContentSupport.getDefaultLocale());
        assertEquals(0, _i18nContentSupport.getLocales().size());

        stubDefaultLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertEquals(Locale.JAPAN, _i18nContentSupport.getDefaultLocale());
        assertEquals(1, _i18nContentSupport.getLocales().size());

        stubDefaultLocale(Locale.GERMAN).of(_i18nContentSupport);
        assertEquals(2, _i18nContentSupport.getLocales().size());

        stubDefaultLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertEquals(2, _i18nContentSupport.getLocales().size());
    }

    @Test
    public void testStubFallbackLocale() throws Exception {
        assertNull(_i18nContentSupport.getFallbackLocale());
        assertEquals(0, _i18nContentSupport.getLocales().size());

        stubFallbackLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertEquals(Locale.JAPAN, _i18nContentSupport.getFallbackLocale());
        assertEquals(1, _i18nContentSupport.getLocales().size());

        stubFallbackLocale(Locale.GERMAN).of(_i18nContentSupport);
        assertEquals(2, _i18nContentSupport.getLocales().size());

        stubFallbackLocale(Locale.JAPAN).of(_i18nContentSupport);
        assertEquals(2, _i18nContentSupport.getLocales().size());
    }

    @Test
    public void testStubbToI18nUri() throws Exception {
        assertEquals("test.aperto.de", _i18nContentSupport.toI18NURI("test.aperto.de"));
        assertEquals("any string", _i18nContentSupport.toI18NURI("any string"));

        stubToI18nUri("test.aperto.jp").of(_i18nContentSupport);
        assertEquals("test.aperto.jp", _i18nContentSupport.toI18NURI("test.aperto.de"));
        assertEquals("test.aperto.jp", _i18nContentSupport.toI18NURI("any string"));
    }

    @Test
    public void testStubDetermineLocale() throws RepositoryException {
        assertNull(_i18nContentSupport.determineLocale());
        assertEquals(0, _i18nContentSupport.getLocales().size());

        stubDetermineLocale(Locale.ITALIAN).of(_i18nContentSupport);
        assertEquals(Locale.ITALIAN, _i18nContentSupport.determineLocale());
        assertEquals(1, _i18nContentSupport.getLocales().size());
    }
}
