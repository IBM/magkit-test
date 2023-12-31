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

import javax.servlet.http.Cookie;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for CookieStubbingOperation.
 * Stubbing operations to be used as parameters in ServletMockUtils.mockCookie(...)
 * or for stubbing the behaviour of an existing mock: CookieStubbingOperation.stubDomain("domain").of(mock).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2014-02-11
 */
public abstract class CookieStubbingOperation {
    public abstract void of(Cookie context);

    public static CookieStubbingOperation stubDomain(final String value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getDomain()).thenReturn(value);
            }
        };
    }

    public static CookieStubbingOperation stubComment(final String value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getComment()).thenReturn(value);
            }
        };
    }

    public static CookieStubbingOperation stubMaxAge(final int value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getMaxAge()).thenReturn(value);
            }
        };
    }

    public static CookieStubbingOperation stubPath(final String value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getPath()).thenReturn(value);
            }
        };
    }

    public static CookieStubbingOperation stubSecure(final boolean value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getSecure()).thenReturn(value);
            }
        };
    }

    public static CookieStubbingOperation stubVersion(final int value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getVersion()).thenReturn(value);
            }
        };
    }
}
