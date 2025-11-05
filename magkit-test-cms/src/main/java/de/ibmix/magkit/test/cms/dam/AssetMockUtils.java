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


import de.ibmix.magkit.assertions.Require;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class providing factory and helper methods to create mocked Magnolia DAM {@link Asset} / {@link JcrAsset}
 * instances and related provider infrastructure for unit tests.
 * <p>
 * The methods encapsulate repetitive Mockito stubbing logic for commonly accessed properties of an asset (name,
 * path, metadata, resource data etc.) and also register the corresponding {@link AssetProvider} in a mocked
 * {@link AssetProviderRegistry}. This greatly simplifies test setup where DAM assets are required but interaction
 * with the real JCR repository should be avoided.
 * </p>
 * <p>
 * Side effects: A mocked {@link AssetProviderRegistry} component is registered via {@link ComponentsMockUtils} so that
 * calls through Magnolia's component resolution return the provided mock instances. Use {@link #cleanAssetProviderRegistry()}
 * to clear the registry between tests to avoid cross test interference.
 * </p>
 * <strong>Typical usage</strong>
 * <pre>
 * // Create a simple JCR asset mock with default provider and random UUID:
 * JcrAsset asset = AssetMockUtils.mockJcrAsset("/path/to/asset.jpg");
 * // Optionally apply additional stubbing operations (e.g. set title, mime type, size):
 * JcrAsset asset2 = AssetMockUtils.mockJcrAsset("/images/logo.png",
 *      AssetStubbingOperation.stubAssetProperty(AssetNodeTypes.Asset.TITLE, "Logo"));
 * </pre>
 * <p><b>Thread safety:</b> Implementation is backed by ComponentProvider that uses ThreadLocal and is thread-safe; intended for multithreaded test initialization code.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-03-25
 */
public final class AssetMockUtils extends ComponentsMockUtils {

    /**
     * Creates a {@link JcrAsset} mock with the given asset path, default JCR provider id
     * ({@link DamConstants#DEFAULT_JCR_PROVIDER_ID}) and a generated random UUID as item identifier.
     * <p>
     * Each provided {@link AssetStubbingOperation} will be applied to further customize the mock.
     * </p>
     *
     * @param path the absolute JCR path of the asset node inside the DAM workspace (e.g. /images/logo.png)
     * @param stubbings optional additional stubbing operations for properties or resource node (must be non-null, may be empty)
     * @return a fully mocked {@link JcrAsset} instance including provider registration
     * @throws RepositoryException if JCR operations performed during setup throw; normally not expected in mocked context
     * @see #mockAsset(String, String, String, AssetStubbingOperation...)
     * @see AssetStubbingOperation
     */
    public static JcrAsset mockJcrAsset(String path, AssetStubbingOperation... stubbings) throws RepositoryException {
        return (JcrAsset) mockAsset(path, DamConstants.DEFAULT_JCR_PROVIDER_ID, UUID.randomUUID().toString(), stubbings);
    }

