package de.ibmix.magkit.test.cms.templating;

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

import info.magnolia.rendering.template.TemplateDefinition;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * Operations to stub behaviour of a TemplateDefinition mock.
 *
 * @author wolf.bubenik
 * @since 14.04.16.
 */
public abstract class TemplateDefinitionStubbingOperation {
    abstract void of(TemplateDefinition template);

    public static TemplateDefinitionStubbingOperation stubDeletable(final boolean value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getDeletable();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubDialog(final String value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getDialog();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubEditable(final boolean value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getEditable();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubMoveable(final boolean value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getMoveable();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubVisible(final boolean value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getVisible();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubWritable(final boolean value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getWritable();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubType(final String value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getType();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubSubtype(final String value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getSubtype();
            }
        };
    }

    //*********** stubbing operations for RenderableDefinition **************

    /**
     * FactoryMethod for a TemplateDefinitionStubbingOperation.
     *
     * @param value the base name for the TemplateDefinition
     * @return the TemplateDefinitionStubbingOperation
     * @deprecated since 5.4.4. I18nBasename is deprecated and will be removed in a future version.
     */
    @Deprecated
    public static TemplateDefinitionStubbingOperation stubI18nBasename(final String value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getI18nBasename();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubDescription(final String value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getDescription();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubId(final String value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getId();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubName(final String value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getName();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubTitle(final String value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getTitle();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubTemplateScript(final String value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getTemplateScript();
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubParameter(final String name, final Object value) {
        return new TemplateDefinitionStubbingOperation() {

            @Override
            public void of(final TemplateDefinition template) {
                assertThat(template, notNullValue());
                if (isNotEmpty(name)) {

                    Map<String, Object> parameters = template.getParameters();
                    if (parameters == null) {
                        parameters = new HashMap<>();
                    }
                    if (value != null) {
                        parameters.put(name, value);
                    } else if (parameters.containsKey(name)) {
                        parameters.remove(name);
                    }
                    doReturn(parameters).when(template).getParameters();
                }
            }
        };
    }

    public static TemplateDefinitionStubbingOperation stubRenderType(final String value) {
        return new TemplateDefinitionStubbingOperation() {

            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getRenderType();
            }
        };
    }
}
