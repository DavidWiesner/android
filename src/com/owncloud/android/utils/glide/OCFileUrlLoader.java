package com.owncloud.android.utils.glide;


import android.accounts.Account;
import android.content.Context;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.OCFile;

import java.io.InputStream;

/**
 * Created by David Wiesner on 03.06.16.
 */

public class OCFileUrlLoader implements ModelLoader<OCFile, InputStream> {
	@Override
	public DataFetcher<InputStream> getResourceFetcher(OCFile model, int width, int height) {
		return null;
	}

	public static class Factory implements ModelLoaderFactory<OCFile, InputStream> {
		private final Account account;

		public Factory(Context context) {
			this(AccountUtils.getCurrentOwnCloudAccount(context));
		}

		public Factory(Account account) {
			this.account = account;
		}

		@Override
		public ModelLoader<OCFile, InputStream> build(Context context, GenericLoaderFactory factories) {
			return null;
		}

		@Override
		public void teardown() {

		}
	}
}
