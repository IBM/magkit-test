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

import de.ibmix.magkit.assertations.Require;
import de.ibmix.magkit.test.ExceptionStubbingOperation;
import de.ibmix.magkit.test.jcr.NodeStubbingOperation;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.jcr.AssetNodeTypes;
import info.magnolia.dam.jcr.JcrAsset;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.Calendar;

import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Factory class providing {@code AssetStubbingOperation} instances to define behaviour of mocked {@link Asset} objects.
 * <p>
 * Each static method returns an {@link AssetStubbingOperation} that, when applied via {@link AssetStubbingOperation#of(Asset)},
 * configures either direct method returns on a generic {@link Asset} mock or underlying JCR node properties for
 * {@link JcrAsset} mocks. This centralises stubbing logic and keeps test code concise and intention revealing.
 * </p>
 * <h3>Usage</h3>
 * <pre>
 * JcrAsset asset = AssetMockUtils.mockJcrAsset("/images/logo.png",
 *     AssetStubbingOperation.stubTitle("Company Logo"),
 *     AssetStubbingOperation.stubMimeType("image/png"),
 *     AssetStubbingOperation.stubFileSize(2048L));
 * </pre>
 * <p>
 * Operations are composable: provide multiple operations to {@link AssetMockUtils#mockAsset(String, String, String, AssetStubbingOperation...)}
 * or invoke {@link AssetStubbingOperation#of(Asset)} sequentially on the same asset mock. If the asset is a {@link JcrAsset},
 * node properties (e.g. {@link AssetNodeTypes.AssetResource#MIMETYPE}) are stubbed and will be readable through Magnolia's API.
 * Otherwise simple Mockito stubbing of getter methods applies.
 * </p>
 * <h3>Thread safety</h3>
 * Returned operations are stateless and thread-safe; they can be reused across tests. Binary stream stubbing creates fresh mocks per invocation.
 * <h3>Error handling</h3>
 * Methods may throw {@link RepositoryException} when interacting with JCR nodes for {@link JcrAsset} stubbing. In typical mock setups these should not occur.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-11-17
 */
public abstract class AssetStubbingOperation implements ExceptionStubbingOperation<Asset, RepositoryException> {

    /*
     * *************** Stubbing of asset content node properties ***************************************
     */

    /**
     * Creates an operation stubbing the link value returned by {@link Asset#getLink()}.
     * <p>For {@link JcrAsset} no JCR property is written; only the method return is stubbed.</p>
     * @param value link value to return (may be null)
     * @return stubbing operation for link
     */
    public static AssetStubbingOperation stubLink(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) {
                Require.Argument.notNull(asset, "asset should not be null");
                when(asset.getLink()).thenReturn(value);
            }
        };
    }

    /**
     * Creates an operation stubbing the asset title.
     * <p>For {@link JcrAsset} delegates to {@link NodeStubbingOperation#stubTitle(String)} writing the title property; for non-JCR assets the getter is stubbed.</p>
     * @param value title text (may be null)
     * @return stubbing operation for title
     */
    public static AssetStubbingOperation stubTitle(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    NodeStubbingOperation.stubTitle(value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getTitle()).thenReturn(value);
                }
            }
        };
    }

    /**
     * Stubs the description of the asset ({@link AssetNodeTypes.Asset#DESCRIPTION}).
     * @param value description text (may be null)
     * @return stubbing operation for description
     */
    public static AssetStubbingOperation stubDescription(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.DESCRIPTION, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getDescription()).thenReturn(value);
                }
            }
        };
    }

    /**
     * Stubs the caption ({@link AssetNodeTypes.Asset#CAPTION}).
     * @param value caption text (may be null)
     * @return stubbing operation for caption
     */
    public static AssetStubbingOperation stubCaption(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.CAPTION, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getCaption()).thenReturn(value);
                }
            }
        };
    }

    /**
     * Stubs the comment ({@link AssetNodeTypes.Asset#COMMENT}).
     * @param value comment text (may be null)
     * @return stubbing operation for comment
     */
    public static AssetStubbingOperation stubComment(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.COMMENT, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getComment()).thenReturn(value);
                }
            }
        };
    }

    /**
     * Stubs the copyright ({@link AssetNodeTypes.Asset#COPYRIGHT}).
     * @param value copyright text (may be null)
     * @return stubbing operation for copyright
     */
    public static AssetStubbingOperation stubCopyright(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.COPYRIGHT, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getCopyright()).thenReturn(value);
                }
            }
        };
    }

    /**
     * Stubs the language ({@link AssetNodeTypes.Asset#LANGUAGE}).
     * @param value ISO language code or descriptive text (may be null)
     * @return stubbing operation for language
     */
    public static AssetStubbingOperation stubLanguage(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.LANGUAGE, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getLanguage()).thenReturn(value);
                }
            }
        };
    }

    /**
     * Stubs the subject ({@link AssetNodeTypes.Asset#SUBJECT}).
     * @param value subject text (may be null)
     * @return stubbing operation for subject
     */
    public static AssetStubbingOperation stubSubject(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.Asset.SUBJECT, value).of(((JcrAsset) asset).getNode());
                } else {
                    when(asset.getSubject()).thenReturn(value);
                }
            }
        };
    }

    /**
     * Stubs the last modified date ({@link NodeTypes.LastModified#LAST_MODIFIED}).
     * @param value calendar timestamp (may be null)
     * @return stubbing operation for last modified date
     */
    public static AssetStubbingOperation stubLastModified(final Calendar value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
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

    /**
     * Stubs the MIME type of the asset's resource node ({@link AssetNodeTypes.AssetResource#MIMETYPE}).
     * @param value mime type string (e.g. image/png) (may be null)
     * @return stubbing operation for MIME type
     */
    public static AssetStubbingOperation stubMimeType(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.MIMETYPE, value).of(getResourceNode((JcrAsset) asset));
                } else {
                    when(asset.getMimeType()).thenReturn(value);
                }
            }
        };
    }

    /**
     * Stubs the file size ({@link AssetNodeTypes.AssetResource#SIZE}).
     * @param value size in bytes (non-negative)
     * @return stubbing operation for file size
     */
    public static AssetStubbingOperation stubFileSize(final long value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.SIZE, value).of(getResourceNode((JcrAsset) asset));
                } else {
                    when(asset.getFileSize()).thenReturn(value);
                }
            }
        };
    }

    /**
     * Stubs the original file name ({@link AssetNodeTypes.AssetResource#FILENAME}).
     * @param value file name (may be null)
     * @return stubbing operation for file name
     */
    public static AssetStubbingOperation stubFileName(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.FILENAME, value).of(getResourceNode((JcrAsset) asset));
                } else {
                    when(asset.getFileName()).thenReturn(value);
                }
            }
        };
    }

    /**
     * Stubs the file extension ({@link AssetNodeTypes.AssetResource#EXTENSION}).
     * @param value extension without dot (e.g. png) (may be null)
     * @return stubbing operation for file extension
     */
    public static AssetStubbingOperation stubFileExtension(final String value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.EXTENSION, value).of(getResourceNode((JcrAsset) asset));
                }
            }
        };
    }

    /**
     * Stubs the width ({@link AssetNodeTypes.AssetResource#WIDTH}) of an image asset.
     * @param value width in pixels (non-negative)
     * @return stubbing operation for image width
     */
    public static AssetStubbingOperation stubWidth(final long value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.WIDTH, value).of(getResourceNode((JcrAsset) asset));
                }
            }
        };
    }

    /**
     * Stubs the height ({@link AssetNodeTypes.AssetResource#HEIGHT}) of an image asset.
     * @param value height in pixels (non-negative)
     * @return stubbing operation for image height
     */
    public static AssetStubbingOperation stubHeight(final long value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
                if (asset instanceof JcrAsset) {
                    stubProperty(AssetNodeTypes.AssetResource.HEIGHT, value).of(getResourceNode((JcrAsset) asset));
                }
            }
        };
    }

    /**
     * Stubs the binary content stream ({@link AssetNodeTypes.AssetResource#DATA}).
     * <p>Creates a Mockito {@link Binary} whose {@link Binary#getStream()} returns the provided stream.</p>
     * @param value input stream representing binary content (may be null)
     * @return stubbing operation for binary content stream
     */
    public static AssetStubbingOperation stubContentStream(final InputStream value) {
        return new AssetStubbingOperation() {
            @Override
            public void of(Asset asset) throws RepositoryException {
                Require.Argument.notNull(asset, "asset should not be null");
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

    /**
     * Helper method resolving the resource node of a {@link JcrAsset} mock.
     * @param asset jcr asset (must not be null)
     * @return resource node or null if the asset node itself is null
     * @throws RepositoryException on JCR interaction errors when accessing child node
     */
    static Node getResourceNode(JcrAsset asset) throws RepositoryException {
        Node assetNode = asset.getNode();
        return assetNode != null ? assetNode.getNode(AssetNodeTypes.AssetResource.RESOURCE_NAME) : null;
    }
}
