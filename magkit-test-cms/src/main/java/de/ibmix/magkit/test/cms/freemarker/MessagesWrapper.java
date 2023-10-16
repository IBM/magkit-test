package de.ibmix.magkit.test.cms.freemarker;

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

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesChain;
import info.magnolia.cms.i18n.MessagesManager;

import java.util.List;
import java.util.Locale;

/**
 * Copy from Magnolias message wrapper which we can not use. It has only a package local scope. :-(
 *
 * @author frank.sommer@ibmix.de
 * @see {@link info.magnolia.freemarker.MessagesWrapper}
 */
public class MessagesWrapper {
    private final Messages _messages;
    private final Locale _locale;

    public MessagesWrapper(String basename, Locale locale) {
        final Messages msg = MessagesManager.getMessages(basename, locale);
        final Messages defMsg = MessagesManager.getMessages(locale);
        _messages = new MessagesChain(msg).chain(defMsg);
        _locale = locale;
    }

    public String get(String key) {
        return get(key, _messages);
    }

    public String get(String key, List<Object> args) {
        return get(key, args, _messages);
    }

    public String get(String key, String basename) {
        return get(key, MessagesManager.getMessages(basename, _locale));
    }

    public String get(String key, List<Object> args, String basename) {
        return get(key, args, MessagesManager.getMessages(basename, _locale));
    }

    public String getWithDefault(String key, String defaultMsg) {
        return getWithDefault(key, defaultMsg, _messages);
    }

    public String getWithDefault(String key, String defaultMsg, String basename) {
        return getWithDefault(key, defaultMsg, MessagesManager.getMessages(basename, _locale));
    }

    public String getWithDefault(String key, List<Object> args, String defaultMsg) {
        return getWithDefault(key, args, defaultMsg, _messages);
    }

    public String getWithDefault(String key, List<Object> args, String defaultMsg, String basename) {
        return getWithDefault(key, defaultMsg, MessagesManager.getMessages(basename, _locale));
    }

    protected String get(String key, Messages messages) {
        return messages.get(key);
    }

    protected String get(String key, List<Object> args, Messages messages) {
        return messages.get(key, args.toArray(new Object[0]));
    }

    protected String getWithDefault(String key, String defaultMsg, Messages messages) {
        return messages.getWithDefault(key, defaultMsg);
    }

    protected String getWithDefault(String key, List<Object> args, String defaultMsg, Messages messages) {
        return messages.getWithDefault(key, args.toArray(new Object[0]), defaultMsg);
    }
}
