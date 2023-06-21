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


import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.AssetProvider;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.jcr.AssetNodeTypes;
import info.magnolia.dam.jcr.DamConstants;
import info.magnolia.dam.jcr.JcrAsset;
import info.magnolia.dam.jcr.JcrAssetProvider;
import info.magnolia.dam.jcr.metadata.JcrMagnoliaAssetMetadata;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;

import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubNode;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class for mocking jcr assets.
 * MgnlContext.getJcrSession("dam") will be stubbed as well.
 *
 * @author wolf.bubenik
 * @since 25.03.11
 */
public final class AssetMockUtils extends ComponentsMockUtils {

    private AssetMockUtils() {
    }

    public static JcrAsset mockJcrAsset(String path, AssetStubbingOperation... stubbings) throws RepositoryException {
        return (JcrAsset) mockAsset(path, DamConstants.DEFAULT_JCR_PROVIDER_ID, UUID.randomUUID().toString(), stubbings);
    }

    public static Asset mockAsset(String path, String providerId, String itemId, AssetStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        String uuid = defaultIfBlank(itemId, UUID.randomUUID().toString());
        String provider = defaultIfBlank(providerId, DamConstants.DEFAULT_JCR_PROVIDER_ID);
        Node assetNode = MagnoliaNodeMockUtils.mockMgnlNode(path, DamConstants.WORKSPACE, AssetNodeTypes.Asset.NAME, stubIdentifier(uuid), stubNode(AssetNodeTypes.AssetResource.RESOURCE_NAME));
        JcrAsset result = mock(JcrAsset.class);
        when(result.getNode()).thenReturn(assetNode);
        when(result.isAsset()).thenReturn(true);
        when(result.isFolder()).thenReturn(false);
        doAnswer(ASSET_NAME_ANSWER).when(result).getName();
        doAnswer(ASSET_PATH_ANSWER).when(result).getPath();
        doAnswer(ASSET_CAPTION_ANSWER).when(result).getCaption();
        doAnswer(ASSET_COMMENT_ANSWER).when(result).getComment();
        doAnswer(ASSET_STREAM_ANSWER).when(result).getContentStream();
        doAnswer(ASSET_COPYRIGHT_ANSWER).when(result).getCopyright();
        doAnswer(ASSET_DESCRIPTION_ANSWER).when(result).getDescription();
        doAnswer(ASSET_FILE_NAME_ANSWER).when(result).getFileName();
        doAnswer(ASSET_FILE_SIZE_ANSWER).when(result).getFileSize();
        doAnswer(ASSET_LANGUAGE_ANSWER).when(result).getLanguage();
        doAnswer(ASSET_LAST_MODIFIED_ANSWER).when(result).getLastModified();
        doAnswer(ASSET_MIMETYPE_ANSWER).when(result).getMimeType();
        doAnswer(ASSET_SUBJECT_ANSWER).when(result).getSubject();
        doAnswer(ASSET_TITLE_ANSWER).when(result).getTitle();
        doReturn(new JcrMagnoliaAssetMetadata(assetNode)).when(result).getMetadata(JcrMagnoliaAssetMetadata.class);
        for (AssetStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        ItemKey itemKey = new ItemKey(provider, itemId);
        when(result.getItemKey()).thenReturn(itemKey);
        mockAssetProvider(result);
        return result;
    }

    public static AssetProviderRegistry mockAssetProviderRegistry(AssetProviderRegistryStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        AssetProviderRegistry result = mockComponentInstance(AssetProviderRegistry.class);
        for (AssetProviderRegistryStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    public static AssetProvider mockAssetProvider(String providerId) {
        assertThat(isNotBlank(providerId), is(true));
        AssetProviderRegistry registry = mockAssetProviderRegistry();
        AssetProvider assetProvider = registry.getProviderById(providerId);
        if (assetProvider == null) {
            assetProvider = DamConstants.DEFAULT_JCR_PROVIDER_ID.equals(providerId) ? mock(JcrAssetProvider.class) : mock(AssetProvider.class);
            when(assetProvider.getIdentifier()).thenReturn(providerId);
            when(registry.getProviderById(providerId)).thenReturn(assetProvider);
        }
        return assetProvider;
    }

    public static AssetProvider mockAssetProvider(ItemKey itemKey) {
        assertThat(itemKey, notNullValue());
        AssetProvider assetProvider = mockAssetProvider(itemKey.getProviderId());
        AssetProviderRegistry registry = mockAssetProviderRegistry();
        when(registry.getProviderFor(itemKey)).thenReturn(assetProvider);
        return assetProvider;
    }

    public static AssetProvider mockAssetProvider(Asset asset) {
        assertThat(asset, notNullValue());
        ItemKey itemKey = asset.getItemKey();
        assertThat(itemKey, notNullValue());
        AssetProvider assetProvider = mockAssetProvider(itemKey);
        when(assetProvider.getItem(itemKey)).thenReturn(asset);
        when(assetProvider.getAsset(itemKey)).thenReturn(asset);
        if (DamConstants.DEFAULT_JCR_PROVIDER_ID.equals(itemKey.getProviderId())) {
            when(((JcrAssetProvider) assetProvider).getAsset(asset.getPath())).thenReturn(asset);
            when(((JcrAssetProvider) assetProvider).getItem(asset.getPath())).thenReturn(asset);
        }
        return assetProvider;
    }

    public static void cleanAssetProviderRegistry() {
        clearComponentProvider(AssetProviderRegistry.class);
    }

    private static final Answer<String> ASSET_NAME_ANSWER = invocation -> {
        JcrAsset asset = (JcrAsset) invocation.getMock();
        return asset.getNode().getName();
    };

    private static final Answer<String> ASSET_PATH_ANSWER = invocation -> {
        JcrAsset asset = (JcrAsset) invocation.getMock();
        return asset.getNode().getPath();
    };

    private static final Answer<String> ASSET_CAPTION_ANSWER = new AssetPropertyStringAnswer(AssetNodeTypes.Asset.CAPTION);
    private static final Answer<String> ASSET_COMMENT_ANSWER = new AssetPropertyStringAnswer(AssetNodeTypes.Asset.COMMENT);
    private static final Answer<String> ASSET_LANGUAGE_ANSWER = new AssetPropertyStringAnswer(AssetNodeTypes.Asset.LANGUAGE);
    private static final Answer<String> ASSET_TITLE_ANSWER = new AssetPropertyStringAnswer(AssetNodeTypes.Asset.TITLE);
    private static final Answer<String> ASSET_SUBJECT_ANSWER = new AssetPropertyStringAnswer(AssetNodeTypes.Asset.SUBJECT);
    private static final Answer<String> ASSET_DESCRIPTION_ANSWER = new AssetPropertyStringAnswer(AssetNodeTypes.Asset.DESCRIPTION);
    private static final Answer<String> ASSET_COPYRIGHT_ANSWER = new AssetPropertyStringAnswer(AssetNodeTypes.Asset.COPYRIGHT);
    private static final Answer<Calendar> ASSET_LAST_MODIFIED_ANSWER = new AssetPropertyCalendarAnswer(NodeTypes.LastModified.LAST_MODIFIED);

    private static final Answer<String> ASSET_MIMETYPE_ANSWER = new AssetResourcePropertyStringAnswer(AssetNodeTypes.AssetResource.MIMETYPE);
    private static final Answer<String> ASSET_FILE_NAME_ANSWER = new AssetResourcePropertyStringAnswer(AssetNodeTypes.AssetResource.FILENAME);
    private static final Answer<Long> ASSET_FILE_SIZE_ANSWER = new AssetResourcePropertyLongAnswer(AssetNodeTypes.AssetResource.SIZE);
    private static final Answer<InputStream> ASSET_STREAM_ANSWER = new AssetResourcePropertyStreamAnswer(AssetNodeTypes.AssetResource.DATA);


    private static class AssetPropertyStringAnswer implements Answer<String> {
        private final String _propertyName;

        AssetPropertyStringAnswer(final String propertyName) {
            _propertyName = propertyName;
        }

        @Override
        public String answer(final InvocationOnMock invocation) {
            JcrAsset asset = (JcrAsset) invocation.getMock();
            return PropertyUtil.getString(asset.getNode(), _propertyName, StringUtils.EMPTY);
        }
    }

    private static class AssetPropertyCalendarAnswer implements Answer<Calendar> {
        private final String _propertyName;

        AssetPropertyCalendarAnswer(final String propertyName) {
            _propertyName = propertyName;
        }

        @Override
        public Calendar answer(final InvocationOnMock invocation) {
            JcrAsset asset = (JcrAsset) invocation.getMock();
            return PropertyUtil.getDate(asset.getNode(), _propertyName);
        }
    }

    private static class AssetResourcePropertyStringAnswer implements Answer<String> {
        private final String _propertyName;

        AssetResourcePropertyStringAnswer(final String propertyName) {
            _propertyName = propertyName;
        }

        @Override
        public String answer(final InvocationOnMock invocation) throws RepositoryException {
            Node resourceNode = AssetStubbingOperation.getResourceNode((JcrAsset) invocation.getMock());
            return PropertyUtil.getString(resourceNode, _propertyName, StringUtils.EMPTY);
        }
    }

    private static class AssetResourcePropertyLongAnswer implements Answer<Long> {
        private final String _propertyName;

        AssetResourcePropertyLongAnswer(final String propertyName) {
            _propertyName = propertyName;
        }

        @Override
        public Long answer(final InvocationOnMock invocation) throws RepositoryException {
            Node resourceNode = AssetStubbingOperation.getResourceNode((JcrAsset) invocation.getMock());
            return PropertyUtil.getLong(resourceNode, _propertyName, 0L);
        }
    }

    private static class AssetResourcePropertyStreamAnswer implements Answer<InputStream> {
        private final String _propertyName;

        AssetResourcePropertyStreamAnswer(final String propertyName) {
            _propertyName = propertyName;
        }

        @Override
        public InputStream answer(final InvocationOnMock invocation) throws RepositoryException {
            InputStream result = null;
            Node resourceNode = AssetStubbingOperation.getResourceNode((JcrAsset) invocation.getMock());
            Property binaryData = PropertyUtil.getPropertyOrNull(resourceNode, _propertyName);
            if (binaryData != null) {
                Binary binary = binaryData.getBinary();
                if (binary != null) {
                    result = binary.getStream();
                }
            }
            return result;
        }
    }

}
