package de.ibmix.magkit.mockito;

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


import de.ibmix.magkit.mockito.jcr.NodeStubbingOperation;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.jcr.AssetNodeTypes;
import info.magnolia.dam.jcr.JcrAsset;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.Calendar;

import static de.ibmix.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 * @since 17.11.2010
 */
public abstract class AssetStubbingOperation {

    public abstract void of(Asset asset) throws RepositoryException;

    /*
     * *************** Stubbing of asset content node properties ***************************************
     */

    public static AssetStubbingOperation stubLink(final String value) {
        return new AssetStubbingOperation() {

            @Override
            public void of(Asset asset) {
                assertThat(asset, notNullValue());
                when(asset.getLink()).thenReturn(value);
            }
        };
    }

    public static AssetStubbingOperation stubTitle(final String value) {
        return new AssetStubbingOperation() {

            @Override
            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    NodeStubbingOperation.stubTitle(value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getTitle()).thenReturn(value);
                }
            }
        };
    }

    public static AssetStubbingOperation stubDescription(final String value) {
        return new AssetStubbingOperation() {

            @Override
            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.DESCRIPTION, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getDescription()).thenReturn(value);
                }
            }
        };
    }

    public static AssetStubbingOperation stubCaption(final String value) {
        return new AssetStubbingOperation() {

            @Override
            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.CAPTION, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getCaption()).thenReturn(value);
                }
            }
        };
    }

    public static AssetStubbingOperation stubComment(final String value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.COMMENT, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getComment()).thenReturn(value);
                }
            }
        };
    }

    public static AssetStubbingOperation stubCopyright(final String value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.COPYRIGHT, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getCopyright()).thenReturn(value);
                }
            }
        };
    }

    public static AssetStubbingOperation stubLanguage(final String value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.LANGUAGE, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getLanguage()).thenReturn(value);
                }
            }
        };
    }

    public static AssetStubbingOperation stubSubject(final String value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.SUBJECT, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getSubject()).thenReturn(value);
                }
            }
        };
    }

    public static AssetStubbingOperation stubLastModified(final Calendar value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(NodeTypes.LastModified.LAST_MODIFIED, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getLastModified()).thenReturn(value);
                }
            }
        };
    }


    /*
     * *************** Stubbing of asset content node properties ***************************************
     */

    public static AssetStubbingOperation stubMimeType(final String value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.MIMETYPE, value).of(getResourceNode((JcrAsset) asset));
                } else {
                    when(asset.getMimeType()).thenReturn(value);
                }
            }
        };
    }

    public static AssetStubbingOperation stubFileSize(final long value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.SIZE, value).of(getResourceNode((JcrAsset) asset));
                } else {
                    when(asset.getFileSize()).thenReturn(value);
                }
            }
        };
    }

    public static AssetStubbingOperation stubFileName(final String value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.FILENAME, value).of(getResourceNode((JcrAsset) asset));
                } else {
                    when(asset.getFileName()).thenReturn(value);
                }
            }
        };
    }

    public static AssetStubbingOperation stubFileExtension(final String value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.EXTENSION, value).of(getResourceNode((JcrAsset) asset));
                }
            }
        };
    }

    public static AssetStubbingOperation stubWidth(final long value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.WIDTH, value).of(getResourceNode((JcrAsset) asset));
                }
            }
        };
    }

    public static AssetStubbingOperation stubHeight(final long value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.HEIGHT, value).of(getResourceNode((JcrAsset) asset));
                }
            }
        };
    }

    public static AssetStubbingOperation stubContentStream(final InputStream value) {
        return new AssetStubbingOperation() {

            public void of(Asset asset) throws RepositoryException {
                assertThat(asset, notNullValue());
                if (asset instanceof JcrAsset) {
                    Binary binary = mock(Binary.class);
                    when(binary.getStream()).thenReturn(value);
                    stubProperty(AssetNodeTypes.AssetResource.DATA, binary).of(getResourceNode((JcrAsset) asset));
                } else {
                    when(asset.getContentStream()).thenReturn(value);
                }
            }
        };
    }

    static Node getResourceNode(JcrAsset asset) throws RepositoryException {
        Node assetNode = asset.getNode();
        return assetNode != null ? assetNode.getNode(AssetNodeTypes.AssetResource.RESOURCE_NAME) : null;
    }
}
