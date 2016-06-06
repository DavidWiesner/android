package com.owncloud.android.utils.glide;

import android.accounts.Account;
import android.content.res.Resources;
import android.net.Uri;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.util.ContentLengthInputStream;
import com.owncloud.android.MainApp;
import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by David Wiesner on 06.06.16.
 */

public class OCFileThumbStreamFetcher implements DataFetcher<InputStream> {
    private static final String TAG = OCFileThumbStreamFetcher.class.getSimpleName();
    private OwnCloudClient mClient;
    private Account mAccount;
    private final FileDataStorageManager storageManager;
    private final OCFile file;
    private final int px;
    private GetMethod get;
    private InputStream inputStream;

    public OCFileThumbStreamFetcher(OwnCloudClient client, Account account, FileDataStorageManager storageManager, OCFile model) {

        this.mClient = client;
        this.mAccount = account;
        this.storageManager = storageManager;
        this.file = model;
        px = getThumbnailDimension();
    }

    private int getThumbnailDimension() {
        // Converts dp to pixel
        Resources r = MainApp.getAppContext().getResources();
        return Math.round(r.getDimension(R.dimen.file_icon_size_grid));
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        if (file == null || mClient == null) {
            return null;
        }
        OwnCloudVersion serverOCVersion = AccountUtils.getServerVersion(mAccount);
        if (serverOCVersion == null || !serverOCVersion.supportsRemoteThumbnails()) {
            Log_OC.d(TAG, "Server too old");
            return null;
        }
        String uri = mClient.getBaseUri() + "/index.php/apps/files/api/v1/thumbnail/" +
                px + "/" + px + Uri.encode(file.getRemotePath(), "/");
        get = new GetMethod(uri);
        int status = mClient.executeMethod(get);
        if (status == HttpStatus.SC_OK) {
            final long length = get.getResponseContentLength();
            inputStream = ContentLengthInputStream.obtain(get.getResponseBodyAsStream(), length);
            return inputStream;
        } else {
            throw new IOException("Request failed with code: " + status);
        }
    }

    @Override
    public void cleanup() {
        if (inputStream != null && mClient != null) {
            try {
                mClient.exhaustResponse(inputStream);
            } catch (Exception e) {
                //Ignored
            }
        }
        try {
            if(get != null){
                get.releaseConnection();
            }
        } catch (Exception e) {
            // ignored
        }
    }

    @Override
    public String getId() {
        final String id = file.getRemoteId() + "?wxh=" + px
                + "&contentMod=" + file.getModificationTimestampAtLastSyncForData()
                + "&localMod=" + file.getLocalModificationTimestamp()
                + "&propMod="+file.getModificationTimestamp();
        return id;
    }

    @Override
    public void cancel() {
        if (get != null) {
            try {
                get.releaseConnection();
            } catch (Exception e) {
                // ignored
            }
        }
    }
}
