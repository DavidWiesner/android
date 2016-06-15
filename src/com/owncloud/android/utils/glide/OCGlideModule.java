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

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.module.GlideModule;
import com.owncloud.android.datamodel.OCFile;

import java.io.InputStream;

/**
 * Register a {@link com.bumptech.glide.load.model.ModelLoaderFactory} for {@link OCFile}
 * @see GlideModule
 */
public class OCGlideModule implements GlideModule {
	@Override
	public void applyOptions(Context context, GlideBuilder builder) {

	}

	@Override
	public void registerComponents(Context context, Glide glide) {
		glide.register(OCFile.class, InputStream.class, new OCFileUrlLoader.Factory(context));
	}
}
