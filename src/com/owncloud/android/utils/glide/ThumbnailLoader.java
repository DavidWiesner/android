/*
 * Copyright (C) 2016 David Boho
 * Copyright (C) 2016 ownCloud Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.utils.glide;

import android.content.Context;
import android.content.res.Resources;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.owncloud.android.R;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.datamodel.UploadsStorageManager;
import com.owncloud.android.db.OCUpload;
import com.owncloud.android.utils.BitmapUtils;
import com.owncloud.android.utils.MimetypeIconUtil;

import java.io.File;

/**
 * Load load thumbnails for {@link OCFile} or {@link File} and
 * Icons for all other FileTypes (e.g.: Folder and Document)
 */
public class ThumbnailLoader {
    private final Context context;
    private final int width;
    private final int height;
    private volatile DrawableTypeRequest<OCFile> ocFileRequest;
    private final DrawableTypeRequest<File> fileRequest;

    /**
     * Init ThumbnailLoader with default or target size
     *
     * Usage:
     * <pre>
     *     ThumbnailLoader loader = new ThumbnailLoader(context, false);
     *     //...
     *     loader.load(ocFile).into(imageView);
     * </pre>
     * @param context if {@link Context} implements {@link com.owncloud.android.ui.activity.ComponentsGetter}
     *                {@link OCFile#setNeedsUpdateThumbnail(boolean)} will be called after load
     *                (see {@link OCFileThumbStreamFetcher}). If context is an instance of {@link com.owncloud.android.ui.activity.FileActivity} the Account will be request of that
     *                otherwise {@link com.owncloud.android.authentication.AccountUtils#getCurrentOwnCloudAccount(Context)} is used.
     * @param useDefaultThumbnailSize if true the ThumbnailLoader will load images in default size
     *                                otherwise the thumbnail size will be automatically
     *                                calculated for the target (e.g. an ImageView)
     */
    public ThumbnailLoader(Context context, boolean useDefaultThumbnailSize) {
        this(context,
                useDefaultThumbnailSize ? getDefaultThumbnailDimension(context): -1,
                useDefaultThumbnailSize ? getDefaultThumbnailDimension(context): -1);
    }

    /**
     * Init ThumbnailLoader with specific size
     *
     * Usage:
     * <pre>
     *     ThumbnailLoader loader = new ThumbnailLoader(context, 1024, 1024);
     *     //...
     *     loader.load(ocFile).into(imageView);
     * </pre>
     * @param context if {@link Context} implements {@link com.owncloud.android.ui.activity.ComponentsGetter}
     *                {@link OCFile#setNeedsUpdateThumbnail(boolean)} will be called after load
     *                (see {@link OCFileThumbStreamFetcher}). If context is an instance of {@link com.owncloud.android.ui.activity.FileActivity} the Account will be request of that
     *                otherwise {@link com.owncloud.android.authentication.AccountUtils#getCurrentOwnCloudAccount(Context)} is used.
     * @param width the target width of the thumbnail
     * @param height the target height of the thumbnail
     */
    public ThumbnailLoader(Context context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;
        fileRequest = Glide.with(context).from(File.class);
    }

    /**
     * Load a thumbnail or an icon corresponding to the mimetype
     * @param file if this file is an image an thumbnail will be loaded otherwise an icon will be
     *             loaded corresponding to the mimetype of this file
     * @return an request builder usage: <pre>loader.load(ocFile).into(imageView)</pre>
     *
     * @see MimetypeIconUtil#getFolderTypeIconId(boolean, boolean)
     * @see MimetypeIconUtil#getFileTypeIconId(String, String)
     */
    public DrawableRequestBuilder<OCFile> load(OCFile file) {
        // TODO discussion: load local file instead if available?
        if (file.isImage() && (file.getRemoteId() != null || file.getRemotePath() != null)) {
            return decorateImageRequest(getOcFileRequest()).load(file);
        } else {
            int iconResourceId;
            if (file.isFolder()) {
                iconResourceId = MimetypeIconUtil.getFolderTypeIconId(
                        file.isSharedWithMe() || file.isSharedWithSharee(),
                        file.isSharedViaLink());
            } else {
                iconResourceId = MimetypeIconUtil.getFileTypeIconId(file.getMimetype(),
                        file.getFileName());
            }
            return getOcFileRequest().placeholder(iconResourceId).load(null);
        }
    }

