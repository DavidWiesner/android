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
import com.owncloud.android.ui.fragment.FileFragment;

import java.io.InputStream;

/**
 * Created by David Wiesner on 03.06.16.
 */

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
		return new OCFileThumbStreamFetcher(this.client, this.account, this.storageManager, model);
	}

	public static class Factory implements ModelLoaderFactory<OCFile, InputStream> {
        private final Account account;
        private final FileDataStorageManager storageManager;
        private final OwnCloudClient client;

        public Factory(Context context) {
			this(AccountUtils.getCurrentOwnCloudAccount(context), getStorageManager(context));
		}

		private static FileDataStorageManager getStorageManager(Context context) {
			if(context instanceof FileFragment.ContainerActivity){
				return ((FileFragment.ContainerActivity) context).getStorageManager();
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
