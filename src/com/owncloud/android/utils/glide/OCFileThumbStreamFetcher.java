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

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.util.ContentLengthInputStream;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientManager;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;
import com.owncloud.android.ui.activity.ComponentsGetter;
import com.owncloud.android.ui.activity.FileActivity;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.io.InputStream;

/**
 * Fetches an InputStream with {@link OwnCloudClient} for an {@link OCFile}
 */
public class OCFileThumbStreamFetcher implements DataFetcher<InputStream> {
    private static final String TAG = OCFileThumbStreamFetcher.class.getSimpleName();
    private final Context context;
    private final OwnCloudClientManager clientManager;
    private final OCFile file;
    private final int width;
    private final int height;
    private GetMethod get;
    private InputStream inputStream;

    public OCFileThumbStreamFetcher(Context context, OwnCloudClientManager clientManager,
                                    OCFile file, int width, int height) {
        this.context = context;
        this.clientManager = clientManager;
        this.file = file;
        this.width = width;
        this.height = height;
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        OwnCloudClient client = getThumbnailCapableClient();
        if (file == null || client == null) {
            return null;
        }
        String uri = client.getBaseUri() + "/index.php/apps/files/api/v1/thumbnail/" +
                width + "/" + height + Uri.encode(file.getRemotePath(), "/");
        get = new GetMethod(uri);
        final int status = client.executeMethod(get);
        if (status == HttpStatus.SC_OK) {
            setThumbnailLoaded(true);
            final long length = get.getResponseContentLength();
            inputStream = ContentLengthInputStream.obtain(get.getResponseBodyAsStream(), length);
            return inputStream;
        } else {
            throw new IOException("Request failed with code: " + status);
        }
    }

    @Override
    public void cleanup() {
        if (inputStream != null ) {
            try {
                inputStream.close();
            } catch (Exception e) {
                //Ignored
            }
        }
    }

    @Override
    public void cancel() {
        setThumbnailLoaded(false);
        if (get != null) {
            try {
                get.abort();
            } catch (Exception e) {
                // ignored
            }
        }
    }


    @Override
    public String getId() {
        final String id = file.getRemoteId() + "?w=" + width + "&h=" + height
                + "&eTag=" + file.getEtag()
                + "&propMod=" + file.getModificationTimestamp();
        return id;
    }

    private OwnCloudClient getThumbnailCapableClient() throws
            com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException,
            OperationCanceledException, AuthenticatorException, IOException {
        final Account account = getAccount();
        if (account == null) {
            throw new AuthenticatorException("account is null");
        }

        OwnCloudVersion serverOCVersion = AccountUtils.getServerVersion(account);
        if (serverOCVersion == null || !serverOCVersion.supportsRemoteThumbnails()) {
            Log_OC.d(TAG, "Server too old");
            return null;
        }
        return  clientManager.getClientFor(new OwnCloudAccount(account, context), context);
    }

    private Account getAccount() {
        if (context instanceof FileActivity) {
            return ((FileActivity) context).getAccount();
        }
        return AccountUtils.getCurrentOwnCloudAccount(context);
    }

    private void setThumbnailLoaded(boolean isLoaded) {
        if (context instanceof ComponentsGetter) {
            final FileDataStorageManager manager = ((ComponentsGetter) context).getStorageManager();
            if (manager == null || file == null) {
                return;
            }
            file.setNeedsUpdateThumbnail(!isLoaded);
            manager.saveFile(file);
        }
    }
}