    /**
     * Load an thumbnail or an icon corresponding to the mimetype
     * @param file if this file is an image a thumbnail will be loaded otherwise an icon will be
     *             loaded corresponding to the mimetype of this file. Use
     *             {@link MimetypeIconUtil#determineMimeTypesByFilename(String)} to determine the
     *             mimetype.
     * @return an request builder usage: <pre>loader.load(ocFile).into(imageView)</pre>
     *
     * @see MimetypeIconUtil#getFolderTypeIconId(boolean, boolean)
     * @see MimetypeIconUtil#getFileTypeIconId(String, String)
     * @see MimetypeIconUtil#determineMimeTypesByFilename(String)
     */
    public DrawableRequestBuilder<File> load(File file) {
        return load(file, null);
    }

    /**
     * Load an thumbnail or an icon corresponding to the mimetype
     * @param file the local file a thumbnail or icon should be requested
     * @param mimeType if mimeType is an image a thumbnail will be loaded otherwise an icon will be
     *             loaded corresponding to the mimetype of this file
     * @return an request builder usage: <pre>loader.load(ocFile).into(imageView, "image/jpeg")</pre>
     *
     * @see MimetypeIconUtil#getFolderTypeIconId(boolean, boolean)
     * @see MimetypeIconUtil#getFileTypeIconId(String, String)
     */
    public DrawableRequestBuilder<File> load(File file, String mimeType) {
        if ((mimeType != null && mimeType.startsWith("image/")) || BitmapUtils.isImage(file)) {
            return decorateImageRequest(fileRequest).load(file);
        } else {
            int iconResourceId;
            if (file.isDirectory()) {
                iconResourceId = MimetypeIconUtil.getFolderTypeIconId(false, false);
            } else {
                iconResourceId = MimetypeIconUtil.getFileTypeIconId(mimeType, file.getName());
            }
            return fileRequest.placeholder(iconResourceId).load(null);
        }
    }

    /**
     * Load an thumbnail or an icon corresponding to the mimetype
     * @param upload if upload is succeeded the thumbnail or will be request from the server
     *               (see {@link #load(OCFile)}) otherwise the thumbnail will be load for the local
     *               file (see {@link #load(File, String)})
     * @return an request builder usage: <pre>loader.load(ocUpload).into(imageView)</pre>
     */
    public DrawableRequestBuilder<?> load(OCUpload upload) {
        if (upload.getUploadStatus() == UploadsStorageManager.UploadStatus.UPLOAD_SUCCEEDED) {
            final OCFile file = new OCFile(upload.getRemotePath());
            file.setMimetype(upload.getMimeType());
            file.setStoragePath(upload.getLocalPath());
            return load(file);
        } else {
            return load(new File(upload.getLocalPath()), upload.getMimeType());
        }
    }

    private static int getDefaultThumbnailDimension(Context context) {
        Resources r = context.getResources();
        return Math.round(r.getDimension(R.dimen.file_icon_size_grid));
    }

    private <T> DrawableRequestBuilder<T> decorateImageRequest(
            DrawableRequestBuilder<T> requestBuilder){
        requestBuilder = requestBuilder.placeholder(R.drawable.file_image);
        if(width != -1 && height != -1){
            requestBuilder = requestBuilder.override(width, height);
        }
        return requestBuilder;
    }

    private DrawableTypeRequest<OCFile> getOcFileRequest() {
        if (ocFileRequest == null) {
            synchronized (this) {
                if (ocFileRequest == null) {
                    ocFileRequest = Glide.with(context).from(OCFile.class);
                }
            }
        }
        return ocFileRequest;
    }
}