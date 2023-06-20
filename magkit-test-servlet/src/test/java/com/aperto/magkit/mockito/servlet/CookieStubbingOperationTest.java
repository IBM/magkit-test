package com.aperto.magkit.mockito.servlet;

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

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.Cookie;

import static com.aperto.magkit.mockito.servlet.CookieStubbingOperation.stubComment;
import static com.aperto.magkit.mockito.servlet.CookieStubbingOperation.stubDomain;
import static com.aperto.magkit.mockito.servlet.CookieStubbingOperation.stubMaxAge;
import static com.aperto.magkit.mockito.servlet.CookieStubbingOperation.stubPath;
import static com.aperto.magkit.mockito.servlet.CookieStubbingOperation.stubSecure;
import static com.aperto.magkit.mockito.servlet.CookieStubbingOperation.stubVersion;
import static com.aperto.magkit.mockito.servlet.ServletMockUtils.mockCookie;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Testing CookieStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 25.02.14
 */
public class CookieStubbingOperationTest {
    private Cookie _cookie;

    @Before
    public void setUp() {
        _cookie = mockCookie("name", "value");
    }

    @Test
    public void testStubDomain() {
        assertThat(_cookie.getDomain(), nullValue());
        stubDomain("test.domain").of(_cookie);
        assertThat(_cookie.getDomain(), is("test.domain"));
    }

    @Test
    public void testStubComment() {
        assertThat(_cookie.getComment(), nullValue());
        stubComment("test").of(_cookie);
        assertThat(_cookie.getComment(), is("test"));
    }

    @Test
    public void testStubMaxAge() {
        assertThat(_cookie.getMaxAge(), is(0));
        stubMaxAge(12).of(_cookie);
        assertThat(_cookie.getMaxAge(), is(12));
    }

    @Test
    public void testStubPath() {
        assertThat(_cookie.getPath(), nullValue());
        stubPath("test").of(_cookie);
        assertThat(_cookie.getPath(), is("test"));
    }

    @Test
    public void testStubSecure() {
        assertThat(_cookie.getSecure(), is(false));
        stubSecure(true).of(_cookie);
        assertThat(_cookie.getSecure(), is(true));
    }

    @Test
    public void testStubVersion() {
        assertThat(_cookie.getVersion(), is(0));
        stubVersion(12).of(_cookie);
        assertThat(_cookie.getVersion(), is(12));
    }
}
