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

import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.rendering.template.TemplateDefinition;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * Fluent factory for {@link StubbingOperation} implementations that stub common properties of a mocked
 * Magnolia {@link TemplateDefinition}. Each static method returns an immutable operation object whose
 * {@link StubbingOperation#of(Object) of(target)} implementation performs one or more Mockito stubbings on the supplied
 * template instance.
 * <ul>
 *   <li>Guards against null templates using hamcrest assertions to produce clear failure messages.</li>
 *   <li>Encapsulates repetitive {@code doReturn(..).when(template)...} constructs for concise test code.</li>
 *   <li>Allows composition: multiple operations can be applied sequentially to the same mock.</li>
 *   <li>The {@code stubParameter} operation mutates (copies if necessary) the parameter map semantics.</li>
 * </ul>
 * Typical usage:
 * <pre>{@code
 * TemplateDefinition template = mock(TemplateDefinition.class);
 * stubId("my-module:pages/home").of(template);
 * stubTitle("Home Page").of(template);
 * stubDeletable(true).of(template);
 * stubParameter("teaserLimit", 5).of(template);
 * }</pre>
 * Operations are intentionally lightweight and may be reused across tests; they keep no state besides the
 * captured constructor arguments.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-04-14
 */
public abstract class TemplateDefinitionStubbingOperation implements StubbingOperation<TemplateDefinition> {

    /**
     * Stub {@link TemplateDefinition#getDeletable()} to return the supplied value.
     *
     * @param value boolean to be returned by {@code getDeletable()}
     * @return operation stubbing the deletable flag
     */
    public static TemplateDefinitionStubbingOperation stubDeletable(final boolean value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getDeletable();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getDialog()} to return the provided dialog id / path.
     *
     * @param value dialog identifier (may be {@code null} to simulate no dialog)
     * @return operation stubbing the dialog
     */
    public static TemplateDefinitionStubbingOperation stubDialog(final String value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getDialog();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getEditable()} to return the supplied value.
     *
     * @param value boolean to be returned by {@code getEditable()}
     * @return operation stubbing the editable flag
     */
    public static TemplateDefinitionStubbingOperation stubEditable(final boolean value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getEditable();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getMoveable()} to return the supplied value.
     *
     * @param value boolean to be returned by {@code getMoveable()}
     * @return operation stubbing the moveable flag
     */
    public static TemplateDefinitionStubbingOperation stubMoveable(final boolean value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getMoveable();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getVisible()} to return the supplied value.
     *
     * @param value boolean to be returned by {@code getVisible()}
     * @return operation stubbing visibility
     */
    public static TemplateDefinitionStubbingOperation stubVisible(final boolean value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getVisible();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getWritable()} to return the supplied value.
     *
     * @param value boolean to be returned by {@code getWritable()}
     * @return operation stubbing writability
     */
    public static TemplateDefinitionStubbingOperation stubWritable(final boolean value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getWritable();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getType()} to return the supplied type string.
     *
     * @param value template type; may be {@code null}
     * @return operation stubbing the type
     */
    public static TemplateDefinitionStubbingOperation stubType(final String value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getType();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getSubtype()} to return the supplied subtype string.
     *
     * @param value template subtype; may be {@code null}
     * @return operation stubbing the subtype
     */
    public static TemplateDefinitionStubbingOperation stubSubtype(final String value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getSubtype();
            }
        };
    }

    //*********** stubbing operations for RenderableDefinition **************

    /**
     * Stub {@link TemplateDefinition#getI18nBasename()} to return the supplied base name.
     *
     * @param value i18n base name; may be {@code null}
     * @return operation stubbing the deprecated i18n base name
     * @deprecated since 5.4.4. I18nBasename is deprecated and will be removed in a future version.
     */
    @Deprecated
    public static TemplateDefinitionStubbingOperation stubI18nBasename(final String value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getI18nBasename();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getDescription()} to return the supplied description.
     *
     * @param value description string; may be {@code null}
     * @return operation stubbing the description
     */
    public static TemplateDefinitionStubbingOperation stubDescription(final String value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getDescription();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getId()} to return the supplied id.
     *
     * @param value template id (e.g. {@code my-module:pages/home}); may be {@code null}
     * @return operation stubbing the id
     */
    public static TemplateDefinitionStubbingOperation stubId(final String value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getId();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getName()} to return the supplied name.
     *
     * @param value internal technical name; may be {@code null}
     * @return operation stubbing the name
     */
    public static TemplateDefinitionStubbingOperation stubName(final String value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getName();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getTitle()} to return the supplied title.
     *
     * @param value human readable title; may be {@code null}
     * @return operation stubbing the title
     */
    public static TemplateDefinitionStubbingOperation stubTitle(final String value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getTitle();
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getTemplateScript()} to return the supplied script path.
     *
     * @param value script path or resource location; may be {@code null}
     * @return operation stubbing the template script
     */
    public static TemplateDefinitionStubbingOperation stubTemplateScript(final String value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getTemplateScript();
            }
        };
    }

    /**
     * Create an operation that mutates (or initializes) the {@link TemplateDefinition#getParameters()} map: if the
     * supplied {@code value} is non-null it is put under {@code name}; if {@code value} is null an existing entry with
     * {@code name} is removed. A null or empty {@code name} results in a no-op. The map returned by subsequent calls
     * to {@code getParameters()} is the mutated map instance.
     *
     * @param name  parameter key; must be non empty for the operation to take effect
     * @param value parameter value to set; {@code null} removes the key if present
     * @return operation applying the parameter mutation semantics
     */
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
                    } else {
                        parameters.remove(name);
                    }
                    doReturn(parameters).when(template).getParameters();
                }
            }
        };
    }

    /**
     * Stub {@link TemplateDefinition#getRenderType()} to return the supplied render type string.
     *
     * @param value render type (e.g. {@code freemarker}); may be {@code null}
     * @return operation stubbing the render type
     */
    public static TemplateDefinitionStubbingOperation stubRenderType(final String value) {
        return new TemplateDefinitionStubbingOperation() {
            @Override
            public void of(TemplateDefinition template) {
                assertThat(template, notNullValue());
                doReturn(value).when(template).getRenderType();
            }
        };
    }
}
