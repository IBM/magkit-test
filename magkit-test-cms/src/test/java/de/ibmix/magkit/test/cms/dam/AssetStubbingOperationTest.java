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
import info.magnolia.dam.jcr.AssetNodeTypes;
import info.magnolia.dam.jcr.JcrAsset;
import info.magnolia.dam.jcr.metadata.JcrMagnoliaAssetMetadata;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.Calendar;

import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubCaption;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubComment;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubContentStream;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubCopyright;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubDescription;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubFileExtension;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubFileName;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubFileSize;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubHeight;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubLanguage;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubLastModified;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubLink;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubMimeType;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubSubject;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubTitle;
import static de.ibmix.magkit.test.cms.dam.AssetStubbingOperation.stubWidth;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test AssetStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2014-04-07
 */
public class AssetStubbingOperationTest {

    private Asset _asset;
    private JcrAsset _jcrAsset;

    @Before
    public void setUp() throws RepositoryException {
        ContextMockUtils.cleanContext();
        _asset = mock(Asset.class);
        _jcrAsset = AssetMockUtils.mockJcrAsset("test/asset");
    }

    @Test
    public void testStubLink() throws RepositoryException {
        stubLink("http://www.migros.ch").of(_asset);
        stubLink("http://www.migros.ch").of(_jcrAsset);
        assertThat(_asset.getLink(), is("http://www.migros.ch"));
        assertThat(_jcrAsset.getLink(), is("http://www.migros.ch"));
    }

    @Test
    public void testStubTitle() throws RepositoryException {
        stubTitle("Titel").of(_asset);
        stubTitle("Titel").of(_jcrAsset);
        assertThat(_asset.getTitle(), is("Titel"));
        assertThat(_jcrAsset.getTitle(), is("Titel"));
    }

    @Test
    public void testStubDescription() throws RepositoryException {
        stubDescription("Description").of(_asset);
        stubDescription("Description").of(_jcrAsset);
        assertThat(_asset.getDescription(), is("Description"));
        assertThat(_jcrAsset.getDescription(), is("Description"));
    }

    @Test
    public void testStubCaption() throws RepositoryException {
        stubCaption("Caption").of(_asset);
        stubCaption("Caption").of(_jcrAsset);
        assertThat(_asset.getCaption(), is("Caption"));
        assertThat(_jcrAsset.getCaption(), is("Caption"));
    }

    @Test
    public void testStubCopyright() throws RepositoryException {
        stubCopyright("Copyright").of(_asset);
        stubCopyright("Copyright").of(_jcrAsset);
        assertThat(_asset.getCopyright(), is("Copyright"));
        assertThat(_jcrAsset.getCopyright(), is("Copyright"));
    }

    @Test
    public void testStubLanguage() throws RepositoryException {
        stubLanguage("Language").of(_asset);
        stubLanguage("Language").of(_jcrAsset);
        assertThat(_asset.getLanguage(), is("Language"));
        assertThat(_jcrAsset.getLanguage(), is("Language"));
    }

    @Test
    public void testStubSubject() throws RepositoryException {
        stubSubject("Subject").of(_asset);
        stubSubject("Subject").of(_jcrAsset);
        assertThat(_asset.getSubject(), is("Subject"));
        assertThat(_jcrAsset.getSubject(), is("Subject"));
    }

    @Test
    public void testStubLastModified() throws RepositoryException {
        Calendar now = Calendar.getInstance();
        stubLastModified(now).of(_asset);
        stubLastModified(now).of(_jcrAsset);
        assertThat(_asset.getLastModified(), is(now));
        assertThat(_jcrAsset.getLastModified(), is(now));
    }

    @Test
    public void testStubMimeType() throws RepositoryException {
        stubMimeType("MimeType").of(_asset);
        stubMimeType("MimeType").of(_jcrAsset);
        assertThat(_asset.getMimeType(), is("MimeType"));
        assertThat(_jcrAsset.getMimeType(), is("MimeType"));
    }

    @Test
    public void testStubFileSize() throws RepositoryException {
        stubFileSize(123456L).of(_asset);
        stubFileSize(123456L).of(_jcrAsset);
        assertThat(_asset.getFileSize(), is(123456L));
        assertThat(_jcrAsset.getFileSize(), is(123456L));
    }

    @Test
    public void testStubFileName() throws RepositoryException {
        stubFileName("FileName").of(_asset);
        stubFileName("FileName").of(_jcrAsset);
        assertThat(_asset.getFileName(), is("FileName"));
        assertThat(_jcrAsset.getFileName(), is("FileName"));
    }

    @Test
    public void testStubContentStream() throws RepositoryException {
        InputStream stream = mock(InputStream.class);
        stubContentStream(stream).of(_asset);
        stubContentStream(stream).of(_jcrAsset);
        assertThat(_asset.getContentStream(), is(stream));
        assertThat(_jcrAsset.getContentStream(), is(stream));
    }

    @Test
    public void testStubComment() throws RepositoryException {
        assertThat(_asset.getComment(), nullValue());
        assertThat(_jcrAsset.getComment(), is(""));

        stubComment("comment").of(_asset);
        stubComment("comment").of(_jcrAsset);
        assertThat(_asset.getComment(), is("comment"));
        assertThat(_jcrAsset.getComment(), is("comment"));
    }

    @Test
    public void testStubFileExtension() throws RepositoryException {
        assertThat(_jcrAsset.getNode().getNode(AssetNodeTypes.AssetResource.RESOURCE_NAME).getProperty("extension"), nullValue());

        stubFileExtension("extension").of(_jcrAsset);
        // this is ignored for general assets:
        stubFileExtension("extension").of(_asset);
        assertThat(_jcrAsset.getNode().getNode(AssetNodeTypes.AssetResource.RESOURCE_NAME).getProperty("extension").getString(), is("extension"));
    }

    @Test
    public void testStubWidth() throws RepositoryException {
        assertThat(_asset.getMetadata(JcrMagnoliaAssetMetadata.class), nullValue());
        assertThat(_jcrAsset.getMetadata(JcrMagnoliaAssetMetadata.class).getWidth(), is(0L));

        stubWidth(123L).of(_asset);
        stubWidth(123L).of(_jcrAsset);
        assertThat(_asset.getMetadata(JcrMagnoliaAssetMetadata.class), nullValue());
        assertThat(_jcrAsset.getMetadata(JcrMagnoliaAssetMetadata.class).getWidth(), is(123L));
    }

    @Test
    public void testStubHeight() throws RepositoryException {
        assertThat(_asset.getMetadata(JcrMagnoliaAssetMetadata.class), nullValue());
        assertThat(_jcrAsset.getMetadata(JcrMagnoliaAssetMetadata.class).getHeight(), is(0L));

        stubHeight(123L).of(_asset);
        stubHeight(123L).of(_jcrAsset);
        assertThat(_asset.getMetadata(JcrMagnoliaAssetMetadata.class), nullValue());
        assertThat(_jcrAsset.getMetadata(JcrMagnoliaAssetMetadata.class).getHeight(), is(123L));
    }
}
