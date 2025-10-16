package de.ibmix.magkit.test.cms.dam;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;

import static de.ibmix.magkit.test.cms.dam.AssetMockUtils.mockAsset;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing DamMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2014-04-07
 */
public class DamMockUtilsTest {

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void testMockAssetWithNull() throws Exception {
        Asset asset = mockAsset(null, null, null);
        assertNotNull(asset);
        assertEquals("untitled", asset.getName());
        assertEquals("/untitled", asset.getPath());
        assertEquals("", asset.getCaption());

        assertEquals("", asset.getComment());
        assertNull(asset.getContentStream());
        assertEquals("", asset.getCopyright());
        assertEquals("", asset.getDescription());
        assertEquals("", asset.getFileName());
        assertEquals(0L, asset.getFileSize());
        assertEquals("", asset.getLanguage());
        assertNull(asset.getLastModified());
        assertNull(asset.getLink());
        assertEquals("", asset.getMimeType());
        assertEquals("", asset.getSubject());
        assertEquals("", asset.getTitle());

        Node assetNode = ((JcrAsset) asset).getNode();
        assertNotNull(assetNode);
        assertEquals("untitled", assetNode.getName());
        assertEquals("/untitled", assetNode.getPath());
        assertNotNull(assetNode.getNode("jcr:content"));
    }

    @Test
    public void testMockAsset() throws Exception {
        AssetStubbingOperation op1 = mock(AssetStubbingOperation.class);
        AssetStubbingOperation op2 = mock(AssetStubbingOperation.class);
        Asset asset = mockAsset("some/folder/with/asset", "some", "test-id", op1, op2);
        assertNotNull(asset);
        assertEquals("asset", asset.getName());
        assertEquals("/some/folder/with/asset", asset.getPath());
        verify(op1, times(1)).of(asset);
        verify(op2, times(1)).of(asset);
    }
}
