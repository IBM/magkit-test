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
import info.magnolia.dam.api.AssetProvider;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.jcr.JcrAsset;
import info.magnolia.dam.jcr.JcrAssetProvider;
import info.magnolia.objectfactory.Components;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test AssetMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-03-05
 */
public class AssetMockUtilsTest {

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @AfterEach
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockAssetProviderRegistry() {
        AssetProviderRegistry apr = AssetMockUtils.mockAssetProviderRegistry();
        assertEquals(apr, Components.getComponent(AssetProviderRegistry.class));

        // repeated mocking returns same mock instance:
        AssetProviderRegistry apr2 = AssetMockUtils.mockAssetProviderRegistry();
        assertEquals(apr, apr2);
    }

    @Test
    public void testMockAssetProviderForId() {
        AssetProviderRegistry apr = AssetMockUtils.mockAssetProviderRegistry();
        AssetProvider testAp = AssetMockUtils.mockAssetProvider("test");

        assertNotNull(apr);
        assertEquals(testAp, apr.getProviderById("test"));

        AssetProvider jcrAp = AssetMockUtils.mockAssetProvider("jcr");
        assertEquals(jcrAp, apr.getProviderById("jcr"));

        // repeated mocking with same id returns same mock instance:
        AssetProvider jcrAp2 = AssetMockUtils.mockAssetProvider("jcr");
        assertEquals(jcrAp, jcrAp2);
        assertEquals(jcrAp2, apr.getProviderById("jcr"));
    }

    @Test
    public void testMockAssetProviderForItemKey() {
        AssetProviderRegistry apr = AssetMockUtils.mockAssetProviderRegistry();
        ItemKey itemKey = new ItemKey("jcr", UUID.randomUUID().toString());
        AssetProvider jcrAp = AssetMockUtils.mockAssetProvider(itemKey);
        assertEquals(jcrAp, apr.getProviderById("jcr"));
        assertEquals(jcrAp, apr.getProviderFor(itemKey));
    }

    @Test
    public void mockAssetProviderForAsset() {
        Asset as = mock(Asset.class);
        ItemKey itemKey = new ItemKey("jcr", UUID.randomUUID().toString());
        doReturn(itemKey).when(as).getItemKey();

        AssetProvider asAp = AssetMockUtils.mockAssetProvider(as);
        assertNotNull(asAp);
        assertEquals(as, asAp.getItem(itemKey));
        assertEquals(as, asAp.getAsset(itemKey));
        assertEquals(as, ((JcrAssetProvider) asAp).getItem(itemKey));
        assertEquals(as, ((JcrAssetProvider) asAp).getAsset(itemKey));
    }

    @Test
    public void mockJcrAsset() throws RepositoryException {
        JcrAsset jcrAsset = AssetMockUtils.mockJcrAsset("/test");
        ItemKey itemKey = jcrAsset.getItemKey();
        assertNotNull(itemKey);
        AssetProviderRegistry apr = Components.getComponent(AssetProviderRegistry.class);
        assertNotNull(apr);
        assertNotNull(apr.getProviderById("jcr"));
        assertEquals(jcrAsset, apr.getProviderById("jcr").getAsset(itemKey));
        assertNotNull(apr.getProviderFor(itemKey));
        assertEquals(jcrAsset, apr.getProviderFor(itemKey).getAsset(itemKey));
    }

}
