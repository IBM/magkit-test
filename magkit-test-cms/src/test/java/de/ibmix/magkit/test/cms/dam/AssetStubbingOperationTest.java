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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    public void setUp() throws RepositoryException {
        ContextMockUtils.cleanContext();
        _asset = mock(Asset.class);
        _jcrAsset = AssetMockUtils.mockJcrAsset("test/asset");
    }

    @Test
    public void testStubLink() throws RepositoryException {
        stubLink("http://www.migros.ch").of(_asset);
        stubLink("http://www.migros.ch").of(_jcrAsset);
        assertEquals("http://www.migros.ch", _asset.getLink());
        assertEquals("http://www.migros.ch", _jcrAsset.getLink());
    }

    @Test
    public void testStubTitle() throws RepositoryException {
        stubTitle("Titel").of(_asset);
        stubTitle("Titel").of(_jcrAsset);
        assertEquals("Titel", _asset.getTitle());
        assertEquals("Titel", _jcrAsset.getTitle());
    }

    @Test
    public void testStubDescription() throws RepositoryException {
        stubDescription("Description").of(_asset);
        stubDescription("Description").of(_jcrAsset);
        assertEquals("Description", _asset.getDescription());
        assertEquals("Description", _jcrAsset.getDescription());
    }

    @Test
    public void testStubCaption() throws RepositoryException {
        stubCaption("Caption").of(_asset);
        stubCaption("Caption").of(_jcrAsset);
        assertEquals("Caption", _asset.getCaption());
        assertEquals("Caption", _jcrAsset.getCaption());
    }

    @Test
    public void testStubCopyright() throws RepositoryException {
        stubCopyright("Copyright").of(_asset);
        stubCopyright("Copyright").of(_jcrAsset);
        assertEquals("Copyright", _asset.getCopyright());
        assertEquals("Copyright", _jcrAsset.getCopyright());
    }

    @Test
    public void testStubLanguage() throws RepositoryException {
        stubLanguage("Language").of(_asset);
        stubLanguage("Language").of(_jcrAsset);
        assertEquals("Language", _asset.getLanguage());
        assertEquals("Language", _jcrAsset.getLanguage());
    }

    @Test
    public void testStubSubject() throws RepositoryException {
        stubSubject("Subject").of(_asset);
        stubSubject("Subject").of(_jcrAsset);
        assertEquals("Subject", _asset.getSubject());
        assertEquals("Subject", _jcrAsset.getSubject());
    }

    @Test
    public void testStubLastModified() throws RepositoryException {
        Calendar now = Calendar.getInstance();
        stubLastModified(now).of(_asset);
        stubLastModified(now).of(_jcrAsset);
        assertEquals(now, _asset.getLastModified());
        assertEquals(now, _jcrAsset.getLastModified());
    }

    @Test
    public void testStubMimeType() throws RepositoryException {
        stubMimeType("MimeType").of(_asset);
        stubMimeType("MimeType").of(_jcrAsset);
        assertEquals("MimeType", _asset.getMimeType());
        assertEquals("MimeType", _jcrAsset.getMimeType());
    }

    @Test
    public void testStubFileSize() throws RepositoryException {
        stubFileSize(123456L).of(_asset);
        stubFileSize(123456L).of(_jcrAsset);
        assertEquals(123456L, _asset.getFileSize());
        assertEquals(123456L, _jcrAsset.getFileSize());
    }

    @Test
    public void testStubFileName() throws RepositoryException {
        stubFileName("FileName").of(_asset);
        stubFileName("FileName").of(_jcrAsset);
        assertEquals("FileName", _asset.getFileName());
        assertEquals("FileName", _jcrAsset.getFileName());
    }

    @Test
    public void testStubContentStream() throws RepositoryException {
        InputStream stream = mock(InputStream.class);
        stubContentStream(stream).of(_asset);
        stubContentStream(stream).of(_jcrAsset);
        assertEquals(stream, _asset.getContentStream());
        assertEquals(stream, _jcrAsset.getContentStream());
    }

    @Test
    public void testStubComment() throws RepositoryException {
        assertNull(_asset.getComment());
        assertEquals("", _jcrAsset.getComment());

        stubComment("comment").of(_asset);
        stubComment("comment").of(_jcrAsset);
        assertEquals("comment", _asset.getComment());
        assertEquals("comment", _jcrAsset.getComment());
    }

    @Test
    public void testStubFileExtension() throws RepositoryException {
        assertNull(_jcrAsset.getNode().getNode(AssetNodeTypes.AssetResource.RESOURCE_NAME).getProperty("extension"));

        stubFileExtension("extension").of(_jcrAsset);
        // this is ignored for general assets:
        stubFileExtension("extension").of(_asset);
        assertEquals("extension", _jcrAsset.getNode().getNode(AssetNodeTypes.AssetResource.RESOURCE_NAME).getProperty("extension").getString());
    }

    @Test
    public void testStubWidth() throws RepositoryException {
        assertNull(_asset.getMetadata(JcrMagnoliaAssetMetadata.class));
        assertEquals(0L, _jcrAsset.getMetadata(JcrMagnoliaAssetMetadata.class).getWidth());

        stubWidth(123L).of(_asset);
        stubWidth(123L).of(_jcrAsset);
        assertNull(_asset.getMetadata(JcrMagnoliaAssetMetadata.class));
        assertEquals(123L, _jcrAsset.getMetadata(JcrMagnoliaAssetMetadata.class).getWidth());
    }

    @Test
    public void testStubHeight() throws RepositoryException {
        assertNull(_asset.getMetadata(JcrMagnoliaAssetMetadata.class));
        assertEquals(0L, _jcrAsset.getMetadata(JcrMagnoliaAssetMetadata.class).getHeight());

        stubHeight(123L).of(_asset);
        stubHeight(123L).of(_jcrAsset);
        assertNull(_asset.getMetadata(JcrMagnoliaAssetMetadata.class));
        assertEquals(123L, _jcrAsset.getMetadata(JcrMagnoliaAssetMetadata.class).getHeight());
    }
}
