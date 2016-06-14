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
import android.content.Context;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.owncloud.android.MainApp;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.ui.activity.ComponentsGetter;
import com.owncloud.android.ui.activity.FileActivity;

import java.io.InputStream;

public class OCFileUrlLoader implements ModelLoader<OCFile, InputStream> {
    private static final String TAG = OCFileUrlLoader.class.getSimpleName();
    private OwnCloudClient client;
    private final Account account;
    private final FileDataStorageManager storageManager;

    public OCFileUrlLoader(OwnCloudClient client, Account account, FileDataStorageManager storageManager) {
        this.client = client;
        this.account = account;
        this.storageManager = storageManager;
    }

    @Override
	public DataFetcher<InputStream> getResourceFetcher(OCFile model, int width, int height) {
        if(client == null){
            Log_OC.d(TAG,account.toString());
            client = OCFileUrlLoader.Factory.getClient(this.account);
        }
		return new OCFileThumbStreamFetcher(this.client, this.account, this.storageManager, model,
                width, height);
	}

	public static class Factory implements ModelLoaderFactory<OCFile, InputStream> {
        private final Account account;
        private final FileDataStorageManager storageManager;
        private final OwnCloudClient client;

        public Factory(Context context) {
			this(getAccount(context), getStorageManager(context));
		}

        private static Account getAccount(Context context) {
            if(context instanceof FileActivity){
                return ((FileActivity) context).getAccount();
            }
            return AccountUtils.getCurrentOwnCloudAccount(context);
        }

        private static FileDataStorageManager getStorageManager(Context context) {
			if(context instanceof ComponentsGetter){
				return ((ComponentsGetter) context).getStorageManager();
			}
			return null;
		}


		public Factory(Account account, FileDataStorageManager storageManager) {
            this(getClient(account), account, storageManager);
		}

        public Factory(OwnCloudClient client, Account account, FileDataStorageManager storageManager) {
            this.client = client;
            this.account = account;
            this.storageManager = storageManager;
        }

        public static OwnCloudClient getClient(Account account) {
            try {
                return OwnCloudClientManagerFactory.getDefaultSingleton().
                        getClientFor(new OwnCloudAccount(account,
                                MainApp.getAppContext()), MainApp.getAppContext());
            } catch (Exception e) {
                Log_OC.e(Factory.class.getCanonicalName(), e.getMessage(), e);
            }
            return null;
        }

        @Override
		public ModelLoader<OCFile, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new OCFileUrlLoader(this.client, this.account, this.storageManager);
		}

		@Override
		public void teardown() {

		}
	}
}
