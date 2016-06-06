package com.owncloud.android.utils.glide;


import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.module.GlideModule;
import com.owncloud.android.datamodel.OCFile;

import java.io.InputStream;

/**
 * Created by David Wiesner on 03.06.16.
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
