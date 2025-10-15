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
 * or for stubbing the behavior of an existing mock: CookieStubbingOperation.stubDomain("domain").of(mock).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2014-02-11
 */
public abstract class CookieStubbingOperation {
    public abstract void of(Cookie context);

    /**
     * Stubs the domain of a Cookie.
     *
     * @param value the domain to be returned by getDomain()
     * @return a CookieStubbingOperation that stubs the domain
     */
    public static CookieStubbingOperation stubDomain(final String value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getDomain()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs the comment of a Cookie.
     *
     * @param value the comment to be returned by getComment()
     * @return a CookieStubbingOperation that stubs the comment
     */
    public static CookieStubbingOperation stubComment(final String value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getComment()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs the max age of a Cookie.
     *
     * @param value the max age to be returned by getMaxAge()
     * @return a CookieStubbingOperation that stubs the max age
     */
    public static CookieStubbingOperation stubMaxAge(final int value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getMaxAge()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs the path of a Cookie.
     *
     * @param value the path to be returned by getPath()
     * @return a CookieStubbingOperation that stubs the path
     */
    public static CookieStubbingOperation stubPath(final String value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getPath()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs the secure flag of a Cookie.
     *
     * @param value the secure flag to be returned by getSecure()
     * @return a CookieStubbingOperation that stubs the secure flag
     */
    public static CookieStubbingOperation stubSecure(final boolean value) {
        return new CookieStubbingOperation() {

            @Override
            public void of(final Cookie cookie) {
                assertThat(cookie, notNullValue());
                when(cookie.getSecure()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs the version of a Cookie.
     *
     * @param value the version to be returned by getVersion()
     * @return a CookieStubbingOperation that stubs the version
     */
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