    /**
     * Creates a generic {@link Asset} mock (backed by a {@link JcrAsset}) for the provided path, provider id and item id.
     * <p>
     * If {@code providerId} or {@code itemId} are blank, sensible defaults are used (default JCR provider and a random UUID).
     * The underlying JCR {@link Node} is created via {@link MagnoliaNodeMockUtils#mockMgnlNode(String, String, String, de.ibmix.magkit.test.jcr.NodeStubbingOperation...)}
     * and standard asset property answers are configured. The corresponding {@link AssetProvider} is mocked and registered.
     * </p>
     * <p>
     * Use {@link AssetStubbingOperation} instances to set additional asset or resource properties (e.g. file size, mime type).
     * </p>
     *
     * @param path the absolute path of the asset node in the DAM workspace
     * @param providerId the desired asset provider id or blank for {@link DamConstants#DEFAULT_JCR_PROVIDER_ID}
     * @param itemId the item identifier (UUID) or blank for a generated random UUID
     * @param stubbings array of stubbing operations (must be non-null, may be empty)
     * @return configured {@link Asset} mock instance
     * @throws RepositoryException if a JCR API interaction during mock setup signals an error
     */
    public static Asset mockAsset(String path, String providerId, String itemId, AssetStubbingOperation... stubbings) throws RepositoryException {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        String uuid = defaultIfBlank(itemId, UUID.randomUUID().toString());
        String provider = defaultIfBlank(providerId, DamConstants.DEFAULT_JCR_PROVIDER_ID);
        Node assetNode = MagnoliaNodeMockUtils.mockMgnlNode(DamConstants.WORKSPACE, path, AssetNodeTypes.Asset.NAME, stubIdentifier(uuid), stubNode(AssetNodeTypes.AssetResource.RESOURCE_NAME));
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

    /**
     * Creates (or retrieves if already existing) a mocked {@link AssetProviderRegistry} component instance.
     * <p>
     * The registry is registered in the Magnolia component provider so subsequent calls during the same test will
     * return the same mock instance, unless cleared with {@link #cleanAssetProviderRegistry()}.
     * </p>
     *
     * @return mocked {@link AssetProviderRegistry}
     */
    public static AssetProviderRegistry mockAssetProviderRegistry() {
        return mockComponentInstance(AssetProviderRegistry.class);
    }

    /**
     * Returns a mocked {@link AssetProvider} for the given provider id. If none exists yet in the registry it will be created.
     * <p>
     * For the default DAM JCR provider id ({@link DamConstants#DEFAULT_JCR_PROVIDER_ID}) a {@link JcrAssetProvider} mock is
     * created, otherwise a generic {@link AssetProvider} mock. The provider is registered in the mocked registry so that
     * future lookups by id return the same instance.
     * </p>
     *
     * @param providerId non-blank provider identifier
     * @return mocked provider instance
     * @throws IllegalArgumentException if {@code providerId} is blank
     */
    public static AssetProvider mockAssetProvider(String providerId) {
        Require.Argument.notBlank(providerId, "providerId should not be blank");
        AssetProviderRegistry registry = mockAssetProviderRegistry();
        AssetProvider assetProvider = registry.getProviderById(providerId);
        if (assetProvider == null) {
            assetProvider = DamConstants.DEFAULT_JCR_PROVIDER_ID.equals(providerId) ? mock(JcrAssetProvider.class) : mock(AssetProvider.class);
            when(assetProvider.getIdentifier()).thenReturn(providerId);
            when(registry.getProviderById(providerId)).thenReturn(assetProvider);
        }
        return assetProvider;
    }

    /**
     * Convenience overload that obtains a mocked {@link AssetProvider} based on an {@link ItemKey}.
     * <p>
     * Delegates to {@link #mockAssetProvider(String)} using the provider id contained in the item key and additionally
     * prepares the mocked registry to return the provider for lookups by item key.
     * </p>
     *
     * @param itemKey the item key containing provider id (must be non-null)
     * @return mocked asset provider for the key's provider id
     * @throws IllegalArgumentException if {@code itemKey} is null
     */
    public static AssetProvider mockAssetProvider(ItemKey itemKey) {
        Require.Argument.notNull(itemKey, "itemKey should not be null");
        AssetProvider assetProvider = mockAssetProvider(itemKey.getProviderId());
        AssetProviderRegistry registry = mockAssetProviderRegistry();
        when(registry.getProviderFor(itemKey)).thenReturn(assetProvider);
        return assetProvider;
    }

    /**
     * Convenience overload that mocks / registers a provider for the given {@link Asset} instance and prepares
     * provider lookups to return the asset.
     * <p>
     * For a default JCR provider id it also stubs path-based lookup on {@link JcrAssetProvider#getAsset(String)} and
     * {@link JcrAssetProvider#getItem(String)} to return the provided asset.
     * </p>
     *
     * @param asset the asset whose provider should be mocked (must be non-null and have a non-null item key)
     * @return mocked {@link AssetProvider} serving the given asset
     * @throws IllegalArgumentException if {@code asset} or its item key are null
     */
    public static AssetProvider mockAssetProvider(Asset asset) {
        Require.Argument.notNull(asset, "asset should not be null");
        ItemKey itemKey = asset.getItemKey();
        Require.Argument.notNull(itemKey, "itemKey should not be null");
        AssetProvider assetProvider = mockAssetProvider(itemKey);
        when(assetProvider.getItem(itemKey)).thenReturn(asset);
        when(assetProvider.getAsset(itemKey)).thenReturn(asset);
        if (DamConstants.DEFAULT_JCR_PROVIDER_ID.equals(itemKey.getProviderId())) {
            when(((JcrAssetProvider) assetProvider).getAsset(asset.getPath())).thenReturn(asset);
            when(((JcrAssetProvider) assetProvider).getItem(asset.getPath())).thenReturn(asset);
        }
        return assetProvider;
    }

    /**
     * Clears / unregisters any mocked {@link AssetProviderRegistry} from the Magnolia component provider.
     * <p>
     * Invoke this after each test that modified the registry to prevent leakage into other tests when the component
     * provider is reused.
     * </p>
     */
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

    private AssetMockUtils() {
    }

    /**
     * Answer implementation resolving a String property directly on the asset node.
     */
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

    /**
     * Answer implementation resolving a {@link Calendar} date property directly on the asset node.
     */
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

    /**
     * Answer implementation resolving a String property from the asset resource node.
     */
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

    /**
     * Answer implementation resolving a Long property from the asset resource node.
     */
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

    /**
     * Answer implementation providing an {@link InputStream} to the binary data of the asset resource node.
     * Returns {@code null} if no binary exists for the requested property.
     */
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
