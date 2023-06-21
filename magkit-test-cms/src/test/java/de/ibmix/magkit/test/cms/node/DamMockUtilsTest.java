package de.ibmix.magkit.test.cms.node;

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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.jcr.JcrAsset;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;

import static de.ibmix.magkit.test.cms.node.AssetMockUtils.mockAsset;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing DamMockUtils.
 *
 * @author wolf.bubenik
 * @since 07.04.14
 */
public class DamMockUtilsTest {
    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void testMockAssetWithNull() throws Exception {
        Asset asset = mockAsset(null, null, null);
        assertThat(asset, notNullValue());
        assertThat(asset.getName(), is("untitled"));
        assertThat(asset.getPath(), is("/untitled"));
        assertThat(asset.getCaption(), is(""));

        assertThat(asset.getComment(), is(""));
        assertThat(asset.getContentStream(), nullValue());
        assertThat(asset.getCopyright(), is(""));
        assertThat(asset.getDescription(), is(""));
        assertThat(asset.getFileName(), is(""));
        assertThat(asset.getFileSize(), is(0L));
        assertThat(asset.getLanguage(), is(""));
        assertThat(asset.getLastModified(), nullValue());
        assertThat(asset.getLink(), nullValue());
        assertThat(asset.getMimeType(), is(""));
        assertThat(asset.getSubject(), is(""));
        assertThat(asset.getTitle(), is(""));

        Node assetNode = ((JcrAsset) asset).getNode();
        assertThat(assetNode, notNullValue());
        assertThat(assetNode.getName(), is("untitled"));
        assertThat(assetNode.getPath(), is("/untitled"));
        assertThat(assetNode.getNode("jcr:content"), notNullValue());
    }

    @Test
    public void testMockAsset() throws Exception {
        AssetStubbingOperation op1 = mock(AssetStubbingOperation.class);
        AssetStubbingOperation op2 = mock(AssetStubbingOperation.class);
        Asset asset = mockAsset("some/folder/with/asset", "some", "test-id", op1, op2);
        assertThat(asset, notNullValue());
        assertThat(asset.getName(), is("asset"));
        assertThat(asset.getPath(), is("/some/folder/with/asset"));
        verify(op1, times(1)).of(asset);
        verify(op2, times(1)).of(asset);
    }
}
