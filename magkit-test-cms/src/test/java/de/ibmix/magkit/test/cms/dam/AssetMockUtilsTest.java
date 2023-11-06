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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test AssetMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-03-05
 */
public class AssetMockUtilsTest {

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockAssetProviderRegistry() {
        AssetProviderRegistry apr = AssetMockUtils.mockAssetProviderRegistry();
        assertThat(Components.getComponent(AssetProviderRegistry.class), is(apr));

        // repeated mocking returns same mock instance:
        AssetProviderRegistry apr2 = AssetMockUtils.mockAssetProviderRegistry();
        assertThat(apr2, is(apr));
    }

    @Test
    public void testMockAssetProviderForId() {
        AssetProviderRegistry apr = AssetMockUtils.mockAssetProviderRegistry();
        AssetProvider testAp = AssetMockUtils.mockAssetProvider("test");

        assertThat(apr, notNullValue());
        assertThat(apr.getProviderById("test"), is(testAp));

        AssetProvider jcrAp = AssetMockUtils.mockAssetProvider("jcr");
        assertThat(apr.getProviderById("jcr"), is(jcrAp));

        // repeated mocking with same id returns same mock instance:
        AssetProvider jcrAp2 = AssetMockUtils.mockAssetProvider("jcr");
        assertThat(jcrAp2, is(jcrAp));
        assertThat(apr.getProviderById("jcr"), is(jcrAp2));
    }

    @Test
    public void testMockAssetProviderForItemKey() {
        AssetProviderRegistry apr = AssetMockUtils.mockAssetProviderRegistry();
        ItemKey itemKey = new ItemKey("jcr", UUID.randomUUID().toString());
        AssetProvider jcrAp = AssetMockUtils.mockAssetProvider(itemKey);
        assertThat(apr.getProviderById("jcr"), is(jcrAp));
        assertThat(apr.getProviderFor(itemKey), is(jcrAp));
    }

    @Test
    public void mockAssetProviderForAsset() {
        Asset as = mock(Asset.class);
        ItemKey itemKey = new ItemKey("jcr", UUID.randomUUID().toString());
        doReturn(itemKey).when(as).getItemKey();

        AssetProvider asAp = AssetMockUtils.mockAssetProvider(as);
        assertThat(asAp, notNullValue());
        assertThat(asAp.getItem(itemKey), is(as));
        assertThat(asAp.getAsset(itemKey), is(as));
        assertThat(((JcrAssetProvider) asAp).getItem(itemKey), is(as));
        assertThat(((JcrAssetProvider) asAp).getAsset(itemKey), is(as));
    }

    @Test
    public void mockJcrAsset() throws RepositoryException {
        JcrAsset jcrAsset = AssetMockUtils.mockJcrAsset("/test");
        ItemKey itemKey = jcrAsset.getItemKey();
        assertThat(itemKey, notNullValue());
        AssetProviderRegistry apr = Components.getComponent(AssetProviderRegistry.class);
        assertThat(apr, notNullValue());
        assertThat(apr.getProviderById("jcr"), notNullValue());
        assertThat(apr.getProviderById("jcr").getAsset(itemKey), is(jcrAsset));
        assertThat(apr.getProviderFor(itemKey), notNullValue());
        assertThat(apr.getProviderFor(itemKey).getAsset(itemKey), is(jcrAsset));
    }

}
