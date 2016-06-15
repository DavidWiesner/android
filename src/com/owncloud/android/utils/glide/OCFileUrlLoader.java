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

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.common.OwnCloudClientManager;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;

import java.io.InputStream;

/**
 * A {@link ModelLoader} for {@link OCFile}
 */
@SuppressWarnings("WeakerAccess")
public class OCFileUrlLoader implements ModelLoader<OCFile, InputStream> {
    private final Context context;
    private final OwnCloudClientManager ownCloudClientManager;

    public OCFileUrlLoader(Context context) {
        this.ownCloudClientManager = OwnCloudClientManagerFactory.newOwnCloudClientManager(
                OwnCloudClientManagerFactory.Policy.SINGLE_SESSION_PER_ACCOUNT);
        this.context = context;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(OCFile model, int width, int height) {
        return new OCFileThumbStreamFetcher(context, ownCloudClientManager, model, width, height);
    }

    public static class Factory implements ModelLoaderFactory<OCFile, InputStream> {
        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @Override
        public ModelLoader<OCFile, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new OCFileUrlLoader(this.context);
        }

        @Override
        public void teardown() {
        }
    }


    private static class CacheOnlyOCFileLoader implements StreamModelLoader<OCFile>{
        @Override
        public DataFetcher<InputStream> getResourceFetcher(OCFile model, int width, int height) {
            return new CacheOnlyStreamFetcher(model, width, height);
        }
    }

    private static class CacheOnlyStreamFetcher extends OCFileThumbStreamFetcher{
        public CacheOnlyStreamFetcher(OCFile file, int width, int height) {
            super(null, null, file, width, height);
        }

        @Override
        public InputStream loadData(Priority priority) throws Exception {
            return null;
        }
    }

    public final static CacheOnlyOCFileLoader CACHE_ONLY = new CacheOnlyOCFileLoader();
}
