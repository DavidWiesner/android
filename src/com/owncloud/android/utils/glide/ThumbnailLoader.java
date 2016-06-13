package com.owncloud.android.utils.glide;

import android.content.Context;

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
 * Load load thumbnails for OCFile or File
 */
public class ThumbnailLoader {
    private final Context context;
    private volatile DrawableTypeRequest<OCFile> ocFileRequest;
    private final DrawableTypeRequest<File> fileRequest;

    /**
     * @param context Should be provide at least a Context + getAccount + getStorage methods
     */
    public ThumbnailLoader(Context context) {
        this.context = context;
        fileRequest = Glide.with(context).from(File.class);
    }

    public DrawableRequestBuilder<OCFile> load(OCFile file){
        if(file.isImage() && (file.getRemoteId() != null || file.getRemotePath() != null)){
            return getOcFileRequest().placeholder(R.drawable.file_image).load(file);
        } else {
            int iconResourceId;
            if (file.isFolder()){
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

    public DrawableRequestBuilder<File> load(File file){
        return load(file, null);
    }
    public DrawableRequestBuilder<File> load(File file, String mimeType){
        if((mimeType != null && mimeType.startsWith("image/")) || BitmapUtils.isImage(file)){
            return fileRequest.placeholder(R.drawable.file_image).load(file);
        } else {
            int iconResourceId;
            if(file.isDirectory()){
                iconResourceId = MimetypeIconUtil.getFolderTypeIconId(false, false);
            } else {
                iconResourceId = MimetypeIconUtil.getFileTypeIconId(mimeType, file.getName());
            }
            return fileRequest.placeholder(iconResourceId).load(null);
        }
    }
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

    private DrawableTypeRequest<OCFile> getOcFileRequest() {
        if(ocFileRequest == null){
            synchronized (this){
                if(ocFileRequest == null){
                    ocFileRequest = Glide.with(context).from(OCFile.class);
                }
            }
        }
        return ocFileRequest;
    }
}