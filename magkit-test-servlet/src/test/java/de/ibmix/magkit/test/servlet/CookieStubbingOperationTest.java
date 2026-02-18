package de.ibmix.magkit.test.servlet;

/*-
 * #%L
 * magkit-test-servlet Magnolia Module
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

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.ibmix.magkit.test.servlet.CookieStubbingOperation.stubComment;
import static de.ibmix.magkit.test.servlet.CookieStubbingOperation.stubDomain;
import static de.ibmix.magkit.test.servlet.CookieStubbingOperation.stubMaxAge;
import static de.ibmix.magkit.test.servlet.CookieStubbingOperation.stubPath;
import static de.ibmix.magkit.test.servlet.CookieStubbingOperation.stubSecure;
import static de.ibmix.magkit.test.servlet.CookieStubbingOperation.stubVersion;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockCookie;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing CookieStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 25.02.14
 */
public class CookieStubbingOperationTest {
    private Cookie _cookie;

    @BeforeEach
    public void setUp() {
        _cookie = mockCookie("name", "value");
    }

    @Test
    public void testStubDomain() {
        assertNull(_cookie.getDomain());
        stubDomain("test.domain").of(_cookie);
        assertEquals("test.domain", _cookie.getDomain());
    }

    @Test
    public void testStubComment() {
        assertNull(_cookie.getComment());
        stubComment("test").of(_cookie);
        assertEquals("test", _cookie.getComment());
    }

    @Test
    public void testStubMaxAge() {
        assertEquals(0, _cookie.getMaxAge());
        stubMaxAge(12).of(_cookie);
        assertEquals(12, _cookie.getMaxAge());
    }

    @Test
    public void testStubPath() {
        assertNull(_cookie.getPath());
        stubPath("test").of(_cookie);
        assertEquals("test", _cookie.getPath());
    }

    @Test
    public void testStubSecure() {
        assertFalse(_cookie.getSecure());
        stubSecure(true).of(_cookie);
        assertTrue(_cookie.getSecure());
    }

    @Test
    public void testStubVersion() {
        assertEquals(0, _cookie.getVersion());
        stubVersion(12).of(_cookie);
        assertEquals(12, _cookie.getVersion());
    }
}
